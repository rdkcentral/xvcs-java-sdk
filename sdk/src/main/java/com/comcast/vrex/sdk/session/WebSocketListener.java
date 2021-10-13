/*
 * Copyright 2021 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.comcast.vrex.sdk.session;

import com.comcast.vrex.sdk.config.SpeechConfiguration;
import com.comcast.vrex.sdk.config.SpeechConfigurationManager;
import com.comcast.vrex.sdk.message.IncomingMessageType;
import com.comcast.vrex.sdk.messageModel.common.EventMessage;
import com.comcast.vrex.sdk.messageModel.receive.VrexResponse;
import com.comcast.vrex.sdk.messageModel.receive.WbwResponse;
import com.comcast.vrex.sdk.messageModel.receive.WuwVerificationMessage;
import com.comcast.vrex.sdk.result.SpeechResultObserver;
import com.comcast.vrex.sdk.util.ObjectMappers;
import com.comcast.vrex.sdk.util.SpeechUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

@WebSocket
@Slf4j
public class WebSocketListener {
    private static ObjectMapper objectMapper = ObjectMappers.noFailOnUnknownPropsMapper();
    private final String trx;
    private URI uri;
    private Session session = null;
    private SpeechConfigurationManager speechConfigurationManager;

    private CountDownLatch listeningLatch;
    private CountDownLatch connectLatch;

    private WebSocketSender sender;
    private ExecutorService executor;

    private Optional<SpeechResultObserver> speechResultObserver;
    private VrexResponse closeConnectionResponse;

    public WebSocketListener(String trx, SpeechConfigurationManager speechConfigurationManager) throws URISyntaxException {
        this.trx = trx;
        this.speechConfigurationManager = speechConfigurationManager;
        initUri();
    }

    private void initUri() throws URISyntaxException {
        SpeechConfiguration.Websocket websocketConfig = speechConfigurationManager.getConfiguration().getConnection().getWebsocket();
        URIBuilder uriBuilder = new URIBuilder(new URI(websocketConfig.getUrl()))
                .setParameter("version", "version=v" + websocketConfig.getVersion())
                .setParameter("trx", trx)
                .setParameter("id", speechConfigurationManager.getAppId());

        appendAuthToken(uriBuilder);
        this.uri = uriBuilder.build();
    }

    private void appendAuthToken(URIBuilder uriBuilder) {
        if (speechConfigurationManager.authIsEnabled()) {
            String token = speechConfigurationManager.getSpeechAuthenticator().getAuthResponse().getToken();
            uriBuilder.setParameter("sat", token);
        }
    }

    public void startSession(WebSocketSender sender, CountDownLatch listeningLatch, CountDownLatch connectLatch,
                             ExecutorService executor, SpeechResultObserver speechResultObserver) throws Exception {
        this.listeningLatch = listeningLatch;
        this.connectLatch = connectLatch;
        this.sender = sender;
        this.executor = executor;
        this.speechResultObserver = Optional.ofNullable(speechResultObserver);
        log.info(SpeechUtils.LogTemplates.LOG_TEMPLATE, trx, "Connecting to Websocket URL...");
        WebSocketClient client = new WebSocketClient();
        client.start();
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        client.connect(this, this.uri, request);
    }

    @OnWebSocketConnect
    public void onOpen(Session session) throws IOException {
        log.info(SpeechUtils.LogTemplates.LOG_SESSION_TEMPLATE, trx, "Successfully established websocket connection with Speech server.");
        this.session = session;
        sender.setSession(session);
        connectLatch.countDown();
    }

    @OnWebSocketClose
    public void onClose(Session userSession, int status, String reason) {
        afterClose();
        log.info(SpeechUtils.LogTemplates.LOG_SESSION_TEMPLATE, trx, "Websocket session closed.");
        speechResultObserver.ifPresent(observer -> observer.onCloseConnection(closeConnectionResponse));
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        log.error(SpeechUtils.LogTemplates.LOG_SESSION_TEMPLATE, trx, "Websocket session error: " + t.getLocalizedMessage());
        afterClose();
        speechResultObserver.ifPresent(observer -> observer.onError(t));
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        log.debug(SpeechUtils.LogTemplates.LOG_TEMPLATE, trx, "RECEIVED message: " + message);
        try {
            EventMessage eventMessage = objectMapper.readValue(message, EventMessage.class);
            IncomingMessageType messageType = IncomingMessageType.reverseLookup(eventMessage.getMsgType());
            handleMessage(messageType, eventMessage);
        } catch (IOException e) {
            log.error(SpeechUtils.LogTemplates.LOG_JSON_TEMPLATE, trx, "Unable to read incoming message", message);
        }
    }

    private void handleMessage(IncomingMessageType messageType, EventMessage eventMessage) {
        switch (messageType) {
            case LISTENING:
                handleListening();
                break;
            case TRANSCRIPTION:
                handleTranscription(eventMessage);
                break;
            case VREX_RESPONSE:
                handleVrexResponse(eventMessage);
                break;
            case WUW_VERIFICATION:
                handleWuwVerification(eventMessage);
                break;
            case CLOSE_CONNECTION:
                handleCloseConnection(eventMessage);
                break;
            default:
                log.error(SpeechUtils.LogTemplates.LOG_SESSION_TEMPLATE, trx, "Internal Error - Unhandled Message: " + messageType);
        }
    }

    private void handleListening() {
        log.info(SpeechUtils.LogTemplates.LOG_TEMPLATE, trx, "Speech server is now awaiting audio from the client.");
        listeningLatch.countDown();
        speechResultObserver.ifPresent(observer -> observer.onListening());
    }

    private void handleWuwVerification(EventMessage eventMessage) {
        WuwVerificationMessage response = objectMapper.convertValue(eventMessage.getMsgPayload(), WuwVerificationMessage.class);
        if (response.isPassed()) {
            log.info(SpeechUtils.LogTemplates.LOG_JSON_TEMPLATE, trx, "WUW Verification received with PASSED status", eventMessage.getMsgPayload());
        } else {
            log.error(SpeechUtils.LogTemplates.LOG_JSON_TEMPLATE, trx, "WUW Verification received with FAILED status", eventMessage.getMsgPayload());
        }
        speechResultObserver.ifPresent(observer -> {
            if (response.isPassed()) {
                observer.onWakeUpWordVerificationSuccess(response.getConfidence());
            } else {
                observer.onWakeUpWordVerificationFailure(response.getConfidence());
                stopSendingAudioOrMessages();
            }
        });
    }

    private void handleVrexResponse(EventMessage eventMessage) {
        VrexResponse response = objectMapper.convertValue(eventMessage.getMsgPayload(), VrexResponse.class);
        if (response.getReturnCode() == 0) {
            log.info(SpeechUtils.LogTemplates.LOG_TEMPLATE, trx, "Final successful response received: " + eventMessage);
        } else {
            log.error(SpeechUtils.LogTemplates.LOG_TEMPLATE, trx, "Final unsuccessful response received: " + eventMessage);
        }
        speechResultObserver.ifPresent(observer -> {
            if (response.getReturnCode() == 0) {
                observer.onFinalResponseSuccess(response);
            } else {
                observer.onFinalResponseFailure(response);
                stopSendingAudioOrMessages();
            }
        });
    }

    private void handleTranscription(EventMessage eventMessage) {
        JsonNode messagePayload = eventMessage.getMsgPayload();
        WbwResponse response = objectMapper.convertValue(messagePayload, WbwResponse.class);
        String transcriptionType = response.getIsFinal() ? "FINAL" : "PARTIAL";
        log.info(SpeechUtils.LogTemplates.LOG_JSON_TEMPLATE, trx, transcriptionType + " transcription received", messagePayload);

        speechResultObserver.ifPresent(observer -> {
            if (response.getIsFinal()) {
                observer.onFinalTranscriptionReceived(response.getText());
            } else {
                observer.onPartialTranscriptionReceived(response.getText());
            }
        });
    }

    private VrexResponse getVrexResponseFromPayload(JsonNode messagePayload) {
        return objectMapper.convertValue(messagePayload, VrexResponse.class);
    }

    private void handleCloseConnection(EventMessage eventMessage) {
        this.closeConnectionResponse = objectMapper.convertValue(eventMessage.getMsgPayload(), VrexResponse.class);
        switch (closeConnectionResponse.getReturnCode()) {
            default:
                log.info(SpeechUtils.LogTemplates.LOG_JSON_TEMPLATE, trx, "Server is closing the websocket connection.", eventMessage.getMsgPayload());
        }
    }

    private void stopSendingAudioOrMessages() {
        executor.shutdownNow();
    }

    private void afterClose() {
        log.debug(SpeechUtils.LogTemplates.LOG_SESSION_TEMPLATE, trx, "Performing speech session cleanup");
        executor.shutdownNow();
    }
}

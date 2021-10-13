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

import com.comcast.vrex.sdk.audio.AudioOption;
import com.comcast.vrex.sdk.config.SpeechConfigurationManager;
import com.comcast.vrex.sdk.exception.SpeechSessionException;
import com.comcast.vrex.sdk.message.ContextMessage;
import com.comcast.vrex.sdk.message.ExtraMessage;
import com.comcast.vrex.sdk.message.InitPayloadBuilder;
import com.comcast.vrex.sdk.messageModel.send.InitPayload;
import com.comcast.vrex.sdk.result.SpeechResultObserver;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.comcast.vrex.sdk.util.SpeechUtils.LogTemplates.LOG_TEMPLATE;

@Slf4j
public class SpeechSession {

    private SpeechConfigurationManager configManager;

    private CountDownLatch listeningLatch;
    private CountDownLatch connectLatch;

    private WebSocketListener listener;
    private WebSocketSender sender;

    private ExecutorService executor;

    private InitPayloadBuilder initPayloadBuilder;
    private Callable<ContextMessage> contextMessageCallable;

    private AudioOption audioOption;
    private SpeechResultObserver speechResultObserver;

    private SpeechSession(String trx, SpeechConfigurationManager configManager) throws URISyntaxException {
        this.configManager = configManager;
        listeningLatch = new CountDownLatch(1);
        connectLatch = new CountDownLatch(1);
        executor = Executors.newCachedThreadPool();
        this.listener = new WebSocketListener(trx, configManager);
        sender = new WebSocketSender(trx, listeningLatch, connectLatch, executor);
    }

    public static SpeechSession newSession(@NonNull String trx, @NonNull SpeechConfigurationManager speechConfigurationManager)
            throws URISyntaxException {
        log.info(LOG_TEMPLATE, trx, "A new Speech session has been initialized.");
        return new SpeechSession(trx, speechConfigurationManager);
    }

    public SpeechSession withInit(@NonNull InitPayload initPayload) {
        this.initPayloadBuilder = InitPayloadBuilder.customizeExistingPayload(initPayload);
        return this;
    }

    public SpeechSession withContext(ContextMessage contextMessage) {
        return withContext(() -> contextMessage);
    }

    public SpeechSession withContext(Callable<ContextMessage> messageCallable) {
        this.contextMessageCallable = messageCallable;
        return this;
    }

    public SpeechSession withAudio(AudioOption audioOption) {
        this.audioOption = audioOption;
        return this;
    }

    public SpeechSession withResultObserver(SpeechResultObserver speechResultObserver) {
        this.speechResultObserver = speechResultObserver;
        return this;
    }

    private void updateIds() {
        boolean shouldRefresh = false;
        InitPayload initPayload = initPayloadBuilder.buildMessage();
        if (initPayload.getDeviceId() == null) {
            String deviceId = configManager.getDeviceId();
            if (deviceId == null) {
                throw new SpeechSessionException("No deviceId provided. Unable to start session");
            }
            initPayloadBuilder.withDeviceId(deviceId);
            shouldRefresh = true;
        }

        if (initPayload.getAccountId() == null) {
            String accountId = configManager.getAccountId();
            if (accountId == null) {
                throw new SpeechSessionException("No accountId provided. Unable to start session");
            }
            initPayloadBuilder.withAccountId(accountId);
            shouldRefresh = true;
        }

        if (initPayload.getCustomerId() == null) {
            String customerId = configManager.getCustomerId();
            if(customerId != null) {
                initPayloadBuilder.withCustomerId(customerId);
                shouldRefresh = true;
            }
        }

        if (shouldRefresh) initPayloadBuilder.refreshIds();
    }

    private void updateInit() {
        if (initPayloadBuilder == null) {
            throw new SpeechSessionException("No init payload provided. Unable to start session");
        }

        if (contextMessageCallable != null) {
            initPayloadBuilder.withContextCapability();
        } else {
            initPayloadBuilder.withRemovedCapability("CONTEXT");
        }

        updateIds();

        if (audioOption.getSelectedOption() instanceof AudioOption.TextOption) {
            initPayloadBuilder.withText(((AudioOption.TextOption) audioOption.getSelectedOption()).getText());
        }
    }

    public void startSession() throws Exception {
        updateInit();
        listener.startSession(sender, listeningLatch, connectLatch, executor, speechResultObserver);
        sender.sendInit(initPayloadBuilder.buildMessage());
        sendMessage(contextMessageCallable);
        sender.handleAudio(audioOption);
    }

    private void sendMessage(Callable<? extends ExtraMessage> message) {
        if (message != null) {
            sender.sendMessage(message);
        }
    }
}

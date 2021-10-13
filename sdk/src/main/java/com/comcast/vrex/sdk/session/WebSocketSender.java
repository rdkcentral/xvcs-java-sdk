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
import com.comcast.vrex.sdk.audio.AudioStreamReader;
import com.comcast.vrex.sdk.message.ExtraMessage;
import com.comcast.vrex.sdk.messageModel.common.EventMessage;
import com.comcast.vrex.sdk.messageModel.common.MessageType;
import com.comcast.vrex.sdk.messageModel.send.EndOfStream;
import com.comcast.vrex.sdk.messageModel.send.InitMessage;
import com.comcast.vrex.sdk.messageModel.send.InitPayload;
import com.comcast.vrex.sdk.util.ObjectMappers;
import com.comcast.vrex.sdk.util.SpeechUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PipedInputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.comcast.vrex.sdk.util.SpeechUtils.LogTemplates.LOG_TEMPLATE;

@Slf4j
public class WebSocketSender {

    private static ObjectMapper objectMapper = ObjectMappers.defaultMapper();
    private final String trx;
    private ExecutorService executor;
    private Session session;
    private RemoteEndpoint remoteEndpoint;

    private CountDownLatch listeningLatch;
    private CountDownLatch connectLatch;

    public WebSocketSender(String trx, CountDownLatch listeningLatch, CountDownLatch connectLatch, ExecutorService executor) {
        this.trx = trx;
        this.listeningLatch = listeningLatch;
        this.connectLatch = connectLatch;
        this.executor = executor;
    }

    public void sendInit(InitPayload initPayload) {
        executor.execute(() -> {
            EventMessage eventMessage = new EventMessage(trx, MessageType.INIT);
            eventMessage.setMsgPayload(objectMapper.valueToTree(new InitMessage(initPayload)));
            try {
                connectLatch.await(3000, TimeUnit.MILLISECONDS);
                sendMessageIfSessionOpen(eventMessage);
            } catch (InterruptedException | IOException e) {
                logOnMessageSendError(eventMessage, e);
            }
        });
    }

    public void handleAudio(AudioOption audioOption) throws IOException {
        AudioOption.SelectedOption option = audioOption.getSelectedOption();
        if (option instanceof AudioOption.AudioInputStreamOption) {
            AudioOption.AudioInputStreamOption audioInputStreamOption = (AudioOption.AudioInputStreamOption) option;
            sendAudio(audioInputStreamOption.getStream(), audioInputStreamOption.getBufferSize());
        } else if (option instanceof AudioOption.PipedInputStreamOption) {
            AudioOption.PipedInputStreamOption audioInputStreamOption = (AudioOption.PipedInputStreamOption) option;
            sendAudio(audioInputStreamOption.getStream(), audioInputStreamOption.getBufferSize());
        } else if (option instanceof AudioOption.TextOption) {
            //Do Nothing
        } else {
            log.error(LOG_TEMPLATE, trx, "Internal error. Could not resolve audio.");
        }
    }

    public void sendAudio(PipedInputStream inputStream, int bufferSize) throws IOException {
        executor.execute(() -> {
            try {
                boolean shouldSendAudio = listeningLatch.await(3000, TimeUnit.MILLISECONDS);
                if (shouldSendAudio) {
                    log.info(LOG_TEMPLATE, trx, "Streaming audio...");
                    int readCount = 0;
                    byte[] bytes = new byte[bufferSize];
                    while ((readCount = inputStream.read(bytes)) != -1 && session.isOpen()) {
                        ByteBuffer buffer = ByteBuffer.wrap(bytes, 0, readCount);
                        remoteEndpoint.sendBytes(buffer);
                        bytes = new byte[bufferSize];
                    }
                    sendEndOfAudio();
                }
            } catch (InterruptedIOException e) {
                log.error(LOG_TEMPLATE, trx, "Sending Audio was interrupted due to unsuccessful message from server.");
            } catch (IOException e) {
                log.error(LOG_TEMPLATE, trx, "Error sending audio: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void sendAudio(AudioInputStream audioInputStream, int bufferSize) {
        executor.execute(() -> {
            try {
                boolean shouldSendAudio = listeningLatch.await(3000, TimeUnit.MILLISECONDS);
                if (shouldSendAudio) {
                    log.info(LOG_TEMPLATE, trx, "Sending audio...");
                    AudioStreamReader reader = new AudioStreamReader(audioInputStream);
                    while (reader.hasMore() && session.isOpen()) {
                        ByteBuffer buffer = ByteBuffer.wrap(reader.read(bufferSize));
                        remoteEndpoint.sendBytes(buffer);
                    }
                    sendEndOfAudio();
                }
            } catch (InterruptedIOException e) {
                log.error(LOG_TEMPLATE, trx, "Sending Audio was interrupted due to unsuccessful message from server.");
            } catch (IOException e) {
                log.error(LOG_TEMPLATE, trx, "Error sending audio: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void sendMessage(EventMessage message) {
        executor.execute(() -> {
            try {
                listeningLatch.await(3000, TimeUnit.MILLISECONDS);
                sendMessageIfSessionOpen(message);
            } catch (InterruptedException | IOException e) {
                logOnMessageSendError(message, e);
            }
        });
    }

    private void sendMessageIfSessionOpen(EventMessage message) throws IOException {
        if (session.isOpen()) {
            String messageText = objectMapper.writeValueAsString(message);
            remoteEndpoint.sendString(messageText);
            log.debug(LOG_TEMPLATE, trx, "Sent Message: " + messageText);
            logOnMessageSendSuccess(message);
        }
    }

    public void sendMessage(Callable<? extends ExtraMessage> extraMessageCallable) {
        executor.submit(() -> {
            Future<? extends ExtraMessage> extraMessageFuture = executor.submit(extraMessageCallable);
            ExtraMessage message = null;
            try {
                message = extraMessageFuture.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            sendMessage(message.getMessage());
        });
    }

    private void sendEndOfAudio() throws IOException {
        EventMessage message = createEndOfStreamMessage(trx, 0);
        String messageText = objectMapper.writeValueAsString(message);
        if (session.isOpen()) {
            remoteEndpoint.sendString(messageText);
            logOnMessageSendSuccess(message);
        }
    }

    private EventMessage createEndOfStreamMessage(String trx, int reasonCode) {
        EndOfStream endOfStream = new EndOfStream(reasonCode);
        JsonNode jsonPayload = objectMapper.valueToTree(endOfStream);
        return new EventMessage(trx, MessageType.END_OF_STREAM, jsonPayload);
    }

    public void setSession(Session session) {
        this.session = session;
        this.remoteEndpoint = session.getRemote();
    }

    private void logOnMessageSendError(EventMessage message, Exception e) {
        log.error(SpeechUtils.LogTemplates.LOG_SESSION_TEMPLATE, trx, "Error sending" + message.getMsgType() + "message: " + e.getLocalizedMessage());
        e.printStackTrace();
    }

    private void logOnMessageSendSuccess(EventMessage message) {
        log.info(SpeechUtils.LogTemplates.LOG_SESSION_TEMPLATE, trx, message.getMsgType() + " message sent.");
    }
}

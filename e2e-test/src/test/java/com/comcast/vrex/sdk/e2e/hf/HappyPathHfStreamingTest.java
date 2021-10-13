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

package com.comcast.vrex.sdk.e2e.hf;

import com.comcast.vrex.sdk.audio.AudioConfig;
import com.comcast.vrex.sdk.audio.AudioOption;
import com.comcast.vrex.sdk.audio.AudioStreamReader;
import com.comcast.vrex.sdk.auth.SpeechAuthenticator;
import com.comcast.vrex.sdk.config.SpeechApplication;
import com.comcast.vrex.sdk.config.SpeechConfigurationManager;
import com.comcast.vrex.sdk.e2e.util.ResultObserverLatchSet;
import com.comcast.vrex.sdk.e2e.util.TestResultObserver;
import com.comcast.vrex.sdk.e2e.util.TestUtils;
import com.comcast.vrex.sdk.message.InitPayloadBuilder;
import com.comcast.vrex.sdk.messageModel.send.InitPayload;
import com.comcast.vrex.sdk.messageModel.send.TriggeredBy;
import com.comcast.vrex.sdk.result.SpeechResultObserver;
import com.comcast.vrex.sdk.sat.SatAuthenticator;
import com.comcast.vrex.sdk.session.SpeechSession;
import com.comcast.vrex.sdk.util.SpeechUtils;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HappyPathHfStreamingTest {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Test
    public void happyPathHfStreamingNetflixTest() throws Exception {
        happyPathHfStreamingTest("hx-netflix", "Netflix", 100, 835);
    }

    @Test
    public void happyPathHfStreamingShowGuideTest() throws Exception {
        happyPathHfStreamingTest("hx-show-me-the-guide", "Show me the guide", 100, 790);
    }

    public void happyPathHfStreamingTest(String file, String transcription, int sowuw, int eowuw) throws Exception {

        SpeechAuthenticator authenticator = new SatAuthenticator();

        SpeechConfigurationManager configManager = SpeechApplication
                .newApplication(HappyPathHfFileTest.class)
                .withAuthenticator(authenticator)
                .init();

        AudioConfig hfAudioConfig = TestUtils.getHfAudioConfig(sowuw, eowuw);

        InitPayload initPayload = InitPayloadBuilder
                .fromInitJson(TestUtils.getJsonNodeFromFile("test_init_payload.json"))
                .withAudio(hfAudioConfig)
                .buildMessage();

        AudioInputStream audioInputStream = TestUtils.openFile(file, TriggeredBy.WUW);

        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

        Runnable streamRunnable = () -> {
            AudioStreamReader reader = new AudioStreamReader(audioInputStream);
            try {
                while (reader.hasMore()) {
                    byte[] b = reader.read(2048);
                    System.out.println("Reading Partial audio.");
                    Thread.sleep(50);
                    pipedOutputStream.write(b);
                    pipedOutputStream.flush();
                }
                pipedOutputStream.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        };

        executorService.execute(streamRunnable);

        ResultObserverLatchSet latchSet = new ResultObserverLatchSet();
        SpeechResultObserver observer = new TestResultObserver(latchSet, transcription);

        //Start session with the backend
        SpeechSession
                .newSession(SpeechUtils.generateTrx(), configManager)
                .withInit(initPayload)
                .withAudio(AudioOption.from(pipedInputStream))
                .withResultObserver(observer)
                .startSession();

        boolean listening = latchSet.listeningLatch.await(3000, TimeUnit.MILLISECONDS);
        assertTrue(listening);
        boolean wuwSuccessReceived = latchSet.wuwVerificationSuccessLatch.await(3000, TimeUnit.MILLISECONDS);
        assertTrue(wuwSuccessReceived);
        boolean finalTranscriptionReceived = latchSet.finalTranscriptionLatch.await(5000, TimeUnit.MILLISECONDS);
        assertTrue(finalTranscriptionReceived);
        boolean successfulResponseReceived = latchSet.finalResSuccessLatch.await(5000, TimeUnit.MILLISECONDS);
        assertTrue(successfulResponseReceived);
        boolean connectionClosed = latchSet.closeConnectionLatch.await(1000, TimeUnit.MILLISECONDS);
        assertTrue(connectionClosed);
    }
}

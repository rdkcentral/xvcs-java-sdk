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

package com.comcast.vrex.sdk.e2e.text;

import com.comcast.vrex.sdk.audio.AudioOption;
import com.comcast.vrex.sdk.auth.SpeechAuthenticator;
import com.comcast.vrex.sdk.config.SpeechApplication;
import com.comcast.vrex.sdk.config.SpeechConfigurationManager;
import com.comcast.vrex.sdk.e2e.ptt.HappyPathPttFileTest;
import com.comcast.vrex.sdk.e2e.util.ResultObserverLatchSet;
import com.comcast.vrex.sdk.e2e.util.TestResultObserver;
import com.comcast.vrex.sdk.e2e.util.TestUtils;
import com.comcast.vrex.sdk.message.InitPayloadBuilder;
import com.comcast.vrex.sdk.messageModel.send.InitPayload;
import com.comcast.vrex.sdk.result.SpeechResultObserver;
import com.comcast.vrex.sdk.sat.SatAuthenticator;
import com.comcast.vrex.sdk.session.SpeechSession;
import com.comcast.vrex.sdk.util.SpeechUtils;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextHappyPathTest {

    @Test
    public void happyPathTextHBOTest() throws Exception {
        happyPathTextTest("HBO");
    }

    @Test
    public void happyPathTextKidsMoviesTest() throws Exception {
        happyPathTextTest("Show me all kids movies");
    }

    public void happyPathTextTest(String text) throws Exception {

        SpeechAuthenticator authenticator = new SatAuthenticator();

        SpeechConfigurationManager configManager = SpeechApplication
                .newApplication(HappyPathPttFileTest.class)
                .withAuthenticator(authenticator)
                .init();

        InitPayload initPayload = InitPayloadBuilder
                .fromInitJson(TestUtils.getJsonNodeFromFile("test_init_payload.json"))
                .withText(text)
                .buildMessage();


        ResultObserverLatchSet latchSet = new ResultObserverLatchSet();
        SpeechResultObserver observer = new TestResultObserver(latchSet, text);

        //Start session with the backend
        SpeechSession
                .newSession(SpeechUtils.generateTrx(), configManager)
                .withInit(initPayload)
                .withAudio(AudioOption.from(text))
                .withResultObserver(observer)
                .startSession();

        boolean listening = latchSet.listeningLatch.await(3000, TimeUnit.MILLISECONDS);
        assertTrue(listening);
        boolean finalTranscriptionReceived = latchSet.finalTranscriptionLatch.await(3000, TimeUnit.MILLISECONDS);
        assertTrue(finalTranscriptionReceived);
        boolean successfulResponseReceived = latchSet.finalResSuccessLatch.await(5000, TimeUnit.MILLISECONDS);
        assertTrue(successfulResponseReceived);
        boolean connectionClosed = latchSet.closeConnectionLatch.await(1000, TimeUnit.MILLISECONDS);
        assertTrue(connectionClosed);
    }
}

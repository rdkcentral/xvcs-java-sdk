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

package com.comcast.vrex.sdk.e2e.util;

import com.comcast.vrex.sdk.messageModel.receive.VrexResponse;
import com.comcast.vrex.sdk.result.SpeechResultObserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class  TestResultObserver implements SpeechResultObserver {

    ResultObserverLatchSet latchSet;
    String expectedTranscription;

    public TestResultObserver(ResultObserverLatchSet latchSet, String expectedTranscription) {
        this.latchSet = latchSet;
        this.expectedTranscription = expectedTranscription;
    }

    @Override
    public void onListening() {
        latchSet.listeningLatch.countDown();
    }

    @Override
    public void onPartialTranscriptionReceived(String s) {
        System.out.println(String.format("RESULT OBSERVER: Partial transcription received: \"%s\"", s));
    }

    @Override
    public void onFinalTranscriptionReceived(String s) {
        System.out.println(String.format("RESULT OBSERVER: Final transcription received: \"%s\"", s));
        assertEquals(s, expectedTranscription);
        latchSet.finalTranscriptionLatch.countDown();
    }

    @Override
    public void onCloseConnection(VrexResponse vrexResponse) {
        System.out.println("RESULT OBSERVER: Server has closed the connection");
        latchSet.closeConnectionLatch.countDown();
    }

    @Override
    public void onWakeUpWordVerificationSuccess(Integer c) {
        System.out.println("RESULT OBSERVER: received WUW verification PASSED message with confidence " + c);
        latchSet.wuwVerificationSuccessLatch.countDown();
    }

    @Override
    public void onWakeUpWordVerificationFailure(Integer c) {
        System.out.println("RESULT OBSERVER: received WUW verification FAILURE message with confidence " + c);
        latchSet.wuwVerificationFailureLatch.countDown();
    }

    @Override
    public void onFinalResponseSuccess(VrexResponse response) {
        System.out.println("RESULT OBSERVER: received successful final message");
        latchSet.finalResSuccessLatch.countDown();
    }

    @Override
    public void onFinalResponseFailure(VrexResponse response) {
        System.out.println("RESULT OBSERVER: received unsuccessful final message");
        latchSet.finalResFailureLatch.countDown();
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("ERROR: " + t);
    }
}

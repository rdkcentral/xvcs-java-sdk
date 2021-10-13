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

package com.comcast.vrex.demo.sdk.demo;

import com.comcast.vrex.sdk.messageModel.receive.VrexResponse;
import com.comcast.vrex.sdk.result.SpeechResultObserver;

import java.util.concurrent.CountDownLatch;

public class MyResultObserver implements SpeechResultObserver {

    CountDownLatch latch;

    public MyResultObserver(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onListening() {
    }

    @Override
    public void onPartialTranscriptionReceived(String s) {
        System.out.println(String.format("RESULT OBSERVER: Partial transcription received: \"%s\"", s));
    }

    @Override
    public void onFinalTranscriptionReceived(String s) {
        System.out.println(String.format("RESULT OBSERVER: Final transcription received: \"%s\"", s));
    }

    @Override
    public void onCloseConnection(VrexResponse vrexResponse) {
        System.out.println("RESULT OBSERVER: Server has closed the connection");
        latch.countDown();
    }

    @Override
    public void onWakeUpWordVerificationSuccess(Integer confidence) {
        System.out.println("RESULT OBSERVER: received WUW PASSED verification message");
    }

    @Override
    public void onWakeUpWordVerificationFailure(Integer confidence) {
        System.out.println("RESULT OBSERVER: received WUW FAILED verification message");
    }

    @Override
    public void onFinalResponseSuccess(VrexResponse response) {
        System.out.println("RESULT OBSERVER: received successful final message");
    }

    @Override
    public void onFinalResponseFailure(VrexResponse response) {
        System.out.println("RESULT OBSERVER: received unsuccessful final message");
    }

    @Override
    public void onError(Throwable t) {
        System.out.println("ERROR: " + t);
    }
}

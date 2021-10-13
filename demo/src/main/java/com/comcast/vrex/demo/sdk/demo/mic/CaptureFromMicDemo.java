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

package com.comcast.vrex.demo.sdk.demo.mic;

import com.comcast.vrex.demo.sdk.demo.Demo;
import com.comcast.vrex.demo.sdk.demo.MyResultObserver;
import com.comcast.vrex.demo.sdk.util.InitPayloadDemoFactory;
import com.comcast.vrex.demo.sdk.util.TriggerSelector;
import com.comcast.vrex.demo.sdk.util.Utils;
import com.comcast.vrex.sdk.audio.AudioOption;
import com.comcast.vrex.sdk.auth.SpeechAuthenticator;
import com.comcast.vrex.sdk.config.SpeechApplication;
import com.comcast.vrex.sdk.config.SpeechConfigurationManager;
import com.comcast.vrex.sdk.messageModel.send.InitPayload;
import com.comcast.vrex.sdk.messageModel.send.TriggeredBy;
import com.comcast.vrex.sdk.result.SpeechResultObserver;
import com.comcast.vrex.sdk.sat.SatAuthenticator;
import com.comcast.vrex.sdk.session.SpeechSession;
import com.comcast.vrex.sdk.util.SpeechUtils;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CaptureFromMicDemo implements Demo {

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @SneakyThrows
    @Override
    public void run() {
        TriggeredBy triggeredBy = TriggerSelector.selectTrigger();


        //Create a SpeechAuthenticator of type SatAuthenticator
        SpeechAuthenticator authenticator = new SatAuthenticator();

        //Initialize configuration
        SpeechConfigurationManager configManager = SpeechApplication
                .newApplication(CaptureFromMicDemo.class)
                .withAuthenticator(authenticator)
                .init();

        CountDownLatch sessionLatch = new CountDownLatch(1);
        Scanner scanner = new Scanner(System.in);

        //Retrieve a unique trx
        String trx = SpeechUtils.generateTrx();

        //Setup Audio Stream
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream, 1024 * 100);
        CountDownLatch endofWuwLatch = new CountDownLatch(1);

        AtomicLong bytesRead = new AtomicLong();

        AudioRecorderHandler audioRecorderHandler = new AudioRecorderHandler(pipedOutputStream, scanner,
                triggeredBy, endofWuwLatch, bytesRead);

        if (triggeredBy.equals(TriggeredBy.WUW)) {
            Utils.println("**************** Press ENTER to start recording, ENTER to mark end of WUW and finally ENTER once again to finish recording ****************");
        } else {
            Utils.println("**************** Press ENTER to start recording and ENTER once again to finish recording ****************");
        }
        scanner.nextLine();

        executorService.execute(audioRecorderHandler);

        //Create Init payload
        InitPayload initPayload;
        if (triggeredBy.equals(TriggeredBy.WUW)) {
            endofWuwLatch.await(8000, TimeUnit.MILLISECONDS);
            long availableBytes = bytesRead.get();
            initPayload = InitPayloadDemoFactory.getHfPayload(0, (int)(availableBytes/32));
        } else {
            initPayload = InitPayloadDemoFactory.getPttPayload();
        }

        SpeechResultObserver observer = new MyResultObserver(sessionLatch);

        //Start session with the backend
        SpeechSession
                .newSession(trx, configManager)
                .withInit(initPayload)
                .withAudio(AudioOption.from(pipedInputStream))
                .withResultObserver(observer)
                .startSession();

        sessionLatch.await(10000, TimeUnit.MILLISECONDS);
    }
}

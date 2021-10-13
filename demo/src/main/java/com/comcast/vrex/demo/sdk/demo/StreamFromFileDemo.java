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

import com.comcast.vrex.demo.sdk.util.DemoAudio;
import com.comcast.vrex.demo.sdk.util.FileSelector;
import com.comcast.vrex.demo.sdk.util.InitPayloadDemoFactory;
import com.comcast.vrex.demo.sdk.util.TriggerSelector;
import com.comcast.vrex.sdk.audio.AudioOption;
import com.comcast.vrex.sdk.audio.AudioStreamReader;
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

import javax.sound.sampled.AudioInputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class
StreamFromFileDemo implements Demo {

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SneakyThrows
    @Override
    public void run() {
        TriggeredBy triggeredBy = TriggerSelector.selectTrigger();

        //Create Init payload
        InitPayload initPayload;
        String fileName;
        if (triggeredBy.equals(TriggeredBy.WUW)) {
            FileSelector.HfFile hfFile = FileSelector.selectHfFile();
            initPayload = InitPayloadDemoFactory.getHfPayload(hfFile.getStart(), hfFile.getEnd());
            fileName = hfFile.getFileName();
        } else {
            fileName = FileSelector.selectPttFile();
            initPayload = InitPayloadDemoFactory.getPttPayload();
        }

        AudioInputStream audioInputStream = DemoAudio.openFile(fileName, triggeredBy);

        //Create a SpeechAuthenticator of type SatAuthenticator
        SpeechAuthenticator authenticator = new SatAuthenticator();

        //Initialize configuration
        SpeechConfigurationManager configManager = SpeechApplication
                .newApplication(StreamFromFileDemo.class)
                .withAuthenticator(authenticator)
                .init();

        //Retrieve a unique trx
        String trx = SpeechUtils.generateTrx();

        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

        Runnable r = () -> {
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

        CountDownLatch latch = new CountDownLatch(1);
        SpeechResultObserver observer = new MyResultObserver(latch);

        //Start session with the backend
        SpeechSession
                .newSession(trx, configManager)
                .withInit(initPayload)
                .withAudio(AudioOption.from(pipedInputStream, 2048))
                .withResultObserver(observer)
                .startSession();

        executorService.execute(r);

        latch.await(10000, TimeUnit.MILLISECONDS);
    }
}

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

import com.comcast.vrex.demo.sdk.util.Utils;
import com.comcast.vrex.sdk.messageModel.send.TriggeredBy;
import lombok.SneakyThrows;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class AudioRecorderHandler implements Runnable {

    private PipedOutputStream pipedOutputStream;
    private Scanner scanner;
    private TriggeredBy triggeredBy;
    private CountDownLatch endofWuwLatch;
    private AtomicLong bytesRead;

    public AudioRecorderHandler(PipedOutputStream pipedOutputStream, Scanner scanner, TriggeredBy triggeredBy,
                                CountDownLatch endofWuwLatch, AtomicLong bytesRead) {
        this.pipedOutputStream = pipedOutputStream;
        this.scanner = scanner;
        this.triggeredBy = triggeredBy;
        this.endofWuwLatch = endofWuwLatch;
        this.bytesRead = bytesRead;
    }

    private AudioFormat getAudioFormat() {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float sampleRate = 16000;
        int sampleSize = 16;
        int channels = 1;
        int sampleSizeInBits = (sampleSize / 8) * channels;
        boolean signed = true;
        boolean bigEndian = false;
        AudioFormat format = new AudioFormat(encoding, sampleRate, sampleSize, channels, sampleSizeInBits, sampleRate, bigEndian);
        return format;
    }

    @SneakyThrows
    @Override
    public void run() {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, getAudioFormat());

        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        AudioRecorder thread = new AudioRecorder(pipedOutputStream, line, bytesRead);

        thread.start();
        if(TriggeredBy.WUW.equals(triggeredBy)) {
            scanner.nextLine();
            Utils.println("**************** END OF WAKE UP WORD ****************");
            endofWuwLatch.countDown();
        }
        scanner.nextLine();
        try {
            synchronized (AudioRecorder.class) {
                pipedOutputStream.close();
                line.stop();
                line.close();
                Utils.println("**************** FINISHED RECORDING ****************");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

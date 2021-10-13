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
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class AudioRecorder extends Thread {

    private TargetDataLine line;
    private PipedOutputStream pipedOutputStream;
    private AtomicLong bytesRead;

    public AudioRecorder(PipedOutputStream pipedOutputStream, TargetDataLine line, AtomicLong bytesRead) {
        this.pipedOutputStream = pipedOutputStream;
        this.line = line;
        this.bytesRead = bytesRead;
    }

    @Override
    public void run() {
        try {
            if (!AudioSystem.isLineSupported(line.getLineInfo())) {
                System.out.println("Line not supported");
                System.exit(0);
            } else {
                line.open(line.getFormat());
                line.start(); // start capturing

                AudioInputStream ais = new AudioInputStream(line);
                Utils.println("**************** STARTED RECORDING ****************");
                while (line.isOpen()) {
                    synchronized (AudioRecorder.class) {
                        int available = ais.available();
                        if (available > 0) {
                            bytesRead.getAndAdd(available);
                            byte[] bytes = new byte[available];
                            ais.read(bytes, 0, available);
                            pipedOutputStream.write(bytes);
                            pipedOutputStream.flush();
                        }
                    }
                }
            }
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            log.error("Cannot write to outputStream. Inputstream might be closed.");
        }
    }
}

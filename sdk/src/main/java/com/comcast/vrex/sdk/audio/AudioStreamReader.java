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

package com.comcast.vrex.sdk.audio;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

public class AudioStreamReader {

    private final AudioInputStream inputStream;

    public AudioStreamReader(AudioInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public byte[] read(int count) throws IOException {
        count = Math.min(count, remainingBytes());
        byte[] bytes = new byte[count];
        inputStream.read(bytes, 0, count);
        return bytes;
    }

    public void close() throws IOException {
        inputStream.close();
    }

    public boolean hasMore() throws IOException {
        return remainingBytes() > 0;
    }

    public int remainingBytes() throws IOException {
        return inputStream.available();
    }
}

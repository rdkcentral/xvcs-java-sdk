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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import javax.sound.sampled.AudioInputStream;
import java.io.PipedInputStream;

@AllArgsConstructor
public class AudioOption {

    private static final int STANDARD_BUFFER_SIZE = 2048;

    @Getter
    private final SelectedOption selectedOption;

    public static AudioOption from(@NonNull AudioInputStream audioInputStream) {
        return new AudioOption(new AudioInputStreamOption(audioInputStream, STANDARD_BUFFER_SIZE));
    }

    public static AudioOption from(@NonNull AudioInputStream audioInputStream, int bufferSize) {
        return new AudioOption(new AudioInputStreamOption(audioInputStream, bufferSize));
    }

    public static AudioOption from(@NonNull PipedInputStream pipedInputStream) {
        return new AudioOption(new PipedInputStreamOption(pipedInputStream, STANDARD_BUFFER_SIZE));
    }

    public static AudioOption from(@NonNull PipedInputStream pipedInputStream, int bufferSize) {
        return new AudioOption(new PipedInputStreamOption(pipedInputStream, bufferSize));
    }

    public static AudioOption from(@NonNull String text) {
        return new AudioOption(new TextOption(text));
    }

    public interface SelectedOption {
    }

    @AllArgsConstructor
    @Getter
    public static class PipedInputStreamOption implements SelectedOption {
        private final PipedInputStream stream;
        private int bufferSize;
    }

    @AllArgsConstructor
    @Getter
    public static class AudioInputStreamOption implements SelectedOption {
        private final AudioInputStream stream;
        private int bufferSize;
    }

    @AllArgsConstructor
    @Getter
    public static class TextOption implements SelectedOption {
        private final String text;
    }
}

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

import com.comcast.vrex.sdk.messageModel.send.Audio;
import com.comcast.vrex.sdk.messageModel.send.AudioModel;
import com.comcast.vrex.sdk.messageModel.send.Codec;
import com.comcast.vrex.sdk.messageModel.send.TriggeredBy;
import com.comcast.vrex.sdk.messageModel.send.WakeUpWord;

public class AudioConfig {

    private final Audio audio;

    private AudioConfig() {
        audio = new Audio();
    }

    public static AudioConfig customConfiguration() {
        return new AudioConfig();
    }

    public static AudioConfig fromDefaultPttConfig() {
        return new AudioConfig()
                .withEnvoyCodec(Codec.PCM_16_16K)
                .withAudioProfile("XR11")
                .withTriggeredBy(TriggeredBy.PTT);
    }

    public AudioConfig withAudioProfile(String audioProfile) {
        audio.setAudioProfile(audioProfile);
        return this;
    }

    public AudioConfig withEnvoyCodec(Codec codec) {
        audio.setEnvoyCodec(codec);
        return this;
    }

    public AudioConfig withTriggeredBy(TriggeredBy triggeredBy) {
        audio.setTriggeredBy(triggeredBy);
        if (TriggeredBy.WUW.equals(triggeredBy)) {
            audio.setAudioModel(AudioModel.HF);
        } else if (TriggeredBy.PTT.equals(triggeredBy)) {
            audio.setAudioModel(AudioModel.PTT);
        }
        return this;
    }

    public AudioConfig withWakeUpWord(WakeUpWord wakeUpWord) {
        audio.setWuw(wakeUpWord);
        return this;
    }

    public Audio buildAudio() {
        return audio;
    }
}

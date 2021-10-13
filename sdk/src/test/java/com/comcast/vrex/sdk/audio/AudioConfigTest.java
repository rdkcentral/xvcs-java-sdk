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
import com.comcast.vrex.sdk.messageModel.send.Unit;
import com.comcast.vrex.sdk.messageModel.send.WakeUpWord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AudioConfigTest {

    private static final String hfAudioProfile = "XR19";

    @Test
    public void pttAudioConfig() {
        Audio pttAudio = AudioConfig.fromDefaultPttConfig().buildAudio();
        assertEquals(pttAudio.getEnvoyCodec(), Codec.PCM_16_16K);
        assertEquals(pttAudio.getAudioProfile(), "XR11");
        assertEquals(pttAudio.getAudioModel(), AudioModel.PTT);
        assertEquals(pttAudio.getTriggeredBy(), TriggeredBy.PTT);
    }

    @Test
    public void hfAudioConfig() {
        WakeUpWord wakeUpWord = WakeUpWord.builder()
                .withStartOfWakeUpWord(100)
                .withEndOfWakeUpWord(780)
                .withUnit(Unit.MS)
                .build();

        AudioConfig hfAudioConfig = AudioConfig.customConfiguration();
        hfAudioConfig.withAudioProfile(hfAudioProfile)
                .withEnvoyCodec(Codec.PCM_16_16K)
                .withTriggeredBy(TriggeredBy.WUW)
                .withWakeUpWord(wakeUpWord);

        Audio hfAudio = hfAudioConfig.buildAudio();

        assertEquals(hfAudio.getAudioProfile(), hfAudioProfile);
        assertEquals(hfAudio.getEnvoyCodec(), Codec.PCM_16_16K);
        assertEquals(hfAudio.getAudioModel(), AudioModel.HF);
        assertEquals(hfAudio.getTriggeredBy(), TriggeredBy.WUW);
        assertEquals(hfAudio.getWuw(), wakeUpWord);
    }
}
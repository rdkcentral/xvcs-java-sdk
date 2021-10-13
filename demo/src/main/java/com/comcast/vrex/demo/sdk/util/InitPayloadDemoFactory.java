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

package com.comcast.vrex.demo.sdk.util;

import com.comcast.vrex.sdk.audio.AudioConfig;
import com.comcast.vrex.sdk.message.InitPayloadBuilder;
import com.comcast.vrex.sdk.messageModel.send.InitPayload;
import com.comcast.vrex.sdk.messageModel.send.TriggeredBy;
import com.comcast.vrex.sdk.messageModel.send.Unit;
import com.comcast.vrex.sdk.messageModel.send.WakeUpWord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class InitPayloadDemoFactory {

    private static JsonNode initJson = null;

    public static InitPayload getPttPayload() throws IOException {
        return InitPayloadBuilder
                .fromInitJson(getInitJsonFromFile())
                .withAudio(AudioConfig.fromDefaultPttConfig())
                .buildMessage();
    }

    public static InitPayload getHfPayload(int start, int end) throws IOException {
        WakeUpWord wakeUpWord = WakeUpWord.builder()
                .withStartOfWakeUpWord(start)
                .withEndOfWakeUpWord(end)
                .withUnit(Unit.MS)
                .build();
        AudioConfig audioConfig = AudioConfig.fromDefaultPttConfig()
                .withAudioProfile("XR19")
                .withTriggeredBy(TriggeredBy.WUW)
                .withWakeUpWord(wakeUpWord);
        return InitPayloadBuilder
                .fromInitJson(getInitJsonFromFile())
                .withAudio(audioConfig)
                .buildMessage();
    }

    public static JsonNode getInitJsonFromFile() throws IOException {
        if (initJson == null) {
            InputStream inputStream = InitPayloadDemoFactory.class.getResourceAsStream("/test_init_payload.json");
            ObjectMapper mapper = new ObjectMapper();
            initJson = mapper.readTree(inputStream);
        }
        return initJson;
    }
}

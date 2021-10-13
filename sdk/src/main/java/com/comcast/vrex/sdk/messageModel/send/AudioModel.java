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

package com.comcast.vrex.sdk.messageModel.send;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public enum AudioModel {

    HF("hf"),
    PTT("ptt");

    private static Map<String, AudioModel> reverseLookup = new HashMap<>();

    static {
        for (AudioModel model : AudioModel.values()) {
            reverseLookup.put(model.model, model);
        }
    }

    private String model;

    @JsonCreator
    public static AudioModel getModel(String audioModel) {
        return reverseLookup.getOrDefault(audioModel, PTT);
    }

    @JsonValue
    public String getAudioModel() {
        return model;
    }

}

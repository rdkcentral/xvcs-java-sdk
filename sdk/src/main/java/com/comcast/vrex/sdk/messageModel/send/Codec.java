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
import com.google.cloud.speech.v1p1beta1.RecognitionConfig.AudioEncoding;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public enum Codec {
    ADPCM(AudioEncoding.LINEAR16, "audio/L16;rate=16000"),
    PCM_16_16K(AudioEncoding.LINEAR16, "audio/L16;rate=16000"),       //Linear PCM, 16 bit, signed little-endian, 16 kHz
    U_LAW(AudioEncoding.MULAW, "audio/basic;rate=8000");         //µ-law, 8-bit, 8 kHz

    private static final Map<String, Codec> reverseLookup = new HashMap<>();

    static {
        for (Codec type : Codec.values()) {
            reverseLookup.put(type.name().toLowerCase(), type);
        }
    }

    private final AudioEncoding googleCodec;
    private final String nuanceCodec;

    @JsonCreator
    public static Codec getCodec(String r) {
        if (StringUtils.isEmpty(r)) return null;
        return reverseLookup.get(r.toLowerCase());
    }

    @Override
    public String toString() {
        return this.name();
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }
}

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
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum Unit {
    SAMPLE("sample"),
    MS("ms");

    private static final Map<String, Unit> STRING_MAP = new HashMap<>();

    static {
        for (Unit unit : Unit.values()) {
            STRING_MAP.put(unit.value.toLowerCase(), unit);
        }
    }

    private final String value;

    Unit(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Unit getUnit(String unit) {
        if (StringUtils.isEmpty(unit)) return SAMPLE;
        return STRING_MAP.getOrDefault(unit.toLowerCase(), SAMPLE);
    }

}

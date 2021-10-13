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

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum VrexMode {
    SR, NLP, EVENT, AR, EXEC;

    private static final Map<String, VrexMode> STRING_MAP = new HashMap<>();

    static {
        for (VrexMode field : VrexMode.values()) {
            STRING_MAP.put(field.name().toLowerCase(), field);
        }
    }

    public static VrexMode getVrexMode(String mode) {
        if (StringUtils.isEmpty(mode)) return null;
        return STRING_MAP.get(mode.toLowerCase());
    }

    public static Optional<VrexMode> getVrexModeOptional(String mode) {
        VrexMode m = getVrexMode(mode);
        return m == null ? Optional.empty() : Optional.of(m);
    }
}
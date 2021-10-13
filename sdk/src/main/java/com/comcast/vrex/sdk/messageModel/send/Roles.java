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
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum Roles {
    INPUT("input"),
    ENVOY("envoy"),
    AV("av"),
    RENDER("render");

    private String value;

    @JsonCreator
    public static Roles forValues(String role) {
        return getRole(role);
    }

    private static final Map<String, Roles> reverseLookup = new HashMap<>();

    static {
        for (Roles type : Roles.values()) {
            reverseLookup.put(type.value.toLowerCase(), type);
        }
    }

    public static Roles getRole(String r) {
        if (StringUtils.isEmpty(r)) return null;
        return reverseLookup.get(r.toLowerCase());
    }

    public static boolean isRenderRole(InitPayload initPayload) {
        if (initPayload.getRoles() == null) return false;
        return initPayload.getRoles().stream().anyMatch(r -> r == Roles.RENDER);
    }

    public static boolean isEnvoyRole(InitPayload initPayload) {
        if (initPayload.getRoles() == null) return false;
        return initPayload.getRoles().stream().anyMatch(r -> r == Roles.ENVOY);
    }
}

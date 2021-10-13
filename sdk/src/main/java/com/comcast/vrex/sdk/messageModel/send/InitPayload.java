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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableSet;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter()
public class InitPayload {
    @JsonIgnore
    private static String THIS_WEB_SOCKET = "thisWebSocket";

    private String aspectRatio;
    private Audio audio;
    private String bouquet;
    private Set<String> capabilities = Collections.emptySet();
    private String clientProfile;
    private String deviceSwVersion;
    private boolean dictationMode;
    @Setter(AccessLevel.NONE)
    private String downstreamProtocol = THIS_WEB_SOCKET;
    @Setter(AccessLevel.NONE)
    private String experience = "X1";
    private Id id = new Id();
    private String language;
    private String mac;
    private String name;
    @NotEmpty
    private Set<Roles> roles = ImmutableSet.of();
    private String stbStatus;
    private String stbSwVersion;
    private String subBouquet;
    private String text;
    private boolean suppressCallRecording;
    private String timeZone;
    @Setter(AccessLevel.NONE)
    private String transmissionProtocol = THIS_WEB_SOCKET;
    private String tvStatus;
    private Set<String> vrexFields = new HashSet<>();
    private Set<String> vrexModes = new HashSet<>();

    @Getter @Setter @JsonIgnore
    private String deviceId;
    @Getter @Setter @JsonIgnore
    private String accountId;
    @Getter @Setter @JsonIgnore
    private String customerId;

}

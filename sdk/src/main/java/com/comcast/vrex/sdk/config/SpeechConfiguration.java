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

package com.comcast.vrex.sdk.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SpeechConfiguration {

    private Connection connection;
    private Logging logging;
    private Auth auth;

    private String appId;
    private String deviceId;
    private String accountId;
    private String customerId;

    @Getter
    @Setter
    @ToString
    public static class Connection {
        private Websocket websocket;
    }

    @Getter
    @Setter
    @ToString
    public static class Websocket {
        private String url;
        private int version = 1;
    }

    @Getter
    @Setter
    @ToString
    public static class Auth {
        private boolean enabled = true;
        private Integer renewInterval; //in minutes
    }

    @Getter
    @Setter
    @ToString
    public static class Logging {
        private boolean enabled = true;
        private String logLevel = "INFO";
    }
}

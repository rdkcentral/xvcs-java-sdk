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

package com.comcast.vrex.sdk.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMappers {

    private static ObjectMapper DEFAULT_MAPPER;
    private static ObjectMapper NO_FAIL_ON_UNKNOWN_MAPPER;

    public static ObjectMapper defaultMapper() {
        if (DEFAULT_MAPPER == null) DEFAULT_MAPPER = new ObjectMapper();
        return DEFAULT_MAPPER;
    }

    public static ObjectMapper noFailOnUnknownPropsMapper() {
        if (NO_FAIL_ON_UNKNOWN_MAPPER == null) {
            NO_FAIL_ON_UNKNOWN_MAPPER = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return NO_FAIL_ON_UNKNOWN_MAPPER;
    }
}

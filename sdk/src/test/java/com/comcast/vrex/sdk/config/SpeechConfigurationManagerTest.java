
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpeechConfigurationManagerTest {

    @Test
    void init() throws Exception {
        SpeechConfigurationManager manager = new SpeechConfigurationManager(this.getClass(),
                new TestSpeechAuthenticator(), "test-speech-config.yml", "test-speech-secrets.properties");

        assertNull(manager.getConfiguration());
        manager.init();
        assertNotNull(manager.getConfiguration());

        assertTrue(manager.authIsEnabled());
        assertEquals(manager.getDeviceId(), "11111");
        assertEquals(manager.getAccountId(), "22222");
        assertEquals(manager.getCustomerId(), "33333");

        assertEquals(manager.getAppId(), "12345678");

        assertNotNull(manager.getSpeechAuthenticator());
    }
}
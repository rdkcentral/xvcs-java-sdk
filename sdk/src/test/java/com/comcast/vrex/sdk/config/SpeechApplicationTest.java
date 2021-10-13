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

import com.comcast.vrex.sdk.exception.SpeechConfigException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpeechApplicationTest {

    @Test
    public void speechApplicationNullException() {
        assertThrows(NullPointerException.class, () -> SpeechApplication.newApplication(null));

        assertThrows(NullPointerException.class, () -> SpeechApplication.
                newApplication(this.getClass())
                .withSecretsFile(null)
                .init());

        assertThrows(NullPointerException.class, () -> SpeechApplication.
                newApplication(this.getClass())
                .withConfigFile(null)
                .init());

        assertThrows(NullPointerException.class, () -> SpeechApplication.
                newApplication(this.getClass())
                .withAuthenticator(null)
                .init());
    }

    @Test
    public void noAuthThrowsException() {
        assertThrows(SpeechConfigException.class, () -> SpeechApplication.
                newApplication(this.getClass())
                .init());
    }


    //TODO
    public void speechApplicationInitTest() throws Exception {
        SpeechConfigurationManager configurationManager = SpeechApplication
                .newApplication(this.getClass())
                .init();

    }
}
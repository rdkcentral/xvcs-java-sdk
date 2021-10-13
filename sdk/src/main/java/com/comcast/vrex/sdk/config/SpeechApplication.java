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

import com.comcast.vrex.sdk.auth.SpeechAuthenticator;
import lombok.NonNull;

public class SpeechApplication {

    private SpeechAuthenticator authenticator;
    private String secretsFile;
    private String configFile;
    private Class<?> clazz;

    private SpeechApplication(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static SpeechApplication newApplication(@NonNull Class<?> clazz) {
        return new SpeechApplication(clazz);
    }

    public SpeechConfigurationManager init() throws Exception {
        SpeechConfigurationManager configManager = new SpeechConfigurationManager(clazz, authenticator, configFile, secretsFile);
        configManager.init();
        return configManager;
    }

    public SpeechApplication withSecretsFile(@NonNull String secretsFile) {
        this.secretsFile = secretsFile;
        return this;
    }

    public SpeechApplication withConfigFile(@NonNull String configFile) {
        this.configFile = configFile;
        return this;
    }

    public SpeechApplication withAuthenticator(@NonNull SpeechAuthenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }
}

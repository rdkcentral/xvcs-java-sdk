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

import com.comcast.vrex.sdk.auth.AuthConfiguration;
import com.comcast.vrex.sdk.auth.SpeechAuthenticator;
import com.comcast.vrex.sdk.exception.SpeechConfigException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.yaml.snakeyaml.Yaml;

import javax.naming.AuthenticationException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class SpeechConfigurationManager {

    private static final Yaml yaml = new Yaml();
    private final String configFile;
    private final String secretsFile;
    @Getter
    private final SpeechAuthenticator speechAuthenticator;
    private final Class<?> clazz;
    private SpeechConfiguration speechConfiguration;
    private AuthConfiguration authConfiguration;

    public SpeechConfigurationManager(Class<?> clazz, SpeechAuthenticator speechAuthenticator, String configFile, String secretsFile) {
        this.configFile = configFile == null ? "speech-config.yml" : configFile;
        this.secretsFile = secretsFile == null ? "speech-secrets.properties" : secretsFile;
        this.speechAuthenticator = speechAuthenticator;
        this.clazz = clazz;
        authConfiguration = new AuthConfiguration();
    }

    public void init() throws Exception {
        speechConfigInit();
        if (authIsEnabled()) {
            log.debug("Authentication is enabled");
            if (speechAuthenticator == null) {
                throw new SpeechConfigException("Authentication is Enabled. However no authenticator provided." +
                        "Either disable authentication or pass an authenticator to SpeechApplication.init()");
            }
            setupAuth();
        } else if (speechAuthenticator != null) {
            log.warn("Authenticator provided. However, authentication is disabled in yml configuration");
        }
    }

    public void speechConfigInit() {
        speechConfiguration = yaml.loadAs(readYamlConfig(), SpeechConfiguration.class);
        updateLoggerConfig();
        checkAppId();
    }

    private void setupAuth() throws Exception {
        InputStream inputStream = getInputStreamForFile(secretsFile);
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (NullPointerException e) {
            throw new SpeechConfigException("Unable to read " + secretsFile);
        }

        String clientId = properties.getProperty("auth.clientId");
        String secret = properties.getProperty("auth.secret");
        String endpoint = properties.getProperty("auth.endpoint");
        Integer renewInterval = speechConfiguration.getAuth().getRenewInterval();

        if (clientId == null || secret == null || endpoint == null) {
            throw new SpeechConfigException("Missing auth.endpoint OR auth.clientId OR auth.secret in speech-secrets.properties");
        }
        if (renewInterval == null) {
            throw new SpeechConfigException("auth.renewInterval not specified in speech-config.yml");
        }

        authConfiguration.setClientId(clientId);
        authConfiguration.setSecret(secret);
        authConfiguration.setEndpoint(endpoint);
        authConfiguration.setRenewInterval(renewInterval);

        speechAuthenticator.setAuthConfig(authConfiguration);
        speechAuthenticator.fetchInitialToken();
        speechAuthenticator.scheduleTokenFetching();
    }

    private void updateLoggerConfig() {
        SpeechConfiguration.Logging speechLogging = speechConfiguration.getLogging();
        if (speechLogging == null) return;

        boolean loggingEnabled = speechLogging.isEnabled();
        Level level = Level.INFO;
        if (loggingEnabled) {
            String levelStr = speechLogging.getLogLevel();
            if (levelStr == null) return;
            try {
                level = Level.valueOf(levelStr);
            } catch (IllegalArgumentException e) {
                log.error("Unknown Log level. Check if the log level specified in speech-config.yml is valid.");
                throw e;
            }
        } else {
            level = Level.OFF;
        }

        Configurator.setAllLevels("com.comcast.vrex.sdk", level);
    }

    private InputStream readYamlConfig() {
        InputStream in = getInputStreamForFile(configFile);
        if (in == null) {
            throw new SpeechConfigException(String.format("No configuration file found. Does %s exist in your project?", configFile));
        }
        return in;
    }

    private InputStream getInputStreamForFile(String file) {
        return clazz
                .getClassLoader()
                .getResourceAsStream(file);
    }

    private void checkAppId() {
        try {
            String id = speechConfiguration.getAppId().trim();
            if (id == null || id.equals("")) {
                throw new NullPointerException();
            }
        } catch (NullPointerException e) {
            throw new SpeechConfigException("No app.id specified in speech-config.yml.");
        }
    }

    public String getAppId() {
        return speechConfiguration.getAppId().trim();
    }

    public String getDeviceId() {
        return trimId(speechConfiguration.getDeviceId());
    }

    public String getAccountId() {
        return trimId(speechConfiguration.getAccountId());
    }

    public String getCustomerId() {
        return trimId(speechConfiguration.getCustomerId());
    }

    private String trimId(String id) {
        if (id != null) return id.trim();
        return null;
    }

    public boolean authIsEnabled() {
        return speechConfiguration.getAuth().isEnabled();
    }

    public SpeechConfiguration getConfiguration() {
        return speechConfiguration;
    }
}

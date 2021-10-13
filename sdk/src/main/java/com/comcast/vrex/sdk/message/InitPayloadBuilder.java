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

package com.comcast.vrex.sdk.message;

import com.comcast.vrex.sdk.audio.AudioConfig;
import com.comcast.vrex.sdk.messageModel.common.LanguageType;
import com.comcast.vrex.sdk.messageModel.send.InitPayload;
import com.comcast.vrex.sdk.messageModel.send.Roles;
import com.comcast.vrex.sdk.messageModel.send.Values;
import com.comcast.vrex.sdk.messageModel.send.VrexMode;
import com.comcast.vrex.sdk.util.ObjectMappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class InitPayloadBuilder {

    private static final String EXECUTE_RESPONSE = "executeResponse";
    private static ObjectMapper objectMapper = ObjectMappers.defaultMapper();

    private InitPayload initPayload;

    private InitPayloadBuilder() {
        this.initPayload = new InitPayload();
    }

    private InitPayloadBuilder(JsonNode jsonNode) {
        this.initPayload = objectMapper.convertValue(jsonNode, InitPayload.class);
    }

    private InitPayloadBuilder(InitPayload initPayload, boolean shouldCopy) {
        if (shouldCopy) {
            JsonNode jsonNode = objectMapper.valueToTree(initPayload);
            this.initPayload = objectMapper.convertValue(jsonNode, InitPayload.class);
        } else {
            this.initPayload = initPayload;
        }
    }

    public static InitPayloadBuilder copyFromExistingPayload(InitPayload initPayload) {
        return new InitPayloadBuilder(initPayload, true);
    }

    public static InitPayloadBuilder fromEmptyInitPayload() {
        return new InitPayloadBuilder();
    }

    public static InitPayloadBuilder customizeExistingPayload(InitPayload initPayload) {
        return new InitPayloadBuilder(initPayload, false);
    }

    public static InitPayloadBuilder fromInitJson(JsonNode jsonNode) {
        return new InitPayloadBuilder(jsonNode);
    }

    public InitPayloadBuilder withRoles(Roles... roles) {
        initPayload.setRoles(Sets.newHashSet(roles));
        return this;
    }

    public InitPayloadBuilder withAddedRole(Roles roles) {
        initPayload.getRoles().add(roles);
        return this;
    }

    public InitPayloadBuilder withCapabilities(String... capabilities) {
        initPayload.setCapabilities(Sets.newHashSet(capabilities));
        return this;
    }

    public InitPayloadBuilder withAddedCapability(String capability) {
        initPayload.getCapabilities().add(capability);
        return this;
    }

    public InitPayloadBuilder withRemovedCapability(String capability) {
        initPayload.getCapabilities().remove(capability);
        return this;
    }

    public InitPayloadBuilder withContextCapability() {
        initPayload.getCapabilities().add("CONTEXT");
        return this;
    }

    public InitPayloadBuilder withLanguage(LanguageType language) {
        initPayload.setLanguage(language.getLanguageCode());
        return this;
    }

    public InitPayloadBuilder withDeviceId(String deviceId) {
        initPayload.setDeviceId(deviceId);
        return this;
    }

    public InitPayloadBuilder withCustomerId(String customerId) {
        initPayload.setCustomerId(customerId);
        return this;
    }

    public InitPayloadBuilder withAccountId(String accountId) {
        initPayload.setAccountId(accountId);
        return this;
    }

    public InitPayloadBuilder withAudio(AudioConfig audioConfig) {
        initPayload.setAudio(audioConfig.buildAudio());
        return this;
    }

    public InitPayloadBuilder withText(@NonNull String text) {
        initPayload.setText(text);
        return this;
    }

    public InitPayloadBuilder withDictationModeEnabled(boolean dictationModeEnabled) {
        initPayload.setDictationMode(dictationModeEnabled);
        return this;
    }

    public InitPayloadBuilder withVrexModes(VrexMode... vrexModes) {
        Arrays.stream(vrexModes).forEach(vrexMode -> initPayload.getVrexModes().add(vrexMode.name()));
        return this;
    }

    public InitPayloadBuilder withExecuteResponse(boolean enabled) {
        Set<String> vrexFields = initPayload.getVrexFields();
        if (enabled) {
            vrexFields.add(EXECUTE_RESPONSE);
        } else {
            vrexFields.remove(EXECUTE_RESPONSE);
        }
        return this;
    }

    public InitPayloadBuilder withTimeZone(String timeZone) {
        initPayload.setTimeZone(timeZone);
        return this;
    }

    private void updateId(String key, String val) {
        if (val != null) {
            initPayload.getId().getValues().add(new Values(key, val));
        }
    }

    public void refreshIds() {
        initPayload.getId().setValues(new ArrayList<>());
        updateId("deviceId", initPayload.getDeviceId());
        updateId("accountId", initPayload.getAccountId());
        updateId("customerId", initPayload.getCustomerId());
    }

    public InitPayload buildMessage() {
        if (initPayload.getText() != null) initPayload.setAudio(null);
        return initPayload;
    }
}

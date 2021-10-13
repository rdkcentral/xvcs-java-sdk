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

import com.comcast.vrex.sdk.messageModel.common.LanguageType;
import com.comcast.vrex.sdk.messageModel.send.InitPayload;
import com.comcast.vrex.sdk.messageModel.send.Roles;
import com.comcast.vrex.sdk.messageModel.send.VrexMode;
import com.comcast.vrex.sdk.util.ObjectMappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitPayloadBuilderTest {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMappers.defaultMapper();

    private static final String CUSTOMER_ID = "someCustomerId";
    private static final String DEVICE_ID = "someDeviceId";
    private static final String ACCOUNT_ID = "someAccountId";
    private static final String EXECUTE_RESPONSE = "executeResponse";

    private void testUnmodifiedInit(InitPayload init) {
        assertEquals(init.getCustomerId(), CUSTOMER_ID);
        assertEquals(init.getDeviceId(), DEVICE_ID);
        assertEquals(init.getAccountId(), ACCOUNT_ID);
        assertEquals(init.getLanguage(), LanguageType.ENG_USA.getLanguageCode());
        assertEquals(init.getRoles().size(), 2);
        assertTrue(init.getRoles().containsAll(Arrays.asList(Roles.RENDER, Roles.AV)));
        assertEquals(init.getCapabilities().size(), 3);
        assertTrue(init.getCapabilities().containsAll(Arrays.asList("WBW", "UHD", "TEST")));
        assertFalse(init.isDictationMode());
        assertTrue(init.getVrexFields().contains(EXECUTE_RESPONSE));
        assertTrue(init.getVrexModes().containsAll(Arrays.asList(VrexMode.EXEC.toString(), VrexMode.NLP.toString())));
    }

    private void testModifiedInit(InitPayload init) {
        assertEquals(init.getCustomerId(), CUSTOMER_ID + 2);
        assertEquals(init.getDeviceId(), DEVICE_ID + 2);
        assertEquals(init.getAccountId(), ACCOUNT_ID + 2);
        assertEquals(init.getLanguage(), LanguageType.FRA_CAN.getLanguageCode());
        assertEquals(init.getRoles().size(), 3);
        assertTrue(init.getRoles().containsAll(Arrays.asList(Roles.RENDER, Roles.AV, Roles.INPUT)));
        assertEquals(init.getCapabilities().size(), 3);
        assertTrue(init.getCapabilities().containsAll(Arrays.asList("WBW", "UHD", "ABC")));
        assertTrue(init.isDictationMode());
        assertTrue(init.getVrexFields().isEmpty());
        assertTrue(init.getVrexModes().containsAll(Arrays.asList(VrexMode.EXEC.toString(),
                VrexMode.NLP.toString(), VrexMode.AR.toString())));
    }

    @Test
    void fromEmptyInitPayload() {
        InitPayload init = getCustomPayload();
        testUnmodifiedInit(init);
    }

    @Test
    void customizeExistingPayload() {
        InitPayload initOld = getCustomPayload();

        InitPayloadBuilder.customizeExistingPayload(initOld)
                .withCustomerId(CUSTOMER_ID + 2)
                .withAccountId(ACCOUNT_ID + 2)
                .withDeviceId(DEVICE_ID + 2)
                .withVrexModes(VrexMode.AR)
                .withExecuteResponse(false)
                .withAddedCapability("ABC")
                .withRemovedCapability("TEST")
                .withAddedRole(Roles.INPUT)
                .withDictationModeEnabled(true)
                .withLanguage(LanguageType.FRA_CAN);

        testModifiedInit(initOld);
    }

    @Test
    void fromInitJson() throws IOException {
        JsonNode initJson = getJsonForFile("test_init_payload.json");
        InitPayload init = InitPayloadBuilder
                .fromInitJson(initJson)
                .withAddedCapability("ABC")
                .buildMessage();

        assertEquals(init.getLanguage(), LanguageType.ENG_USA.getLanguageCode());
        assertEquals(init.getRoles().size(), 4);
        assertTrue(init.getRoles().containsAll(Arrays.asList(Roles.RENDER, Roles.AV, Roles.INPUT, Roles.ENVOY)));
        assertEquals(init.getCapabilities().size(), 4);
        assertTrue(init.getCapabilities().containsAll(Arrays.asList("WBW", "UHD", "ABC", "TEST")));
        assertFalse(init.isDictationMode());
        assertTrue(init.getVrexFields().contains(EXECUTE_RESPONSE));
        assertEquals(init.getVrexModes().size(), 4);
        assertTrue(init.getVrexModes().containsAll(Arrays.asList(VrexMode.EXEC.toString(),
                VrexMode.NLP.toString(), VrexMode.AR.toString(), VrexMode.SR.toString())));

    }

    @Test
    void copyFromExistingPayload() {

        InitPayload initOld = getCustomPayload();

        InitPayload initNew = InitPayloadBuilder.copyFromExistingPayload(initOld)
                .withCustomerId(CUSTOMER_ID + 2)
                .withAccountId(ACCOUNT_ID + 2)
                .withDeviceId(DEVICE_ID + 2)
                .withVrexModes(VrexMode.AR)
                .withExecuteResponse(false)
                .withAddedCapability("ABC")
                .withRemovedCapability("TEST")
                .withAddedRole(Roles.INPUT)
                .withDictationModeEnabled(true)
                .withLanguage(LanguageType.FRA_CAN)
                .buildMessage();

        testUnmodifiedInit(initOld);
        testModifiedInit(initNew);
    }

    private InitPayload getCustomPayload() {
        return InitPayloadBuilder.fromEmptyInitPayload()
                .withCustomerId(CUSTOMER_ID)
                .withDeviceId(DEVICE_ID)
                .withAccountId(ACCOUNT_ID)
                .withLanguage(LanguageType.ENG_USA)
                .withRoles(Roles.RENDER, Roles.AV)
                .withCapabilities("WBW", "UHD", "TEST")
                .withExecuteResponse(true)
                .withVrexModes(VrexMode.EXEC, VrexMode.NLP)
                .buildMessage();
    }

    private JsonNode getJsonForFile(String file) throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(file);
        return OBJECT_MAPPER.readTree(inputStream);
    }
}
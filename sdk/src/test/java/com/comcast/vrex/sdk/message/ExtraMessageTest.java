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

import com.comcast.vrex.sdk.messageModel.common.EventMessage;
import com.comcast.vrex.sdk.util.ObjectMappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtraMessageTest {

    ObjectMapper objectMapper = ObjectMappers.defaultMapper();

    @Test
    public void contextMessage() {
        String trx = "123";
        JsonNode jsonNode = objectMapper.createObjectNode().put("a", "b");
        ExtraMessage contextMessage = new ContextMessage(trx, jsonNode);

        EventMessage eventMessage = contextMessage.getMessage();
        assertEquals(eventMessage.getTrx(), trx);
        assertTrue(eventMessage.getMsgPayload().equals(jsonNode));
        assertNotNull(eventMessage.getCreated());
    }
}
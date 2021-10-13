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

package com.comcast.vrex.sdk.messageModel.common;


import com.comcast.vrex.sdk.util.ObjectMappers;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "msgType",
        "trx",
        "created",
        "msgPayload"
})
@Getter
@Setter
@NoArgsConstructor
@ToString
public class EventMessage {
    private String msgType;
    private String trx;
    private Long created;
    private JsonNode msgPayload;
    
    private EventMessage(String trx) {
        this.created = System.currentTimeMillis();
        this.trx = trx;
    }

    public EventMessage(String trx, MessageType messageType) {
        this.created = System.currentTimeMillis();
        this.trx = trx;
        this.msgType = messageType.getValue();
    }

    public EventMessage(String trx, String messageType, JsonNode payload) {
        this.created = System.currentTimeMillis();
        this.trx = trx;
        this.msgType = messageType;
        this.msgPayload = payload;
    }

    public EventMessage(String trx, MessageType messageType, JsonNode payload) {
        this.created = System.currentTimeMillis();
        this.trx = trx;
        this.msgType = messageType.getValue();
        this.msgPayload = payload;
    }

    public <T> EventMessage(String trx, MessageType messageType, T vrexResponse) {
        this.created = System.currentTimeMillis();
        this.trx = trx;
        this.msgType = messageType.getValue();
        this.msgPayload = ObjectMappers.defaultMapper().convertValue(vrexResponse, JsonNode.class);
    }

}
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

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum MessageType {

	//Bidirectional message
	CLOSE_CONNECTION("closeConnection"),

	//Incoming messages
	INIT("init"),
	ARS("ars"),
	ARS_READY("ars_ready"),
	CONTEXT("context"),
	CONTEXT_READY("context_ready"),
	END_OF_STREAM("endOfStream"),

	//Outgoing messages
	LISTENING("listening"),
	WBW_END_OF_SPEECH("vrexResponse"),
	FINAL_SESSION_RESPONSE("vrexResponse"),
	WBW_TRANSCRIPTION("asr"),
	END_OF_UTTERANCE("asr"),
	TRANSCRIPTION_READY("transcription_ready"),
	WUW_VERIFICATION("wuwVerification"),

	//Internal messages
	TRANSCRIPTION_UPDATE(""),
	AR_REQUEST(""),
	AR_COMPLETED(""),
	AR_FAILED(""),
	AUDIO(""),
	FATAL_ERROR(""),
	INTERNAL_ERROR(""),
	CLOSE_CONNECTION_ON_FAILURE(""),
	STORE_AUDIO(""),
	STORE_METADATA(""),
	DICTATED_STOP(""),

	//Detailed Fatal Errors
	FATAL_ERROR_VOICE(""),
	FATAL_WEB_SOCKET_ERROR(""),
	INCOMPLETE_INIT(""),
	INTER_PACKET_DELAY(""),
	NO_TRANSCRIPTION_FORCED_CLOSE(""),
	SILENCE_AUDIO(""),
	SR_NULL_TRANSCRIPTION(""),
	SR_RESPONSE_TIMEOUT(""),
	USER_MUTED_MIC(""),
	WUW_VERIFICATION_RESULT("");

	private static Map<String, MessageType> valueToEnumMap = new HashMap<>();

	static {
		for(MessageType messageType : MessageType.values()) {
			valueToEnumMap.put(messageType.getValue().toLowerCase(), messageType);
		}
		valueToEnumMap = ImmutableMap.copyOf(valueToEnumMap);
	}

	public static MessageType getMessageType(String value) {
		return valueToEnumMap.get(StringUtils.lowerCase(value));
	}

	private String value;

	MessageType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return this.value;
	}
}
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum LanguageType {
	ENG_USA("eng-USA", "en-US", "eng-USA","eng" ),
	SPA_XLA("spa-XLA", "es-US", "esp-USA", "spa"),
	FRA_CAN("fra-CAN", "fr-CA", "fra-CAN","fra"),
	ENG_GBR("eng-GBR", "en-GB", "eng-GBR","eng-gb"),
	ITA_ITA("ita-ITA", "it-IT",  "ita-ITA", "ita"),
	DEU_DEU("deu-DEU", "de-DE", "deu-DEU","ger");

	private static final Map<String, LanguageType> reverseLookup = new HashMap<>();

	static {
		for (LanguageType type : LanguageType.values()) {
			for (String lang : type.languageFromRequest) {
				reverseLookup.put(lang.toLowerCase(), type);
			}
			reverseLookup.put(type.languageCode.toLowerCase(), type);
		}
	}

	private static final Map<String, LanguageType> reverseLookupOnlyNextGen = new HashMap<>();

	static {
		for (LanguageType type : LanguageType.values()) {
			reverseLookupOnlyNextGen.put(type.nextGenLanguage.toLowerCase(), type);
		}
	}

	private String languageCode;
	private String[] languageFromRequest;
	private String googleLanguage;
	private String nextGenLanguage;
	
	LanguageType(String languageCode, String googleLanguage, String nextGenLanguage, String... languageFromRequest) {
		this.languageCode = languageCode;
		this.googleLanguage = googleLanguage;
		this.languageFromRequest = languageFromRequest;
		this.nextGenLanguage = nextGenLanguage;
	}

	@JsonCreator
	public static LanguageType forValues(String language) {
		return getLanguageTypeOrDefault(language);
	}

	@JsonValue
	public String getLanguageCode() {
		return languageCode;
	}

	public String getGoogleLanguage() {
		return googleLanguage;
	}

	@Override
	public String toString() {
        return this.languageCode;
    }

    public static LanguageType getLanguageTypeOrDefault(String language) {
		if (language == null) return ENG_USA;
		return reverseLookup.getOrDefault(language.toLowerCase(), ENG_USA);
	}

	public static Optional<LanguageType> getLanguageTypeForNextGen(String language) {
		if (language == null) return Optional.empty();
		return Optional.ofNullable(reverseLookupOnlyNextGen.get(language.toLowerCase()));
	}
}
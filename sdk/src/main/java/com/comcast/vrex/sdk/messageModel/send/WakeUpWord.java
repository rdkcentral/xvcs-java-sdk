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

package com.comcast.vrex.sdk.messageModel.send;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true, setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@EqualsAndHashCode
public class WakeUpWord {
    @JsonProperty("sowuw")
    private Integer startOfWakeUpWord;
    @JsonProperty("eowuw")
    private Integer endOfWakeUpWord;
    private Unit unit;
    private Detector detector;
//    private long startTs;
//    private long endTs;

    public int getSowuwByMs() {
        if (null == unit || Unit.SAMPLE == unit) {
            return getTimeByMs(startOfWakeUpWord);
        }
        return startOfWakeUpWord;
    }

    public int getEowuwByMs() {
        if (null == unit || Unit.SAMPLE == unit) {
            return getTimeByMs(endOfWakeUpWord);
        }
        return endOfWakeUpWord;
    }

    private int getTimeByMs(int sampleTime) {
        // Our audio rate is 16K.
        return sampleTime >>> 4;
    }
}

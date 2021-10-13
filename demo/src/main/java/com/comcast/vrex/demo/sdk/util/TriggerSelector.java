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

package com.comcast.vrex.demo.sdk.util;

import com.comcast.vrex.sdk.messageModel.send.TriggeredBy;
import com.google.common.collect.Lists;

import java.util.List;

public class TriggerSelector {
    private static List<String> triggers = Lists.newArrayList("Push To Talk", "Hands Free");

    public static TriggeredBy selectTrigger() {
        int selectedOption = Utils.displayOptions("Select a trigger:", triggers);

        return selectedOption == 0 ? TriggeredBy.PTT : TriggeredBy.WUW;
    }
}

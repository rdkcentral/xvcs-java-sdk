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

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;

public class DemoAudio {

    public static AudioInputStream openFile(String fileName, TriggeredBy triggeredBy) throws UnsupportedAudioFileException, IOException {
        String triggerPath = triggeredBy.equals(TriggeredBy.WUW) ? "hf/" : "ptt/";
        return AudioSystem.getAudioInputStream(
                new BufferedInputStream(
                        DemoAudio.class.getResourceAsStream("/audio/" + triggerPath + fileName + ".wav")
                )
        );
    }
}

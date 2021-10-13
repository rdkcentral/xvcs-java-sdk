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

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class FileSelector {

    private static List<HfFile> hfFileNames = Lists.newArrayList(
            new HfFile("hx-netflix", 100, 835),
            new HfFile("hx-show-me-the-guide", 100, 790)
    );
    private static List<String> pttFileNames = Lists.newArrayList("HBO", "show_me_all_kids_movies");

    public static String selectPttFile() {
        int selectedFile = Utils.displayOptions("Select an audio file to stream:", pttFileNames);

        return pttFileNames.get(selectedFile);
    }

    public static HfFile selectHfFile() {
        List<String> fileNames = hfFileNames.stream().map(x -> x.getFileName()).collect(Collectors.toList());
        int selectedFile = Utils.displayOptions("Select an audio file to stream:", fileNames);

        return hfFileNames.get(selectedFile);
    }

    @AllArgsConstructor
    @Getter
    public static class HfFile {
        private String fileName;
        private int start;
        private int end;
    }
}

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

import java.util.List;
import java.util.Scanner;

public class Utils {

    public static void println(Object obj) {
        System.out.println(obj);
    }

    public static void print(Object obj) {
        System.out.print(obj);
    }

    public static int displayOptions(String message, List<String> options) {
        Scanner scanner = new Scanner(System.in);

        int selectedOption;
        do {
            println(message);
            int i = 1;
            for (String fileName : options) {
                println(i++ + ". " + fileName);
            }
            selectedOption = scanner.nextInt();
        } while (selectedOption < 1 || selectedOption > options.size());

        return selectedOption - 1;
    }
}

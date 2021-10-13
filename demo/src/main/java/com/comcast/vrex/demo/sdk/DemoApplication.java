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

package com.comcast.vrex.demo.sdk;

import com.comcast.vrex.demo.sdk.demo.mic.CaptureFromMicDemo;
import com.comcast.vrex.demo.sdk.demo.Demo;
import com.comcast.vrex.demo.sdk.demo.ReadFromFileDemo;
import com.comcast.vrex.demo.sdk.demo.StreamFromFileDemo;

import java.util.Scanner;

import static com.comcast.vrex.demo.sdk.util.Utils.println;

public class DemoApplication {

    public static void main(String[] args) {
        println("\t\t******************************************");
        println("\t\t*                                        *");
        println("\t\t*    This is a demo of VREX-JAVA-SDK     *");
        println("\t\t*                                        *");
        println("\t\t******************************************\n");

        startDemo();
    }

    public static void startDemo() {
        while (true) {
            Scanner scanner = new Scanner(System.in);

            println("\n***** Select an option *****");
            println("1. Exit");
            println("2. DEMO - Read from existing audio file");
            println("3. DEMO - Capture from Microphone");
            println("4. DEMO - Stream from file (Simulate Capture from Microphone)");

            int demoChoice = scanner.nextInt();
            Demo demo = null;
            switch (demoChoice) {
                case 1:
                    println("\n******** ENDING DEMO *****\n");
                    System.exit(0);
                case 2:
                    println("\n******** STARTING READ FROM FILE DEMO *****\n");
                    demo = new ReadFromFileDemo();
                    break;
                case 3:
                    println("\n******** CAPTURE FROM MIC *****\n");
                    demo = new CaptureFromMicDemo();
                    break;
                case 4:
                    println("\n******** STREAM FROM FILE *****\n");
                    demo = new StreamFromFileDemo();
                    break;
                default:
                    println("\n******** ERROR: INVALID CHOICE *****\n");
                    continue;
            }

            try {
                demo.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

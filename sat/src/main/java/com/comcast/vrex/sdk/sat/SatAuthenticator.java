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

package com.comcast.vrex.sdk.sat;

import com.comcast.vrex.sdk.auth.AuthResponse;
import com.comcast.vrex.sdk.auth.SpeechAuthenticator;
import com.comcast.vrex.sdk.exception.SatException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class SatAuthenticator extends SpeechAuthenticator {
    private final ScheduledExecutorService SAT_EXECUTOR;
    private final OkHttpClient client;
    private final ObjectMapper mapper;
    private final ReadWriteLock rwLock;
    private final Lock readLock, writeLock;

    public SatAuthenticator() {
        SAT_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
        client = new OkHttpClient();
        mapper = new ObjectMapper();
        client.retryOnConnectionFailure();
        rwLock = new ReentrantReadWriteLock();
        readLock = rwLock.readLock();
        writeLock = rwLock.writeLock();
    }

    private Request buildRequest() {
        return new Request.Builder()
                .url(authConfig.getEndpoint())
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Client-Id", authConfig.getClientId())
                .addHeader("X-Client-Secret", authConfig.getSecret())
                .post(RequestBody.create(null, new byte[]{}))
                .build();
    }


    @Override
    public AuthResponse getAuthResponse() {
        readLock.lock();
        try {
            return authResponse;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void fetchInitialToken() throws IOException {
        updateToken(); //runs on init synchronously just once
    }

    @Override
    public void scheduleTokenFetching() {
        Runnable satRunnable = () -> {
            try {
                updateToken();
            } catch (IOException e) {
                throw new SatException("SAT Error", e);
            }
        };
        int renewInterval = authConfig.getRenewInterval();
        SAT_EXECUTOR.scheduleAtFixedRate(satRunnable, renewInterval, renewInterval, TimeUnit.MINUTES);
    }

    public void updateToken() throws IOException {
        Request request = buildRequest();
        log.debug("SAT request URL: " + request.url());
        Call call = client.newCall(request);
        Response response = call.execute();
        if (response.isSuccessful()) {
            log.info("Sat Token retrieved successfully");
            ResponseBody responseBody = response.body();
            SatResponse satResponse = mapper.readValue(responseBody.byteStream(), SatResponse.class);
            satResponse.setTimestamp(Instant.now());
            writeLock.lock();
            try {
                authResponse = satResponse;
            } finally {
                writeLock.unlock();
            }
        } else {
            throw new SatException("SAT Response was not successful. response=" + response);
        }
    }
}

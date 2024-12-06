/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sohu.tv.mq.cloud.common.model;

public class RequestCode {
    /**
     * get broker store stats
     */
    public static final int GET_BROKER_STORE_STATS = 327;

    /**
     * view moment stats data
     */
    public static final int VIEW_MOMENT_STATS_DATA = 328;

    /**
     * view send message rate limit
     */
    public static final int VIEW_SEND_MESSAGE_RATE_LIMIT = 329;

    /**
     * view send message rate limit
     */
    public static final int UPDATE_SEND_MESSAGE_RATE_LIMIT = 330;

    // 为了修复RequestCode的冲突，MQCloud的RequestCode从9000开始
    /**
     * get broker store stats
     */
    public static final int GET_BROKER_STORE_STATS_V2 = 9000;

    /**
     * view moment stats data
     */
    public static final int VIEW_MOMENT_STATS_DATA_V2 = 9001;

    /**
     * view send message rate limit
     */
    public static final int VIEW_SEND_MESSAGE_RATE_LIMIT_V2 = 9002;

    /**
     * view send message rate limit
     */
    public static final int UPDATE_SEND_MESSAGE_RATE_LIMIT_V2 = 9003;

    /**
     * get client connection size
     */
    public static final int GET_CLIENT_CONNECTION_SIZE = 9004;

    /**
     * get all consumer info
     */
    public static final int GET_ALL_CONSUMER_INFO = 9005;

    public static final int GET_ALL_PRODUCER_INFO = 328;
}

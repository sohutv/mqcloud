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
package com.sohu.tv.mq.flink.source;

import java.io.Serializable;

public class RunningChecker implements Serializable {
    private volatile State state = State.RUNNING;

    public boolean isRunning() {
        return state.equals(State.RUNNING);
    }

    public boolean isFailed() {
        return state.equals(State.FAILED);
    }

    public boolean isFinished(){
        return state.equals(State.FINISHED);
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum State {
        RUNNING,
        FAILED,
        FINISHED
    }
}

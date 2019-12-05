/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.callhome;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This agent connects to the WSO2 Update servers to check whether there are new
 * updates available for the running product and notify users accordingly.
 *
 * @since 1.0.0
 */
public class Agent {
    private static final Logger logger = Logger.getLogger(Agent.class.getName());

    public static void premain(@SuppressWarnings("unused") final String agentArgument) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.schedule(executeCallHome, 0, TimeUnit.SECONDS);
        scheduledExecutorService.shutdown();
    }

    private static Runnable executeCallHome = () -> {
        CallHome callHome = new CallHome();
        callHome.execute();
    };
}

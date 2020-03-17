/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.callhome.core.CallHome;
import org.wso2.callhome.core.DataHolder;
import org.wso2.callhome.utils.CallHomeInfo;
import org.wso2.callhome.utils.MessageFormatter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class is used to invoke the CallHome server and print the CallHome response.
 *
 * @since 1.0.0
 */
public class CallHomeExecutor {

    private static final Log log = LogFactory.getLog(CallHomeExecutor.class);
    private static final int CALL_HOME_TIMEOUT_SECONDS = 180;
    private static final int LINE_LENGTH = 80;

    /**
     * This method is used to execute the CallHome service.
     *
     * @param callHomeInfo CallHomeInfo object containing the information required to invoke the CallHome server
     */
    public static void execute(CallHomeInfo callHomeInfo) {

        CallHome callHome = new CallHome(callHomeInfo);
        callHome.execute();
    }

    /**
     * This method is used to print the CallHome response message.
     */
    public static void printMessage() {

        Future<String> callHomeResponse = DataHolder.getInstance().getResponse();

        Thread callHomeThread = new Thread(() -> {
            if (callHomeResponse != null) {
                try {
                    String response = callHomeResponse.get(CALL_HOME_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    if (!response.isEmpty()) {
                        String formattedMessage = MessageFormatter.formatMessage(response, LINE_LENGTH);
                        log.info(formattedMessage);
                    }
                } catch (InterruptedException e) {
                    log.debug("CallHome is interrupted", e);
                } catch (ExecutionException e) {
                    log.debug("CallHome execution failure", e);
                } catch (TimeoutException e) {
                    log.debug("CallHome did not complete in expected time", e);
                }
            } else {
                log.debug("CallHome response is not available");
            }
        });
        callHomeThread.setDaemon(true);
        callHomeThread.setName("callHomeThread");
        callHomeThread.start();
    }
}

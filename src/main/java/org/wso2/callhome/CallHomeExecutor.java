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
    public static final String CARBON_AUTO_UPDATE_CHECK = "carbon.auto.update.check";

    /**
     * This method is used to execute the CallHome service.
     *
     * @param callHomeInfo CallHomeInfo object containing the information required to invoke the CallHome server
     */
    public static void execute(CallHomeInfo callHomeInfo) {

        if ((System.getProperty(CARBON_AUTO_UPDATE_CHECK) == null) ||
                !System.getProperty(CARBON_AUTO_UPDATE_CHECK).equals("false")) {
            CallHome callHome = new CallHome(callHomeInfo);
            callHome.execute();
        }
    }

    /**
     * This method is used to print the CallHome response message.
     */
    public static void printMessage() {

        Thread callHomeThread = new Thread(() -> {
            String message = getMessage();
            if (!message.isEmpty()) {
                String formattedMessage = MessageFormatter.formatMessage(message, LINE_LENGTH);
                log.info(formattedMessage);
            }
        });
        callHomeThread.setDaemon(true);
        callHomeThread.setName("callHomeThread");
        callHomeThread.start();
    }

    /**
     * This method is used to get the CallHome message.
     *
     * @return Returns the CallHome message
     */
    public static String getMessage() {

        Future<String> callHomeResponse = DataHolder.getInstance().getResponse();
        String response = "";
        if (callHomeResponse != null) {
            try {
                response = callHomeResponse.get(CALL_HOME_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.debug("Error while getting message");
            }
        }
        return response;
    }
}

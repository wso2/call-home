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
package org.wso2.callhome.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.callhome.utils.MessageFormatter;
import org.wso2.carbon.kernel.CarbonServerInfo;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This service component creates a CallHome service.
 *
 * @since 1.0.2
 */
@Component(
        name = "org.wso2.callhome.internal.CallHomeComponent",
        immediate = true
)
public class CallHomeComponent {

    private static final Logger logger = LoggerFactory.getLogger(CallHome.class);
    private static final int CALL_HOME_TIMEOUT_SECONDS = 180;
    private static final int LINE_LENGTH = 80;

    @Activate
    public void activate() {

        Thread callHomeComponentThread = new Thread(() -> {
            logger.debug("Activating CallHome component");
            Future<String> callHomeResponse = DataHolder.getInstance().getResponse();
            if (callHomeResponse != null) {
                try {
                    String response = callHomeResponse.get(CALL_HOME_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    if (!response.isEmpty()) {
                        String formattedMessage = MessageFormatter.formatMessage(response, LINE_LENGTH);
                        logger.info(formattedMessage);
                    }
                } catch (InterruptedException e) {
                    logger.debug("CallHome is interrupted", e);
                } catch (ExecutionException e) {
                    logger.debug("CallHome execution failure", e);
                } catch (TimeoutException e) {
                    logger.debug("CallHome did not complete in expected time", e);
                }
            } else {
                logger.debug("CallHome response is not available");
            }
        });
        callHomeComponentThread.setDaemon(true);
        callHomeComponentThread.setName("callHomeComponentThread");
        callHomeComponentThread.start();
    }

    @Deactivate
    public void deactivate() {

        logger.debug("Deactivating CallHome component");
    }

    @Reference(
            name = "org.wso2.carbon.kernel",
            service = CarbonServerInfo.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterCallHomeService"
    )
    protected void registerCallHomeService(CarbonServerInfo carbonServerInfo) {

        logger.debug("CallHome service registered");
    }

    protected void unregisterCallHomeService(CarbonServerInfo carbonServerInfo) {

        logger.debug("CallHome service unregistered");
    }
}

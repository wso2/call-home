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
package org.wso2.carbon.callhome.internal;

import org.wso2.callhome.CallHomeExecutor;
import org.wso2.callhome.utils.CallHomeInfo;
import org.wso2.callhome.utils.Util;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * This method implements the ServerStartupObserver.
 *
 * @since 1.0.0
 */
public class CallHomeObserver implements ServerStartupObserver {
    private static final String TRUSTSTORE_LOCATION = "Security.TrustStore.Location";
    private static final String TRUSTSTORE_PASSWORD = "Security.TrustStore.Password";
    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {

        ServerConfigurationService serverConfigurationService =
                DataHolder.getInstance().getServerConfigurationService();

        String productHome = org.wso2.carbon.callhome.utils.Util.getProductHome();
        String trustStoreLocation = serverConfigurationService.getFirstProperty(TRUSTSTORE_LOCATION);
        String trustStorePassword = serverConfigurationService.getFirstProperty(TRUSTSTORE_PASSWORD);

        CallHomeInfo callHomeInfo = Util.createCallHomeInfo(productHome, trustStoreLocation, trustStorePassword);
        CallHomeExecutor.execute(callHomeInfo);
        CallHomeExecutor.printMessage();
    }
}

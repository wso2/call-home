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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.api.ServerConfigurationService;

/**
 * This service component creates a CallHomeComponent.
 * @since 1.0.2
 */
@Component(
        name = "org.wso2.callhome.internal.CallHomeComponent",
        immediate = true
)
public class CallHomeComponent {

    private static final Log log = LogFactory.getLog(CallHomeComponent.class);
    private static ServerConfigurationService serverConfigurationService = null;

    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            log.debug("Activating CallHomeComponent");
            CallHome callHome = new CallHome();
            callHome.execute();
        } catch (Throwable e) {
            log.error("Failed to load CallHomeComponent.", e);
        }
    }

    @Deactivate
    protected void deactivate() {

        log.debug("Deactivating CallHomeComponent");
    }

    public ServerConfigurationService getServerConfigurationService() {

        return CallHomeComponent.serverConfigurationService;
    }

    @Reference(
            name = "org.wso2.carbon.base.api",
            service = ServerConfigurationService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerConfigurationService"
    )
    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {

        log.debug("Set ServerConfiguration service");
        DataHolder.getInstance().setServerConfigurationService(serverConfigurationService);
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {

        log.debug("Unset ServerConfiguration Service");
        DataHolder.getInstance().setServerConfigurationService(null);
    }
}

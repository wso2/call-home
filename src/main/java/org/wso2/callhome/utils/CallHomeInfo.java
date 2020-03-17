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
package org.wso2.callhome.utils;

/**
 * The CallHomeInfo class contains the information required for the call-home feature.
 *
 * @since 1.0.0
 */
public class CallHomeInfo {

    private String productName;
    private String productVersion;
    private String channel;
    private String trialSubscriptionId;
    private String trustStoreLocation;
    private String trustStorePassword;
    private String operatingSystem;

    private long updateLevel;

    public String getProductName() {

        return productName;
    }

    void setProductName(String productName) {

        this.productName = productName;
    }

    public String getProductVersion() {

        return productVersion;
    }

    void setProductVersion(String productVersion) {

        this.productVersion = productVersion;
    }

    public String getChannel() {

        return channel;
    }

    void setChannel(String channel) {

        this.channel = channel;
    }

    public String getTrialSubscriptionId() {

        return trialSubscriptionId;
    }

    void setTrialSubscriptionId(String trialSubscriptionId) {

        this.trialSubscriptionId = trialSubscriptionId;
    }

    public String getTrustStoreLocation() {

        return trustStoreLocation;
    }

    void setTrustStoreLocation(String trustStoreLocation) {

        this.trustStoreLocation = trustStoreLocation;
    }

    public String getTrustStorePassword() {

        return trustStorePassword;
    }

    void setTrustStorePassword(String trustStorePassword) {

        this.trustStorePassword = trustStorePassword;
    }

    public String getOperatingSystem() {

        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {

        this.operatingSystem = operatingSystem;
    }

    public long getUpdateLevel() {

        return updateLevel;
    }

    void setUpdateLevel(long updateLevel) {

        this.updateLevel = updateLevel;
    }
}

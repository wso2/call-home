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
package org.wso2.callhome.utils;

/**
 * The ExtractedInfo class contains the information required for the call-home feature.
 *
 * @since 1.0.0
 */
public class ExtractedInfo {

    private String channel;
    private String productName;
    private String productVersion;
    private String operatingSystem;
    private long updateLevel;

    public String getChannel() {

        return channel;
    }

    public void setChannel(String channel) {

        this.channel = channel;
    }

    public String getProductName() {

        return productName;
    }

    public void setProductName(String productName) {

        this.productName = productName;
    }

    public String getProductVersion() {

        return productVersion;
    }

    public void setProductVersion(String productVersion) {

        this.productVersion = productVersion;
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

    public void setUpdateLevel(long updateLevel) {

        this.updateLevel = updateLevel;
    }
}

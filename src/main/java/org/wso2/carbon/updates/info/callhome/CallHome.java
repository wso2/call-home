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
package org.wso2.carbon.updates.info.callhome;

import org.wso2.carbon.updates.info.callhome.updates.Updates;

import java.io.IOException;
import java.net.URISyntaxException;


/**
 * The CallHome program implements an application that uses WSO2 Update servers to check whether there are new
 * updates available for the running product and notify users accordingly.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class CallHome {

    /**
     * This is the main method which makes use of getOs, getUsername and getUpdateLevel methods.
     *
     * @param args Unused
     * @throws IOException If an IO exception occurs
     */
    public static void main(String[] args) throws IOException, URISyntaxException {
        Updates update = new Updates();
        update.displayUpdateLevelInfo();
    }
}

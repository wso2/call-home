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
package org.wso2.carbon.updates.info.callhome.updates;

import org.wso2.carbon.updates.info.callhome.CallHome;
import org.wso2.carbon.updates.info.callhome.utils.ExtractInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;

/**
 * The Updates program implements an application where it retrieves the data from the ExtractInfo class and calls
 * to the WSO2 Update Servers to check whether there are new updates available.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class Updates {
    private static final Logger LOGGER = Logger.getLogger(CallHome.class.getName());

    /**
     * This method calls the WSO2 Update server and warns the user regarding the available updates.
     *
     * @throws IOException        If an IO exception occurs
     * @throws URISyntaxException If an URI Syntax Error occurs
     */
    public void displayUpdateLevelInfo() throws IOException, URISyntaxException {
        String readLine;
        int lastIndexOf = ExtractInfo.getProductDetails().lastIndexOf('-');
        String url = "<endpoint>" + "?email=" +
                URLEncoder.encode(ExtractInfo.getUsername(), "UTF-8") + "&product-name=" +
                URLEncoder.encode(ExtractInfo.getProductDetails().substring(0, lastIndexOf), "UTF-8")
                + "&product-version=" + URLEncoder.encode(ExtractInfo.getProductDetails().substring(lastIndexOf + 1),
                "UTF-8") + "&operating-system=" + URLEncoder.encode(ExtractInfo.getOs(), "UTF-8") +
                "&updates-level=" + URLEncoder.encode(String.valueOf(ExtractInfo.getUpdateLevel()), "UTF-8");
        URL urlForGetRequest = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) urlForGetRequest.openConnection();
        connection.setRequestProperty("-H", "Content-Type: application/json");
        connection.setRequestProperty("-H", "Accept: application/json");
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(),
                    Charset.forName("UTF-8"));
            StringBuilder response;
            try (BufferedReader in = new BufferedReader(inputStreamReader)) {
                response = new StringBuilder();
                while ((readLine = in.readLine()) != null) {
                    response.append(readLine);
                }
                LOGGER.info("There are " + response.toString() + " updates available.");
                inputStreamReader.close();
            }
        }
    }
}

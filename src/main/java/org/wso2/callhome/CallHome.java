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

import org.wso2.callhome.exception.CallHomeException;
import org.wso2.callhome.utils.ExtractedInfo;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;

import static java.lang.System.getProperty;

/**
 * The CallHome class contains all the methods required to extract the required information.
 *
 * @since 1.0.0
 */
class CallHome {

    private static final Logger logger = Logger.getLogger(CallHome.class.getName());
    private static final String OS_NAME = "os.name";
    private static final String CALL_HOME_ENDPOINT = "https://api.updates.wso2.com/call-home/v1.0.0/check-update";
    private static final String ACCESS_TOKEN = "c0ba3727-e7ec-31d3-9dc2-528f30c387af";
    private static final int RETRY_DELAY = 10000;
    private static final int HTTP_CONNECTION_TIMEOUT = 10000;

    /**
     * This method executes in order to retrieve the required data.
     */
    void execute() {

        try {
            String productNameAndVersion = getProductNameAndVersion();
            long updateLevel = getUpdateLevel();
            String channel = getChannelFromConfigYaml();
            String operatingSystem = getOSDetails();

            String productName = extractProductName(productNameAndVersion);
            String productVersion = extractProductVersion(productNameAndVersion);

            ExtractedInfo extractedInfo = new ExtractedInfo();
            extractedInfo.setChannel(channel);
            extractedInfo.setProductName(productName);
            extractedInfo.setProductVersion(productVersion);
            extractedInfo.setOperatingSystem(operatingSystem);
            extractedInfo.setUpdateLevel(updateLevel);

            String updateInfo = retrieveUpdateInfoFromServer(extractedInfo);
            printUpdateInfo(updateInfo);
        } catch (CallHomeException e) {
            logger.warning("Failed to get the number of updates available.");
            logger.fine(e.toString());
        }
    }

    /**
     * This method returns the product home path.
     *
     * @return String The product home path
     * @throws CallHomeException If it is unable to get the product home path
     */
    private String getProductHome() throws CallHomeException {

        try {
            return new File(CallHome.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile().getParent();
        } catch (URISyntaxException e) {
            logger.fine("Cannot find product path " + e.getMessage());
            throw new CallHomeException("Cannot find product path", e);
        }
    }

    /**
     * This method gets the index of the last "-" character.
     *
     * @param productDetails Product name with the version
     * @return Index of the last "-" character
     */
    private int getLastIndexOfHyphen(String productDetails) {

        return productDetails.lastIndexOf('-');
    }

    /**
     * This method extracts the product name.
     *
     * @param productNameAndVersion Product name with the version
     * @return Name of the product
     */
    private String extractProductName(String productNameAndVersion) {

        return productNameAndVersion.substring(0, getLastIndexOfHyphen(productNameAndVersion));
    }

    /**
     * This method extracts the product version.
     *
     * @param productNameAndVersion Product name with the version
     * @return Version of the product
     */
    private String extractProductVersion(String productNameAndVersion) {

        return productNameAndVersion.substring(getLastIndexOfHyphen(productNameAndVersion) + 1);
    }

    /**
     * This method reads a yaml and returns the data in the yaml as a map.
     *
     * @param yamlPath Path to the config.yaml file
     * @return Username details in the config.yaml
     * @throws FileNotFoundException File not found in the given path
     */
    private Map readYaml(String yamlPath) throws FileNotFoundException {

        Yaml yaml = new Yaml();
        Map<String, String> configs = Collections.emptyMap();
        if (Files.exists(Paths.get(yamlPath))) {
            InputStream inputStream = new FileInputStream(yamlPath);
            Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            configs = yaml.load(fileReader);
        }
        return configs;
    }

    /**
     * This method reads the required information from the config.yaml.
     *
     * @return The necessary information the user
     * @throws CallHomeException If the config.yaml is not available
     */
    private String getChannelFromConfigYaml() throws CallHomeException {

        String channel = "";
        String configPath = Paths.get(getProductHome(), "updates", "config.yaml").toString();
        try {
            Map configs = readYaml(configPath);
            if (configs.containsKey("channel")) {
                channel = (String) configs.get("channel");
            }
        } catch (FileNotFoundException e) {
            logger.fine("Config yaml not found " + e.toString());
        }
        return channel;
    }

    /**
     * This method returns the product name and the version.
     *
     * @return Product name and the version
     * @throws CallHomeException If unable to find the product.txt file within the updates directory of the product or
     *                           it is unable to read the content of the file.
     */
    private String getProductNameAndVersion() throws CallHomeException {

        Path productTxtPath = Paths.get(getProductHome(), "updates", "product.txt");
        if (!Files.exists(productTxtPath)) {
            logger.fine("Unable to find the product.txt file");
            throw new CallHomeException("Unable to find the product.txt file " + productTxtPath.toString());
        }

        byte[] productTxtContent;
        try {
            productTxtContent = Files.readAllBytes(productTxtPath);
        } catch (IOException e) {
            logger.fine("Unable to read the product.txt content");
            throw new CallHomeException("Unable to read the product.txt content", e);
        }
        return new String(productTxtContent, StandardCharsets.UTF_8);
    }

    /**
     * This method returns the operating system.
     *
     * @return Operating system of the user
     */
    private String getOSDetails() {

        return getProperty(OS_NAME);
    }

    /**
     * This method reads the last updated timestamp level of the product.
     *
     * @return Last updated timestamp
     * @throws CallHomeException If it is unable to get the list of files in the updates/wum directory
     */
    private long getUpdateLevel() throws CallHomeException {

        File updatesDirectory = Paths.get(getProductHome(), "updates", "wum").toFile();
        File[] listOfFiles = updatesDirectory.listFiles();

        long lastUpdateLevel = 0L;
        if (listOfFiles != null) {
            lastUpdateLevel = getLastUpdateLevel(listOfFiles);
        }
        return lastUpdateLevel;
    }

    /**
     * This method gets the latest update level.
     *
     * @param listOfFiles A list of all the updates files
     * @return The last updated timestamp
     */
    private long getLastUpdateLevel(File[] listOfFiles) {

        long lastUpdateLevel = 0L;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                if (Long.parseLong(listOfFile.getName()) > lastUpdateLevel) {
                    lastUpdateLevel = Long.parseLong(listOfFile.getName());
                }
            }
        }
        return lastUpdateLevel;
    }

    /**
     * This method constructs the URL from the given {@link ExtractedInfo} object.
     *
     * @param extractedInfo ExtractedInfo object
     * @return Endpoint URL
     * @throws CallHomeException If it is enable to construct the CallHome URL
     */
    private URL constructCallHomeURL(ExtractedInfo extractedInfo) throws CallHomeException {

        String productName = extractedInfo.getProductName();
        String productVersion = extractedInfo.getProductVersion();
        String operatingSystem = extractedInfo.getOperatingSystem();
        String channel = extractedInfo.getChannel();
        long updateLevel = extractedInfo.getUpdateLevel();
        try {
            return new URL(CALL_HOME_ENDPOINT +
                    "&product-name=" + productName +
                    "&product-version=" + productVersion +
                    "&operating-system=" + operatingSystem +
                    "&updates-level=" + updateLevel +
                    "&channel=" + channel);
        } catch (MalformedURLException e) {
            logger.fine("Error while creating URL for the CallHome endpoint " + e.getMessage());
            throw new CallHomeException("Error while creating URL for the CallHome endpoint", e);
        }
    }

    /**
     * This method creates an HTTPS connection to a given url.
     *
     * @param url URL of the call home server
     * @return An HTTP connection to the given url
     * @throws IOException If an error occurs while creating an HTTP connection
     */
    private HttpsURLConnection createHttpsURLConnection(URL url) throws IOException {

        HttpsURLConnection connection;
        connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(HTTP_CONNECTION_TIMEOUT);
        return connection;
    }

    /**
     * This method gets the response from the HTTPS connection.
     *
     * @param httpsURLConnection HTTPS connection
     * @return Response from the server
     * @throws CallHomeException If an error occurs while getting the response
     */
    private String getResponse(HttpsURLConnection httpsURLConnection) throws CallHomeException {

        StringBuilder response = new StringBuilder();
        try {
            int responseCode = httpsURLConnection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                try (InputStreamReader inputStreamReader = new InputStreamReader(httpsURLConnection.getInputStream(),
                        StandardCharsets.UTF_8);
                     BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

                    String readLine;
                    while ((readLine = bufferedReader.readLine()) != null) {
                        response.append(readLine);
                    }
                }
            }
        } catch (IOException e) {
            logger.fine("Error while setting request method " + e.getMessage());
            throw new CallHomeException("Error while setting request method", e);
        }
        return response.toString();
    }

    /**
     * This method uses the {@link ExtractedInfo} object to create the connection.
     *
     * @param extractedInfo ExtractedInfo object
     * @return The response from the CallHome server
     * @throws CallHomeException If an error occurs while retrieving information from the CallHome server
     */
    private String retrieveUpdateInfoFromServer(ExtractedInfo extractedInfo) throws CallHomeException {

        URL url = constructCallHomeURL(extractedInfo);
        logger.info(String.valueOf(url));
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                HttpsURLConnection connection = createHttpsURLConnection(url);
                switch (connection.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:
                        logger.fine(url + " OK");
                        return getResponse(connection);
                    case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                        logger.fine(url + " Gateway timeout");
                        break;
                    case HttpURLConnection.HTTP_UNAVAILABLE:
                        logger.fine(url + " Unavailable");
                        break;
                    default:
                        logger.fine(url + " Unknown response code");
                        break;
                }
            } catch (IOException e) {
                logger.fine("Error while connecting to update server " + e.getMessage());
            }

            try {
                Thread.sleep(RETRY_DELAY);
            } catch (InterruptedException e) {
                logger.fine("Error while trying to apply the retry delay");
            }
        }
        throw new CallHomeException("Enable to retrieve updates information from server");
    }

    /**
     * This method logs the message.
     *
     * @param msg Message to be logged
     */
    private void printUpdateInfo(String msg) {

        logger.info(msg);
    }
}

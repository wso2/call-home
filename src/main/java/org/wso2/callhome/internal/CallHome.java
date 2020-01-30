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
package org.wso2.callhome.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.net.ssl.HttpsURLConnection;

import static java.lang.System.getProperty;

/**
 * The CallHome class contains all the methods required to extract the required information.
 *
 * @since 1.0.0
 */
public class CallHome implements Callable<String> {

    private static final Logger log = LoggerFactory.getLogger(CallHome.class);
    private static final String OS_NAME = "os.name";
    private static final String CALL_HOME_ENDPOINT = "https://api.updates.wso2.com/call-home/v1.0.0/check-updates";
    private static final String ACCESS_TOKEN = "45ffddfa-281c-36df-9fd0-d806c3f607ca";
    private static final int RETRY_DELAY = 10000;
    private static final int HTTP_CONNECTION_TIMEOUT = 10000;
    private String carbonProductHome;

    /**
     * This method registers the CallHome object (this) to an {@link ExecutorService}.
     */
    void execute() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> callHomeResponse = executorService.submit(this);
        DataHolder.getInstance().setResponse(callHomeResponse);
    }

    /**
     * This method is executed by the ${@link ExecutorService} and will do series of steps
     * to retrieve the update data.
     *
     * @return update response message
     */
    @Override
    public String call() {

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

            return retrieveUpdateInfoFromServer(extractedInfo);
        } catch (CallHomeException e) {
            log.warn("Failed to get the number of updates available.");
            log.debug(e.toString());
        }
        return "";
    }

    /**
     * This method returns the product home path.
     *
     * @return String The product home path
     */
    private String getProductHome() {

        if (carbonProductHome == null) {
            carbonProductHome = System.getProperty("carbon.home");
        }
        return carbonProductHome;
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
     * This method reads the channel information from the config.yaml.
     *
     * @return The channel used to update the product
     */
    private String getChannelFromConfigYaml() {

        String channel = "";
        String configPath = Paths.get(getProductHome(), "updates", "config.yaml").toString();
        try {
            Map configs = readYaml(configPath);
            if (configs.containsKey("channel")) {
                channel = (String) configs.get("channel");
            }
        } catch (FileNotFoundException e) {
            log.debug("Config yaml not found " + e.toString());
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
            log.debug("Unable to find the product.txt file");
            throw new CallHomeException("Unable to find the product.txt file " + productTxtPath.toString());
        }

        byte[] productTxtContent;
        try {
            productTxtContent = Files.readAllBytes(productTxtPath);
        } catch (IOException e) {
            log.debug("Unable to read the product.txt content");
            throw new CallHomeException("Unable to read the product.txt content", e);
        }
        return new String(productTxtContent, StandardCharsets.UTF_8).trim();
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
     */
    private long getUpdateLevel() {

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
                    "?product-name=" + URLEncoder.encode(productName, "UTF-8") +
                    "&product-version=" + URLEncoder.encode(productVersion, "UTF-8") +
                    "&operating-system=" + URLEncoder.encode(operatingSystem, "UTF-8") +
                    "&updates-level=" + URLEncoder.encode(String.valueOf(updateLevel), "UTF-8") +
                    "&channel=" + URLEncoder.encode(channel, "UTF-8"));
        } catch (MalformedURLException e) {
            log.debug("Error while creating URL for the CallHome endpoint " + e.getMessage());
            throw new CallHomeException("Error while creating URL for the CallHome endpoint", e);
        } catch (UnsupportedEncodingException e) {
            log.debug("Error while encoding URL" + e.getMessage());
            throw new CallHomeException("Error while encoding URL");
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
            log.debug("Error while setting request method " + e.getMessage());
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
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                HttpsURLConnection connection = createHttpsURLConnection(url);
                switch (connection.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:
                        log.debug(url + " OK");
                        return getResponse(connection);
                    case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                        log.debug(url + " Gateway timeout");
                        break;
                    case HttpURLConnection.HTTP_UNAVAILABLE:
                        log.debug(url + " Unavailable");
                        break;
                    default:
                        log.debug(url + " Unknown response code");
                        break;
                }
            } catch (IOException e) {
                log.debug("Error while connecting to update server " + e.getMessage());
            }

            try {
                Thread.sleep(RETRY_DELAY);
            } catch (InterruptedException e) {
                log.debug("Error while trying to apply the retry delay");
            }
        }
        throw new CallHomeException("Unable to retrieve updates information from server");
    }
}

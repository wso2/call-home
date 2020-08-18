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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.callhome.exception.UtilException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * This Util class contains the methods required to create a CallHomeInfo object.
 *
 * @since 1.0.0
 */
public class Util {

    private static final Log log = LogFactory.getLog(Util.class);
    private static final String FULL = "full";
    private static final String UNKNOWN = "unknown";
    private static String carbonProductHome;

    /**
     * This method is used to create a CallHomeInfo object using the product home, trustStoreLocation and
     * trustStorePassword.
     *
     * @param productHome        Product Home
     * @param trustStoreLocation Location of the truststore
     * @param trustStorePassword Password of the truststore
     * @return A CallHomeInfo object
     */
    public static CallHomeInfo createCallHomeInfo(String productHome, String trustStoreLocation,
                                                  String trustStorePassword) {

        if (carbonProductHome == null) {
            carbonProductHome = productHome;
        }

        CallHomeInfo callHomeInfo = new CallHomeInfo();
        try {
            String productNameAndVersion = getProductNameAndVersion();
            long updateLevel = getUpdateLevel();
            String channel = getChannelFromConfigYaml();
            String productName = extractProductName(productNameAndVersion);
            String productVersion = extractProductVersion(productNameAndVersion);
            String trialSubscriptionId = getTrialSubscriptionId();

            callHomeInfo.setProductName(productName);
            callHomeInfo.setProductVersion(productVersion);
            callHomeInfo.setChannel(channel);
            callHomeInfo.setUpdateLevel(updateLevel);
            callHomeInfo.setTrialSubscriptionId(trialSubscriptionId);
            callHomeInfo.setTrustStoreLocation(trustStoreLocation);
            callHomeInfo.setTrustStorePassword(trustStorePassword);

            return callHomeInfo;
        } catch (UtilException e) {
            log.warn("Failed to get the create the CallHomeInfo object.");
            log.debug(e.toString());
        }
        return callHomeInfo;
    }

    /**
     * This method is used to create a CallHomeInfo object using the product name, product version, trustStoreLocation
     * and trustStorePassword.
     *
     * @param productName        Name of the product
     * @param productVersion     Version of the product
     * @param trustStoreLocation Location of the truststore
     * @param trustStorePassword Password of the truststore
     * @return A CallHomeInfo object
     */
    public static CallHomeInfo createDefaultCallHomeInfo(String productName, String productVersion,
                                                         String trustStoreLocation, String trustStorePassword) {

        CallHomeInfo callHomeInfo = new CallHomeInfo();
        callHomeInfo.setProductName(productName);
        callHomeInfo.setProductVersion(productVersion);
        callHomeInfo.setTrustStoreLocation(trustStoreLocation);
        callHomeInfo.setTrustStorePassword(trustStorePassword);
        callHomeInfo.setChannel(FULL);
        callHomeInfo.setTrialSubscriptionId(UNKNOWN);
        callHomeInfo.setUpdateLevel(0L);

        return callHomeInfo;
    }

    /**
     * This method gets the index of the last "-" character.
     *
     * @param productDetails Product name with the version
     * @return Index of the last "-" character
     */
    private static int getLastIndexOfHyphen(String productDetails) {

        return productDetails.lastIndexOf('-');
    }

    /**
     * This method extracts the product name.
     *
     * @param productNameAndVersion Product name with the version
     * @return Name of the product
     */
    private static String extractProductName(String productNameAndVersion) {

        return productNameAndVersion.substring(0, getLastIndexOfHyphen(productNameAndVersion));
    }

    /**
     * This method extracts the product version.
     *
     * @param productNameAndVersion Product name with the version
     * @return Version of the product
     */
    private static String extractProductVersion(String productNameAndVersion) {

        return productNameAndVersion.substring(getLastIndexOfHyphen(productNameAndVersion) + 1);
    }

    /**
     * This method reads a yaml and returns the data in the yaml as a map.
     *
     * @param yamlPath Path to the config.yaml file
     * @return Username details in the config.yaml
     * @throws FileNotFoundException File not found in the given path
     */
    private static Map readYaml(String yamlPath) throws FileNotFoundException {

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
    private static String getChannelFromConfigYaml() {

        String channel = "";
        String configPath = Paths.get(carbonProductHome, "updates", "config.yaml").toString();
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
     * @throws UtilException If unable to find the product.txt file within the updates directory of the product or
     *                       it is unable to read the content of the file.
     */
    private static String getProductNameAndVersion() throws UtilException {

        Path productTxtPath = Paths.get(carbonProductHome, "updates", "product.txt");
        if (!Files.exists(productTxtPath)) {
            log.debug("Unable to find the product.txt file");
            throw new UtilException("Unable to find the product.txt file " + productTxtPath.toString());
        }

        byte[] productTxtContent;
        try {
            productTxtContent = Files.readAllBytes(productTxtPath);
        } catch (IOException e) {
            log.debug("Unable to read the product.txt content");
            throw new UtilException("Unable to read the product.txt content", e);
        }
        return new String(productTxtContent, StandardCharsets.UTF_8).trim();
    }

    /**
     * This method reads the last updated timestamp level of the product.
     *
     * @return Last updated timestamp
     */
    private static long getUpdateLevel() {

        File updatesDirectory = Paths.get(carbonProductHome, "updates", "wum").toFile();
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
    private static long getLastUpdateLevel(File[] listOfFiles) {

        long lastUpdateLevel = 0L;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                String fileName = listOfFile.getName();
                if (fileName.contains(UPDATE)) {
                    fileName = fileName.replace(UPDATE, "");
                }
                if (Long.parseLong(fileName) > lastUpdateLevel) {
                    lastUpdateLevel = Long.parseLong(fileName);
                }
            }
        }
        return lastUpdateLevel;
    }

    /**
     * This method returns the trial subscription id.
     *
     * @return Trial subscription id
     * @throws UtilException If unable to read the content of the trialSubscriptionId.txt
     */
    private static String getTrialSubscriptionId() throws UtilException {

        Path trialSubscriptionIdPath = Paths.get(carbonProductHome, "updates", "trialSubscriptionId.txt");
        byte[] trialSubscriptionId = new byte[0];
        if (Files.exists(trialSubscriptionIdPath)) {
            try {
                trialSubscriptionId = Files.readAllBytes(trialSubscriptionIdPath);
            } catch (IOException e) {
                log.debug("Unable to read the trialSubscriptionId.txt content");
                throw new UtilException("Unable to read the trialSubscriptionId.txt content", e);
            }
        }
        return new String(trialSubscriptionId, StandardCharsets.UTF_8).trim();
    }
}

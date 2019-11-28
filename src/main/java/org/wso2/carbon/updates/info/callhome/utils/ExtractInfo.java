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
package org.wso2.carbon.updates.info.callhome.utils;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.System.getProperty;


/**
 * The ExtractInfo program implements an application where it retrieves the Operating System, email address
 * of the user and the latest updates level of the product.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class ExtractInfo {

    /**
     * This method returns the path of the product home.
     *
     * @return String This returns the path of product home
     * @throws URISyntaxException URI exception
     */
    private static String getProductHome() throws URISyntaxException {
        return new File(ExtractInfo.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()).getParentFile().getParent();
    }

    /**
     * This method is used to get the Operating System of the user.
     *
     * @return String This returns the Operating System.
     */
    public static String getOs() {
        return getProperty("os.name");
    }

    /**
     * This method is used to get the email address of the user.
     *
     * @return String This returns the email address
     * @throws IOException        IO exception
     * @throws URISyntaxException URI exception
     */
    public static String getUsername() throws IOException, URISyntaxException {

        Yaml yaml = new Yaml();
        String configPath = getProductHome() + "/updates/config.yaml";
        if (Files.exists(Paths.get(configPath))) {
            InputStream inputStream = new FileInputStream(configPath);
            Reader fileReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            Map<String, Object> yamlMaps = yaml.load(fileReader);
            return (String) yamlMaps.get("username");
        }
        return "";
    }

    /**
     * This method is used to retrieve the latest the updates level of the users product.
     *
     * @return Long This returns the updates time stamp
     * @throws URISyntaxException URI exception
     */
    public static Long getUpdateLevel() throws URISyntaxException {
        Long lastUpdateLevel = 0L;
        File updatesDirectory = new File(getProductHome() + "/updates/wum/");
        File[] listOfFiles = updatesDirectory.listFiles();
        List<Long> myList = new ArrayList<>();
        if (listOfFiles != null) {
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isDirectory()) {
                    myList.add(Long.valueOf(listOfFile.getName()));
                }
            }
            lastUpdateLevel = Collections.max(myList);
        }
        return lastUpdateLevel;
    }

    /**
     * This method is used to retrieve the product details.
     *
     * @return String This returns the product name and version
     * @throws URISyntaxException If an URI Syntax Error occurs
     * @throws IOException        If an IO exception occurs
     */
    public static String getProductDetails() throws URISyntaxException, IOException {
        String productInfoPath = getProductHome() + "/updates/product.txt";
        File file = new File(productInfoPath);
        return FileUtils.readFileToString(file, String.valueOf(StandardCharsets.UTF_8));
    }
}

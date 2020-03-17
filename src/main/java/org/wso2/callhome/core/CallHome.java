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
package org.wso2.callhome.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import org.wso2.callhome.exception.CallHomeException;
import org.wso2.callhome.utils.CallHomeInfo;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.net.ssl.SSLContext;

import static java.lang.System.getProperty;

/**
 * The CallHome class contains all the methods required to extract the required information.
 *
 * @since 1.0.0
 */
public class CallHome implements Callable<String> {

    private static final Log log = LogFactory.getLog(CallHome.class);
    private static final String OS_NAME = "os.name";
    private static final String CALL_HOME_ENDPOINT = "https://api.updates.wso2.com/call-home/v1.0.0/check-updates";
    private static final String ACCESS_TOKEN = "45ffddfa-281c-36df-9fd0-d806c3f607ca";
    private static final int RETRY_DELAY = 10000;
    private CallHomeInfo callHomeInfo;

    public CallHome(CallHomeInfo callHomeInfo) {

        this.callHomeInfo = callHomeInfo;
    }

    /**
     * This method registers the CallHome object (this) to an {@link ExecutorService}.
     */
    public void execute() {

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
            String operatingSystem = getOSDetails();
            callHomeInfo.setOperatingSystem(operatingSystem);

            return retrieveUpdateInfoFromServer(callHomeInfo);
        } catch (CallHomeException e) {
            log.warn("Failed to get the number of updates available.");
            log.debug(e.toString());
        }
        return "";
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
     * This method constructs the URL from the given {@link CallHomeInfo} object.
     *
     * @param callHomeInfo CallHomeInfo object
     * @return Endpoint URL
     * @throws CallHomeException If it is enable to construct the CallHome URL
     */
    private URL constructCallHomeURL(CallHomeInfo callHomeInfo) throws CallHomeException {

        String productName = callHomeInfo.getProductName();
        String productVersion = callHomeInfo.getProductVersion();
        String operatingSystem = callHomeInfo.getOperatingSystem();
        String channel = callHomeInfo.getChannel();
        String trialSubscriptionId = callHomeInfo.getTrialSubscriptionId();
        long updateLevel = callHomeInfo.getUpdateLevel();
        try {
            return new URL(CALL_HOME_ENDPOINT +
                    "?product-name=" + URLEncoder.encode(productName, "UTF-8") +
                    "&product-version=" + URLEncoder.encode(productVersion, "UTF-8") +
                    "&operating-system=" + URLEncoder.encode(operatingSystem, "UTF-8") +
                    "&updates-level=" + URLEncoder.encode(String.valueOf(updateLevel), "UTF-8") +
                    "&channel=" + URLEncoder.encode(channel, "UTF-8") +
                    "&trial-subscription-id=" + URLEncoder.encode(trialSubscriptionId, "UTF-8"));
        } catch (MalformedURLException e) {
            log.debug("Error while creating URL for the CallHome endpoint " + e.getMessage());
            throw new CallHomeException("Error while creating URL for the CallHome endpoint", e);
        } catch (UnsupportedEncodingException e) {
            log.debug("Error while encoding URL" + e.getMessage());
            throw new CallHomeException("Error while encoding URL");
        }
    }

    /**
     * This method creates a ClosableHTTPClient with the WSO2 TrustStore as a trust manager.
     *
     * @return CloseableHttpClient
     * @throws CallHomeException If an error occurs while create a CloseableHttpClient
     */
    private CloseableHttpClient getCloseableHttpClient(String trustStoreLocation, String trustStorePassword)
            throws CallHomeException {

        File trustStoreFile = new File(trustStoreLocation);
        SSLContextBuilder sslContextBuilder = SSLContexts.custom();

        try {
            sslContextBuilder = sslContextBuilder.loadTrustMaterial(trustStoreFile, trustStorePassword.toCharArray());
            SSLContext sslContext = sslContextBuilder.build();
            SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(sslContext,
                    new NoopHostnameVerifier());
            HttpClientBuilder httpClientBuilder = HttpClients.custom();
            httpClientBuilder.setSSLSocketFactory(sslConSocFactory);
            return httpClientBuilder.build();
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException |
                KeyManagementException e) {
            log.debug("Error while creating CloseableHttpClient");
            throw new CallHomeException("Error while creating CloseableHttpClient", e);
        }
    }

    /**
     * This method gets the response body from the CloseableHttpResponse.
     *
     * @param response ClosableHttpResponse
     * @return Response from the server
     * @throws CallHomeException If an error occurs while getting the response
     */
    private String getClosableHttpResponseBody(CloseableHttpResponse response) throws CallHomeException {

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            return responseHandler.handleResponse(response);
        } catch (IOException e) {
            log.debug("Error while getting the response body from the CloseableHttpResponse");
            throw new CallHomeException("Error while getting the response body from the CloseableHttpResponse", e);
        }
    }

    /**
     * This method uses the {@link CallHomeInfo} object to create the connection.
     *
     * @param callHomeInfo CallHomeInfo object
     * @return The response from the CallHome server
     * @throws CallHomeException If an error occurs while retrieving information from the CallHome server
     */
    private String retrieveUpdateInfoFromServer(CallHomeInfo callHomeInfo) throws CallHomeException {

        URL url = constructCallHomeURL(callHomeInfo);
        HttpGet request = new HttpGet(String.valueOf(url));
        request.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/json");

        try (CloseableHttpClient httpClient = getCloseableHttpClient(
                callHomeInfo.getTrustStoreLocation(), callHomeInfo.getTrustStorePassword())) {
            for (int attempt = 0; attempt < 3; attempt++) {
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    switch (response.getStatusLine().getStatusCode()) {
                        case HttpURLConnection.HTTP_OK:
                            log.debug(url + " OK");
                            return getClosableHttpResponseBody(response);
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
        } catch (IOException e) {
            log.debug("Error while creating a CloseableHttpClient");
        }
        throw new CallHomeException("Unable to retrieve updates information from server");
    }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link MessageFormatter} class helps to print the formatted message.
 *
 * @since 1.0.2
 */
public class MessageFormatter {

    /**
     * The printMessage prints the given message in multiple lines of the given line length.
     *
     * @param message    the message to be printed
     * @param lineLength length of a line
     */
    public static String formatMessage(String message, int lineLength) {

        StringBuilder stringBuilder = new StringBuilder();
        List<String> lines;

        if (message.length() > lineLength) {
            lines = splitToLines(message, lineLength);
            lineLength = trimAndGetMaxLength(lines);
        } else {
            lines = new ArrayList<>(Collections.singletonList(message));
            lineLength = message.length();
        }

        String logSeparator = "\n" + String.format("%" + (lineLength) + "s", "").replace(" ", ".");
        stringBuilder.append(logSeparator);

        for (String line : lines) {
            stringBuilder.append("\n");
            stringBuilder.append(line);
        }

        stringBuilder.append(logSeparator);
        return stringBuilder.toString();
    }

    /**
     * This method trims the lines in a String List and returns the maximum line length.
     *
     * @param lines String List to be trimmed and get the maximum line length
     * @return Maximum line length
     */
    private static int trimAndGetMaxLength(List<String> lines) {

        int maxLen = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = line.trim();
            int len = line.length();
            if (maxLen < len) {
                maxLen = len;
            }
            lines.set(i, line);
        }
        return maxLen;
    }

    /**
     * This method splits the content into lines of the given length.
     *
     * @param content    content to be split into lines
     * @param lineLength length of a line
     * @return a list of lines split into the given length
     */
    private static List<String> splitToLines(String content, int lineLength) {

        List<String> lines = new ArrayList<>();
        Matcher matcher = Pattern.compile("(?s)(.{1," + (lineLength - 1) + "}(\\s|$)|\\S{" + lineLength + "}|\\S+$)")
                .matcher(content);
        while (matcher.find()) {
            String line = matcher.group(1);
            lines.add(line);
        }
        return lines;
    }
}
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
import org.wso2.callhome.internal.CallHomeActivator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link PrintUtils} class helps to print the formatted message.
 *
 * @since 1.0.2
 */
public class PrintUtils {
    private static final Log log = LogFactory.getLog(CallHomeActivator.class);
    private static final String LINE_START = "\n## ";
    private static final String LINE_END = " ##";
    private static final int LEN_LINE_START_AND_END = 6;

    /**
     * The printMessage prints the given message in multiple lines of the given line length.
     *
     * @param message    the message to be printed
     * @param lineLength length of a line
     */
    public static void printMessage(String message, int lineLength) {
        int lineContentLength = lineLength - LEN_LINE_START_AND_END;

        List<String> lines = splitToLines(message, lineContentLength);

        StringBuilder stringBuilder = new StringBuilder();
        String logSeparator = "\n" + String.format("%" + (lineLength) + "s", "").replace(" ", "#");
        stringBuilder.append(logSeparator);

        for (String line : lines) {
            stringBuilder.append(LINE_START);
            stringBuilder.append(line);
            if (line.length() == lineLength) {
                stringBuilder.append(LINE_END);
            } else {
                stringBuilder.append(String.format("%" + (lineContentLength - line.length()) + "s", ""));
                stringBuilder.append(LINE_END);
            }
        }

        stringBuilder.append(logSeparator);
        log.info(stringBuilder.toString());
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

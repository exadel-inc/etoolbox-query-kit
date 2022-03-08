/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.etoolbox.querykit.core.utils;

import lombok.experimental.UtilityClass;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@UtilityClass
public class ResponseUtil {

    private static final String OUTPUT_MESSAGE_TEMPLATE = "{\"%s\":\"%s\"}";

    public static String getJsonMessage(String key, String value) {
        return String.format(OUTPUT_MESSAGE_TEMPLATE, key, value);
    }

    public static void sendString(SlingHttpServletResponse response, String content) throws IOException {
        sendContent(response, HttpServletResponse.SC_OK, Constants.CONTENT_TYPE_TEXT, content);
    }

    public static void sendStringError(SlingHttpServletResponse response, String value) throws IOException {
        sendContent(response, HttpServletResponse.SC_BAD_REQUEST, Constants.CONTENT_TYPE_TEXT, value);
    }

    public static void sendJson(SlingHttpServletResponse response, String content) throws IOException {
        sendJson(response, HttpServletResponse.SC_OK, content);
    }

    public static void sendJsonMessage(SlingHttpServletResponse response, String key, String value) throws IOException {
        sendJson(response, getJsonMessage(key, value));
    }

    public static void sendJsonError(SlingHttpServletResponse response, String value) throws IOException {
        sendJson(response, HttpServletResponse.SC_BAD_REQUEST, getJsonMessage("error", value));
    }

    private static void sendJson(SlingHttpServletResponse response, int status, String content) throws IOException {
        sendContent(response, status, Constants.CONTENT_TYPE_JSON, content);
    }

    private static void sendContent(SlingHttpServletResponse response, int status, String contentType, String content) throws IOException {
        response.setContentType(contentType);
        response.setStatus(status);
        response.getWriter().println(content);
        response.getWriter().flush();
        response.getWriter().close();

    }

}

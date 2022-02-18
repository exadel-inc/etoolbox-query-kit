package com.exadel.etoolbox.querykit.core.utils;

import lombok.experimental.UtilityClass;
import org.apache.sling.api.SlingHttpServletResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@UtilityClass
public class ResponseUtil {

    private static final String OUTPUT_MESSAGE_TEMPLATE = "{\"%s\":\"%s\"}";

    public static void sendMessage(SlingHttpServletResponse response, String key, String value) throws IOException {
        sendJson(response, getJsonMessage(key, value));
    }

    public static void sendError(SlingHttpServletResponse response, String value) throws IOException {
        sendJson(response, HttpServletResponse.SC_BAD_REQUEST, getJsonMessage("error", "Invalid request"));
    }

    public static void sendJson(SlingHttpServletResponse response, String content) throws IOException {
        sendJson(response, HttpServletResponse.SC_OK, content);
    }

    private static void sendJson(SlingHttpServletResponse response, int status, String content) throws IOException {
        response.setContentType(Constants.CONTENT_TYPE_JSON);
        response.setStatus(status);
        response.getWriter().println(content);
        response.getWriter().flush();
        response.getWriter().close();
    }

    public static String getJsonMessage(String key, String value) {
        return String.format(OUTPUT_MESSAGE_TEMPLATE, key, value);
    }

}

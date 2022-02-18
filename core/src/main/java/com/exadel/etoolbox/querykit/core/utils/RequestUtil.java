package com.exadel.etoolbox.querykit.core.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class RequestUtil {

    private static final Pattern SPLITTER = Pattern.compile(",");

    public static long getNumericValue(String rawValue, int defaultValue) {
        if (!StringUtils.isNumeric(rawValue)) {
            return defaultValue;
        }
        return Long.parseLong(rawValue);
    }

    public static String decode(String value, SlingHttpServletRequest request) {
        if (StringUtils.isBlank(value) || "POST".equals(request.getMethod())) {
            return value;
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            log.warn("Could not decode string '{}'", value);
            return value;
        }
    }

    public static List<String> getStringCollection(SlingHttpServletRequest request, String param) {
        RequestParameter[] namedParameters = request.getRequestParameters(param);
        if (namedParameters == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(namedParameters)
                .map(RequestParameter::getString)
                .map(str -> decode(str, request))
                .flatMap(SPLITTER::splitAsStream)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public static Object getValueOrStringifiedArray(SlingHttpServletRequest request, String param) {
        RequestParameter[] namedParameters = request.getRequestParameters(param);
        if (namedParameters != null && namedParameters.length == 1) {
            String raw = decode(namedParameters[0].getString(), request);
            return getValue(raw);
        } else if (namedParameters != null) {
            return Arrays
                    .stream(namedParameters)
                    .map(RequestParameter::getString)
                    .map(str -> decode(str, request))
                    .collect(Collectors.joining(","));
        }
        return null;
    }

    private static Object getValue(String rawValue) {
        if (StringUtils.isNumeric(rawValue)) {
            return Long.parseLong(rawValue);
        }
        if (Boolean.TRUE.toString().equals(rawValue) || Boolean.FALSE.toString().equals(rawValue)) {
            return Boolean.parseBoolean(rawValue);
        }
        return rawValue;
    }
}

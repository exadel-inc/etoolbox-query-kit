package com.exadel.etoolbox.querykit.core.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class RequestUtil {

    public static final String PARAMETER_PREFIX = "q_";
    private static final Pattern SPLITTER = Pattern.compile(Constants.COMMA);

    public static long getNumericValue(String rawValue, int defaultValue) {
        if (!StringUtils.isNumeric(rawValue)) {
            return defaultValue;
        }
        return Long.parseLong(rawValue);
    }

    public static String getParameter(SlingHttpServletRequest request, Resource resource, String name) {
        String result = request.getParameter(name);
        if (StringUtils.isNotEmpty(result)) {
            return RequestUtil.decode(result, request);
        }
        if (resource != null) {
            return resource.getValueMap().get(name.substring(PARAMETER_PREFIX.length()), StringUtils.EMPTY);
        }
        return StringUtils.EMPTY;
    }

    public static List<String> getStringCollection(SlingHttpServletRequest request, Resource resource, String name) {
        List<String> parameterStrings = new ArrayList<>();
        RequestParameter[] matchingRequestParameters = request.getRequestParameters(name);
        if (matchingRequestParameters != null) {
            parameterStrings.addAll(Arrays
                    .stream(matchingRequestParameters)
                    .map(RequestParameter::getString)
                    .map(str -> decode(str, request))
                    .collect(Collectors.toList()));
        }
        if (resource != null) {
            parameterStrings.add(resource.getValueMap().get(name, String.class));
        }
        return parameterStrings
                .stream()
                .filter(StringUtils::isNotBlank)
                .flatMap(SPLITTER::splitAsStream)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Object getValueOrArray(SlingHttpServletRequest request, String param) {
        RequestParameter[] namedParameters = request.getRequestParameters(param);
        if (namedParameters != null && namedParameters.length == 1) {
            String raw = decode(namedParameters[0].getString(), request);
            return getValue(raw);
        } else if (namedParameters != null) {
            return Arrays
                    .stream(namedParameters)
                    .map(RequestParameter::getString)
                    .map(str -> decode(str, request))
                    .toArray(String[]::new);
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

    private static String decode(String value, SlingHttpServletRequest request) {
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
}

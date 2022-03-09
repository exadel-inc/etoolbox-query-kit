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

/**
 * Contains utility methods for manipulating {@code SlingHttpServletRequest} parameters
 */
@UtilityClass
@Slf4j
public class RequestUtil {

    public static final String PARAMETER_PREFIX = "-";
    private static final Pattern SPLITTER = Pattern.compile(Constants.COMMA);

    /**
     * Tries to extract the numeric value from the provided string. Returns the provided default on failure
     * @param rawValue     String representation of a value
     * @param defaultValue Default number
     * @return Long value
     */
    public static long getNumericValue(String rawValue, int defaultValue) {
        if (!StringUtils.isNumeric(rawValue)) {
            return defaultValue;
        }
        return Long.parseLong(rawValue);
    }

    /**
     * Retrieves the string value of a named parameter from the given {@code SlingHttpServletRequest}. If missing in
     * request, tries to retrieve it from the provided fallback resource
     * @param request  {@code SlingHttpServletRequest} instance
     * @param resource {Sling {@code Resource}} object
     * @param name     Name of the parameter
     * @return String value; might be an empty string
     */
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

    /**
     * Retrieves the list of strings as the value of a named parameter from the given {@code SlingHttpServletRequest}
     * object. If missing in request, tries to retrieve it from the provided fallback resource
     * @param request  {@code SlingHttpServletRequest} instance
     * @param resource {Sling {@code Resource}} object
     * @param name     Name of the parameter
     * @return {@code List} object; might be an empty list
     */
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

    /**
     * Retrieves the value of a named parameter from the given {@code SlingHttpServletRequest}. The return value is of
     * an arbitrary type. However, if an array of same-named request parameters was provided, the array-typed value is
     * retrieved
     * @param request {@code SlingHttpServletRequest} instance
     * @param name    Name of the parameter
     * @return Nullable object or an array of objects
     */
    public static Object getValueOrArray(SlingHttpServletRequest request, String name) {
        RequestParameter[] namedParameters = request.getRequestParameters(name);
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

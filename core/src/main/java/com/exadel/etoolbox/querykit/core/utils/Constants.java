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

import java.text.SimpleDateFormat;

/**
 * Contains literal values used across the application
 */
@UtilityClass
public class Constants {

    public static final String CLOSING_BRACKET = ")";
    public static final String COMMA = ",";
    public static final String DOT = ".";
    public static final String DOUBLE_AT = "@@";
    public static final String EQUALITY_SIGN = "=";
    public static final String OPENING_BRACKET = "(";
    public static final String QUOTE = "\"";
    public static final String SEMICOLON = ";";
    public static final String SINGLE_QUOTE = "'";
    public static final String SPACE = " ";

    public static final String ATTRIBUTE_ERROR_MESSAGE = "eqk-errorMessage";
    public static final String ATTRIBUTE_EXECUTION_TIME = "eqk-executionTime";
    public static final String ATTRIBUTE_TOTAL = "eqk-total";

    public static final String CONTENT_TYPE_CSV = "text/csv";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_TEXT = "text/html";

    public static final String NODE_DATASOURCE = "datasource";

    public static final String NODE_TYPE_PLACEHOLDER = "eqk:placeholder";

    public static final String OPERATOR_IN = "in";
    public static final String OPERATOR_UNION = "union";
    public static final String OPERATOR_OPTIONS = "EQK-OPTIONS";

    public static final String PROPERTY_JCR_PATH = "jcr:path";
    public static final String PROPERTY_JCR_TITLE = "jcr:title";
    public static final String PROPERTY_PATH = "path";
    public static final String PROPERTY_TEXT = "text";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_ORDINAL = DOUBLE_AT + "ordinal";

    public static final String TITLE_PATH = "Path";

    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

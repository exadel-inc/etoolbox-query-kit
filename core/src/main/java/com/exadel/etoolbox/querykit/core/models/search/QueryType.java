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
package com.exadel.etoolbox.querykit.core.models.search;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Enumerates possible query types
 */
public enum QueryType {
    JCR_SQL2, XPATH, QUERY_BUILDER, UNSUPPORTED;

    private static final Pattern QUERY_BUILDER_NEWLINE = Pattern.compile("[\\n\\r]");

    private static final Pattern QUERY_BUILDER_ASSERT = Pattern.compile("^\\w+(?:\\.\\w+)*\\s*=");

    /**
     * Retrieves a {@link QueryType} value out of the provided statement
     * @param statement String value
     * @return Enum element
     */
    static QueryType from(String statement) {
        if (StringUtils.isBlank(statement)) {
            return UNSUPPORTED;
        }
        if (StringUtils.startsWithIgnoreCase(statement, "select ")) {
            return JCR_SQL2;
        }
        if (statement.startsWith("/")) {
            return XPATH;
        }
        if (QUERY_BUILDER_NEWLINE.splitAsStream(statement).map(String::trim).allMatch(line -> QUERY_BUILDER_ASSERT.matcher(line).find())) {
            return QUERY_BUILDER;
        }
        return UNSUPPORTED;
    }
}

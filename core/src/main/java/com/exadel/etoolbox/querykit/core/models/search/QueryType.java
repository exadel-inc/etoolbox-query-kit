package com.exadel.etoolbox.querykit.core.models.search;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public enum QueryType {
    JCR_SQL2, XPATH, QUERY_BUILDER, UNSUPPORTED;

    private static final Pattern QUERYBUILDER_NEWLINE = Pattern.compile("[\\n\\r]");

    private static final Pattern QUERYBUILDER_ASSERT = Pattern.compile("^\\w+(?:\\.\\w+)*\\s*=");

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
        if (QUERYBUILDER_NEWLINE.splitAsStream(statement).map(String::trim).allMatch(line -> QUERYBUILDER_ASSERT.matcher(line).find())) {
            return QUERY_BUILDER;
        }
        return UNSUPPORTED;
    }
}

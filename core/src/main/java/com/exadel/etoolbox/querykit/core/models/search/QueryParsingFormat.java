package com.exadel.etoolbox.querykit.core.models.search;

import org.apache.commons.lang3.StringUtils;

public enum QueryParsingFormat {
    SQL, JSON;

    public static QueryParsingFormat of(String value) {
        if (StringUtils.equalsIgnoreCase(value, "json")) {
            return JSON;
        }
        return SQL;
    }
}

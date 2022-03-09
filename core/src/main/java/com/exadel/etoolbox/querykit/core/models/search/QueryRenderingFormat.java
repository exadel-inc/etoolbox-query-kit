package com.exadel.etoolbox.querykit.core.models.search;

import org.apache.commons.lang3.StringUtils;

/**
 * Enumerates possible formats with which a parsed query is rendered
 */
public enum QueryRenderingFormat {
    SQL, JSON;

    /**
     * Retrieves a {@link QueryRenderingFormat} value out of the provided string
     * @param value String value
     * @return Enum element
     */
    public static QueryRenderingFormat of(String value) {
        if (StringUtils.equalsIgnoreCase(value, "json")) {
            return JSON;
        }
        return SQL;
    }
}

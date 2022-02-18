package com.exadel.etoolbox.querykit.core.models.search;

import org.apache.commons.lang3.EnumUtils;

public enum SearchResultFormat {
    JSON, HTML, CSV;

    static SearchResultFormat from(String value) {
        return EnumUtils
                .getEnumList(SearchResultFormat.class)
                .stream()
                .filter(item -> item.toString().equalsIgnoreCase(value))
                .findFirst()
                .orElse(JSON);
    }

}

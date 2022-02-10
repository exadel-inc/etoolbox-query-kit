package com.exadel.etoolbox.query.core.models;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.*;

public class QueryResultModel {

    private static final String PARAMETER_QUERY = "query";
    private static final String PARAMETER_OFFSET = "offset";
    private static final String PARAMETER_LIMIT = "limit";
    private static final long DEFAULT_OFFSET = 0;
    private static final long DEFAULT_LIMIT = 1000;

    private final Map<String, List<String>> results = new LinkedHashMap<>();

    private final String query;
    private final long offset;
    private final long limit;
    private long resultCount;

    public QueryResultModel(SlingHttpServletRequest request) {
        query = request.getParameter(PARAMETER_QUERY);
        offset = getNumericValue(request.getParameter(PARAMETER_OFFSET), DEFAULT_OFFSET);
        limit = getNumericValue(request.getParameter(PARAMETER_LIMIT), DEFAULT_LIMIT);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(query);
    }

    public String getQuery() {
        return query;
    }

    public Long getOffset() {
        return offset;
    }

    public Long getLimit() {
        return limit;
    }

    public long getResultCount() {
        return resultCount;
    }

    public Map<String, List<String>> getResults() {
        return results;
    }

    public void setResultCount(long resultCount) {
        this.resultCount = resultCount;
    }

    private static long getNumericValue(String value, long defaultValue) {
        return StringUtils.isNumeric(value)
                ? Long.parseLong(value)
                : defaultValue;
    }
}
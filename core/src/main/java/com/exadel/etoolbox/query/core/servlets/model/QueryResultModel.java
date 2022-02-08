package com.exadel.etoolbox.query.core.servlets.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.*;

public class QueryResultModel {
    private final static String QUERY_PARAMETER = "query";
    private final static String OFFSET_PARAMETER = "offset";
    private final static String LIMIT_PARAMETER = "limit";
    private static final long DEFAULT_OFFSET = 0;
    private static final long DEFAULT_LIMIT = 1000;

    private final Map<String, String> headers = new TreeMap<>();
    private final List<Map<String, String>> data = new ArrayList<>();
    private final String query;
    private final long offset;
    private final long limit;
    private long resultCount;

    public QueryResultModel(SlingHttpServletRequest request) {
        query = request.getParameter(QUERY_PARAMETER);
        offset = request.getParameter(OFFSET_PARAMETER) != null ? Long.parseLong(request.getParameter(OFFSET_PARAMETER)) : DEFAULT_OFFSET;
        limit = request.getParameter(LIMIT_PARAMETER) != null ? Long.parseLong(request.getParameter(LIMIT_PARAMETER)) : DEFAULT_LIMIT;
    }

    public void addData(Map<String, String> columnToValue) {
        data.add(columnToValue);
    }

    public void setHeaders(Set<String> columns) {
        columns.forEach(column -> headers.put(column, column));
    }

    public boolean isValid() {
        return query != null && !query.equals(StringUtils.EMPTY);
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public List<Map<String, String>> getData() {
        return Collections.unmodifiableList(data);
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

    public void setResultCount(long resultCount) {
        this.resultCount = resultCount;
    }
}
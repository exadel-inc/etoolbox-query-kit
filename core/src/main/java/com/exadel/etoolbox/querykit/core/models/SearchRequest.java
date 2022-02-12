package com.exadel.etoolbox.querykit.core.models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SearchRequest {

    private static final String PARAMETER_ALL = "_allColumns";
    private static final String PARAMETER_OFFSET = "_offset";
    private static final String PARAMETER_LIMIT = "_limit";
    private static final String PARAMETER_QUERY = "_query";
    private static final String PARAMETER_TRAVERSE = "_traverse";

    private static final int DEFAULT_OFFSET = 0;
    private static final int DEFAULT_LIMIT = 1000;

    private transient ResourceResolver resourceResolver;

    private String statement;

    private long offset;

    private long limit;

    private boolean traverse;

    private boolean queryAll;

    public boolean isValid() {
        return StringUtils.isNotBlank(statement) && offset >= 0 && limit >= 0;
    }

    public static SearchRequest from(SlingHttpServletRequest request) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.resourceResolver = request.getResourceResolver();
        searchRequest.statement = request.getParameter(PARAMETER_QUERY);
        searchRequest.offset = getNumericValue(request.getParameter(PARAMETER_OFFSET), DEFAULT_OFFSET);
        searchRequest.limit = getNumericValue(request.getParameter(PARAMETER_LIMIT), DEFAULT_LIMIT);
        searchRequest.queryAll = Boolean.parseBoolean(request.getParameter(PARAMETER_ALL));
        searchRequest.traverse = Boolean.parseBoolean(request.getParameter(PARAMETER_TRAVERSE));
        return searchRequest;
    }

    private static long getNumericValue(String raw, int defaultValue) {
        if (!StringUtils.isNumeric(raw)) {
            return defaultValue;
        }
        return Long.parseLong(raw);
    }
}

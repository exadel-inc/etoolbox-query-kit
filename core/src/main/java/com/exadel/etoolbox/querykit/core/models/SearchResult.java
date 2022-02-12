package com.exadel.etoolbox.querykit.core.models;

import com.adobe.xfa.ut.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.apache.sling.api.resource.Resource;

import java.util.List;

@Builder(builderClassName = "Builder")
@Getter
public class SearchResult {

    private final SearchRequest request;

    private final long executionTime;

    private final long total;

    private final List<String> columns;

    @Singular
    private final List<Resource> items;

    private final String errorMessage;

    public boolean isSuccess() {
        return StringUtils.isEmpty(errorMessage);
    }

    public static SearchResult error(SearchRequest request, String message) {
        return SearchResult
                .builder()
                .request(request)
                .errorMessage(message)
                .build();
    }
}

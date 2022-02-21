package com.exadel.etoolbox.querykit.core.services.query;

import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;

public interface QueryParserService {

    String parse(SearchRequest request) throws Exception;
}

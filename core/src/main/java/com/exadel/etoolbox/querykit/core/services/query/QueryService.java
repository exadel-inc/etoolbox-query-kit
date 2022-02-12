package com.exadel.etoolbox.querykit.core.services.query;

import com.exadel.etoolbox.querykit.core.models.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.SearchResult;

public interface QueryService {

    SearchResult execute(SearchRequest request);
}

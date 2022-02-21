package com.exadel.etoolbox.querykit.core.services.query;

import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;

public interface QueryService {

    SearchResult execute(SearchRequest request);

    SearchResult dryRun(SearchRequest request);
}

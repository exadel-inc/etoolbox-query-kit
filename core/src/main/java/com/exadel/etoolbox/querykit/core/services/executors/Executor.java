package com.exadel.etoolbox.querykit.core.services.executors;

import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;

public interface Executor {

    String getType();

    SearchResult execute(SearchRequest request) throws Exception;
}

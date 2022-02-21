package com.exadel.etoolbox.querykit.core.services.executors;

import com.exadel.etoolbox.querykit.core.models.query.ParsedQueryInfo;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;

public interface Executor {

    ExecutorType getType();

    ParsedQueryInfo parse(SearchRequest request) throws Exception;

    SearchResult execute(SearchRequest request) throws Exception;

    SearchResult dryRun(SearchRequest request) throws Exception;
}

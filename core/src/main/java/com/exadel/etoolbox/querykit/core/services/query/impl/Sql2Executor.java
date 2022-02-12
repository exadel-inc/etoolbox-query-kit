package com.exadel.etoolbox.querykit.core.services.query.impl;

import com.exadel.etoolbox.querykit.core.models.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.SearchResult;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.RequiredArgsConstructor;

import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.List;

@RequiredArgsConstructor
class Sql2Executor extends QueryExecutor {

    private final SearchRequest request;
    private final Workspace workspace;

    @Override
    public SearchResult execute() throws Exception {
        QueryManager queryManager = workspace.getQueryManager();
        Query query = queryManager.createQuery(request.getStatement(), Query.JCR_SQL2);

        query.setOffset(request.getOffset());
        if (request.getLimit() > 0) {
            query.setLimit(request.getLimit());
        }

        long total = 0;
        long startingTime = System.currentTimeMillis();
        QueryResult queryResult = query.execute();
        long executionTime = System.currentTimeMillis() - startingTime;

        ColumnCollection columnCollection = ColumnCollection.from(request.getStatement(), request.getResourceResolver());
        if (request.isQueryAll()) {
            columnCollection.injectNamesForWildcards(queryResult.getColumnNames());
        }
        SearchResult.Builder resultBuilder = SearchResult
                .builder()
                .request(request)
                .executionTime(executionTime)
                .total(total);

        List<String> columnNames = columnCollection.getNames();
        columnNames.add(0, Constants.PROPERTY_PATH);
        resultBuilder.columns(columnNames);

        populateQueryResults(queryResult, columnCollection, resultBuilder, request.getResourceResolver());
        return resultBuilder.build();
    }
}

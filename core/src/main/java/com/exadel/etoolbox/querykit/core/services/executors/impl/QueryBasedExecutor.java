package com.exadel.etoolbox.querykit.core.services.executors.impl;

import com.exadel.etoolbox.querykit.core.models.queryengine.MeasuredQueryResult;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ModifiableColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import com.exadel.etoolbox.querykit.core.utils.ValueUtil;
import org.apache.commons.lang3.ArrayUtils;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.Map;

abstract class QueryBasedExecutor extends ExecutorImpl {

    @Override
    public SearchResult execute(SearchRequest request) throws Exception {
        Query query = prepareQuery(request);
        ModifiableColumnCollection columnCollection = (ModifiableColumnCollection) getColumnCollection(request, query);

        long startingTime = System.currentTimeMillis();
        QueryResult queryResult = request.isIterating()
                ? query.execute()
                : executeMeasured(request, query);
        long executionTime = System.currentTimeMillis() - startingTime;

        if (request.isShowAllProperties()) {
            columnCollection.injectNamesForWildcards(queryResult.getColumnNames());
        }
        SearchResult.Builder resultBuilder = SearchResult
                .builder()
                .request(request)
                .executionTime(executionTime)
                .info("Via " + getClass().getSimpleName())
                .columns(columnCollection);

        if (request.isIterating()) {
            populateAndMeasure(resultBuilder, queryResult, request, columnCollection);
        } else {
            populate(resultBuilder, queryResult, request, columnCollection);
            resultBuilder.total(((MeasuredQueryResult) queryResult).getTotal());
        }

        return resultBuilder.build();
    }

    private QueryResult executeMeasured(SearchRequest request, Query query) throws Exception {
        if (!request.isShowTotal()) {
            return new MeasuredQueryResult(query.execute());
        }
        return MeasuredExecutorHelper.execute(request, query);
    }

    abstract Query getBasicQuery(SearchRequest request) throws RepositoryException, ConverterException;

    private Query prepareQuery(SearchRequest request) throws RepositoryException, ConverterException {
        ValueFactory valueFactory = request.getValueFactory();
        Query query = getBasicQuery(request);
        if (!request.isIterating()) {
            query.setOffset(request.getOffset());
            if (request.getLimit() > 0 && request.getLimit() != SearchRequest.DEFAULT_LIMIT) {
                query.setLimit(request.getLimit());
            }
        }
        for (Map.Entry<String, Object> entry : request.getUserParameters().entrySet()) {
            if (ArrayUtils.contains(query.getBindVariableNames(), entry.getKey())) {
                query.bindValue(entry.getKey(), ValueUtil.createValue(entry.getValue(), valueFactory));
            }
        }
        return query;
    }
}

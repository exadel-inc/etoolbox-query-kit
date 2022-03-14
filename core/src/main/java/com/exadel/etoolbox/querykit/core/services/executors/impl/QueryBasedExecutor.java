/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.etoolbox.querykit.core.services.executors.impl;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ModifiableColumnCollection;
import com.exadel.etoolbox.querykit.core.models.query.MeasuredQueryResult;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.utils.ValueUtil;
import org.apache.commons.lang3.ArrayUtils;

import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.Map;

abstract class QueryBasedExecutor extends ExecutorImpl {

    @Override
    public SearchResult execute(SearchRequest request) throws Exception {
        Query query = compileAndSetUp(request);
        ModifiableColumnCollection columnCollection = (ModifiableColumnCollection) getColumnCollection(request, query);

        long startingTime = System.currentTimeMillis();
        QueryResult queryResult = request.shouldIterate()
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
                .metadata("Via " + getClass().getSimpleName())
                .columns(columnCollection);

        if (request.shouldIterate()) {
            populateAndMeasure(resultBuilder, queryResult, request, columnCollection);
        } else {
            populate(resultBuilder, queryResult, request, columnCollection);
            resultBuilder.total(((MeasuredQueryResult) queryResult).getTotal());
        }

        return resultBuilder.build();
    }

    @Override
    public SearchResult dryRun(SearchRequest request) throws Exception {
        Query query = compileAndSetUp(request);
        ModifiableColumnCollection columnCollection = (ModifiableColumnCollection) getColumnCollection(request, query);
        return SearchResult
                .builder()
                .request(request)
                .metadata("Via " + getClass().getSimpleName())
                .columns(columnCollection)
                .build();
    }

    abstract Query compile(SearchRequest request) throws Exception;

    private Query compileAndSetUp(SearchRequest request) throws Exception {
        ValueFactory valueFactory = request.getValueFactory();
        Query query = compile(request);
        if (!request.shouldIterate()) {
            query.setOffset(request.getOffset());
            if (request.getLimit() > 0 && request.getLimit() != SearchRequest.DEFAULT_LIMIT) {
                query.setLimit(request.getLimit());
            }
        }
        for (Map.Entry<String, Object> entry : request.getUserParameters().entrySet()) {
            if (ArrayUtils.contains(query.getBindVariableNames(), entry.getKey())) {
                query.bindValue(entry.getKey(), ValueUtil.createFlatValue(entry.getValue(), valueFactory));
            }
        }
        return query;
    }

    private QueryResult executeMeasured(SearchRequest request, Query query) throws Exception {
        if (!request.shouldCalculateTotal()) {
            return new MeasuredQueryResult(query.execute(), Math.max(request.getPredefinedTotal(), 0));
        }
        return MeasuredExecutorHelper.execute(request, query);
    }
}

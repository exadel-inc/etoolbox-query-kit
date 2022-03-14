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

import com.exadel.etoolbox.querykit.core.models.query.MeasuredQueryResult;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.utils.ValueUtil;
import lombok.Builder;
import lombok.Getter;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Helper class that facilitates running parallel queries for retrieving results and measuring the total number of
 * results
 */
@Builder
class MeasuredExecutorHelper {

    private final SearchRequest request;
    private final Query query;

    /**
     * Implements parallel queries for retrieving results and measuring the total number of results
     * @param request {@link SearchRequest} instance
     * @param query   {@link Query} instance
     * @return {@link MeasuredQueryResult} object, non-null
     * @throws Exception In case query running failed
     */
    public static MeasuredQueryResult execute(SearchRequest request, Query query) throws Exception {
        CompletableFuture<ValueOrException<QueryResult>> resultSupplier =
                CompletableFuture.supplyAsync(() -> executeMainQuery(query));
        CompletableFuture<ValueOrException<Long>> totalSupplier =
                CompletableFuture.supplyAsync(() -> executeMeasurementQuery(request, query));

        ValueOrException<QueryResult> queryResult = resultSupplier.join();
        ValueOrException<Long> total = totalSupplier.join();

        if (queryResult.isException()) {
            throw queryResult.getException();
        } else if (total.isException()) {
            throw total.getException();
        }

        return new MeasuredQueryResult(queryResult.getValue(), total.getValue());
    }

    private static ValueOrException<QueryResult> executeMainQuery(Query query) {
        try {
            return new ValueOrException<>(query.execute());
        } catch (RepositoryException | UnsupportedOperationException e) {
            return new ValueOrException<>(e);
        }
    }

    private static ValueOrException<Long> executeMeasurementQuery(SearchRequest request, Query query) {
        try {
            Query measurementQuery = getMeasurementQuery(request, query);
            QueryResult queryResult = measurementQuery.execute();
            RowIterator rowIterator = queryResult.getRows();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.nextRow();
                LinkedList<String> values = Arrays.stream(row.getValues())
                        .map(Object::toString)
                        .collect(Collectors.toCollection(LinkedList::new));
                if ("query".equals(values.getFirst())) {
                    return new ValueOrException<>(Long.parseLong(values.getLast()));
                }
            }
            return new ValueOrException<>(0L);
        } catch (RepositoryException | NumberFormatException | UnsupportedOperationException e) {
            return new ValueOrException<>(e);
        }
    }

    private static Query getMeasurementQuery(SearchRequest request, Query query) throws RepositoryException {
        Query measurementQuery = request
                .getQueryManager()
                .createQuery("MEASURE " + query.getStatement(), Query.JCR_SQL2);
        for (String bindVariable : query.getBindVariableNames()) {
            Value newBindValue = ValueUtil.createFlatValue(
                    request.getUserParameters().get(bindVariable),
                    request.getValueFactory());
            measurementQuery.bindValue(bindVariable, newBindValue);
        }
        return measurementQuery;
    }

    @Getter
    private static class ValueOrException<T> {
        private T value;
        private Exception exception;

        public ValueOrException(T value) {
            this.value = value;
        }

        public ValueOrException(Exception exception) {
            this.exception = exception;
        }

        public boolean isException() {
            return exception != null;
        }
    }
}

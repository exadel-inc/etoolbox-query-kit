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
package com.exadel.etoolbox.querykit.core.services.query.impl;

import com.exadel.etoolbox.querykit.core.models.search.QueryType;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.syntax.WordModel;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.services.executors.Executor;
import com.exadel.etoolbox.querykit.core.services.executors.ExecutorType;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;

import java.util.List;

abstract class QueryServiceBase {
    private static final String[] SQL_SPECIFIC_KEYWORDS = new String[] {"union", "coalesce", "similar", "native", "spellcheck", "suggest"};

    abstract List<QueryConverter> getConverters();

    abstract List<Executor> getExecutors();

    SearchRequest convertToSql2IfNeeded(SearchRequest request) throws ConverterException {
        QueryType queryType = request.getType();
        if (queryType == QueryType.JCR_SQL2) {
            return request;
        }
        QueryConverter queryConverter = getConverters()
                .stream()
                .filter(converter -> converter.getSourceType() == queryType)
                .findFirst()
                .orElse(null);

        if (queryConverter == null) {
            throw new ConverterException("Converter not found for type " + queryType);
        }
        String effectiveStatement = queryConverter.convert(request.getStatement(), request.getResourceResolver(), String.class);
        return request.withStatement(effectiveStatement);
    }

    Executor pickExecutor(SearchRequest request) {
        ExecutorType executorType = getExecutorType(request);
        return getExecutors()
                .stream()
                .filter(exec -> executorType.equals(exec.getType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find an appropriate query executor"));
    }

    private static ExecutorType getExecutorType(SearchRequest request) {
        WordModel statementModel = new WordModel(request.getStatement());
        boolean isSql2QueryNeeded = statementModel.hasToken(SQL_SPECIFIC_KEYWORDS);
        ExecutorType executorType = isSql2QueryNeeded ? ExecutorType.SQL : ExecutorType.QOM;
        if (request.isTraverse()) {
            executorType = ExecutorType.TRAVERSAL;
        }
        return executorType;
    }

}

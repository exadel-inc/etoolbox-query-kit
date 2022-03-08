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

import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.services.executors.Executor;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.services.query.QueryService;
import com.exadel.etoolbox.querykit.core.utils.TryBiFunction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.List;

@Component(service = QueryService.class)
@Slf4j
public class QueryServiceImpl extends QueryServiceBase implements QueryService {

    @Reference
    @Getter(AccessLevel.PACKAGE)
    private List<QueryConverter> converters;

    @Reference(cardinality = ReferenceCardinality.MULTIPLE)
    @Getter(AccessLevel.PACKAGE)
    private List<Executor> executors;

    @Override
    public SearchResult execute(SearchRequest request) {
        return execute(request, ((req, exec) -> exec.execute(req)));
    }

    @Override
    public SearchResult dryRun(SearchRequest request) {
        return execute(request, ((req, exec) -> exec.dryRun(req)));
    }

    private SearchResult execute(SearchRequest request, TryBiFunction<SearchRequest, Executor, SearchResult> supplier) {
        if (!request.isValid()) {
            return SearchResult.error(request, "Invalid request");
        }
        try {
            SearchRequest effectiveRequest = convertToSql2IfNeeded(request);
            Executor queryExecutor = pickExecutor(effectiveRequest);
            return supplier.apply(effectiveRequest, queryExecutor);
        } catch (Exception e) {
            log.error("Could not execute query {}", request.getStatement(), e);
            return SearchResult.error(request, "Could not execute: " + e.getMessage());
        }
    }
}

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

import com.exadel.etoolbox.querykit.core.models.query.ParsedQueryInfo;
import com.exadel.etoolbox.querykit.core.models.search.QueryParsingFormat;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.services.executors.Executor;
import com.exadel.etoolbox.querykit.core.services.query.QueryParserService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.List;

/**
 * Basic {@link QueryParserService} implementation
 */
@Component(service = QueryParserService.class)
public class QueryParserServiceImpl extends QueryServiceBase implements QueryParserService {

    /**
     * {@inheritDoc}
     */
    @Reference
    @Getter(AccessLevel.PACKAGE)
    private List<QueryConverter> converters;

    /**
     * {@inheritDoc}
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE)
    @Getter(AccessLevel.PACKAGE)
    private List<Executor> executors;

    /**
     * {@inheritDoc}
     */
    @Override
    public String parse(SearchRequest request) throws Exception {
        if (!request.isValid()) {
            return StringUtils.EMPTY;
        }
        SearchRequest effectiveRequest = convertToSql2IfNeeded(request);
        Executor queryExecutor = pickExecutor(effectiveRequest);
        ParsedQueryInfo parsedQuery = queryExecutor.parse(effectiveRequest);
        return request.getRenderingFormat() == QueryRenderingFormat.SQL
                ? parsedQuery.toSqlString()
                : parsedQuery.toJson();
    }
}

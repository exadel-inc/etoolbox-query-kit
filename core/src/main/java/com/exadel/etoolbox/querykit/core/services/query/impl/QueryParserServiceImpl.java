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

@Component(service = QueryParserService.class)
@Slf4j
public class QueryParserServiceImpl extends QueryServiceBase implements QueryParserService {

    @Reference
    @Getter(AccessLevel.PACKAGE)
    private List<QueryConverter> converters;

    @Reference(cardinality = ReferenceCardinality.MULTIPLE)
    @Getter(AccessLevel.PACKAGE)
    private List<Executor> executors;

    @Override
    public String parse(SearchRequest request) throws Exception {
        if (!request.isValid()) {
            return StringUtils.EMPTY;
        }
        SearchRequest effectiveRequest = convertToSql2IfNeeded(request);
        Executor queryExecutor = pickExecutor(effectiveRequest);
        ParsedQueryInfo parsedQuery = queryExecutor.parse(effectiveRequest);
        return request.getParsingFormat() == QueryParsingFormat.SQL
                ? parsedQuery.toSqlString()
                : parsedQuery.toJson();
    }
}

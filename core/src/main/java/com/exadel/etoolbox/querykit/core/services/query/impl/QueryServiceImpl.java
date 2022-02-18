package com.exadel.etoolbox.querykit.core.services.query.impl;

import com.exadel.etoolbox.querykit.core.models.search.QueryType;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.models.syntax.WordModel;
import com.exadel.etoolbox.querykit.core.services.executors.Executor;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.services.query.QueryService;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.List;

@Component(service = QueryService.class)
@Slf4j
public class QueryServiceImpl implements QueryService {

    private static final String[] SQL_SPECIFIC_KEYWORDS = new String[] {"union", "similar", "native", "spellcheck", "suggest"};

    @Reference
    private List<QueryConverter> queryConverters;

    @Reference(cardinality = ReferenceCardinality.MULTIPLE)
    private List<Executor> queryExecutors;

    @Override
    public SearchResult execute(SearchRequest request) {
        try {
            SearchRequest effectiveRequest = convertToSql2IfNeeded(request);
            Executor queryExecutor = pickQueryExecutor(effectiveRequest);
            return queryExecutor.execute(request);
        } catch (Exception e) {
            log.error("Could not execute query {}", request.getStatement(), e);
            return SearchResult.error(request, "Could not execute: " + e.getMessage());
        }
    }

    private SearchRequest convertToSql2IfNeeded(SearchRequest original) throws ConverterException {
        QueryType queryType = original.getType();
        if (queryType == QueryType.JCR_SQL2) {
            return original;
        }
        QueryConverter queryConverter = queryConverters
                .stream()
                .filter(converter -> converter.getSourceType() == queryType)
                .findFirst()
                .orElse(null);

        if (queryConverter == null) {
            throw new ConverterException("Converter not found for type " + queryType);
        }
        String effectiveStatement = queryConverter.convert(original.getStatement(), original.getResourceResolver(), String.class);
        return original.withStatement(effectiveStatement);
    }

    private Executor pickQueryExecutor(SearchRequest request) {
        WordModel statementModel = new WordModel(request.getStatement());
        boolean isSql2QueryNeeded = statementModel.hasToken(SQL_SPECIFIC_KEYWORDS);
        String targetType = isSql2QueryNeeded ? "SQL" : "QOM";
        if (request.isTraverse()) {
            targetType = "TRAVERSAL";
        }
        String finalTargetType = targetType;
        return queryExecutors
                .stream()
                .filter(exec -> finalTargetType.equals(exec.getType()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find an appropriate query executor"));
    }
}

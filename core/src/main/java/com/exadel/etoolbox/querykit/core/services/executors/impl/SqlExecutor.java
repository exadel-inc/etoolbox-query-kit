package com.exadel.etoolbox.querykit.core.services.executors.impl;

import com.exadel.etoolbox.querykit.core.models.qom.QomAdapter;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ModifiableColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.syntax.WordModel;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemConverterFactory;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemFilterFactory;
import com.exadel.etoolbox.querykit.core.services.executors.Executor;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import lombok.AccessLevel;
import lombok.Getter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.qom.QueryObjectModel;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component(service = Executor.class)
public class SqlExecutor extends QueryBasedExecutor  {

    private static final String KEYWORD_SELECT = "select";
    private static final String KEYWORD_FROM = "from";

    @Reference(target = "(converter.output=Qom)", policyOption = ReferencePolicyOption.GREEDY)
    private QueryConverter queryConverterService;

    @Getter(AccessLevel.PACKAGE)
    @Reference
    private List<SearchItemFilterFactory> itemFilters;

    @Getter(AccessLevel.PACKAGE)
    @Reference
    private List<SearchItemConverterFactory> itemConverters;

    @Override
    public String getType() {
        return "SQL";
    }

    @Override
    Query getBasicQuery(SearchRequest request) throws RepositoryException {
        return request.getQueryManager().createQuery(request.getStatement(), Query.JCR_SQL2);
    }

    @Override
    ColumnCollection getColumnCollection(SearchRequest request, Query source) throws ConverterException {
        QueryObjectModel qom = queryConverterService.convert(
                prepareSelectExtract(source.getStatement()),
                request.getResourceResolver(),
                QomAdapter.class)
                .getModel();
        return new ModifiableColumnCollection(qom);
    }

    private static String prepareSelectExtract(String statement) {
        WordModel wordModel = new WordModel(statement);
        Set<String> selectors = new LinkedHashSet<>();
        WordModel selectorString = wordModel.extractBetween(KEYWORD_SELECT, KEYWORD_FROM);
        while (selectorString != null) {
            selectors.addAll(selectorString.split(",")
                    .stream()
                    .map(WordModel::toString)
                    .map(String::trim)
                    .collect(Collectors.toList()));
            selectorString = wordModel.extractBetween(
                    KEYWORD_SELECT,
                    KEYWORD_FROM,
                    selectorString.getEndPosition());
        }
        return String.format("SELECT %s FROM [nt:base]", String.join(",", selectors));
    }
}

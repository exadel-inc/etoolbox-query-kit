package com.exadel.etoolbox.querykit.core.services.executors.impl;

import com.exadel.etoolbox.querykit.core.models.qom.QomAdapter;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ModifiableColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
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

import javax.jcr.query.Query;
import javax.jcr.query.qom.QueryObjectModel;
import java.util.List;

@Component(service = Executor.class)
public class QomExecutor extends QueryBasedExecutor {

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
        return "QOM";
    }

    @Override
    Query getBasicQuery(SearchRequest request) throws ConverterException {
        return queryConverterService.convert(
                request.getStatement(),
                request.getResourceResolver(),
                QomAdapter.class)
                .getModel();
    }

    @Override
    ColumnCollection getColumnCollection(SearchRequest request, Query source) {
        return new ModifiableColumnCollection((QueryObjectModel) source);
    }
}

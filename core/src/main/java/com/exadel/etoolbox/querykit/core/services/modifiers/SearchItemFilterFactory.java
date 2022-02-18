package com.exadel.etoolbox.querykit.core.services.modifiers;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;

import javax.jcr.query.Row;
import java.util.function.Predicate;

public interface SearchItemFilterFactory {

    String getName();

    Predicate<Row> getFilter(SearchRequest request, ColumnCollection columns);
}

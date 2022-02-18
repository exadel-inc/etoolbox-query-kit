package com.exadel.etoolbox.querykit.core.services.filters;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemFilterFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.Row;
import java.util.function.Predicate;

public class SkipByRankingFilterFactory implements SearchItemFilterFactory {

    @Override
    public String getName() {
        return "skip-by-ranking";
    }

    @Override
    public Predicate<Row> getFilter(SearchRequest request, ColumnCollection columns) {
        return row -> {
            try {
                long ranking = row.getNode("content").getProperty("ranking").getLong();
                return ranking != 42;
            } catch (RepositoryException e) {
                return true;
            }
        };
    }
}

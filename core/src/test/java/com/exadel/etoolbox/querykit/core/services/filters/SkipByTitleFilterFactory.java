package com.exadel.etoolbox.querykit.core.services.filters;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemFilterFactory;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.Row;
import java.util.function.Predicate;

public class SkipByTitleFilterFactory implements SearchItemFilterFactory {

    @Override
    public String getName() {
        return "skip-by-title";
    }

    @Override
    public Predicate<Row> getFilter(SearchRequest request, ColumnCollection columns) {
        return row -> {
            String title;
            try {
                title = row.getNode("content").getProperty("jcr:title").getString();
                String ordinal = title.startsWith("Page ") ? title.substring(5) : StringUtils.EMPTY;
                int number = StringUtils.isNumeric(ordinal) ? Integer.parseInt(ordinal) : -1;
                return number >= 0 && number % 2 == 0;
            } catch (RepositoryException e) {
                return false;
            }
        };
    }
}

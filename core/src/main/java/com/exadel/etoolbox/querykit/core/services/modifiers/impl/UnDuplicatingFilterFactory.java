package com.exadel.etoolbox.querykit.core.services.modifiers.impl;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import javax.jcr.query.Row;
import java.util.List;
import java.util.function.Predicate;

@Component(service = SearchItemFilterFactory.class)
@Slf4j
public class UnDuplicatingFilterFactory implements SearchItemFilterFactory {

    private static final String SLASH_JCR_CONTENT = "/jcr:content";

    @Override
    public String getName() {
        return "no-duplicate-pages";
    }

    @Override
    public Predicate<Row> getFilter(SearchRequest request, ColumnCollection columns) {
        return new Filter(columns);
    }

    private static class Filter implements Predicate<Row> {
        private final String pathSelector;
        private String lastProcessedPath;

        public Filter(ColumnCollection columnCollection) {
            List<String> selectors = columnCollection.getSelectors();
            pathSelector = !CollectionUtils.isEmpty(selectors) ? selectors.get(0) : null;
        }

        @Override
        public boolean test(Row row) {
            try {
                String resourcePath = pathSelector == null
                        ? row.getPath()
                        : row.getPath(pathSelector);
                String pagePath = StringUtils.contains(resourcePath, SLASH_JCR_CONTENT)
                        ? StringUtils.substringBefore(resourcePath, SLASH_JCR_CONTENT)
                        : resourcePath;
                boolean result = StringUtils.equals(pagePath, lastProcessedPath);
                lastProcessedPath = pagePath;
                return result;
            } catch (RepositoryException e) {
                log.error("Could not extract path for the row", e);
            }
            return false;
        }
    }
}

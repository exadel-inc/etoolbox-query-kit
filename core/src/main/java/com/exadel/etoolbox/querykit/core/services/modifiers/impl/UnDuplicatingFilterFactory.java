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

/**
 * Implements {@link SearchItemFilterFactory} to make sure query result entries are not duplicated by path
 */
@Component(service = SearchItemFilterFactory.class)
@Slf4j
public class UnDuplicatingFilterFactory implements SearchItemFilterFactory {

    private static final String SLASH_JCR_CONTENT = "/jcr:content";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "no-duplicate-pages";
    }

    /**
     * {@inheritDoc}
     */
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

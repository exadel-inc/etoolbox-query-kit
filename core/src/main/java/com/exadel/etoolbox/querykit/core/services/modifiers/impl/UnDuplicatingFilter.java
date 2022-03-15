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

import com.exadel.etoolbox.querykit.core.models.search.SearchItem;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemFilter;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Implements {@link SearchItemFilter} to make sure query result entries are not duplicated by path
 */
@Component(service = SearchItemFilter.class)
public class UnDuplicatingFilter implements SearchItemFilter {

    private static final ThreadLocal<Filter> FILTER = ThreadLocal.withInitial(Filter::new);
    private static final String SLASH_CONTENT = "/jcr:content";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "no-duplicates";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getTargetClass() {
        return SearchItem.class;
    }

    @Override
    public boolean test(SearchRequest request, Object item) {
        if (!(item instanceof SearchItem)) {
            return false;
        }
        return FILTER.get().test((SearchItem) item);
    }

    @Override
    public void reset() {
        FILTER.set(new Filter());
    }

    private static class Filter implements Predicate<SearchItem> {
        private final Set<String> processedPaths = new HashSet<>();

        @Override
        public boolean test(SearchItem item) {
            String pagePath = StringUtils.substringBefore(item.getPath(), SLASH_CONTENT);
            boolean result = !processedPaths.contains(pagePath);
            processedPaths.add(pagePath);
            return result;
        }
    }
}

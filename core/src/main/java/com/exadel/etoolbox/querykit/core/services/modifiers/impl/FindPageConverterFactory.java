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

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.exadel.etoolbox.querykit.core.models.search.SearchItem;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemConverterFactory;
import lombok.RequiredArgsConstructor;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.util.function.UnaryOperator;

/**
 * Implements {@link SearchItemConverterFactory} to convert the search results matching the retrieved pages' {@code
 * jcr:content} sub-nodes into items matching pages themselves
 */
@Component
public class FindPageConverterFactory implements SearchItemConverterFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "find-page";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnaryOperator<SearchItem> getModifier(SearchRequest request) {
        return new Modifier(request, request.getResourceResolver().adaptTo(PageManager.class));
    }

    @RequiredArgsConstructor
    private static class Modifier implements UnaryOperator<SearchItem> {
        private final SearchRequest request;
        private final PageManager pageManager;

        @Override
        public SearchItem apply(SearchItem searchItem) {
            Page page = pageManager != null ? pageManager.getContainingPage(searchItem.getPath()) : null;
            if (page == null) {
                return searchItem;
            }
            Resource contentResource = page.getContentResource();
            SearchItem newItem = SearchItem.newInstance(request, page.getPath());
            contentResource.getValueMap().forEach((name, value) -> {
                newItem.putProperty(name, value, contentResource.getPath());
            });
            return newItem;
        }
    }
}

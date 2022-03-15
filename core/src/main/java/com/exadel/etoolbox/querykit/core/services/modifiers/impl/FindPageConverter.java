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

import com.day.cq.commons.jcr.JcrConstants;
import com.exadel.etoolbox.querykit.core.models.search.SearchItem;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemConverter;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.util.function.UnaryOperator;

/**
 * Implements {@link SearchItemConverter} to convert the search results matching the retrieved pages' {@code
 * jcr:content} sub-nodes into items matching pages themselves
 */
@Component
public class FindPageConverter implements SearchItemConverter {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "find-page";
    }

    @Override
    public SearchItem apply(SearchRequest request, SearchItem searchItem) {
        Resource pageResource = getPageResource(request.getResourceResolver().getResource(searchItem.getPath()));
        Resource pageContentResource = pageResource != null ? pageResource.getChild(JcrConstants.JCR_CONTENT) : null;
        if (pageContentResource == null) {
            return searchItem;
        }
        SearchItem newItem = SearchItem.newInstance(request, pageResource.getPath());
        pageContentResource
                .getValueMap()
                .forEach((name, value) -> newItem.putProperty(name, value, pageContentResource.getPath()));
        return newItem;
    }

    private static Resource getPageResource(Resource original) {
        if (original == null) {
            return null;
        }
        for (Resource current = original;  current != null; current = current.getParent()) {
            if (current.isResourceType("cq:Page")) {
                return current;
            }
        }
        return null;
    }
}

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
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemConverter;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import java.util.function.UnaryOperator;

/**
 * Implements {@link SearchItemConverter} to transform the search results into items that conform to Granite
 * Select's datasource entries
 */
@Component
public class ListItemConverter implements SearchItemConverter {

    public static final String NAME = "list-item";

    private static final String PREFIX_APPS = "/apps/";
    private static final String FALLBACK_TITLE_FORMAT = "[%s]";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public SearchItem apply(SearchRequest request, SearchItem searchItem) {
        String value = StringUtils.removeStart(searchItem.getPath(), PREFIX_APPS);
        String text = StringUtils.defaultString(
                searchItem.getProperty(Constants.PROPERTY_JCR_TITLE, String.class),
                String.format(FALLBACK_TITLE_FORMAT, value));
        searchItem.clearProperties();
        searchItem.putProperty(Constants.PROPERTY_TEXT, text);
        searchItem.putProperty(Constants.PROPERTY_VALUE, value);
        return searchItem;
    }
}

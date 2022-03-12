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
package com.exadel.etoolbox.querykit.core.services.modifiers;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchItem;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.function.UnaryOperator;

/**
 * Provides search item converters
 */
public interface SearchItemConverterFactory {

    /**
     * Retrieves the name associated with the current implementation
     * @return String value, non-blank
     */
    String getName();

    /**
     * Retrieves a modifier instance
     * @param request {@link SearchRequest} object
     * @return A {@code UnaryOperator} instance used to process {@link SearchItem} objects
     */
    UnaryOperator<SearchItem> getModifier(SearchRequest request);
}

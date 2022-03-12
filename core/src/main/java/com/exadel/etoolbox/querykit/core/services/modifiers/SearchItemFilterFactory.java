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
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;

import javax.jcr.query.Row;
import java.util.function.Predicate;

/**
 * Provides search item filters
 */
public interface SearchItemFilterFactory {

    /**
     * Retrieves the name associated with the current implementation
     * @return String value, non-blank
     */
    String getName();

    /**
     * Retrieves a filter instance
     * @param request {@link SearchRequest} object
     * @param columns {@link ColumnCollection} object
     * @return A {@code Predicate} instance used to filter query result entries
     */
    Predicate<Row> getFilter(SearchRequest request, ColumnCollection columns);
}

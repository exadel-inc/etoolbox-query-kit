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
package com.exadel.etoolbox.querykit.core.services.query;

import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;

/**
 * Provides generic query execution facilities
 */
public interface QueryService {

    /**
     * Executes the query as defined by the provided {@link SearchRequest}
     * @param request {@link SearchRequest} instance
     * @return Non-null {@link SearchResult} object
     */
    SearchResult execute(SearchRequest request);

    /**
     * Imitates execution of the query as defined by the provided {@link SearchRequest} (can be useful for clearing out
     * query model, retrieving available columns, etc.)
     * @param request {@link SearchRequest} instance
     * @return Non-null {@link SearchResult} object
     */
    SearchResult dryRun(SearchRequest request);
}

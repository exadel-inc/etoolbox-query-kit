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

/**
 * Provides generic query parsing facilities
 */
public interface QueryParserService {

    /**
     * Parses the query as defined by the provided {@link SearchRequest}
     * @param request {@link SearchRequest} instance
     * @return String value; might be an empty string
     */
    String parse(SearchRequest request) throws Exception;
}

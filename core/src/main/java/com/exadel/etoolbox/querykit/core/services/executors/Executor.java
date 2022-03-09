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
package com.exadel.etoolbox.querykit.core.services.executors;

import com.exadel.etoolbox.querykit.core.models.query.ParsedQueryInfo;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;

/**
 * Parses and/or executes the query statement as defined by the provided {@link SearchRequest}
 */
public interface Executor {

    /**
     * Retrieves the type of the current executor
     * @return One of {@link ExecutorType} values
     */
    ExecutorType getType();

    /**
     * Parses the query statement as defined by the provided {@link SearchRequest}
     * @param request {@code SearchRequest} object that contains the data for parsing
     * @return {@link ParsedQueryInfo} instance
     * @throws Exception In case of a failed parsing
     */
    ParsedQueryInfo parse(SearchRequest request) throws Exception;

    /**
     * Parses and executes the query statement as defined by the provided {@link SearchRequest}
     * @param request {@code SearchRequest} object that contains the data for parsing
     * @return {@link SearchResult} instance
     * @throws Exception In case of a failed parsing or execution
     */
    SearchResult execute(SearchRequest request) throws Exception;

    /**
     * Parses the query statement as defined by the provided {@link SearchRequest} and imitates execution without actual
     * results retrieving (can be useful for clearing out query model, retrieving available columns, etc.)
     * @param request {@code SearchRequest} object that contains the data for parsing
     * @return {@link SearchResult} instance
     * @throws Exception In case of a failed parsing or execution
     */
    SearchResult dryRun(SearchRequest request) throws Exception;
}

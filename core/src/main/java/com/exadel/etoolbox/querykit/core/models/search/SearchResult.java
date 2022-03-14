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
package com.exadel.etoolbox.querykit.core.models.search;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportable;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the comprehensive data that characterizes the result of running a query
 */
@Getter
public class SearchResult implements JsonExportable {

    /**
     * Gets the {@link SearchRequest} associated with the current query
     */
    private SearchRequest request;

    /**
     * Gets the time (in milliseconds) during which the query was executed
     */
    private long executionTime;

    /**
     * Gets the total number of query results
     */
    private long total;

    /**
     * Gets the columns that define the particular properties of every query result. They are adapted with a {@link
     * ColumnCollection} object
     */
    private ColumnCollection columns;

    /**
     * Gets the list of {@link SearchItem} that represent the query results entries
     */
    private List<SearchItem> items;

    /**
     * If the query execution resulted in an error, this member contains the error message. Otherwise returns {@code
     * null}
     */
    private String errorMessage;

    /**
     * Gets arbitrary data associated with the query result (generally used for debugging purposes)
     */
    private String metadata;

    /**
     * Gets whether the query execution was successful
     * @return True or false
     */
    public boolean isSuccess() {
        return StringUtils.isEmpty(errorMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonElement toJson(JsonSerializationContext serializer) {
        JsonObject result = new JsonObject();
        if (StringUtils.isNotEmpty(errorMessage)) {
            result.addProperty("error", errorMessage);
            return result;
        }
        result.add("request", serializer.serialize(request));
        result.add("executionTime", serializer.serialize(executionTime));
        result.add("total", serializer.serialize(total));
        result.add("columns", serializer.serialize(columns));
        if (CollectionUtils.isEmpty(items)) {
            return result;
        }
        JsonArray itemsArray = new JsonArray();
        for (SearchItem item : items) {
            itemsArray.add(item.toJson(serializer, columns));
        }
        result.add("items", itemsArray);
        return result;
    }

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Creates a new {@link SearchResult} instance for an unsuccessful query
     * @param request {@code SearchRequest} associated with the current result
     * @param message Error message
     * @return {@code SearchResult} object
     */
    public static SearchResult error(SearchRequest request, String message) {
        return builder()
                .request(request)
                .errorMessage(message)
                .build();
    }

    /**
     * Retrieves a {@code Builder} instance used to build a new {@link SearchResult} object
     * @return {@link Builder} object
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private SearchRequest request;
        private long total;
        private ColumnCollection columns;
        private final List<SearchItem> items = new ArrayList<>();
        private String errorMessage;
        private String metadata;
        private long executionStart;
        private long executionEnd;

        /**
         * Stores a {@link SearchRequest} object into this builder
         * @param value {@code SlingHttpServletRequest} object
         * @return This instance
         */
        public Builder request(SearchRequest value) {
            this.request = value;
            return this;
        }

        /**
         * Stores the total number of query results into this builder
         * @param value Long value
         * @return This instance
         */
        public Builder total(long value) {
            this.total = value;
            return this;
        }

        /**
         * Stores a {@link ColumnCollection} object into this builder
         * @param value {@code ColumnCollection} object
         * @return This instance
         */
        public Builder columns(ColumnCollection value) {
            this.columns = value;
            return this;
        }

        /**
         * Assigns a {@link SearchItem} object to this builder
         * @param value {@code SearchItem} object
         * @return This instance
         */
        public Builder item(SearchItem value) {
            this.items.add(value);
            return this;
        }

        /**
         * Stores string representing an optional into this builder
         * @param value String value
         * @return This instance
         */
        public Builder errorMessage(String value) {
            this.errorMessage = value;
            return this;
        }

        /**
         * Stores string representing optional metadata this builder
         * @param value String value
         * @return This instance
         */
        public Builder metadata(String value) {
            this.metadata = value;
            return this;
        }

        /**
         * Adds a checkpoint for execution start that will be used for the calculation of execution time
         * @return This instance
         */
        public Builder markExecutionStart() {
            this.executionStart = System.currentTimeMillis();
            return this;
        }

        /**
         * Adds a checkpoint for execution end that will be used for the calculation of execution time
         * @return This instance
         */
        public Builder markExecutionEnd() {
            this.executionEnd = System.currentTimeMillis();
            return this;
        }

        /**
         * Creates a new {@link SearchResult} object
         * @return {@code SearchResult} object
         */
        public SearchResult build() {
            if (executionEnd < executionStart && executionStart > 0) {
                markExecutionEnd();
            }
            SearchResult result = new SearchResult();
            result.request = request;
            result.executionTime = executionEnd - executionStart;
            result.total = total;
            result.columns = columns;
            result.items = items;
            result.errorMessage = errorMessage;
            result.metadata = metadata;
            return result;
        }
    }
}

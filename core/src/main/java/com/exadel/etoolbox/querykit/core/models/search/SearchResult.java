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
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Contains the comprehensive data that characterizes the result of running a query
 */
@Builder(builderClassName = "Builder")
@Getter
public class SearchResult implements JsonExportable {

    /**
     * Gets the {@link SearchRequest} associated with the current query
     */
    private final SearchRequest request;

    /**
     * Gets the time (in milliseconds) during which the query was executed
     */
    private final long executionTime;

    /**
     * Gets the total number of query results
     */
    private final long total;

    /**
     * Gets the columns that define the particular properties of every query result. They are adapted with a {@link
     * ColumnCollection} object
     */
    private ColumnCollection columns;

    /**
     * Gets the list of {@link SearchItem} that represent the query results entries
     */
    @Singular
    private final List<SearchItem> items;

    /**
     * If the query execution resulted in an error, this member contains the error message. Otherwise returns {@code
     * null}
     */
    private final String errorMessage;

    /**
     * Gets arbitrary data associated with the query result (generally used for debugging purposes)
     */
    private final String metadata;

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
        return SearchResult
                .builder()
                .request(request)
                .errorMessage(message)
                .build();
    }
}

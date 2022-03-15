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
package com.exadel.etoolbox.querykit.core.servlets;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchItem;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Works in pair with {@link QueryServlet} to provide downloading query results in {@code CSV} format
 */
@UtilityClass
class CsvOutputHelper {

    /**
     * Outputs the given query results in {@code CSV} format
     * @param value {@link SearchResult} instance containing the results of query execution
     * @return String value
     */
    public static String output(SearchResult value) {
        if (CollectionUtils.isEmpty(value.getItems())) {
            return StringUtils.EMPTY;
        }
        SearchItem firstItem = value.getItems().get(0);
        String headers = Stream.concat(Stream.of(Constants.TITLE_PATH), firstItem.getPropertyNames().stream())
                .map(CsvOutputHelper::escapeSpecialCharacters)
                .collect(Collectors.joining(Constants.SEMICOLON));
        StringBuilder builder = new StringBuilder(headers).append("\n");
        value.getItems().forEach(item -> appendRow(builder, item, value.getColumns()));
        return builder.toString();
    }

    private static void appendRow(StringBuilder builder, SearchItem item, ColumnCollection columns) {
        builder.append(escapeSpecialCharacters(item.getPath()));
        for (String property : columns.getPropertyNames()) {
            builder.append(Constants.SEMICOLON).append(escapeSpecialCharacters(item.getProperty(property)));
        }
        builder.append("\n");
    }

    private static String escapeSpecialCharacters(Object value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        String result = value.toString().replaceAll("[\\r\\n]+", Constants.SPACE).trim();
        if (StringUtils.containsAny(value.toString(), Constants.SEMICOLON, Constants.QUOTE)) {
            result = Constants.QUOTE + result.replace(Constants.QUOTE, "\"\"") + Constants.QUOTE;
        }
        return result;
    }
}

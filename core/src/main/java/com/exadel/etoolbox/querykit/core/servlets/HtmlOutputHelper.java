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
import com.exadel.etoolbox.querykit.core.utils.ValueUtil;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Works in pair with {@link QueryServlet} to provide downloading query results in {@code HTML} format
 */
@UtilityClass
class HtmlOutputHelper {

    /**
     * Outputs the given query results in {@code HTML} format
     * @param value {@link SearchResult} instance containing the results of query execution
     * @return String value
     */
    public static String output(SearchResult value) {
        StringBuilder builder = new StringBuilder();
        builder
                .append("<!DOCTYPE html><html lang=\"en\"><head><title>Query Results</title></head>")
                .append("<body>");

        if (CollectionUtils.isEmpty(value.getItems())) {
            builder.append("No results");
        } else {
            appendTable(builder, value);
        }
        builder.append("</body></html>");
        return builder.toString();
    }

    private static void appendTable(StringBuilder builder, SearchResult value) {
        builder.append("<table width=\"100%\" border=\"1\" style=\"border-collapse:collapse\"><thead><tr>");
        builder.append("<th>#</th><th>Path</th>");
        for (String prop : value.getColumns().getPropertyNames()) {
            builder.append("<th>").append(prop).append("</th>");
        }
        builder.append("</tr><tbody>");
        AtomicLong atomicLong = new AtomicLong(1);
        for (SearchItem item : value.getItems()) {
            appendRow(builder, item, value.getColumns(), atomicLong.getAndIncrement());
        }
        builder.append("</tbody></table>");
    }

    private void appendRow(StringBuilder builder, SearchItem item, ColumnCollection columns, long ordinal) {
        builder
                .append("<tr>")
                .append("<td>").append(ordinal).append("</td>")
                .append("<td>").append(item.getPath()).append("</td>");

        for (String prop : columns.getPropertyNames()) {
            builder.append("<td>").append(ValueUtil.getString(item.getProperty(prop))).append("</td>");
        }
        builder.append("</tr>");
    }
}

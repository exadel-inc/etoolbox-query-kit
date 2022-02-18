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

@Builder(builderClassName = "Builder")
@Getter
public class SearchResult implements JsonExportable {

    private final SearchRequest request;

    private final long executionTime;

    private final long total;

    private ColumnCollection columns;

    @Singular
    private final List<SearchItem> items;

    private final String errorMessage;

    private final String info;

    public boolean isSuccess() {
        return StringUtils.isEmpty(errorMessage);
    }

    public static SearchResult error(SearchRequest request, String message) {
        return SearchResult
                .builder()
                .request(request)
                .errorMessage(message)
                .build();
    }

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
}

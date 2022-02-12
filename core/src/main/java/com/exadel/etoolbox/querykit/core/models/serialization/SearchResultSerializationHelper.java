package com.exadel.etoolbox.querykit.core.models.serialization;

import com.exadel.etoolbox.querykit.core.models.SearchItem;
import com.exadel.etoolbox.querykit.core.models.SearchResult;
import com.exadel.etoolbox.querykit.core.utils.ResponseUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SearchResultSerializationHelper {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(SearchItem.class, new SearchItemSerializer())
            .create();

    public static String serialize(SearchResult value) {
        try {
            return GSON.toJson(value);
        } catch (IllegalArgumentException | IllegalStateException | StackOverflowError e) {
            return ResponseUtil.getJsonMessage("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}

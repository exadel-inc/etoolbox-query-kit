package com.exadel.etoolbox.querykit.core.models.serialization;

import com.exadel.etoolbox.querykit.core.models.SearchItem;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class SearchItemSerializer implements JsonSerializer<SearchItem> {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public JsonElement serialize(SearchItem value, Type type, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.addProperty(Constants.PROPERTY_PATH, value.getPath());
        for (String key : value.getValueMap().keySet()) {
            Object propertyValue = value.getValueMap().get(key);
            if (propertyValue != null && propertyValue.getClass().isArray()) {
                result.add(key, getArray(propertyValue));
            } else {
                result.addProperty(key, getString(propertyValue));
            }
        }
        return result;
    }

    private String getString(Object value) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        if (value instanceof Calendar) {
            return FORMATTER.format(((Calendar) value).getTime());
        }
        return String.valueOf(value);
    }

    private JsonArray getArray(Object value) {
        Object[] array = (Object[]) value;
        JsonArray result = new JsonArray();
        Arrays.stream(array).map(this::getString).filter(StringUtils::isNotEmpty).forEach(result::add);
        return result;
    }
}

package com.exadel.etoolbox.querykit.core.models.search;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SimpleSearchItem implements SearchItem {

    @Getter
    @Setter
    private String path;

    private final Map<String, Object> properties;

    public SimpleSearchItem(String path) {
        this(path, new HashMap<>());
    }

    public SimpleSearchItem(String path, Map<String, Object> properties) {
        this.path = path;
        this.properties = properties;
    }

    @Override
    public <T> T getProperty(String name, Class<T> type) {
        if (name == null || type == null) {
            return null;
        }
        return type.cast(properties.get(name));
    }

    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public void storeProperty(String name, Object value, String localPath, int type, boolean multiple) {
        properties.put(name, value);
    }

    /* -------------
       Serialization
       ------------- */

    @Override
    public JsonElement toJson(JsonSerializationContext serializer, ColumnCollection data) {
        JsonObject result = new JsonObject();
        result.addProperty(Constants.PROPERTY_JCR_PATH, path);
        List<String> matchingPropertyNames = data == null || CollectionUtils.isEmpty(data.getItems())
                ? new ArrayList<>(properties.keySet())
                : data.getPropertyNames();
        for (String propertyName : matchingPropertyNames) {
            Object propertyValue = properties.get(propertyName);
            JsonExportUtil.submitValue(result, propertyName, propertyValue);
        }
        return result;
    }
}

package com.exadel.etoolbox.querykit.core.models.search;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.exadel.etoolbox.querykit.core.utils.ValueUtil;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportUtil;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.PropertyType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TypeAwareSearchItem implements SearchItem {

    @Getter
    @Setter
    private String path;

    private Map<String, PropertyDefinition> properties;

    public TypeAwareSearchItem(String path) {
        this(path, new HashMap<>());
    }

    public TypeAwareSearchItem(String path, Map<String, Object> properties) {
        this(path, properties, path);
    }

    public TypeAwareSearchItem(String rootPath, Map<String, Object> properties, String propertiesPath) {
        this.path = rootPath;
        if (MapUtils.isEmpty(properties)) {
            return;
        }
        this.properties = new HashMap<>();
        properties.forEach((name, value) -> storeProperty(
                name,
                value,
                propertiesPath,
                ValueUtil.detectType(value), ValueUtil.detectMultivalue(value)));
    }

    @Override
    public <T> T getProperty(String name, Class<T> type) {
        if (name == null || type == null) {
            return null;
        }
        PropertyDefinition result = properties.get(name);
        if (result == null) {
            return null;
        }
        return type.cast(result.getValue());
    }

    @Override
    public Object getProperty(String name) {
        if (name == null) {
            return null;
        }
        PropertyDefinition result = properties.get(name);
        if (result == null) {
            return null;
        }
        return result.getValue();
    }

    @Override
    public void storeProperty(String name, Object value) {
        storeProperty(name, value, null, 0, false);
    }

    public void storeProperty(String name, Object value, String path, int type, boolean multiple) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, new PropertyDefinition(value, path, type, multiple));
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
            PropertyDefinition property = properties.get(propertyName);
            result.add(propertyName, serializer.serialize(property));
        }
        return result;
    }

    /* ---------------
       Service classes
       --------------- */

    @RequiredArgsConstructor
    @Getter
    private static class PropertyDefinition implements JsonExportable {

        private final Object value;
        private final String path;
        private final int type;
        private final boolean multiple;

        @Override
        public JsonElement toJson(JsonSerializationContext serializer) {
            JsonObject result = new JsonObject();
            JsonExportUtil.submitValue(result, Constants.PROPERTY_VALUE, value);
            result.addProperty(Constants.PROPERTY_PATH, path);
            if (type > 0) {
                result.addProperty(Constants.PROPERTY_TYPE, PropertyType.nameFromValue(type) + (multiple ? "[]" : StringUtils.EMPTY));
            }
            return result;
        }
    }
}

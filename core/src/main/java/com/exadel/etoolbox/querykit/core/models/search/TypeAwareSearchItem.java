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

import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnAdapter;
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
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import javax.jcr.PropertyType;
import javax.jcr.query.qom.Column;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@link SearchItem} implementation for an item that can be rendered or modified in UI
 */
class TypeAwareSearchItem implements SearchItem {

    /**
     * Accesses the path associated with the current entry
     */
    @Getter
    @Setter
    private String path;

    private Map<String, PropertyDefinition> properties;

    /**
     * Creates a new {@link TypeAwareSearchItem} instance
     * @param path Path associated with the current entry
     */
    public TypeAwareSearchItem(String path) {
        this(path, new HashMap<>());
    }

    /**
     * Creates a new {@link TypeAwareSearchItem} instance
     * @param path       Path associated with the current entry
     * @param properties Properties which the current entry will report
     */
    public TypeAwareSearchItem(String path, Map<String, Object> properties) {
        this(path, properties, path);
    }

    /**
     * Creates a new {@link TypeAwareSearchItem} instance
     * @param rootPath       JCR path associated with the search item in a whole
     * @param properties     Properties which the current entry will report
     * @param propertiesPath JCR path associated with the particular properties of the item
     */
    public TypeAwareSearchItem(String rootPath, Map<String, Object> properties, String propertiesPath) {
        this.path = rootPath;
        if (MapUtils.isEmpty(properties)) {
            return;
        }
        this.properties = new HashMap<>();
        properties.forEach((name, value) -> putProperty(
                name,
                value,
                propertiesPath,
                ValueUtil.detectType(value), ValueUtil.detectMultivalue(value)));
    }

    /* ------------------------
       Common interface methods
       ------------------------ */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getPropertyNames() {
        return properties != null ? properties.keySet() : Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void putProperty(String name, Object value) {
        putProperty(name, value, null, 0, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putProperty(String name, Object value, String path, int type, boolean multiple) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, new PropertyDefinition(value, path, type, multiple));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearProperties() {
        if (properties == null) {
            return;
        }
        properties.clear();
    }

    /* -------------
       Serialization
       ------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource toVirtualResource(ResourceResolver resourceResolver, ColumnCollection columns, String resourceType) {
        Map<String, Object> valueProperties = SearchItem.super.toVirtualResource(resourceResolver, columns, resourceType).getValueMap();
        Map<String, Object> allProperties = new LinkedHashMap<>(valueProperties);

        Map<String, Object> serviceProperties = new HashMap<>();
        for (Column column : columns.getItems()) {
            String propName = column instanceof ColumnAdapter
                    ? ((ColumnAdapter) column).getUniquePropertyName()
                    : column.getPropertyName();
            String propPath = StringUtils.endsWith(propName, Constants.PROPERTY_JCR_PATH) || MapUtils.isEmpty(properties)
                    ? StringUtils.EMPTY
                    : StringUtils.defaultString(properties.get(propName).getPath(), getPath());
            String propType = StringUtils.endsWith(propName, Constants.PROPERTY_JCR_PATH) || MapUtils.isEmpty(properties)
                    ? PropertyType.nameFromValue(PropertyType.STRING)
                    : PropertyType.nameFromValue(properties.get(propName).getType());

            if (StringUtils.isNotEmpty(propPath)) {
                serviceProperties.put(propName + Constants.DOUBLE_AT + Constants.PROPERTY_PATH, propPath);
            }
            if (StringUtils.isNotEmpty(propType)) {
                serviceProperties.put(propName + Constants.DOUBLE_AT + Constants.PROPERTY_TYPE, propType);
            }
        }

        allProperties.putAll(serviceProperties);

        ValueMap valueMap = new ValueMapDecorator(allProperties);
        return new ValueMapResource(
                resourceResolver,
                getPath(),
                resourceType,
                valueMap);
    }

    /**
     * {@inheritDoc}
     */
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

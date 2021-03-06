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
import java.util.Set;

/**
 * A {@link SearchItem} implementation for an item that is not expected to be rendered or modified in UI
 */
class SimpleSearchItem implements SearchItem {

    /**
     * Accesses the path associated with the current entry
     */
    @Getter
    @Setter
    private String path;

    private final Map<String, Object> properties;

    /**
     * Creates a new {@link SimpleSearchItem} instance
     * @param path Path associated with the current entry
     */
    public SimpleSearchItem(String path) {
        this(path, new HashMap<>());
    }

    /**
     * Creates a new {@link SimpleSearchItem} instance
     * @param path       Path associated with the current entry
     * @param properties Properties which the current entry will report
     */
    public SimpleSearchItem(String path, Map<String, Object> properties) {
        this.path = path;
        this.properties = properties;
    }

    /* ------------------------
       Common interface methods
       ------------------------ */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getProperty(String name, Class<T> type) {
        if (name == null || type == null) {
            return null;
        }
        return type.cast(properties.get(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putProperty(String name, Object value, String localPath, int type, boolean multiple) {
        properties.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearProperties() {
        properties.clear();
    }

    /* -------------
       Serialization
       ------------- */

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
            Object propertyValue = properties.get(propertyName);
            JsonExportUtil.storeValue(result, propertyName, propertyValue);
        }
        return result;
    }
}

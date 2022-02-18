package com.exadel.etoolbox.querykit.core.models.search;

import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.day.crx.JcrConstants;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnAdapter;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportableWithContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import javax.jcr.query.qom.Column;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public interface SearchItem extends JsonExportableWithContext<ColumnCollection> {

    String getPath();

    void setPath(String path);

    Set<String> getPropertyNames();

    <T> T getProperty(String name, Class<T> type);

    Object getProperty(String name);

    default void putProperty(String name, Object value) {
        putProperty(name, value, null, 0, false);
    }

    void putProperty(String name, Object value, String localPath, int type, boolean multiple);

    void clearProperties();

    default Resource toVirtualResource(ResourceResolver resourceResolver, ColumnCollection columns) {
        if (columns == null || CollectionUtils.isEmpty(columns.getItems())) {
            return toVirtualResource(resourceResolver);
        }
        Map<String, Object> matchingProperties = new LinkedHashMap<>();
        for (Column item : columns.getItems()) {
            if (!(item instanceof ColumnAdapter)) {
                continue;
            }
            ColumnAdapter columnAdapter = (ColumnAdapter) item;
            String propName = columnAdapter.getUniquePropertyName();
            matchingProperties.put(propName, getProperty(propName));
        }

        ValueMap valueMap = new ValueMapDecorator(matchingProperties);
        return new ValueMapResource(
                resourceResolver,
                getPath(),
                JcrConstants.NT_UNSTRUCTURED,
                valueMap);
    }

    default Resource toVirtualResource(ResourceResolver resourceResolver) {
        Map<String, Object> properties = new HashMap<>();
        getPropertyNames().forEach(name -> properties.put(name, getProperty(name)));
        ValueMap valueMap = new ValueMapDecorator(properties);
        return new ValueMapResource(
                resourceResolver,
                getPath(),
                JcrConstants.NT_UNSTRUCTURED,
                valueMap);
    }

    default Resource toResource(ResourceResolver resourceResolver) {
        return resourceResolver.getResource(getPath());
    }

    @Override
    JsonElement toJson(JsonSerializationContext serializer, ColumnCollection data);

    static SearchItem newInstance(SearchRequest request, String path) {
        return request.isStoreDetails()
                ? new TypeAwareSearchItem(path)
                : new SimpleSearchItem(path);
    }

    static SearchItem newInstance(SearchRequest request, String path, Map<String, Object> properties) {
        return request.isStoreDetails()
                ? new TypeAwareSearchItem(path, properties)
                : new SimpleSearchItem(path, properties);
    }

    static SearchItem newInstance(SearchRequest request, String rootPath, Map<String, Object> properties, String propertiesPath) {
        return request.isStoreDetails()
                ? new TypeAwareSearchItem(rootPath, properties, propertiesPath)
                : new SimpleSearchItem(rootPath, properties);
    }

}

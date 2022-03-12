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
import com.day.crx.JcrConstants;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnAdapter;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.utils.Constants;
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

/**
 * Represents a single entry within the executed query result
 */
public interface SearchItem extends JsonExportableWithContext<ColumnCollection> {

    /* ------------------------
       Common interface methods
       ------------------------ */

    /**
     * Retrieves the JCR path associated with the current entry
     * @return String value
     */
    String getPath();

    /**
     * Assigns the JCR path associated with the current entry
     * @param path String value; a non-blank string is expected
     */
    void setPath(String path);

    /**
     * Retrieves names of properties contained within the current entry
     * @return {@code Set} of strings
     */
    Set<String> getPropertyNames();

    /**
     * Retrieves a particular named value contained within the current entry
     * @param name Property name
     * @param type {@code Class<?>} reference manifesting the required value type
     * @param <T>  Type of the value
     * @return {@code T}-typed object, or null
     */
    <T> T getProperty(String name, Class<T> type);

    /**
     * Retrieves a particular named value contained within the current entry
     * @param name Property name
     * @return Arbitrary object, or null
     */
    Object getProperty(String name);

    /**
     * Stores a named property into the current entry
     * @param name  Property name
     * @param value Property value
     */
    default void putProperty(String name, Object value) {
        putProperty(name, value, null, 0, false);
    }

    /**
     * Stores a named property into the current entry
     * @param name      Property name
     * @param value     Property value
     * @param localPath {@code Path} associated with the value (can differ from the "overall" {@code path} of the
     *                  current entry
     * @param type      Integer type flag; see {@link javax.jcr.PropertyType} values
     * @param multiple  True if this is an array-like property; otherwise, false
     */
    void putProperty(String name, Object value, String localPath, int type, boolean multiple);

    /**
     * Removes all the properties associated with the current entry
     */
    void clearProperties();

    /**
     * Converts this instance into d virtual Sling resource suitable for use with Granite datasources
     * @param resourceResolver {@code ResourceResolver} instance
     * @param columns          {@link ColumnCollection} instance that defines the names and sequence of properties
     *                         stored within the resulting resource
     * @return {@code Resource} object
     */
    default Resource toVirtualResource(ResourceResolver resourceResolver, ColumnCollection columns) {
        return toVirtualResource(resourceResolver, columns, JcrConstants.NT_UNSTRUCTURED);
    }

    /**
     * Retrieves an "actual" JCR resource associated with the current entry
     * @param resourceResolver {@code ResourceResolver} instance
     * @return {@code Resource} object
     */
    default Resource toResource(ResourceResolver resourceResolver) {
        return resourceResolver.getResource(getPath());
    }

    /* -------------
       Serialization
       ------------- */

    /**
     * Converts this instance into d virtual Sling resource suitable for use with Granite datasources
     * @param resourceResolver {@code ResourceResolver} instance
     * @param columns          {@link ColumnCollection} instance that defines the names and sequence of properties
     *                         stored within the resulting resource
     * @param resourceType     String value used as the {@code sling:resourceType} attribute
     * @return {@code Resource} object
     */
    default Resource toVirtualResource(
            ResourceResolver resourceResolver,
            ColumnCollection columns,
            String resourceType) {

        if (columns == null || CollectionUtils.isEmpty(columns.getItems())) {
            return toVirtualResource(resourceResolver);
        }

        Map<String, Object> displayedProperties = new LinkedHashMap<>();
        for (Column column : columns.getItems()) {
            String propName = column instanceof ColumnAdapter
                    ? ((ColumnAdapter) column).getUniquePropertyName()
                    : column.getPropertyName();
            Object propValue = Constants.PROPERTY_JCR_PATH.equals(propName) ? getPath() : getProperty(propName);
            displayedProperties.put(propName, propValue);
        }

        ValueMap valueMap = new ValueMapDecorator(displayedProperties);
        return new ValueMapResource(
                resourceResolver,
                getPath(),
                resourceType,
                valueMap);
    }

    /**
     * Converts this instance into d virtual Sling resource suitable for use with Granite datasources
     * @param resourceResolver {@code ResourceResolver} instance
     * @return {@code Resource} object
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    JsonElement toJson(JsonSerializationContext serializer, ColumnCollection data);

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Creates a new search item out of the given request and path argument
     * @param request {@link SearchRequest} instance
     * @param path    JCR path associated with the search item
     * @return {@code SearchItem} object
     */
    static SearchItem newInstance(SearchRequest request, String path) {
        return request.isStoreDetails()
                ? new TypeAwareSearchItem(path)
                : new SimpleSearchItem(path);
    }

    /**
     * Creates a new search item out of the given request, path, and arbitrary properties
     * @param request    {@link SearchRequest} instance
     * @param path       JCR path associated with the search item
     * @param properties Properties that the search item will report
     * @return {@code SearchItem} object
     */
    static SearchItem newInstance(SearchRequest request, String path, Map<String, Object> properties) {
        return request.isStoreDetails()
                ? new TypeAwareSearchItem(path, properties)
                : new SimpleSearchItem(path, properties);
    }

    /**
     * Creates a new search item out of the given request, arbitrary properties, and also root path and properties path.
     * The difference between the paths is that the first refers to the exact position of the result in JCR (e.g., the
     * path to a {@code Page}) while the second provides then {@code path} metadata value associated with the particular
     * properties (for a page, this is a path to the child {@code jcr:content} node)
     * @param request        {@link SearchRequest} instance
     * @param rootPath       JCR path associated with the search item in a whole
     * @param properties     Properties that the search item will report
     * @param propertiesPath JCR path associated with the particular properties of the item
     * @return {@code SearchItem} object
     */
    static SearchItem newInstance(SearchRequest request, String rootPath, Map<String, Object> properties, String propertiesPath) {
        return request.isStoreDetails()
                ? new TypeAwareSearchItem(rootPath, properties, propertiesPath)
                : new SimpleSearchItem(rootPath, properties);
    }

}

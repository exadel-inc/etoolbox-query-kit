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
package com.exadel.etoolbox.querykit.core.models.qom;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a context for filtering JCR nodes with {@link com.exadel.etoolbox.querykit.core.models.qom.constraints.ConstraintAdapter}'s
 * in a non-query manner
 */
public class EvaluationContext {
    private final Map<String, Resource> resources = new HashMap<>();

    /**
     * Retrieves the collection of query bind variables
     */
    @Getter
    private final Map<String, Object> bindVariables = new HashMap<>();

    /**
     * Gets whether there exists a resource that corresponds to the given identifier
     * @param identifier String value
     * @return True or false
     */
    public boolean hasResource(String identifier) {
        return getResource(identifier) != null;
    }

    /**
     * Retrieves a resource that corresponds to the given selector
     * @param identifier String value
     * @return Nullable {@code Resource} object
     */
    public Resource getResource(String identifier) {
        return resources.get(identifier);
    }

    /**
     * Retrieves the path to a resource that corresponds to the given identifier
     * @param identifier String value
     * @return String value; might be an empty string
     */
    public String getPath(String identifier) {
        return getResource(identifier) != null ? getResource(identifier).getPath() : StringUtils.EMPTY;
    }

    /**
     * Retrieves the name of a resource that corresponds to the given identifier
     * @param identifier String value
     * @return String value; might be an empty string
     */
    public String getName(String identifier) {
        return getResource(identifier) != null ? getResource(identifier).getName() : StringUtils.EMPTY;
    }

    /**
     * Retrieves the property value of a resource that corresponds to the given identifier
     * @param identifier Resource identifier
     * @param name       Name of the property to look for
     * @return A nullable object
     */
    public Object getProperty(String identifier, String name) {
        if (!hasResource(identifier)) {
            return null;
        }
        if ("jcr:path".equals(name)) {
            return getResource(identifier).getPath();
        }
        return getResource(identifier).getValueMap().get(name);
    }
}

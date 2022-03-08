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

public class EvaluationContext {
    private final Map<String, Resource> resources = new HashMap<>();

    @Getter
    private final Map<String, Object> bindVariables = new HashMap<>();

    public boolean hasResource(String selector) {
        return getResource(selector) != null;
    }

    public Resource getResource(String selector) {
        return resources.get(selector);
    }

    public String getPath(String selector) {
        return getResource(selector) != null ? getResource(selector).getPath() : StringUtils.EMPTY;
    }

    public String getName(String selector) {
        return getResource(selector) != null ? getResource(selector).getName() : StringUtils.EMPTY;
    }

    public Object getProperty(String selector, String name) {
        if (!hasResource(selector)) {
            return null;
        }
        if ("jcr:path".equals(name)) {
            return getResource(selector).getPath();
        }
        return getResource(selector).getValueMap().get(name);
    }
}

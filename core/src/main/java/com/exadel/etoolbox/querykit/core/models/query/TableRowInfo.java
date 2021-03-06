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
package com.exadel.etoolbox.querykit.core.models.query;

import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the data needed to render the query results table row
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TableRowInfo {

    @SlingObject
    @SuppressWarnings("unused") // Assigned via injection
    private Resource resource;

    /**
     * Retrieves the ordinal of this row in the collection of query results
     */
    @Getter(lazy = true)
    private final long ordinal = prepareOrdinal();

    /**
     * Retrieves the collection of cells to be rendered within the current table row
     */
    @Getter(lazy = true)
    private final Collection<TableCellInfo> cells = prepareCells();

    private long prepareOrdinal() {
        if (resource == null) {
            return 0L;
        }
        return resource.getValueMap().get(Constants.PROPERTY_ORDINAL, 0L);
    }

    private Collection<TableCellInfo> prepareCells() {
        Map<String, TableCellInfo> cells = new LinkedHashMap<>();
        if (resource == null) {
            return cells.values();
        }
        for(String key : resource.getValueMap().keySet()) {
            if (!StringUtils.contains(key, Constants.DOUBLE_AT)) {
                cells.put(key, new TableCellInfo(key, resource.getValueMap().get(key)));
            } else if (StringUtils.endsWith(key, Constants.DOUBLE_AT + Constants.PROPERTY_PATH)) {
                String propertyName = extractPropertyName(key);
                cells.computeIfPresent(propertyName, (k, cell) -> {
                    cell.setPath(resource.getValueMap().get(key, String.class));
                    return cell;
                });
            } else if (StringUtils.endsWith(key, Constants.DOUBLE_AT + Constants.PROPERTY_TYPE)) {
                String propertyName = extractPropertyName(key);
                cells.computeIfPresent(propertyName, (k, cell) -> {
                    cell.setType(resource.getValueMap().get(key, String.class));
                    return cell;
                });
            }
        }
        return cells.values();
    }

    private static String extractPropertyName(String value) {
        return StringUtils.substringBefore(value, Constants.DOUBLE_AT);
    }
}

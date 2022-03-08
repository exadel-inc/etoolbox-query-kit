package com.exadel.etoolbox.querykit.core.models.query;

import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class TableRowInfo {

    @SlingObject
    private Resource resource;

    @Getter(lazy = true)
    private final Map<String, TableCellInfo> cells = prepareCells();

    private Map<String, TableCellInfo> prepareCells() {
        Map<String, TableCellInfo> cells = new LinkedHashMap<>();
        if (resource == null) {
            return cells;
        }
        for(String key : resource.getValueMap().keySet()) {
            if (!StringUtils.contains(key, Constants.DOUBLE_AT)) {
                cells.put(key, new TableCellInfo(resource.getValueMap().get(key)));
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
        return cells;
    }

    private static String extractPropertyName(String value) {
        return StringUtils.substringBefore(value, Constants.DOUBLE_AT);
    }
}
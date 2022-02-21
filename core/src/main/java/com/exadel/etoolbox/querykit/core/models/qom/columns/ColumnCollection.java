package com.exadel.etoolbox.querykit.core.models.qom.columns;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.query.qom.Column;
import java.util.List;
import java.util.stream.Collectors;

public interface ColumnCollection {

    List<Column> getItems();

    List<String> getSelectors();

    default List<String> getColumnNames() {
        return CollectionUtils.emptyIfNull(getItems())
                .stream()
                .map(Column::getColumnName)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    List<String> getPropertyNames();

}

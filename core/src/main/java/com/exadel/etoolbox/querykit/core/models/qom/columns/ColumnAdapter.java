package com.exadel.etoolbox.querykit.core.models.qom.columns;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.query.qom.Column;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ColumnAdapter implements Column {
    private final String selector;
    private final String property;
    private final String title;

    @Setter(AccessLevel.PACKAGE)
    private transient boolean useQualifiedProperty;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private transient boolean isDefault;

    ColumnAdapter(String selector, String property) {
        this(selector, property, selector + "." + property);
    }

    ColumnAdapter(Column source) {
        this(source.getSelectorName(), source.getPropertyName(), source.getColumnName());
    }

    @Override
    public String getSelectorName() {
        return selector;
    }

    @Override
    public String getPropertyName() {
        return property;
    }

    public String getUniquePropertyName() {
        return useQualifiedProperty && StringUtils.isNotBlank(property)
                ? selector + "." + property
                : property;
    }

    @Override
    public String getColumnName() {
        return title;
    }

    boolean isWildcard() {
        return StringUtils.isEmpty(property);
    }
}

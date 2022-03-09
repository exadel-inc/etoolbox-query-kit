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
package com.exadel.etoolbox.querykit.core.models.qom.columns;

import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.query.qom.Column;

/**
 * Represents a {@link Column} entity used in processing a query while adding several utility accessors
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ColumnAdapter implements Column {

    private static final String WILDCARD = "*";

    private final String selector;
    private final String property;
    private final String title;

    /**
     * Represents an accessor to the property defining whether this object will report its {@code propertyName}  with
     * the selector qualifier
     */
    @Setter(AccessLevel.PACKAGE)
    private transient boolean useQualifiedProperty;

    /**
     * Represents an accessor to the property defining whether this object is a default column
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private transient boolean isDefault;

    /**
     * Creates a new {@link ColumnAdapter} instance for the given selector and property name
     * @param selector A non-blank string representing the selector
     * @param property A non-blank string representing the property name
     */
    ColumnAdapter(String selector, String property) {
        this(selector, property, selector + Constants.DOT + property);
    }

    /**
     * Creates a new {@link ColumnAdapter} instance based up[on the given {@code Column}
     * @param source {@code Column} object used as the source of properties
     */
    ColumnAdapter(Column source) {
        this(source.getSelectorName(), source.getPropertyName(), source.getColumnName());
    }

    /**
     * Retrieves the selector name associated with the current {@code Column}
     * @return String value
     */
    @Override
    public String getSelectorName() {
        return selector;
    }

    /**
     * Retrieves the property name associated with the current {@code Column}
     * @return String value
     */
    @Override
    public String getPropertyName() {
        return property;
    }

    /**
     * Retrieves the property name associated with the current {@code Column} prepended by a selector qualifier if
     * needed
     * @return String value
     */
    public String getUniquePropertyName() {
        return useQualifiedProperty && StringUtils.isNotBlank(property)
                ? selector + Constants.DOT + property
                : property;
    }

    /**
     * Retrieves the column name (i.e. title) associated with the current {@code Column}
     * @return String value
     */
    @Override
    public String getColumnName() {
        return title;
    }

    /**
     * Gets whether this {@code Column} is a wildcard column (corresponds to the asterisk in {@code SELECT * FROM...} or
     * {@code SELECT foo.* FROM...} statement)
     * @return True or false
     */
    boolean isWildcard() {
        return StringUtils.isEmpty(property) || property.equals(WILDCARD);
    }
}

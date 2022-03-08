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

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ColumnAdapter implements Column {

    private static final String WILDCARD = "*";

    private final String selector;
    private final String property;
    private final String title;

    @Setter(AccessLevel.PACKAGE)
    private transient boolean useQualifiedProperty;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private transient boolean isDefault;

    ColumnAdapter(String selector, String property) {
        this(selector, property, selector + Constants.DOT + property);
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
                ? selector + Constants.DOT + property
                : property;
    }

    @Override
    public String getColumnName() {
        return title;
    }

    boolean isWildcard() {
        return StringUtils.isEmpty(property) || property.equals(WILDCARD);
    }
}

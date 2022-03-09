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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.query.qom.Column;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Presents a collection of {@link Column} objects used to populate query results together with utility accessors to the
 * columns' main properties
 */
public interface ColumnCollection {

    /**
     * Retrieves a list of {@code Column} objects
     * @return {@code List} instance; might be an empty non-null list
     */
    List<Column> getItems();

    /**
     * Retrieves a list of strings representing selectors of columns that are presented in this collection
     * @return {@code List} instance; might be an empty non-null list
     */
    List<String> getSelectors();

    /**
     * Retrieves a list of strings representing names of columns that are presented in this collection
     * @return {@code List} instance; might be an empty non-null list
     */
    default List<String> getColumnNames() {
        return CollectionUtils.emptyIfNull(getItems())
                .stream()
                .map(Column::getColumnName)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of strings representing names of columns that are presented in this collection
     * @return {@code List} instance; might be an empty non-null list
     */
    List<String> getPropertyNames();
}

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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Contains the data needed to render the query results table cell
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class TableCellInfo {

    /**
     * Gets the {@code name} attribute of the cell
     */
    private final String name;

    /**
     * Gets the value of the cell
     */
    private final Object value;

    /**
     * Gets or sets the {@code path} attribute of the cell
     */
    @Setter(AccessLevel.PACKAGE)
    private String path;

    /**
     * Gets or sets the {@code type} attribute of the cell
     */
    @Setter(AccessLevel.PACKAGE)
    private String type;

    /**
     * Retrieves the hash code of the associated value. This method is used to distinguish between different instances
     * of the value editing dialog
     * @return Int value
     */
    public int getValueHash() {
        return Objects.hashCode(value);
    }

    /**
     * Gets the {@code type} attribute of the cell
     */
    public String getType() {
        if ("String".equals(type) && value != null && value.toString().length() > 76) {
            return "longString";
        }
        return type;
    }
}
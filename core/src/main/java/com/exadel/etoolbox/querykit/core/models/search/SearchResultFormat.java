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
package com.exadel.etoolbox.querykit.core.models.search;

import org.apache.commons.lang3.EnumUtils;

public enum SearchResultFormat {
    UNDEFINED, JSON, HTML, CSV;

    static SearchResultFormat from(String value) {
        return EnumUtils
                .getEnumList(SearchResultFormat.class)
                .stream()
                .filter(item -> item.toString().equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNDEFINED);
    }

}

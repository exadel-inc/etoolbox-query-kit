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
package com.exadel.etoolbox.querykit.core.servlets.datasources;

import com.adobe.granite.ui.components.ds.DataSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.sling.api.resource.Resource;

import java.util.Iterator;

/**
 * Augments the standard {@link DataSource} by providing the total number of query result entries
 */
@RequiredArgsConstructor
@Getter
class MeasuredDatasourceAdapter implements DataSource {

    private final DataSource original;

    private final long total;

    /**
     * Retrieves the iterator of datasource entries
     * @return {@code Iterator} instance
     */
    @Override
    public Iterator<Resource> iterator() {
        return original.iterator();
    }

    /**
     * Retrieves the total number of query result entries
     * @return Long value
     */
    @Override
    public Long getGuessTotal() {
        return total;
    }
}
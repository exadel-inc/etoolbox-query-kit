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
package com.exadel.etoolbox.querykit.core.services.converters;

import com.exadel.etoolbox.querykit.core.models.search.QueryType;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import org.apache.sling.api.resource.ResourceResolver;

/**
 * Converts a query statement into the requested format
 */
public interface QueryConverter {

    /**
     * Retrieves the {@link QueryType} the current converter supports
     * @return One of the {@code QueryType} values
     */
    QueryType getSourceType();

    /**
     * Converts the provided query statement
     * @param statement        Statements string; a non-blank value is expected
     * @param resourceResolver {@code ResourceResolver} object used to process queries
     * @param type             {@code Class<?>} reference manifesting the class of the conversion result
     * @param <T>              Type of result
     * @return {@code T}-typed value or null (implementation-dependent)
     * @throws ConverterException In case the conversion failed
     */
    <T> T convert(String statement, ResourceResolver resourceResolver, Class<T> type) throws ConverterException;
}

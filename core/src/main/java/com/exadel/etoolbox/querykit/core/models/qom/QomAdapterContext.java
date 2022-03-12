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
package com.exadel.etoolbox.querykit.core.models.qom;

import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.qom.QueryObjectModelFactory;

/**
 * Represents a factory context for creating query object model adapters and bundles
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class QomAdapterContext {

    /**
     * Retrieves a {@link QueryObjectModelFactory} used to create new entities
     */
    private final QueryObjectModelFactory modelFactory;

    /**
     * Retrieves a {@link ValueFactory} used to create new entities
     */
    private final ValueFactory valueFactory;

    /**
     * Gets whether the query object model has been changed (e.g., by interpolating user-defined variable templates or
     * escaping functions)
     */
    private boolean changed;

    /**
     * Sets that the query object model has been changed (e.g., by interpolating user-defined variable templates or
     * escaping functions)
     */
    public void reportChange() {
        changed = true;
    }

    /**
     * Creates a new {@link QomAdapterContext} instance for the given factory objects
     * @param modelFactory {@link QueryObjectModelFactory} instance
     * @param valueFactory {@link ValueFactory} instance
     * @return New {@code QomAdapterContext}
     */
    public static QomAdapterContext from(QueryObjectModelFactory modelFactory, ValueFactory valueFactory) {
        return new QomAdapterContext(modelFactory, valueFactory);
    }

    /**
     * Creates a new {@link QomAdapterContext} instance for the given request
     * @param request {@link SearchRequest} instance
     * @return New {@code QomAdapterContext}
     */
    public static QomAdapterContext from(SearchRequest request) throws RepositoryException {
        return new QomAdapterContext(request.getQueryManager().getQOMFactory(), request.getValueFactory());
    }

    /**
     * Creates a new {@link QomAdapterContext} by cloning the existing context
     * @param original {@link QomAdapterContext} instance
     * @return New {@code QomAdapterContext}
     */
    public static QomAdapterContext from(QomAdapterContext original) {
        return new QomAdapterContext(original.getModelFactory(), original.getValueFactory());
    }
}

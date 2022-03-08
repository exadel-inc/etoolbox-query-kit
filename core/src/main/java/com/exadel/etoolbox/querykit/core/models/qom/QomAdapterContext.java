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

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class QomAdapterContext {

    private final QueryObjectModelFactory modelFactory;

    private final ValueFactory valueFactory;

    private boolean changed;

    public void reportChange() {
        changed = true;
    }

    public static QomAdapterContext from(QueryObjectModelFactory modelFactory, ValueFactory valueFactory) {
        return new QomAdapterContext(modelFactory, valueFactory);
    }

    public static QomAdapterContext from(SearchRequest request) throws RepositoryException {
        return new QomAdapterContext(request.getQueryManager().getQOMFactory(), request.getValueFactory());
    }

    public static QomAdapterContext from(QomAdapterContext original) {
        return new QomAdapterContext(original.getModelFactory(), original.getValueFactory());
    }
}

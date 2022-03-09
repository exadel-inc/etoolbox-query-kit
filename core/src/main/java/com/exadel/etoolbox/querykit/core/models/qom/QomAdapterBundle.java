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

import com.exadel.etoolbox.querykit.core.models.query.ParsedQueryInfo;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportable;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a bundle (collection) of adapters for query object models. Usually, this one stands behind a {@code
 * UNION-joined} complex query
 */
@RequiredArgsConstructor
@Getter
public class QomAdapterBundle implements ParsedQueryInfo, JsonExportable {

    private final List<QomAdapter> adapters;

    /**
     * Retrieves the list of original query object models used as the source of this adapter
     * @return {@code List} of models; might be an empty non-null list
     */
    public List<QueryObjectModel> getModels() {
        return CollectionUtils.emptyIfNull(adapters).stream().map(QomAdapter::getModel).collect(Collectors.toList());
    }

    /**
     * Re-creates the current instance with the given factory. This method can be used to "compile" changes that were
     * introduced into constraints, etc. as a result of user-defined templates interpolation
     * @param factory {@link QueryObjectModelFactory} instance
     * @return A new {@link QomAdapter} instance; not equal to the original object
     * @throws RepositoryException If instance creation failed
     */
    public QomAdapterBundle buildWith(QueryObjectModelFactory factory) throws RepositoryException {
        return buildWith(factory, null);
    }

    /**
     * Re-creates the current instance with the given factory. This method can be used to "compile" changes that were
     * introduced into constraints etc. as a result of user-defined templates interpolation
     * @param factory   {@link QueryObjectModelFactory} instance
     * @param arguments User-provided arguments that can be used for the interpolation
     * @return A new {@link QomAdapter} instance; not equal to the original object
     * @throws RepositoryException If instance creation failed
     */
    public QomAdapterBundle buildWith(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException {
        if (CollectionUtils.isEmpty(adapters)) {
            return this;
        }
        List<QomAdapter> newAdapters = new ArrayList<>();
        for (QomAdapter adapter : adapters) {
            newAdapters.add(adapter.buildWith(factory, arguments));
        }
        return new QomAdapterBundle(newAdapters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toJson() {
        return JsonExportUtil.export(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonElement toJson(JsonSerializationContext serializer) {
        return serializer.serialize(adapters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toSqlString() {
        if (CollectionUtils.isEmpty(adapters)) {
            return StringUtils.EMPTY;
        }
        StringBuilder result = new StringBuilder();
        for (QomAdapter adapter : adapters) {
            String newChunk = adapter.toSqlString();
            if (StringUtils.isEmpty(newChunk)) {
                continue;
            }
            result.append(result.length() > 0 ? " UNION " : StringUtils.EMPTY).append(newChunk);
        }
        return result.toString();
    }
}

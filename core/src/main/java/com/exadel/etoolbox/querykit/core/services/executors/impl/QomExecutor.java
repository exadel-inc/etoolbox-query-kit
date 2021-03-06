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
package com.exadel.etoolbox.querykit.core.services.executors.impl;

import com.exadel.etoolbox.querykit.core.models.qom.QomAdapter;
import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterContext;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ModifiableColumnCollection;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.ConstraintAdapter;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.LiteralHolder;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.InterpolationHelper;
import com.exadel.etoolbox.querykit.core.models.query.ParsedQueryInfo;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.executors.ExecutorType;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemConverterFactory;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemFilterFactory;
import com.exadel.etoolbox.querykit.core.services.executors.Executor;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import lombok.AccessLevel;
import lombok.Getter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.qom.QueryObjectModel;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements {@link Executor} for running queries based on query object models
 */
@Component(service = Executor.class)
public class QomExecutor extends QueryBasedExecutor {

    @Reference(target = "(converter.output=QOM)", policyOption = ReferencePolicyOption.GREEDY)
    private QueryConverter queryConverterService;

    /**
     * {@inheritDoc}
     */
    @Getter(AccessLevel.PACKAGE)
    @Reference
    private List<SearchItemFilterFactory> itemFilters;

    /**
     * {@inheritDoc}
     */
    @Getter(AccessLevel.PACKAGE)
    @Reference
    private List<SearchItemConverterFactory> itemConverters;

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutorType getType() {
        return ExecutorType.QOM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParsedQueryInfo parse(SearchRequest request) throws Exception {
        return parse(request, true);
    }

    private ParsedQueryInfo parse(SearchRequest request, boolean recreateIfModified) throws ConverterException, RepositoryException {
        QomAdapter qomAdapter = queryConverterService.convert(
                request.getStatement(),
                request.getResourceResolver(),
                QomAdapter.class);
        if (request.getUserParameters().isEmpty()) {
            return qomAdapter;
        }
        if (!hasInterpolateableFieldsOrModifications(qomAdapter.getConstraintAdapter(), request.getUserParameters().keySet())) {
            return qomAdapter;
        }
        QomAdapter modifiedVersion = qomAdapter.buildWith(request.getQueryManager().getQOMFactory(), request.getUserParameters());
        if (!recreateIfModified) {
            return modifiedVersion;
        }
        return QomAdapter.from(
                modifiedVersion.getModel(),
                QomAdapterContext.from(request.getQueryManager().getQOMFactory(), request.getValueFactory()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Query compile(SearchRequest request) throws Exception {
        QomAdapter qomAdapter = (QomAdapter) parse(request, false);
        return qomAdapter.getModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    ColumnCollection getColumnCollection(SearchRequest request, Query query) {
        return new ModifiableColumnCollection((QueryObjectModel) query);
    }

    private static boolean hasInterpolateableFieldsOrModifications(ConstraintAdapter constraintAdapter, Set<String> candidateVariables) {
        AtomicBoolean success = new AtomicBoolean();
        constraintAdapter.visit(adapter -> {
            if (!(adapter instanceof LiteralHolder)) {
                return;
            }
            String value = ((LiteralHolder) adapter).getLiteralValue();
            if (candidateVariables.stream().anyMatch(candidate -> InterpolationHelper.isProcessable(value, candidate))) {
                success.set(true);
            }
        });
        return success.get();
    }
}

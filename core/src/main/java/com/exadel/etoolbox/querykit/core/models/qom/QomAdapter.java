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

import com.exadel.etoolbox.querykit.core.models.qom.constraints.ConstraintAdapter;
import com.exadel.etoolbox.querykit.core.models.query.ParsedQueryInfo;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportable;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.query.sql2.QOMFormatter;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Source;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents an adapter for a query object model. This one has additional features such as the ability to interpolate
 * user-defined property templates and being exported to a node JCR node predicate
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
public class QomAdapter implements ParsedQueryInfo, JsonExportable {

    private static final Pattern ESCAPED_PATH = Pattern.compile("\\[(/[^]]+)]");

    private final transient QueryObjectModel original;

    /**
     * Retrieves an adapted tree of constraints for the current query object model
     */
    @Getter
    private final ConstraintAdapter constraintAdapter;

    /**
     * Retrieves the source representation for the current query object model
     * @return {@code Source} instance
     */
    public Source getSource() {
        return original.getSource();
    }

    /**
     * Retrieves an array of columns presented by the current query object model
     * @return An array of {@code Column} objects; might be a non-null zero-length array
     */
    public Column[] getColumns() {
        return original.getColumns();
    }

    /**
     * Retrieves an array of orderings presented by the current query object model
     * @return An array of {@code Ordering} objects; might be a non-null zero-length array
     */
    public Ordering[] getOrderings() {
        return original.getOrderings();
    }

    /**
     * Retrieves the original query object model used as the source for the current adapter
     * @return {@code QueryObjectModel} instance
     */
    public QueryObjectModel getModel() {
        return original;
    }

    /**
     * Re-creates the current instance with the given factory. This method can be used to "compile" changes that were
     * introduced into constraints, etc. as a result of user-defined templates interpolation
     * @param factory {@link QueryObjectModelFactory} instance
     * @return A new {@link QomAdapter} instance; not equal to the original object
     * @throws RepositoryException If instance creation failed
     */
    public QomAdapter buildWith(QueryObjectModelFactory factory) throws RepositoryException {
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
    public QomAdapter buildWith(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException {
        QueryObjectModel newModel = factory.createQuery(
                getSource(),
                constraintAdapter != null ? constraintAdapter.getConstraint(factory, arguments) : null,
                getOrderings(),
                getColumns());
        return new QomAdapter(newModel, getConstraintAdapter());
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
        JsonObject result = new JsonObject();
        result.add("source", serializer.serialize(getSource()));
        result.add("constraint", serializer.serialize(getConstraintAdapter()));
        result.add("orderings", serializer.serialize(getOrderings()));
        result.add("columns", serializer.serialize(getColumns()));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toSqlString() {
        try {
            String result = QOMFormatter.format(this.getModel());
            return ESCAPED_PATH.matcher(result).replaceAll("'$1'");
        } catch (RepositoryException e) {
            log.error("Could not convert to a string", e);
        }
        return StringUtils.EMPTY;
    }

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Creates a new {@link QomAdapter} instance based upon the given query object model with the use of the
     * given factory context
     * @param original Original {@link QueryObjectModel} instance
     * @param context  {@link QomAdapterContext} used for creating the adapters tree
     * @return New {@code QomAdapter} instance
     */
    public static QomAdapter from(QueryObjectModel original, QomAdapterContext context) throws RepositoryException {
        ConstraintAdapter newConstraintAdapter = ConstraintAdapter.from(original.getConstraint(), context);
        QomAdapter result = new QomAdapter(original, newConstraintAdapter);
        if (context.isChanged()) {
            return from(result.buildWith(context.getModelFactory()).getModel(), QomAdapterContext.from(context));
        }
        return result;
    }
}

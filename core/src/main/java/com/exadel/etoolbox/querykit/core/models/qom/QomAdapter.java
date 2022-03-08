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
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Source;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
public class QomAdapter implements ParsedQueryInfo, JsonExportable {

    private static final Pattern ESCAPED_PATH = Pattern.compile("\\[(/[^]]+)]");

    private final transient QueryObjectModel original;

    @Getter
    private final ConstraintAdapter constraintAdapter;

    public Source getSource() {
        return original.getSource();
    }

    public Column[] getColumns() {
        return original.getColumns();
    }

    public Ordering[] getOrderings() {
        return original.getOrderings();
    }

    public QueryObjectModel getModel() {
        return original;
    }

    public QomAdapter buildWith(QueryObjectModelFactory factory) throws RepositoryException {
        return buildWith(factory, null);
    }

    public QomAdapter buildWith(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException {
        QueryObjectModel newModel = factory.createQuery(
                getSource(),
                constraintAdapter != null ? constraintAdapter.getConstraint(factory, arguments) : null,
                getOrderings(),
                getColumns());
        return new QomAdapter(newModel, getConstraintAdapter());
    }

    @Override
    public String toJson() {
        return JsonExportUtil.export(this);
    }

    @Override
    public JsonElement toJson(JsonSerializationContext serializer) {
        JsonObject result = new JsonObject();
        result.add("source", serializer.serialize(getSource()));
        result.add("constraint", serializer.serialize(getConstraintAdapter()));
        result.add("orderings", serializer.serialize(getOrderings()));
        result.add("columns", serializer.serialize(getColumns()));
        return result;
    }

    public String toSqlString() {
        try {
            String result = QOMFormatter.format(this.getModel());
            return ESCAPED_PATH.matcher(result).replaceAll("'$1'");
        } catch (RepositoryException e) {
            log.error("Could not convert to a string", e);
        }
        return StringUtils.EMPTY;
    }

    public static QomAdapter from(QueryObjectModel original, QomAdapterContext context) throws RepositoryException {
        ConstraintAdapter newConstraintAdapter = ConstraintAdapter.from(original.getConstraint(), context);
        QomAdapter result = new QomAdapter(original, newConstraintAdapter);
        if (context.isChanged()) {
            return from(result.buildWith(context.getModelFactory()).getModel(), QomAdapterContext.from(context));
        }
        return result;
    }
}

package com.exadel.etoolbox.querykit.core.models.qom;

import com.exadel.etoolbox.querykit.core.models.qom.constraints.ConstraintAdapter;
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

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
public class QomAdapter implements JsonExportable {

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

    public String toJson() {
        return JsonExportUtil.export(this);
    }

    public String toFormattedString() {
        try {
            return QOMFormatter.format(this.getModel());
        } catch (RepositoryException e) {
            log.error("Could not convert to a string", e);
        }
        return StringUtils.EMPTY;
    }

    public static QomAdapter from(QueryObjectModel original, QomAdapterContext context) {
        return new QomAdapter(
                original,
                ConstraintAdapter.from(original.getConstraint(), context));
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
}

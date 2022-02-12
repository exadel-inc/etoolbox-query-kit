package com.exadel.etoolbox.querykit.core.models.qom;

import com.exadel.etoolbox.querykit.core.models.qom.constraints.ConstraintAdapter;
import com.exadel.etoolbox.querykit.core.models.serialization.QomSerializationHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Source;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class QomAdapter {

    private final transient QueryObjectModel original;

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

    public QueryObjectModel getModel(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException {
        if ( MapUtils.isEmpty(arguments)) {
            return original;
        }
        return factory.createQuery(
                getSource(),
                constraintAdapter.getConstraint(factory, arguments),
                getOrderings(),
                getColumns());
    }

    public String toJson() {
        return QomSerializationHelper.serialize(this);
    }

    public static QomAdapter from(QueryObjectModel original) {
        return new QomAdapter(
                original,
                ConstraintAdapter.from(original.getConstraint()));
    }
}

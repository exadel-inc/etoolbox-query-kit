package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterContext;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Not;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Map;
import java.util.function.Predicate;

public class NotAdapter extends ConstraintAdapter {

    private final ConstraintAdapter constraint;

    NotAdapter(Not original, QomAdapterContext context) {
        super(original, "NOT");
        constraint = ConstraintAdapter.from(original.getConstraint(), context);
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException {
        return constraint.getConstraint(factory, arguments);
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return context -> !constraint.getPredicate().test(context);
    }
}

package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterContext;
import lombok.Getter;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Or;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Map;
import java.util.function.Predicate;

@Getter
public class OrAdapter extends ConstraintAdapter implements JunctionConstraint {

    private final ConstraintAdapter constraint1;
    private final ConstraintAdapter constraint2;

    OrAdapter(Or original, QomAdapterContext context) {
        super(original, "OR");
        constraint1 = ConstraintAdapter.from(original.getConstraint1(), context);
        constraint2 = ConstraintAdapter.from(original.getConstraint2(), context);
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException {
        return factory.or(
                constraint1.getConstraint(factory, arguments),
                constraint2.getConstraint(factory, arguments));
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return constraint1.getPredicate().or(constraint2.getPredicate());
    }
}

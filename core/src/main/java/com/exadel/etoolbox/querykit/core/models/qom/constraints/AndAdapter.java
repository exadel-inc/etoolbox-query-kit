package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import lombok.Getter;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.And;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Map;
import java.util.function.Predicate;

@Getter
public class AndAdapter extends ConstraintAdapter implements JunctionConstraint {

    private final ConstraintAdapter constraint1;
    private final ConstraintAdapter constraint2;

    AndAdapter(And original) {
        super(original, "AND");
        constraint1 = from(original.getConstraint1());
        constraint2 = from(original.getConstraint2());
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException {
        return factory.and(
                constraint1.getConstraint(factory, arguments),
                constraint2.getConstraint(factory, arguments));
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return constraint1.getPredicate().and(constraint2.getPredicate());
    }
}

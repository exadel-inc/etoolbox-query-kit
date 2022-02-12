package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import lombok.Getter;

import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.PropertyExistence;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Map;
import java.util.function.Predicate;

@Getter
public class PropertyExistenceAdapter extends ConstraintAdapter {

    private final String selector;
    private final String property;

    PropertyExistenceAdapter(PropertyExistence original) {
        super(original, "EXIST");
        selector = original.getSelectorName();
        property = original.getPropertyName();
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) {
        // No interpolation required
        return getConstraint();
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return context -> context.getProperty(selector, property) != null;
    }
}

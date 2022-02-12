package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.InterpolationHelper;

import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.SameNode;
import java.util.Map;
import java.util.function.Predicate;

public class SameNodeAdapter extends ConstraintAdapter {

    private final String selector;
    private final String path;

    SameNodeAdapter(SameNode original) {
        super(original, "ISSAMENODE");
        selector = original.getSelectorName();
        path = original.getPath();
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) {
        return InterpolationHelper.interpolate(
                this,
                arguments,
                factory,
                () -> path,
                (qomFactory, str) -> qomFactory.sameNode(selector, str));
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return context -> context.getPath(selector).equals(path);
    }
}

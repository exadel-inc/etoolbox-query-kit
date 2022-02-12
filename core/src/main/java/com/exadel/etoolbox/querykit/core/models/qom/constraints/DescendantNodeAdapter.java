package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.InterpolationHelper;

import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DescendantNode;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Map;
import java.util.function.Predicate;

public class DescendantNodeAdapter extends ConstraintAdapter {

    private final String selector;
    private final String path;

    DescendantNodeAdapter(DescendantNode original) {
        super(original, "ISDESCENDANTNODE");
        selector = original.getSelectorName();
        path = original.getAncestorPath();
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) {
        return InterpolationHelper.interpolate(
                this,
                arguments,
                factory,
                () -> path,
                (qomFactory, str) -> qomFactory.descendantNode(selector, str));
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return context -> context.getPath(selector).startsWith(path + "/");
    }
}

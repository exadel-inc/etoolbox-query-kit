package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.InterpolationHelper;

import javax.jcr.query.qom.ChildNode;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Map;
import java.util.function.Predicate;

public class ChildNodeAdapter extends ConstraintAdapter {

    private final String selector;
    private final String path;

    ChildNodeAdapter(ChildNode original) {
        super(original, "ISCHILDNODE");
        selector = original.getSelectorName();
        path = original.getParentPath();
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) {
        return InterpolationHelper.interpolate(
                this,
                arguments,
                factory,
                () -> path,
                (qomFactory, str) -> qomFactory.childNode(selector, str));
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return context -> context.getPath(selector).equals(path + "/" + context.getResource(selector).getName());
    }
}

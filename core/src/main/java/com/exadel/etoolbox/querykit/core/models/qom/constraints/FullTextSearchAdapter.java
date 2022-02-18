package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.ConstraintHelper;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.InterpolationHelper;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.PredicateHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.commons.query.qom.Operator;
import org.apache.jackrabbit.value.StringValue;

import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.FullTextSearch;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.StaticOperand;
import java.util.Map;
import java.util.function.Predicate;

@Slf4j
public class FullTextSearchAdapter extends ConstraintAdapter {

    private final String selector;
    private final String property;
    private final StaticOperand expression;

    FullTextSearchAdapter(FullTextSearch original) {
        super(original, "TEXT");
        selector = original.getSelectorName();
        property = original.getPropertyName();
        expression = original.getFullTextSearchExpression();
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) {
        String value = ConstraintHelper.getLiteralValue(expression);
        return InterpolationHelper.interpolate(
                this,
                arguments,
                factory,
                () -> value,
                (qomFactory, str) -> qomFactory.fullTextSearch(selector, property, factory.literal(new StringValue(str))));
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return context -> PredicateHelper.compare(context, selector, property, Operator.LIKE.toString(), expression);
    }
}

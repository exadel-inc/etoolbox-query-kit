package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.InterpolationHelper;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.OperandHelper;
import com.exadel.etoolbox.querykit.core.models.qom.operands.DynamicOperandAdapter;
import org.apache.jackrabbit.value.StringValue;

import javax.jcr.query.qom.And;
import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Or;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.StaticOperand;
import java.util.Map;
import java.util.function.Predicate;

public class ComparisonAdapter extends ConstraintAdapter {

    private final DynamicOperandAdapter operand1;
    private final String operator;
    private final StaticOperand operand2;

    ComparisonAdapter(Comparison original) {
        super(original, "COMPARE");
        operand1 = DynamicOperandAdapter.from(original.getOperand1());
        operator = original.getOperator();
        operand2 = original.getOperand2();
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) {
        String value = InterpolationHelper.getLiteralValue(operand2);
        return InterpolationHelper.interpolate(
                this,
                arguments,
                factory,
                () -> value,
                (qomFactory, str) -> qomFactory.comparison(operand1.getOperand(), operator, factory.literal(new StringValue(str))),
                QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO.equals(operator) ? Or.class : And.class);
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return context -> OperandHelper.compare(context, operand1, operator, operand2);
    }
}

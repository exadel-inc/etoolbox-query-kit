package com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.exadel.etoolbox.querykit.core.models.qom.operands.DynamicOperandAdapter;
import com.exadel.etoolbox.querykit.core.utils.ValueUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.query.qom.Operator;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.BindVariableValue;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.StaticOperand;
import java.util.Calendar;
import java.util.Map;

import static org.apache.jackrabbit.commons.query.qom.Operator.EQ;
import static org.apache.jackrabbit.commons.query.qom.Operator.LIKE;

@UtilityClass
@Slf4j
public class OperandHelper {

    private static final String WILDCARD = "%";

    public static boolean compare(
            EvaluationContext context,
            DynamicOperandAdapter left,
            String operator,
            StaticOperand right) {

        Object leftValue = left.getValue(context);
        return compare(leftValue, operator, right, context.getBindVariables());
    }

    public static boolean compare(
            EvaluationContext context,
            String selector,
            String property,
            String operator,
            StaticOperand right) {

        Object leftValue = context.getProperty(selector, property);
        if (leftValue == null) {
            return false;
        }
        return compare(leftValue, operator, right, context.getBindVariables());
    }

    public static boolean compare(
            Object leftValue,
            String operator,
            StaticOperand right,
            Map<String, Object> bindVariables) {

        Object rightValue = extractValue(right, bindVariables);
        Class<?> castType = guessType(rightValue);
        if (rightValue == null) {
            return false;
        }
        Operator operatorObject = getOperator(operator);
        if (boolean.class.equals(castType)) {
            return compare((boolean) leftValue, operatorObject, (boolean) rightValue);
        }
        if (Calendar.class.equals(castType)) {
            return compare((Calendar) leftValue, operatorObject, (Calendar) rightValue);
        }
        if (long.class.equals(castType) || double.class.equals(castType)) {
            return compare((double) leftValue, operatorObject, (double) rightValue);
        }
        if (String.class.equals(castType)) {
            return compare(leftValue.toString(), operatorObject, rightValue.toString());
        }
        return false;
    }

    private boolean compare(boolean left, Operator operator, boolean right) {
        if (operator == EQ) {
            return left == right;
        }
        return left != right;
    }

    private boolean compare(Calendar left, Operator operator, Calendar right) {
        switch (operator) {
            case EQ:
                return left.equals(right);
            case LT:
                return left.before(right);
            case LE:
                return left.before(right) || left.equals(right);
            case GT:
                return left.after(right);
            case GE:
                return left.after(right) || left.equals(right);
            case NE:
                return !left.equals(right);
            default:
                return false;
        }
    }

    private boolean compare(double left, Operator operator, double right) {
        switch (operator) {
            case EQ:
                return left == right;
            case LT:
                return left < right;
            case LE:
                return left <= right;
            case GT:
                return left > right;
            case GE:
                return left >= right;
            case NE:
                return left != right;
            default:
                return false;
        }
    }

    private boolean compare(String left, Operator operator, String right) {
        if (operator == EQ) {
            return left.equals(right);
        }
        if (operator != LIKE) {
            return false;
        }
        if (right.startsWith(WILDCARD) && right.endsWith(WILDCARD)) {
            return left.contains(StringUtils.strip(right, WILDCARD));
        }
        if (right.startsWith(WILDCARD)) {
            return left.endsWith(StringUtils.strip(right, WILDCARD));
        }
        if (right.endsWith(WILDCARD)) {
            return left.startsWith(StringUtils.strip(right, WILDCARD));
        }
        return left.equals(right);
    }

    private static Object extractValue(StaticOperand operand, Map<String, Object> bindVariables) {
        if (operand instanceof Literal) {
            try {
                return ValueUtil.getValue(((Literal) operand).getLiteralValue());
            } catch (RepositoryException e) {
                log.error("Could not extract value", e);
                return null;
            }
        } else if (operand instanceof BindVariableValue && MapUtils.isNotEmpty(bindVariables)) {
            String variableName = ((BindVariableValue) operand).getBindVariableName();
            return bindVariables.get(variableName);
        }
        return null;
    }

    private static Class<?> guessType(Object value) {
        if (value == null) {
            return null;
        }
        if (ClassUtils.isAssignable(value.getClass(), Boolean.class)) {
            return boolean.class;
        }
        if (ClassUtils.isAssignable(value.getClass(), Calendar.class)) {
            return Calendar.class;
        }
        if (ClassUtils.isAssignable(value.getClass(), Long.class)) {
            return long.class;
        }
        if (ClassUtils.isAssignable(value.getClass(), Double.class)) {
            return double.class;
        }
        if (ClassUtils.isAssignable(value.getClass(), String.class)) {
            return String.class;
        }
        return null;
    }

    private static Operator getOperator(String name) {
        try {
            return Operator.getOperatorByName(name);
        } catch (RepositoryException e) {
            log.error("Could not recognize operator '{}'", name, e);
            return EQ;
        }
    }
}

/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.exadel.etoolbox.querykit.core.models.qom.operands.DynamicOperandAdapter;
import com.exadel.etoolbox.querykit.core.utils.ValueUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.query.qom.Operator;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.query.qom.BindVariableValue;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.StaticOperand;
import java.util.Calendar;
import java.util.Map;

import static org.apache.jackrabbit.commons.query.qom.Operator.EQ;
import static org.apache.jackrabbit.commons.query.qom.Operator.LIKE;

/**
 * Contains utility methods for composing JCR node predicates out of constraint adapters
 * <p><u>Note</u>: this class is not a part of a public API</p>
 */
@UtilityClass
@Slf4j
public class PredicateHelper {

    private static final String WILDCARD = "%";

    /**
     * Compares the two operands in the given evaluation context with the provided operator. Returns a boolean value
     * manifesting the result of the comparison
     * @param context  {@link EvaluationContext} instance
     * @param left     Left operand
     * @param operator A string representing the operator in "real-world" notation
     * @param right    Right operand
     * @return True or false
     */
    public static boolean compare(
            EvaluationContext context,
            DynamicOperandAdapter left,
            String operator,
            StaticOperand right) {

        Object leftValue = left.getValue(context);
        return compare(leftValue, operator, right, context.getBindVariables());
    }

    /**
     * Compares a property selector against a static operand in the given evaluation context with the provided operator.
     * Returns a boolean value manifesting the result of the comparison
     * @param context  {@link EvaluationContext} instance
     * @param selector Property selector
     * @param operator A string representing the operator in "real-world" notation
     * @param right    Right (static) operand
     * @return True or false
     */
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

    /**
     * Compares an arbitrary value a static operand in the given evaluation context with the provided operator. Returns
     * a boolean value manifesting the result of the comparison
     * @param leftValue     Left (arbitrary) operand
     * @param operator      A string representing the operator in "real-world" notation
     * @param right         Right (static) operand
     * @param bindVariables Query's bind variables used to compose the static operand
     * @return True or false
     */
    public static boolean compare(
            Object leftValue,
            String operator,
            StaticOperand right,
            Map<String, Object> bindVariables) {

        Object rightValue = extractValue(right, bindVariables);
        if (rightValue == null) {
            return false;
        }
        int castType = ValueUtil.detectType(rightValue);
        Operator operatorObject = getOperator(operator);
        if (castType == PropertyType.BOOLEAN) {
            return compare((boolean) leftValue, operatorObject, (boolean) rightValue);
        }
        if (castType == PropertyType.DATE) {
            return compare((Calendar) leftValue, operatorObject, (Calendar) rightValue);
        }
        if (castType == PropertyType.LONG || castType == PropertyType.DOUBLE) {
            return compare((double) leftValue, operatorObject, (double) rightValue);
        }
        if (castType == PropertyType.STRING) {
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
                return ValueUtil.extractValue(((Literal) operand).getLiteralValue());
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

    private static Operator getOperator(String name) {
        try {
            return Operator.getOperatorByName(name);
        } catch (RepositoryException e) {
            log.error("Could not recognize operator '{}'", name, e);
            return EQ;
        }
    }
}

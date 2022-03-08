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
package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.ConstraintHelper;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.InterpolationHelper;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.OperandHelper;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.PredicateHelper;
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
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ComparisonAdapter extends ConstraintAdapter implements LiteralHolder {

    private final DynamicOperandAdapter operand1;
    private final String operator;
    private final StaticOperand operand2;

    public ComparisonAdapter(Comparison original) {
        super(original, "COMPARE");
        operand1 = DynamicOperandAdapter.from(original.getOperand1());
        operator = original.getOperator();
        operand2 = original.getOperand2();
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) {
        String value = OperandHelper.getLiteralValue(operand2);
        return InterpolationHelper.interpolate(
                this,
                arguments,
                factory,
                () -> value,
                (qomFactory, str) -> qomFactory.comparison(operand1.getOperand(), operator, factory.literal(new StringValue(str))),
                useDisjunctionWithValueArray(operator) ? Or.class : And.class);
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return context -> PredicateHelper.compare(context, operand1, operator, operand2);
    }

    private static boolean useDisjunctionWithValueArray(String operator) {
        return QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO.equals(operator)
                || QueryObjectModelConstants.JCR_OPERATOR_LIKE.equals(operator);
    }

    @Override
    public String getLiteralValue() {
        return OperandHelper.getLiteralValue(operand2);
    }
}

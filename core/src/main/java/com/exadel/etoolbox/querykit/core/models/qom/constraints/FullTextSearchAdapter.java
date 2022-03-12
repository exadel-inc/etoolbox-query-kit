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
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.InterpolationHelper;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.OperandHelper;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.PredicateHelper;
import org.apache.jackrabbit.commons.query.qom.Operator;
import org.apache.jackrabbit.value.StringValue;

import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.FullTextSearch;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.StaticOperand;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Represents a {@link ConstraintAdapter} for the full-text search functions
 */
public class FullTextSearchAdapter extends ConstraintAdapter implements LiteralHolder {

    private final String selector;
    private final String property;
    private final StaticOperand expression;

    /**
     * Creates a new constraint adapter instance
     * @param original Original {@link Constraint} instance
     */
    FullTextSearchAdapter(FullTextSearch original) {
        super(original, "TEXT");
        selector = original.getSelectorName();
        property = original.getPropertyName();
        expression = original.getFullTextSearchExpression();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) {
        String value = OperandHelper.getLiteralValue(expression);
        return InterpolationHelper.interpolate(
                this,
                arguments,
                factory,
                () -> value,
                (qomFactory, str) -> qomFactory.fullTextSearch(selector, property, factory.literal(new StringValue(str))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return context -> PredicateHelper.compare(context, selector, property, Operator.LIKE.toString(), expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLiteralValue() {
        return OperandHelper.getLiteralValue(expression);
    }
}

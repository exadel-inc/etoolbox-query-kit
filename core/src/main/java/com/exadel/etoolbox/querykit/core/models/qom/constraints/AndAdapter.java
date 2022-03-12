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
import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterContext;
import lombok.Getter;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.And;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a {@link ConstraintAdapter} for the logical {@code And} operator
 */
@Getter
public class AndAdapter extends ConstraintAdapter implements JunctionConstraint {

    private final ConstraintAdapter constraint1;
    private final ConstraintAdapter constraint2;

    /**
     * Creates a new constraint adapter instance
     * @param original Original {@link Constraint} instance
     * @param context  {@link QomAdapterContext} used for creating the adapters tree
     */
    AndAdapter(And original, QomAdapterContext context) {
        super(original, "AND");
        constraint1 = from(original.getConstraint1(), context);
        constraint2 = from(original.getConstraint2(), context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException {
        return factory.and(
                constraint1.getConstraint(factory, arguments),
                constraint2.getConstraint(factory, arguments));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return constraint1.getPredicate().and(constraint2.getPredicate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Consumer<ConstraintAdapter> consumer) {
        consumer.accept(this);
        constraint1.visit(consumer);
        constraint2.visit(consumer);
    }
}

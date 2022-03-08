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
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Or;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public class OrAdapter extends ConstraintAdapter implements JunctionConstraint {

    private final ConstraintAdapter constraint1;
    private final ConstraintAdapter constraint2;

    OrAdapter(Or original, QomAdapterContext context) {
        super(original, "OR");
        constraint1 = ConstraintAdapter.from(original.getConstraint1(), context);
        constraint2 = ConstraintAdapter.from(original.getConstraint2(), context);
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException {
        return factory.or(
                constraint1.getConstraint(factory, arguments),
                constraint2.getConstraint(factory, arguments));
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return constraint1.getPredicate().or(constraint2.getPredicate());
    }

    @Override
    public void visit(Consumer<ConstraintAdapter> consumer) {
        consumer.accept(this);
        constraint1.visit(consumer);
        constraint2.visit(consumer);
    }
}

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

import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.SameNode;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Represents a {@link ConstraintAdapter} for the {@code ISSAMENODE} function
 */
public class SameNodeAdapter extends ConstraintAdapter implements LiteralHolder {

    private final String selector;
    private final String path;

    /**
     * Creates a new constraint adapter instance
     * @param original Original {@link Constraint} instance
     */
    SameNodeAdapter(SameNode original) {
        super(original, "ISSAMENODE");
        selector = original.getSelectorName();
        path = original.getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) {
        return InterpolationHelper.interpolate(
                this,
                arguments,
                factory,
                () -> path,
                (qomFactory, str) -> qomFactory.sameNode(selector, str));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return context -> context.getPath(selector).equals(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLiteralValue() {
        return path;
    }
}

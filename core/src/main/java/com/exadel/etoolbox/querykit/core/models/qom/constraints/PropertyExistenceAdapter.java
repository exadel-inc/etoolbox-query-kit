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
import lombok.Getter;

import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.PropertyExistence;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Map;
import java.util.function.Predicate;

@Getter
public class PropertyExistenceAdapter extends ConstraintAdapter {

    private final String selector;
    private final String property;

    PropertyExistenceAdapter(PropertyExistence original) {
        super(original, "EXIST");
        selector = original.getSelectorName();
        property = original.getPropertyName();
    }

    @Override
    public Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) {
        // No interpolation required
        return getConstraint();
    }

    @Override
    public Predicate<EvaluationContext> getPredicate() {
        return context -> context.getProperty(selector, property) != null;
    }
}

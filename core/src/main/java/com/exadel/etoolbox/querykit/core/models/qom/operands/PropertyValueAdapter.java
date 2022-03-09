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
package com.exadel.etoolbox.querykit.core.models.qom.operands;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;

import javax.jcr.query.qom.PropertyValue;

/**
 * Represents a {@link DynamicOperandAdapter} for retrieving the value of a property
 */
public class PropertyValueAdapter extends DynamicOperandAdapter {

    private final String selector;
    private final String property;

    /**
     * Creates a new dynamic operand adapter instance
     * @param original Original {@code DynamicOperand} object
     */
    PropertyValueAdapter(PropertyValue original) {
        super(original,null);
        selector = original.getSelectorName();
        property = original.getPropertyName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(EvaluationContext context) {
        if (!context.hasResource(selector)) {
            return null;
        }
        return context.getResource(selector).getValueMap().get(property);
    }
}

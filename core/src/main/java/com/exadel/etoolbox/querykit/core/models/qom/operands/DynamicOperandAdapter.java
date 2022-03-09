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
import com.exadel.etoolbox.querykit.core.models.qom.constraints.ConstraintAdapter;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;

import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.Length;
import javax.jcr.query.qom.LowerCase;
import javax.jcr.query.qom.NodeLocalName;
import javax.jcr.query.qom.NodeName;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.UpperCase;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents an adapter for a dynamic operand within a query object model. This one has additional features to comply
 * with {@link com.exadel.etoolbox.querykit.core.models.qom.constraints.ConstraintAdapter} possibilities, e.g., the ability
 * to be used in JCR node predicates and the augmented visual/JSON presentation
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class DynamicOperandAdapter {

    private static final Map<Class<? extends DynamicOperand>, Function<DynamicOperand, DynamicOperandAdapter>> OPERANDS =
            ImmutableMap.<Class<? extends DynamicOperand>, Function<DynamicOperand, DynamicOperandAdapter>>builder()
            .put(
                    Length.class,
                    original -> new LengthAdapter((Length) original))
            .put(
                    LowerCase.class,
                    original -> new LowerCaseAdapter((LowerCase) original))
            .put(
                    UpperCase.class,
                    original -> new UpperCaseAdapter((UpperCase) original))
            .put(
                    NodeName.class,
                    original -> new NodeNameAdapter((NodeName) original))
            .put(
                    NodeLocalName.class,
                    original -> new NodeLocalNameAdapter((NodeLocalName) original))
            .put(
                    PropertyValue.class,
                    original -> new PropertyValueAdapter((PropertyValue) original))
            .build();

    private final transient DynamicOperand original;

    /**
     * Retrieves the type of the current operand
     */
    @Getter
    private final String type;

    /**
     * Retrieves the dynamic operand used as the source for the current instance
     * @return {@code DynamicOperand} object
     */
    public DynamicOperand getOperand() {
        return original;
    }

    /**
     * Retrieves a value this operand produces in the given context
     * @param context {@link EvaluationContext} instance
     * @return An arbitrary (nullable) object
     */
    public abstract Object getValue(EvaluationContext context);

    /**
     * Creates a new {@link DynamicOperandAdapter} instance based upon the given original operand
     * @param original {@code DynamicOperand} instance
     * @return {@code DynamicOperandAdapter} instance, or null in case of invalid arguments or creation failure
     */
    public static DynamicOperandAdapter from(DynamicOperand original) {
        return OPERANDS
                .keySet()
                .stream()
                .filter(key -> ClassUtils.isAssignable(original.getClass(), key))
                .findFirst()
                .map(OPERANDS::get)
                .map(supplier -> supplier.apply(original))
                .orElse(null);
    }
}

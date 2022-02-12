package com.exadel.etoolbox.querykit.core.models.qom.operands;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
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

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class DynamicOperandAdapter {

    private static final Map<Class<? extends DynamicOperand>, Function<DynamicOperand, DynamicOperandAdapter>> OPERANDS = ImmutableMap
            .<Class<? extends DynamicOperand>, Function<DynamicOperand, DynamicOperandAdapter>>builder()
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
    private final String type;

    public DynamicOperand getOperand() {
        return original;
    }

    public abstract Object getValue(EvaluationContext context);

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

package com.exadel.etoolbox.querykit.core.models.qom.operands;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;

import javax.jcr.query.qom.Length;

public class LengthAdapter extends DynamicOperandAdapter {

    private final String selector;
    private final String property;

    LengthAdapter(Length original) {
        super(original,"LENGTH");
        selector = original.getPropertyValue().getSelectorName();
        property = original.getPropertyValue().getPropertyName();
    }

    @Override
    public Object getValue(EvaluationContext context) {
        if (!context.hasResource(selector)) {
            return null;
        }
        Object propertyValue = context.getProperty(selector, property);
        if (propertyValue == null) {
            return null;
        }
        return propertyValue.toString().length();
    }
}

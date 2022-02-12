package com.exadel.etoolbox.querykit.core.models.qom.operands;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;

import javax.jcr.query.qom.PropertyValue;

public class PropertyValueAdapter extends DynamicOperandAdapter {

    private final String selector;
    private final String property;

    PropertyValueAdapter(PropertyValue original) {
        super(original,null);
        selector = original.getSelectorName();
        property = original.getPropertyName();
    }

    @Override
    public Object getValue(EvaluationContext context) {
        if (!context.hasResource(selector)) {
            return null;
        }
        return context.getResource(selector).getValueMap().get(property);
    }
}

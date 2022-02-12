package com.exadel.etoolbox.querykit.core.models.qom.operands;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;

import javax.jcr.query.qom.NodeLocalName;

public class NodeLocalNameAdapter extends DynamicOperandAdapter {

    private final String selector;

    NodeLocalNameAdapter(NodeLocalName original) {
        super(original,"LOCAL_NAME");
        selector = original.getSelectorName();
    }

    @Override
    public Object getValue(EvaluationContext context) {
        if (!context.hasResource(selector)) {
            return false;
        }
        return context.getResource(selector).getName();
    }
}

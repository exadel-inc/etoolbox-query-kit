package com.exadel.etoolbox.querykit.core.models.qom.operands;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;

import javax.jcr.query.qom.NodeName;

public class NodeNameAdapter extends DynamicOperandAdapter {

    private final String selector;

    NodeNameAdapter(NodeName original) {
        super(original,"NAME");
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

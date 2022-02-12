package com.exadel.etoolbox.querykit.core.models.qom.operands;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;

import javax.jcr.query.qom.UpperCase;

public class UpperCaseAdapter extends DynamicOperandAdapter {

    private final DynamicOperandAdapter operand;

    UpperCaseAdapter(UpperCase original) {
        super(original,"UPPER");
        operand = from(original.getOperand());
    }

    @Override
    public Object getValue(EvaluationContext context) {
        Object result = operand.getValue(context);
        if (result == null) {
            return null;
        }
        return result.toString().toUpperCase();
    }
}

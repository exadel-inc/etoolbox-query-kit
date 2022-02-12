package com.exadel.etoolbox.querykit.core.models.qom.operands;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;

import javax.jcr.query.qom.LowerCase;

public class LowerCaseAdapter extends DynamicOperandAdapter {

    private final DynamicOperandAdapter operand;

    LowerCaseAdapter(LowerCase original) {
        super(original,"LOWER");
        operand = from(original.getOperand());
    }

    @Override
    public Object getValue(EvaluationContext context) {
        Object result = operand.getValue(context);
        if (result == null) {
            return null;
        }
        return result.toString().toLowerCase();
    }
}

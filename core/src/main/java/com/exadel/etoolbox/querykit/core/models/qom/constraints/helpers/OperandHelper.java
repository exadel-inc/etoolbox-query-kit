package com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.Operand;

@UtilityClass
@Slf4j
public class OperandHelper {

    public static String getLiteralValue(Operand operand) {
        if (!(operand instanceof Literal)) {
            return StringUtils.EMPTY;
        }
        try {
            return ((Literal) operand).getLiteralValue().getString();
        } catch (RepositoryException e) {
            log.error("Could not retrieve a value for the static operand", e);
        }
        return StringUtils.EMPTY;
    }
}

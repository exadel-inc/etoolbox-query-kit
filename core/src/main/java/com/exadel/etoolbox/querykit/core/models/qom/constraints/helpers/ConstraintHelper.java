package com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers;

import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterContext;
import com.exadel.etoolbox.querykit.core.models.syntax.WordModel;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.qom.And;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.Operand;
import javax.jcr.query.qom.Or;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.StaticOperand;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@UtilityClass
@Slf4j
public class ConstraintHelper {

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

    public static Constraint unmaskInFunction(
            DynamicOperand dynamicOperand,
            String maskedValue,
            QomAdapterContext context) throws RepositoryException {

        List<WordModel> arguments = new WordModel(maskedValue).extractBetween(Constants.OPENING_BRACKET, Constants.CLOSING_BRACKET).split(",");
        if (arguments.isEmpty()) {
            return null;
        }
        if (arguments.size() == 1) {
            StaticOperand newStaticOperand = getStaticOperand(arguments.get(0).toString(), context);
            return context.getModelFactory().comparison(
                    dynamicOperand,
                    QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
                    newStaticOperand);
        }

        List <Constraint> inVariants = new ArrayList<>();
        for (WordModel argument : arguments) {
            StaticOperand staticOperand = getStaticOperand(argument.toString(), context);
            Constraint inVariant = context.getModelFactory().comparison(
                    dynamicOperand,
                    QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
                    staticOperand);
            inVariants.add(inVariant);
        }

        return reduce(inVariants, Or.class, context.getModelFactory());
    }

    static Constraint reduce(
            List<Constraint> items,
            Class<?> reducerOperator,
            QueryObjectModelFactory factory) {

        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        Queue<Constraint> queue = new LinkedList<>(items);
        boolean isAndReduction = And.class.equals(reducerOperator);
        try {
            Constraint result = isAndReduction
                    ? factory.and(queue.remove(), queue.remove())
                    : factory.or(queue.remove(), queue.remove());
            while (!queue.isEmpty()) {
                result = isAndReduction
                        ? factory.and(result, queue.remove())
                        : factory.or(result, queue.remove());
            }
            return result;
        } catch (RepositoryException e) {
            log.error("Could not create constraint", e);
        }
        return null;
    }

    private static StaticOperand getStaticOperand(String value, QomAdapterContext context) throws RepositoryException {
        String adapted = value.replace(Constants.QUOTE_ESCAPED, Constants.QUOTE);
        adapted = StringUtils.strip(adapted, " '");
        Value literal = context.getValueFactory().createValue(adapted);
        return context.getModelFactory().literal(literal);
    }
}

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
package com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers;

import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterContext;
import com.exadel.etoolbox.querykit.core.models.syntax.WordModel;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.exadel.etoolbox.querykit.core.utils.EscapingUtil;
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

/**
 * Contains utility methods for handling constraints
 * <p><u>Note</u>: this class is not a part of a public API</p>
 */
@UtilityClass
@Slf4j
public class ConstraintHelper {

    /**
     * Unwraps the previously "masked" (escaped) {@code IN} function into a basic {@code Or}-connected  comparison
     * constraint
     * @param dynamicOperand {@link DynamicOperand} instance
     * @param maskedValue    The string to extract {@code IN} function from
     * @param context        {@link QomAdapterContext} used for creating the adapters tree
     * @return New {@code Constraint} instance
     * @throws RepositoryException If constraint creation fails
     */
    public static Constraint unmaskInFunction(
            DynamicOperand dynamicOperand,
            String maskedValue,
            QomAdapterContext context) throws RepositoryException {

        List<WordModel> arguments = new WordModel(maskedValue).extractBetween(Constants.OPENING_BRACKET, Constants.CLOSING_BRACKET).split(Constants.COMMA);
        if (arguments.isEmpty()) {
            return null;
        }
        if (arguments.size() == 1) {
            StaticOperand newStaticOperand = getStaticFunctionOperand(arguments.get(0).toString(), context);
            return context.getModelFactory().comparison(
                    dynamicOperand,
                    QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
                    newStaticOperand);
        }

        List<Constraint> inVariants = new ArrayList<>();
        for (WordModel argument : arguments) {
            StaticOperand staticOperand = getStaticFunctionOperand(argument.toString(), context);
            Constraint inVariant = context.getModelFactory().comparison(
                    dynamicOperand,
                    QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO,
                    staticOperand);
            inVariants.add(inVariant);
        }

        return reduce(inVariants, Or.class, context.getModelFactory());
    }

    /**
     * Converts (joins) a list of constraints into a basic {@code And}- or {@code Or}-connected constraint
     * @param items    {@code List} of constraints to join
     * @param operator {@code Class<?>} reference representing the logical operator to use
     * @param factory  {@link QueryObjectModelFactory} instance
     * @return New {@code Constraint} instance, or null if the data is invalid or constraint creation failed
     */
    static Constraint reduce(
            List<Constraint> items,
            Class<?> operator,
            QueryObjectModelFactory factory) {

        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        if (items.size() == 1) {
            return items.get(0);
        }
        Queue<Constraint> queue = new LinkedList<>(items);
        boolean isAndReduction = And.class.equals(operator);
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

    private static StaticOperand getStaticFunctionOperand(String value, QomAdapterContext context) throws RepositoryException {
        String adapted = EscapingUtil.unescape(value);
        adapted = StringUtils.strip(adapted, " '");
        Value literal = context.getValueFactory().createValue(adapted);
        return context.getModelFactory().literal(literal);
    }
}

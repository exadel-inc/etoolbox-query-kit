package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterContext;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.ConstraintHelper;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers.OperandHelper;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.exadel.etoolbox.querykit.core.utils.TryBiFunction;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.And;
import javax.jcr.query.qom.ChildNode;
import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DescendantNode;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.FullTextSearch;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.Not;
import javax.jcr.query.qom.Or;
import javax.jcr.query.qom.PropertyExistence;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.SameNode;
import javax.jcr.query.qom.StaticOperand;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
public abstract class ConstraintAdapter {

    private static final Pattern MASKED_FUNCTION = Pattern.compile("^\\$(\\w+)\\$");
    private static final String ERROR_MESSAGE = "Could not create an adapter for the constraint of type {}";

    private static final Map<Class<? extends Constraint>, TryBiFunction<Constraint, QomAdapterContext, ConstraintAdapter>> CONSTRAINT_SUPPLIERS = ImmutableMap
            .<Class<? extends Constraint>, TryBiFunction<Constraint, QomAdapterContext, ConstraintAdapter>>builder()
            .put(
                    And.class,
                    (original, context) -> new AndAdapter((And) original, context))
            .put(
                    ChildNode.class,
                    (original, context) -> new ChildNodeAdapter((ChildNode) original))
            .put(
                    Comparison.class,
                    (original, context) -> comparisonOrUnmaskedIn((Comparison) original, context))
            .put(
                    DescendantNode.class,
                    (original, context) -> new DescendantNodeAdapter((DescendantNode) original))
            .put(
                    FullTextSearch.class,
                    (original, context) -> new FullTextSearchAdapter((FullTextSearch) original))
            .put(
                    Not.class,
                    (original, context) -> new NotAdapter((Not) original, context))
            .put(
                    Or.class,
                    (original, context) -> new OrAdapter((Or) original, context))
            .put(
                    PropertyExistence.class,
                    (original, context) -> new PropertyExistenceAdapter((PropertyExistence) original))
            .put(
                    SameNode.class,
                    (original, context) -> new SameNodeAdapter((SameNode) original))
            .build();

    private final transient Constraint original;

    @Getter
    private final String type;

    public Constraint getConstraint() {
        return original;
    }

    public abstract Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException;

    public abstract Predicate<EvaluationContext> getPredicate();

    public void visit(Consumer<ConstraintAdapter> consumer) {
        consumer.accept(this);
    }

    public static ConstraintAdapter from(
            Constraint original,
            QomAdapterContext context) {
        if (original == null || context == null) {
            return null;
        }
        TryBiFunction<Constraint, QomAdapterContext, ConstraintAdapter> adapterSupplier = CONSTRAINT_SUPPLIERS
                .keySet()
                .stream()
                .filter(key -> ClassUtils.isAssignable(original.getClass(), key))
                .findFirst()
                .map(CONSTRAINT_SUPPLIERS::get)
                .orElse(null);
        if (adapterSupplier == null) {
            log.error(ERROR_MESSAGE, original.getClass());
            return null;
        }
        try {
            return adapterSupplier.apply(original, context);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, original.getClass(), e);
        }
        return null;
    }

    private static ConstraintAdapter comparisonOrUnmaskedIn(Comparison original, QomAdapterContext context) throws RepositoryException {
        DynamicOperand operand1 = original.getOperand1();
        StaticOperand operand2 = original.getOperand2();
        String stringLiteral = operand2 instanceof Literal
                ? OperandHelper.getLiteralValue(original.getOperand2())
                : StringUtils.EMPTY;

        Matcher matcher = MASKED_FUNCTION.matcher(stringLiteral);
        if (!matcher.find() || !matcher.group(1).equalsIgnoreCase(Constants.OPERATOR_IN)) {
            return new ComparisonAdapter(original);
        }

        Constraint convertedInConstraint = ConstraintHelper.unmaskInFunction(operand1, stringLiteral, context);
        if (convertedInConstraint == null) {
            return new ComparisonAdapter(original);
        }

        context.reportChange();
        ConstraintAdapter result = null;

        if (convertedInConstraint instanceof Or) {
            result = new OrAdapter((Or) convertedInConstraint, context);
        } else if (convertedInConstraint instanceof Comparison) {
            result = new ComparisonAdapter((Comparison) convertedInConstraint);
        }
        return result;
    }
}

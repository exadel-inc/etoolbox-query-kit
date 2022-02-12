package com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers;

import com.exadel.etoolbox.querykit.core.models.qom.constraints.ConstraintAdapter;
import com.exadel.etoolbox.querykit.core.utils.TryBiFunction;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.And;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.Operand;
import javax.jcr.query.qom.Or;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class InterpolationHelper {

    private static final Pattern INTERPOLATION_TEMPLATE = Pattern.compile("(?<!\\\\)\\$([a-z]\\w*)\\b");

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

    public static Constraint interpolate(
            ConstraintAdapter source,
            Map<String, Object> arguments,
            QueryObjectModelFactory qomFactory,
            Supplier<String> valueFactory,
            TryBiFunction<QueryObjectModelFactory, String, Constraint> constraintFactory) {

        return interpolate(source, arguments, qomFactory, valueFactory, constraintFactory, Or.class);
    }

    public static Constraint interpolate(
            ConstraintAdapter source,
            Map<String, Object> arguments,
            QueryObjectModelFactory qomFactory,
            Supplier<String> valueFactory,
            TryBiFunction<QueryObjectModelFactory, String, Constraint> constraintFactory,
            Class<?> reductionOperator) {

        String templatedString = valueFactory.get();
        if (StringUtils.isEmpty(templatedString) || !INTERPOLATION_TEMPLATE.matcher(templatedString).find()) {
            return source.getConstraint();
        }
        List<String> variants = InterpolationHelper.getVariants(templatedString, arguments);
        List<Constraint> elements = new ArrayList<>();
        for (String variant : variants) {
            Constraint newConstraint = null;
            try {
                newConstraint = constraintFactory.apply(qomFactory, variant);
            } catch (Exception e) {
                log.error("Could not create a constraint", e);
            }
            if (newConstraint != null) {
                elements.add(newConstraint);
            }
        }
        if (elements.isEmpty()) {
            return source.getConstraint();
        }
        return reduce(qomFactory, elements, reductionOperator);
    }

    private static List<String> getVariants(String value, Map<String, Object> arguments) {
        List<String> result = new ArrayList<>();
        result.add(value);
        Set<String> relevantKeys = new HashSet<>();
        Matcher matcher = INTERPOLATION_TEMPLATE.matcher(value);
        while (matcher.find()) {
            relevantKeys.add(matcher.group(1));
        }
        for (String key : relevantKeys) {
            expandVariants(result, key, arguments.get(key));
        }
        return result.stream().filter(variant -> !value.equals(variant)).collect(Collectors.toList());
    }

    private static void expandVariants(List<String> variants, String key, Object value) {
        if (value == null) {
            return;
        }
        String formattedKey = "$" + key;
        List<String> affectedStrings = variants.stream().filter(variant -> variant.contains(formattedKey)).collect(Collectors.toList());
        for (String affected : affectedStrings) {
            variants.remove(affected);
            if (!value.getClass().isArray()) {
                variants.add(affected.replace(formattedKey, value.toString()));
                continue;
            }
            for (Object val : (Object[]) value) {
                variants.add(affected.replace(formattedKey, val.toString()));
            }
        }
    }

    private static Constraint reduce(
            QueryObjectModelFactory factory,
            List<Constraint> items,
            Class<?> reducerOperator) {

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
}

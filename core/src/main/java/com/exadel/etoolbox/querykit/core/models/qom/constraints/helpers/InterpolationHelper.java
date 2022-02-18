package com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers;

import com.exadel.etoolbox.querykit.core.models.qom.constraints.ConstraintAdapter;
import com.exadel.etoolbox.querykit.core.utils.TryBiFunction;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Or;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class InterpolationHelper {

    private static final Pattern INTERPOLATION_TEMPLATE = Pattern.compile("(?<!\\w)\\$([a-z]\\w*)\\b");

    public static Constraint interpolate(
            ConstraintAdapter source,
            Map<String, Object> arguments,
            QueryObjectModelFactory qomFactory,
            Supplier<String> valueSupplier,
            TryBiFunction<QueryObjectModelFactory, String, Constraint> constraintSupplier) {

        return interpolate(source, arguments, qomFactory, valueSupplier, constraintSupplier, Or.class);
    }

    public static Constraint interpolate(
            ConstraintAdapter source,
            Map<String, Object> arguments,
            QueryObjectModelFactory qomFactory,
            Supplier<String> valueSupplier,
            TryBiFunction<QueryObjectModelFactory, String, Constraint> constraintSupplier,
            Class<?> reductionOperator) {

        String templatedString = valueSupplier.get();
        if (StringUtils.isEmpty(templatedString) || !INTERPOLATION_TEMPLATE.matcher(templatedString).find()) {
            return source.getConstraint();
        }
        List<String> variants = InterpolationHelper.getVariants(templatedString, arguments);
        List<Constraint> elements = new ArrayList<>();
        for (String variant : variants) {
            Constraint newConstraint = null;
            try {
                newConstraint = constraintSupplier.apply(qomFactory, variant);
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
        return ConstraintHelper.reduce(elements, reductionOperator, qomFactory);
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
            expandVariants(result, key, MapUtils.isNotEmpty(arguments) ? arguments.get(key) : null);
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
}

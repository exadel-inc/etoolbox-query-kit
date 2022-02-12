package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.models.qom.EvaluationContext;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ClassUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.And;
import javax.jcr.query.qom.ChildNode;
import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DescendantNode;
import javax.jcr.query.qom.FullTextSearch;
import javax.jcr.query.qom.Not;
import javax.jcr.query.qom.Or;
import javax.jcr.query.qom.PropertyExistence;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.SameNode;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class ConstraintAdapter {
    private static final Map<Class<? extends Constraint>, Function<Constraint, ConstraintAdapter>> CONSTRAINT_SUPPLIERS = ImmutableMap
            .<Class<? extends Constraint>, Function<Constraint, ConstraintAdapter>>builder()
            .put(
                    And.class,
                    original -> new AndAdapter((And) original))
            .put(
                    ChildNode.class,
                    original -> new ChildNodeAdapter((ChildNode) original))
            .put(
                    Comparison.class,
                    original -> new ComparisonAdapter((Comparison) original))
            .put(
                    DescendantNode.class,
                    original -> new DescendantNodeAdapter((DescendantNode) original))
            .put(
                    FullTextSearch.class,
                    original -> new FullTextSearchAdapter((FullTextSearch) original))
            .put(
                    Not.class,
                    original -> new NotAdapter((Not) original))
            .put(
                    Or.class,
                    original -> new OrAdapter((Or) original))
            .put(
                    PropertyExistence.class,
                    original -> new PropertyExistenceAdapter((PropertyExistence) original))
            .put(
                    SameNode.class,
                    original -> new SameNodeAdapter((SameNode) original))
            .build();

    private final transient Constraint original;

    @Getter
    private final String type;

    public Constraint getConstraint() {
        return original;
    }

    public abstract Constraint getConstraint(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException;

    public abstract Predicate<EvaluationContext> getPredicate();

    public static ConstraintAdapter from(Constraint original) {
        return CONSTRAINT_SUPPLIERS
                .keySet()
                .stream()
                .filter(key -> ClassUtils.isAssignable(original.getClass(), key))
                .findFirst()
                .map(CONSTRAINT_SUPPLIERS::get)
                .map(supplier -> supplier.apply(original))
                .orElse(null);
    }

}

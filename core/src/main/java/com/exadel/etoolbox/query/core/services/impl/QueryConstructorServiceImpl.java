package com.exadel.etoolbox.query.core.services.impl;

import com.exadel.etoolbox.query.core.services.QueryConstructorService;
import com.exadel.etoolbox.query.core.servlets.model.QueryConstructorModel;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.qom.*;
import java.util.List;
import java.util.Objects;

@Component(service = QueryConstructorService.class)
public class QueryConstructorServiceImpl implements QueryConstructorService {

    private static final String DEFAULT_SELECTOR_NAME = "s";
    private Session session;

    public String convertConstructorToSql2Query(ResourceResolver resourceResolver, QueryConstructorModel queryConstructorModel) {

        session = resourceResolver.adaptTo(Session.class);
        if (session == null) {
            return null;
        }

        try {
            QueryObjectModelFactory qomFactory = session.getWorkspace().getQueryManager().getQOMFactory();
            Source source = getSource(qomFactory, queryConstructorModel);
            Column[] columns = queryConstructorModel.getPropertyToColumn() == null ? null : getColumns(queryConstructorModel, qomFactory);

            Constraint constraint = buildConstraint(qomFactory, queryConstructorModel.getConstraints());

            Ordering[] orderings = queryConstructorModel.getPropertyToOrderType() == null ? null : getOrderings(queryConstructorModel, qomFactory);

            QueryObjectModel query = qomFactory.createQuery(source, constraint, orderings, columns);
            return query.getStatement();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Source getSource(QueryObjectModelFactory qomFactory, QueryConstructorModel queryConstructorModel) throws RepositoryException {
         return qomFactory.selector(queryConstructorModel.getNodeTypeName(), DEFAULT_SELECTOR_NAME);
    }

    private Column[] getColumns(QueryConstructorModel queryConstructorModel, QueryObjectModelFactory qomFactory) {
        return queryConstructorModel.getPropertyToColumn().entrySet().stream().map(entry -> {
            try {
                return qomFactory.column(DEFAULT_SELECTOR_NAME, entry.getKey(), entry.getValue());
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            return null;
        })
                .filter(Objects::nonNull)
                .toArray(Column[]::new);
    }

    private Constraint buildConstraint(QueryObjectModelFactory qomFactory, List<QueryConstructorModel.QueryConstructorConstraint> queryConstructorConstraints) throws RepositoryException {
        Constraint constraint = getConstraint(qomFactory, queryConstructorConstraints.get(0));
        for (int i = 1; i < queryConstructorConstraints.size(); i++) {
            constraint = addConstraint(constraint, qomFactory, queryConstructorConstraints.get(i));
        }
        return constraint;
    }

    private Constraint addConstraint(Constraint constraint, QueryObjectModelFactory qomFactory, QueryConstructorModel.QueryConstructorConstraint queryConstructorConstraint) throws RepositoryException {
        switch (queryConstructorConstraint.getConnector()) {
            case "and": return qomFactory.and(constraint, getConstraint(qomFactory, queryConstructorConstraint));
            case "or": return qomFactory.or(constraint, getConstraint(qomFactory, queryConstructorConstraint));
            default: return constraint;
        }
    }

    private Constraint getConstraint(QueryObjectModelFactory qomFactory, QueryConstructorModel.QueryConstructorConstraint queryConstructorConstraint) throws RepositoryException {
        switch (queryConstructorConstraint.getConstraintName()) {
            case "childNode": return qomFactory.childNode(DEFAULT_SELECTOR_NAME, queryConstructorConstraint.getPropertyName());
            case "sameNode": return qomFactory.sameNode(DEFAULT_SELECTOR_NAME, queryConstructorConstraint.getPropertyName());
            case "propertyExistence": return qomFactory.propertyExistence(DEFAULT_SELECTOR_NAME, queryConstructorConstraint.getPropertyName());
            case "descendantNode": return qomFactory.descendantNode(DEFAULT_SELECTOR_NAME, queryConstructorConstraint.getPropertyName());
            case "fullTextSearch": return qomFactory.fullTextSearch(DEFAULT_SELECTOR_NAME, queryConstructorConstraint.getPropertyName(), qomFactory.literal(session.getValueFactory().createValue(queryConstructorConstraint.getExpression())));
            case "name":
            case "localName":
            case "length":
            case "simpleSearch": {
                DynamicOperand dynamicOperand = getDynamicOperand(qomFactory, queryConstructorConstraint.getConstraintName(), queryConstructorConstraint.getPropertyName());
                StaticOperand staticOperand = qomFactory.literal(session.getValueFactory().createValue(queryConstructorConstraint.getExpression()));
                return qomFactory.comparison(dynamicOperand, queryConstructorConstraint.getOperator(), staticOperand);
            }
            case "not": {
                queryConstructorConstraint.setConstraintName("propertyExistence");
                return qomFactory.not(getConstraint(qomFactory, queryConstructorConstraint));
            }
            default: return null;
        }
    }

    private DynamicOperand getDynamicOperand(QueryObjectModelFactory qomFactory, String constraintName, String propertyName) throws RepositoryException {
        switch (constraintName) {
            case "name": return qomFactory.nodeName(DEFAULT_SELECTOR_NAME);
            case "localName": return qomFactory.nodeLocalName(DEFAULT_SELECTOR_NAME);
            case "simpleSearch": return qomFactory.propertyValue(DEFAULT_SELECTOR_NAME, propertyName);
            case "length": return qomFactory.length(qomFactory.propertyValue(DEFAULT_SELECTOR_NAME, propertyName));
            default: return null;
        }
    }

    private Ordering[] getOrderings(QueryConstructorModel queryConstructorModel, QueryObjectModelFactory qomFactory) {
        return queryConstructorModel.getPropertyToOrderType().entrySet().stream().map(entry -> {
            try {
                return entry
                        .getValue()
                        .equals("asc") ? qomFactory.ascending(getDynamicOperand(qomFactory, "simpleSearch", entry.getKey())) : qomFactory.descending(getDynamicOperand(qomFactory, "simpleSearch", entry.getKey()));
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            return null;
        })
                .filter(Objects::nonNull)
                .toArray(Ordering[]::new);
    }
}
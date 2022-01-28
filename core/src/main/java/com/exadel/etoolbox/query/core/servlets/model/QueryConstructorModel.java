package com.exadel.etoolbox.query.core.servlets.model;

import javax.jcr.query.qom.QueryObjectModelConstants;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QueryConstructorModel {
    private final Map<String, String> propertyToColumn;
    private final String fromType;
    private final String nodeTypeName;
    private final List<QueryConstructorConstraint> queryConstructorConstraints;
    private final Map<String, String> propertyToOrderType;

    public QueryConstructorModel(Map<String, String[]> parameterMap) {
        String[] propertyNames = parameterMap.get("propertyNameSelect");
        String[] columnNames = parameterMap.get("columnNameSelect");
        propertyToColumn = propertyNames == null ? null : IntStream.range(0, propertyNames.length)
                .filter(index -> propertyNames[index] != null && columnNames[index] != null)
                .boxed()
                .collect(Collectors.toMap(index -> propertyNames[index], index -> columnNames[index], (o1, o2) -> o1, LinkedHashMap::new));

        fromType = parameterMap.get("fromType")[0];
        nodeTypeName = parameterMap.get("nodeTypeNameFrom")[0];

        String[] constraintsConnectors = parameterMap.get("constraintsConnectors");
        String[] constraintsNames = parameterMap.get("constraint");
        String[] propertyNamesWhere = parameterMap.get("propertyNameWhere");
        String[] expressions = parameterMap.get("expressionWhere");
        String[] operators = parameterMap.get("operatorWhere");
        queryConstructorConstraints = IntStream.range(0, constraintsNames.length)
                .filter(index -> constraintsNames[index] != null && propertyNamesWhere[index] != null)
                .mapToObj(index -> new QueryConstructorConstraint(constraintsNames[index], propertyNamesWhere[index], expressions.length > index ? expressions[index] : null, constraintsConnectors.length > index ? constraintsConnectors[index] : null, operators[index]))
                .collect(Collectors.toList());

        String[] orderType = parameterMap.entrySet().stream().filter(entry -> entry.getKey().endsWith("orderType")).map(entry -> entry.getValue()[0]).toArray(String[]::new);
        String[] propertyNameOrder = parameterMap.entrySet().stream().filter(entry -> entry.getKey().endsWith("propertyNameOrder")).map(entry -> entry.getValue()[0]).toArray(String[]::new);
        propertyToOrderType = IntStream.range(0, orderType.length)
                .boxed()
                .collect(Collectors.toMap(index -> propertyNameOrder[index], index -> orderType[index], (o1, o2) -> o1, LinkedHashMap::new));
    }

    public static class QueryConstructorConstraint {
        private String constraintName;
        private final String propertyName;
        private final String expression;
        private final String connector;
        private final String operator;

        public QueryConstructorConstraint(String constraintName, String propertyName, String expression, String connector, String operator) {
            switch (operator) {
                case "null":
                    this.constraintName = "not";
                    break;
                case "notNull":
                    this.constraintName = "propertyExistence";
                    break;
                case "notEmpty":
                    operator = QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO;
                default:
                    this.constraintName = constraintName;
                    break;
            }
            this.propertyName = propertyName;
            this.expression = expression;
            this.connector = connector;
            this.operator = operator;
        }

        public void setConstraintName(String constraintName) {
            this.constraintName = constraintName;
        }

        public String getConstraintName() {
            return constraintName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getExpression() {
            return expression;
        }

        public String getConnector() {
            return connector;
        }

        public String getOperator() {
            return operator;
        }
    }

    public Map<String, String> getPropertyToColumn() {
        return propertyToColumn;
    }

    public String getFromType() {
        return fromType;
    }

    public String getNodeTypeName() {
        return nodeTypeName;
    }

    public List<QueryConstructorConstraint> getConstraints() {
        return queryConstructorConstraints;
    }

    public Map<String, String> getPropertyToOrderType() {
        return propertyToOrderType;
    }
}

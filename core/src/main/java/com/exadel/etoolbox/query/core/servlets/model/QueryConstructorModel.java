package com.exadel.etoolbox.query.core.servlets.model;

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

    }

    public static class QueryConstructorConstraint {
        private String constraintName;
        private final String propertyName;
        private final String expression;
        private final String connector;
        private final String operator;

        public QueryConstructorConstraint(String constraintName, String propertyName, String expression, String connector, String operator) {
            if (operator.equals("null")) {
                this.constraintName = "not";
            } else if (operator.equals("notNull")) {
                this.constraintName = "propertyExistence";
            } else {
                this.constraintName = constraintName;
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
}

package com.exadel.etoolbox.query.core.servlets.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QueryConstructorModel {
    private Map<String, String> propertyToColumn;
    private Map<String, String> constraintToField;
    private String from;
    private String nodeTypeName;
    private String[] buttongroup;

    public QueryConstructorModel(Map<String, String[]> parameterMap) {
        String[] properties = parameterMap.get("property");
        String[] columnName = parameterMap.get("columnName");
        propertyToColumn  = IntStream.range(0, properties.length)
                .filter(index -> properties[index] != null && columnName[index] != null)
                .boxed()
                .collect(Collectors.toMap(index -> properties[index], index -> columnName[index], (o1, o2) -> o1, LinkedHashMap::new));

        from = parameterMap.get("from")[0];
        nodeTypeName = parameterMap.get("nodeTypeName")[0];
        buttongroup = parameterMap.get("buttongroup");
        String[] constraint = parameterMap.get("constraint");
        String[] field = parameterMap.get("field");
        constraintToField = IntStream.range(0, constraint.length)
                .filter(index -> constraint[index] != null && field[index] != null)
                .boxed()
                .collect(Collectors.toMap(index -> constraint[index], index -> field[index], (o1, o2) -> o1, LinkedHashMap::new));

    }

    public Map<String, String> getPropertyToColumn() {
        return propertyToColumn;
    }

    public Map<String, String> getConstraintToField() {
        return constraintToField;
    }

    public String getFrom() {
        return from;
    }

    public String getNodeTypeName() {
        return nodeTypeName;
    }

    public String[] getButtongroup() {
        return buttongroup;
    }
}

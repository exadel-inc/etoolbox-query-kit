package com.exadel.etoolbox.query.core.services.impl;

import com.exadel.etoolbox.query.core.services.QueryExecutorService;
import com.exadel.etoolbox.query.core.models.QueryResultModel;
import org.osgi.service.component.annotations.Component;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.QueryObjectModel;
import java.util.*;
import java.util.stream.Collectors;

@Component(service = QueryExecutorService.class)
public class QueryExecutorServiceImpl implements QueryExecutorService {

    private static final String PATH_COLUMN = "path";

    @Override
    public void executeJqomQuery(QueryObjectModel queryObjectModel, QueryResultModel queryResultModel) {
        try {
            QueryResult result = queryObjectModel.execute();
            Map<String, String> columnsNameToProperty = getColumnsNamesAndProperties(queryObjectModel);
            Map<String, List<String>> results = queryResultModel.getResults();
            columnsNameToProperty.forEach((key, value) -> results.put(key, new LinkedList<>()));
            NodeIterator nodes = result.getNodes();
            while (nodes.hasNext()) {
                Node node = nodes.nextNode();
                for (String columnsName : columnsNameToProperty.keySet()) {
                    List<String> columnValues = results.get(columnsName);
                    if (columnsName.equals(PATH_COLUMN)) {
                        columnValues.add(node.getPath());
                    } else {
                        String property = columnsNameToProperty.get(columnsName);
                        if (node.hasProperty(property)) {
                            columnValues.add(node.getProperty(property).getString());
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getColumnsNamesAndProperties(QueryObjectModel queryObjectModel) {
        Map<String, String> columnsNameToProperty = Arrays.stream(queryObjectModel.getColumns())
                .filter(Objects::nonNull)
                .filter(column -> Objects.nonNull(column.getColumnName()))
                .filter(column -> Objects.nonNull(column.getPropertyName()))
                .collect(Collectors.toMap(Column::getColumnName, Column::getPropertyName));
        columnsNameToProperty.put(PATH_COLUMN, PATH_COLUMN);
        return columnsNameToProperty;
    }
}
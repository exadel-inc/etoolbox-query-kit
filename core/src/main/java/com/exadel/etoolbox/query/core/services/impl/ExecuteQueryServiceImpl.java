package com.exadel.etoolbox.query.core.services.impl;

import com.exadel.etoolbox.query.core.services.ExecuteQueryService;
import com.exadel.etoolbox.query.core.servlets.model.QueryResultModel;
import org.osgi.service.component.annotations.Component;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.QueryObjectModel;
import java.util.*;
import java.util.stream.Collectors;

@Component(service = ExecuteQueryService.class)
public class ExecuteQueryServiceImpl implements ExecuteQueryService {

    private static final String PATH_COLUMN = "path";

    @Override
    public void executeJQOMQuery(QueryObjectModel queryObjectModel, QueryResultModel queryResultModel) {
        try {
            QueryResult result = queryObjectModel.execute();
            Map<String, String> columnsNameToProperty = getColumnsNamesAndProperties(queryObjectModel);
            if (queryResultModel.getHeaders().isEmpty()) {
                queryResultModel.setHeaders(columnsNameToProperty.keySet());
            }
            NodeIterator nodes = result.getNodes();
            while (nodes.hasNext()) {
                Node node = nodes.nextNode();
                Map<String, String> columnToValue = new TreeMap<>();
                for (String columnsName : columnsNameToProperty.keySet()) {
                    if (columnsName.equals(PATH_COLUMN)) {
                        columnToValue.put(columnsName, node.getPath());
                    } else {
                        columnToValue.put(columnsName, node.getProperty(columnsNameToProperty.get(columnsName)).getString());
                    }
                }
                queryResultModel.addData(columnToValue);
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
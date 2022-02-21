package com.exadel.etoolbox.query.core.services.impl;

import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.exadel.etoolbox.query.core.services.QueryExecutorService;
import com.exadel.etoolbox.query.core.models.QueryResultModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.wrappers.ValueMapDecorator;
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
    public List<Resource> executeJqomQuery(ResourceResolver resolver, QueryObjectModel queryObjectModel) {
        try {
            List<Resource> resources = new LinkedList<>();
            QueryResult result = queryObjectModel.execute();
            Map<String, String> columnsNameToProperty = getColumnsNamesToProperties(queryObjectModel);
            NodeIterator nodes = result.getNodes();
            while (nodes.hasNext()) {
                Node node = nodes.nextNode();
                Map<String, Object> keyToValue = new HashMap<>();
                for (String columnName : columnsNameToProperty.keySet()) {
                    if (columnName.equals(PATH_COLUMN)) {
                        keyToValue.put(columnName, node.getPath());
                    } else {
                        String property = columnsNameToProperty.get(columnName);
                        if (node.hasProperty(property)) {
                            keyToValue.put(columnName, node.getProperty(property).getString());
                        }
                    }

                }
                if (!keyToValue.isEmpty()) {
                    ValueMapDecorator valueMap = new ValueMapDecorator(keyToValue);
                    resources.add(new ValueMapResource(resolver, StringUtils.EMPTY, StringUtils.EMPTY, valueMap));
                }
            }
            return resources;
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private Map<String, String> getColumnsNamesToProperties(QueryObjectModel queryObjectModel) {
        Map<String, String> columnsNameToProperty = Arrays.stream(queryObjectModel.getColumns())
                .filter(Objects::nonNull)
                .filter(column -> Objects.nonNull(column.getColumnName()))
                .filter(column -> Objects.nonNull(column.getPropertyName()))
                .collect(Collectors.toMap(Column::getColumnName, Column::getPropertyName));
        columnsNameToProperty.put(PATH_COLUMN, PATH_COLUMN);
        return columnsNameToProperty;
    }
}
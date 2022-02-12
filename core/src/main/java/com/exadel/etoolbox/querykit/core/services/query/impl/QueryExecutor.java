package com.exadel.etoolbox.querykit.core.services.query.impl;

import com.exadel.etoolbox.querykit.core.models.SearchItem;
import com.exadel.etoolbox.querykit.core.models.SearchResult;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.utils.ValueUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.HashSet;
import java.util.Set;

abstract class QueryExecutor {

    public abstract SearchResult execute() throws Exception;

    static void populateQueryResults(
            QueryResult results,
            ColumnCollection columnCollection,
            SearchResult.Builder resultBuilder,
            ResourceResolver resourceResolver) throws RepositoryException {

        String firstOfManySelectors = columnCollection.getSelectors().size() > 1
                ? columnCollection.getSelectors().get(0)
                : null;
        RowIterator rowIterator = results.getRows();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.nextRow();
            String path = firstOfManySelectors != null ? row.getNode(firstOfManySelectors).getPath() : row.getPath();
            SearchItem searchItem = new SearchItem(path, resourceResolver);
            populateColumns(row, searchItem, columnCollection);
            resultBuilder.item(searchItem);
        }
    }

    private static void populateColumns(Row row, SearchItem searchItem, ColumnCollection columnCollection) throws RepositoryException {
        if (columnCollection.isEmpty()) {
            return;
        }
        boolean hasDuplicatingPropNames = columnCollection.hasDuplicatingPropertyNames();
        Set<String> usedPropertyKeys = new HashSet<>();
        for (Pair<String, String> next : columnCollection) {
            String selector = next.getKey();
            String propertyName = next.getValue();
            if (StringUtils.isAnyBlank(selector, propertyName)) {
                continue;
            }
            String propertyKey = hasDuplicatingPropNames && usedPropertyKeys.contains(propertyName)
                    ? selector + "." + propertyName
                    : propertyName;
            usedPropertyKeys.add(propertyKey);
            Node node = row.getNode(selector);
            try {
                Property property = node.getProperty(propertyName);
                searchItem.getValueMap().put(propertyKey, ValueUtil.getValue(property));
            } catch (PathNotFoundException e) {
                searchItem.getValueMap().put(propertyKey, null);
            }
        }
    }

}

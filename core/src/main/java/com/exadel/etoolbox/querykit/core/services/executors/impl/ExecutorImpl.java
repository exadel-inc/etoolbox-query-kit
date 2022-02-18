package com.exadel.etoolbox.querykit.core.services.executors.impl;

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnAdapter;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchItem;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.services.executors.Executor;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemFilterFactory;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemConverterFactory;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import com.exadel.etoolbox.querykit.core.utils.ValueUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.Column;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

abstract class ExecutorImpl implements Executor {

    private static final Predicate<Row> DEFAULT_FILTER = row -> true;
    private static final UnaryOperator<SearchItem> DEFAULT_MODIFIER = item -> item;

    abstract List<SearchItemFilterFactory> getItemFilters();

    abstract List<SearchItemConverterFactory> getItemConverters();

    abstract ColumnCollection getColumnCollection(SearchRequest request, Query source) throws ConverterException;

    void populate(
            SearchResult.Builder searchResultBuilder,
            QueryResult queryResult,
            SearchRequest request,
            ColumnCollection columns) throws RepositoryException {

        Function<SearchItem, SearchItem> modifier = getAggregateModifier(request);

        RowIterator rowIterator = queryResult.getRows();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.nextRow();
            String path = getDefaultNode(row, columns).getPath();
            SearchItem searchItem = SearchItem.newInstance(request, path);
            populateItemProperties(searchItem, row, columns);
            searchResultBuilder.item(modifier.apply(searchItem));
        }
    }

    void populateAndMeasure(
            SearchResult.Builder searchResultBuilder,
            QueryResult queryResult,
            SearchRequest request,
            ColumnCollection columns) throws RepositoryException {

        long total = 0;
        int outputCount = 0;
        Predicate<Row> filter = getAggregateFilter(request, columns);
        Function<SearchItem, SearchItem> modifier = getAggregateModifier(request);

        RowIterator rowIterator = queryResult.getRows();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.nextRow();
            if (!filter.test(row)) {
                continue;
            }
            if (total++ >= request.getOffset() && outputCount++ < request.getLimit()) {
                String path = getDefaultNode(row, columns).getPath();
                SearchItem searchItem = SearchItem.newInstance(request, path);
                populateItemProperties(searchItem, row, columns);
                searchResultBuilder.item(modifier.apply(searchItem));
            }
        }

        searchResultBuilder.total(total);
    }

    private void populateItemProperties(
            SearchItem searchItem,
            Row row,
            ColumnCollection columns) throws RepositoryException {

        if (columns.getItems().isEmpty()) {
            return;
        }

        for (Column column : columns.getItems()) {
            if (!(column instanceof ColumnAdapter)) {
                continue;
            }
            ColumnAdapter columnAdapter = (ColumnAdapter) column;
            String selector = columnAdapter.getSelectorName();
            String propertyName = columnAdapter.getPropertyName();
            if (StringUtils.isAnyBlank(selector, propertyName)) {
                continue;
            }
            Node node = row.getNode(selector);
            try {
                Property property = node.getProperty(propertyName);
                Object value = ValueUtil.extractValue(property);
                String path = !((ColumnAdapter) column).isDefault() ? node.getPath() : null;
                searchItem.putProperty(columnAdapter.getUniquePropertyName(), value, path, property.getType(), property.isMultiple());
            } catch (PathNotFoundException e) {
                searchItem.putProperty(columnAdapter.getUniquePropertyName(), null);
            }
        }
    }

    private Function<SearchItem, SearchItem> getAggregateModifier(SearchRequest request) {
        if (CollectionUtils.isEmpty(getItemConverters()) || CollectionUtils.isEmpty(request.getItemConverters())) {
            return DEFAULT_MODIFIER;
        }
        return request.getItemConverters()
                .stream()
                .map(modifierName -> getItemConverters().stream().filter(modifier -> modifierName.equals(modifier.getName())).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .map(modifierFactory -> modifierFactory.getModifier(request))
                .map(modifier -> (Function<SearchItem, SearchItem>) modifier)
                .reduce(DEFAULT_MODIFIER, Function::andThen);
    }

    private Predicate<Row> getAggregateFilter(SearchRequest request, ColumnCollection columns) {
        if (CollectionUtils.isEmpty(getItemFilters()) || CollectionUtils.isEmpty(request.getItemFilters())) {
            return DEFAULT_FILTER;
        }
        return request.getItemFilters()
                .stream()
                .map(filterName -> getItemFilters().stream().filter(filter -> filterName.equals(filter.getName())).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .map(filterFactory -> filterFactory.getFilter(request, columns))
                .reduce(DEFAULT_FILTER, Predicate::and);
    }

    private static Node getDefaultNode(Row row, ColumnCollection columns) throws RepositoryException {
        String firstOfManySelectors = columns.getSelectors().size() > 1
                ? columns.getSelectors().get(0)
                : null;
        return firstOfManySelectors != null ? row.getNode(firstOfManySelectors) : row.getNode();
    }
}

/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Provides the basic functionality for services implementing {@link Executor}
 */
abstract class ExecutorImpl implements Executor {

    private static final Predicate<Row> DEFAULT_FILTER = row -> true;
    private static final UnaryOperator<SearchItem> DEFAULT_MODIFIER = item -> item;

    /**
     * Retrieves the list of requested item filter factories
     * @return A nullable {@code List} value
     */
    abstract List<SearchItemFilterFactory> getItemFilters();

    /**
     * Retrieves the list of requested item converter factories
     * @return A nullable {@code List} value
     */
    abstract List<SearchItemConverterFactory> getItemConverters();

    /**
     * Retrieves the {@link ColumnCollection} per the current search request and {@link Query} object
     * @param request {@code SearchRequest} instance
     * @param query   {@code Query} instance
     * @return {@code ColumnCollection} object, non-null
     * @throws ConverterException If the conversion failed
     */
    abstract ColumnCollection getColumnCollection(SearchRequest request, Query query) throws ConverterException;

    /**
     * Fills in the provided {@code SearchResult} builder the {@link SearchResult} instances created out of query result
     * entries (rows)
     * @param searchResultBuilder The builder to populate
     * @param queryResult         {@link QueryResult} instance
     * @param request             {@link SearchRequest} used to create new search items
     * @param columns             {@link ColumnCollection} used to create new search items
     * @throws RepositoryException If data retrieval for the search item creation failed
     */
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

    /**
     * Fills in the provided {@code SearchResult} builder the {@link SearchResult} instances created out of query result
     * entries (rows) while simultaneously calculating the "total results" value via iteration
     * @param searchResultBuilder The builder to populate
     * @param queryResult         {@link QueryResult} instance
     * @param request             {@link SearchRequest} used to create new search items
     * @param columns             {@link ColumnCollection} used to create new search items
     * @throws RepositoryException If data retrieval for the search item creation failed
     */
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
        String firstSelector = columns.getSelectors().size() > 0
                ? columns.getSelectors().get(0)
                : null;
        return firstSelector != null ? row.getNode(firstSelector) : row.getNode();
    }
}

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
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemFilter;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemConverter;
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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides the basic functionality for services implementing {@link Executor}
 */
abstract class ExecutorImpl implements Executor {

    private static final BiPredicate<SearchRequest, Object> DEFAULT_FILTER = (r,o) -> true;

    /**
     * Retrieves the list of requested item converters
     * @return A nullable {@code List} value
     */
    abstract List<SearchItemConverter> getConverters();

    /**
     * Retrieves the list of requested item filters
     * @return A nullable {@code List} value
     */
    abstract List<SearchItemFilter> getFilters();

    /**
     * Retrieves the list of requested item filters that are specific for the particular search entry
     * @param target The type of entities the filters must manage
     * @return A nullable {@code List} value
     */
    List<SearchItemFilter> getFilters(Class<?> target) {
        List<SearchItemFilter> allFilters = getFilters();
        if (allFilters == null) {
            return null;
        }
        return allFilters.stream().filter(current -> current.getTargetClass().equals(target)).collect(Collectors.toList());
    }

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

        BiFunction<SearchRequest, SearchItem, SearchItem> modifier = getAggregateConverter(request);

        RowIterator rowIterator = queryResult.getRows();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.nextRow();
            String path = getDefaultNode(row, columns).getPath();
            SearchItem searchItem = SearchItem.newInstance(request, path);
            populateItemProperties(searchItem, row, columns);
            searchResultBuilder.item(modifier != null ? modifier.apply(request, searchItem) : searchItem);
        }
        searchResultBuilder.markExecutionEnd().total(request.getPredefinedTotal());
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

        BiPredicate<SearchRequest, Object> rowFilter = getAggregateFilter(request, Row.class);
        BiPredicate<SearchRequest, Object> searchItemFilter = getAggregateFilter(request, SearchItem.class);
        BiFunction<SearchRequest, SearchItem, SearchItem> converter = getAggregateConverter(request);

        RowIterator rowIterator = queryResult.getRows();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.nextRow();
            if (rowFilter != null && !rowFilter.test(request, row)) {
                continue;
            }
            SearchItem searchItem = null;
            if (searchItemFilter != null) {
                searchItem = initializeIfNull(null, request, row, columns);
                if (!searchItemFilter.test(request, searchItem)) {
                    continue;
                }
            }
            if (total++ >= request.getOffset() && outputCount++ < request.getLimit()) {
                searchItem = initializeIfNull(searchItem, request, row, columns);
                populateItemProperties(searchItem, row, columns);
                searchResultBuilder.item(converter != null ? converter.apply(request, searchItem) : searchItem);
            }
        }
        searchResultBuilder.markExecutionEnd().total(total);
        CollectionUtils.emptyIfNull(getFilters()).forEach(SearchItemFilter::reset);
    }

    private SearchItem initializeIfNull(SearchItem original, SearchRequest request, Row row, ColumnCollection columns) throws RepositoryException {
        if (original != null) {
            return original;
        }
        String path = getDefaultNode(row, columns).getPath();
        return SearchItem.newInstance(request, path);
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

    private BiPredicate<SearchRequest, Object> getAggregateFilter(SearchRequest request, Class<?> target) {
        if (CollectionUtils.isEmpty(getFilters(target)) || CollectionUtils.isEmpty(request.getItemFilters())) {
            return null;
        }
        List<BiPredicate<SearchRequest, Object>> matchingFilters = request.getItemFilters()
                .stream()
                .map(filterName -> getFilters(target).stream().filter(filter -> filterName.equals(filter.getName())).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .map(filter -> (BiPredicate<SearchRequest, Object>) filter)
                .collect(Collectors.toList());
        if (matchingFilters.isEmpty()) {
            return null;
        }
        return matchingFilters.stream().reduce(DEFAULT_FILTER, BiPredicate::and);
    }

    private BiFunction<SearchRequest, SearchItem, SearchItem> getAggregateConverter(SearchRequest request) {
        if (CollectionUtils.isEmpty(getConverters()) || CollectionUtils.isEmpty(request.getItemConverters())) {
            return null;
        }
        List<SearchItemConverter> converters = request.getItemConverters()
                .stream()
                .map(modifierName -> getConverters().stream().filter(modifier -> modifierName.equals(modifier.getName())).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return (req, item) -> {
            SearchItem result = item;
            for (SearchItemConverter converter : converters) {
                result = converter.apply(req, result);
            }
            return result;
        };
    }

    private static Node getDefaultNode(Row row, ColumnCollection columns) throws RepositoryException {
        String firstSelector = columns.getSelectors().size() > 0
                ? columns.getSelectors().get(0)
                : null;
        return firstSelector != null ? row.getNode(firstSelector) : row.getNode();
    }
}

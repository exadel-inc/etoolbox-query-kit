package com.exadel.etoolbox.querykit.core.models.qom.columns;

import com.exadel.etoolbox.querykit.core.utils.ParseException;
import com.exadel.etoolbox.querykit.core.utils.QueryUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.QueryObjectModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ColumnCollection implements Iterable<Pair<String, String>>{

    private final List<Column> columns;

    public List<String> getSelectors() {
        return columns
                .stream()
                .map(Column::getSelectorName)
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return columns.isEmpty();
    }

    public List<String> getNames() {
        return columns
                .stream()
                .map(Column::getColumnName)
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
    }

    public void injectNamesForWildcards(String[] names) {
        if (ArrayUtils.isEmpty(names)) {
            return;
        }

        Map<String, List<String>> selectorsToProperties = new HashMap<>();
        for (String name : names) {
            if (!name.contains(".")) {
                continue;
            }
            String selector = StringUtils.substringBefore(name, ".");
            String property = StringUtils.substringAfter(name, ".");
            selectorsToProperties.computeIfAbsent(selector, sel -> new ArrayList<>()).add(property);
        }

        for (String selector : selectorsToProperties.keySet()) {
            List<String> propertiesToInsert = selectorsToProperties.get(selector);
            List<Column> matchingColumns = columns
                    .stream()
                    .filter(col -> selector.contains(col.getSelectorName()))
                    .collect(Collectors.toList());
            Column wildcardColumn = matchingColumns
                    .stream()
                    .filter(col -> StringUtils.isEmpty(col.getPropertyName()))
                    .findFirst()
                    .orElse(null);
            matchingColumns.forEach(col -> propertiesToInsert.remove(col.getPropertyName()));
            if (wildcardColumn == null || propertiesToInsert.isEmpty()) {
                continue;
            }
            int insertionPosition = columns.indexOf(wildcardColumn) + 1;
            for (String  propertyToInsert : propertiesToInsert) {
                columns.add(insertionPosition++, new ColumnAdapter(selector, propertyToInsert));
            }
            columns.remove(wildcardColumn);
        }
    }

    public boolean hasDuplicatingPropertyNames() {
        return columns
                .stream()
                .map(Column::getPropertyName)
                .distinct()
                .count() < columns.size();
    }

    public static ColumnCollection from(String statement, ResourceResolver resourceResolver) throws ParseException {
        QueryObjectModel model = QueryUtil.parseSql2(statement, resourceResolver).getModel();
        return from(model);
    }

    public static ColumnCollection from(QueryObjectModel model) {
        List<Column> columns = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(model.getColumns())) {
            columns.addAll(Arrays.asList(model.getColumns()));
        }
        return new ColumnCollection(columns);
    }

    @Override
    public Iterator<Pair<String, String>> iterator() {
        return new Iterator<Pair<String, String>>() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < columns.size();
            }

            @Override
            public Pair<String, String> next() {
                if (!hasNext()) {
                    throw new IllegalStateException("Tried to read past the bounds of collection");
                }
                return Pair.of(columns.get(cursor).getSelectorName(), columns.get(cursor++).getPropertyName());
            }
        };
    }

    @RequiredArgsConstructor
    private static class ColumnAdapter implements Column {
        private final String selectorName;
        private final String propertyName;

        @Override
        public String getSelectorName() {
            return selectorName;
        }

        @Override
        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public String getColumnName() {
            return selectorName + "." + propertyName;
        }
    }
}

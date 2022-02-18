package com.exadel.etoolbox.querykit.core.models.qom.columns;

import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportable;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.QueryObjectModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModifiableColumnCollection implements ColumnCollection, JsonExportable {

    private static final Column PATH_COLUMN = new PathColumn();

    @Getter
    private final List<Column> items = new ArrayList<>();

    public ModifiableColumnCollection(QueryObjectModel model) {
        items.add(PATH_COLUMN);
        initColumns(model);
        removeDuplicatingPathColumn();
        if (items.size() > 1) {
            ((ColumnAdapter) items.get(1)).setDefault(true);
        }
    }

    /* --------------
       Initialization
       -------------- */

    private void initColumns(QueryObjectModel model) {
        Set<String> existingPropertyNames = new HashSet<>();
        for (Column column : ArrayUtils.nullToEmpty(model.getColumns(), Column[].class)) {
            ColumnAdapter columnAdapter = new ColumnAdapter(column);
            if (StringUtils.isNotBlank(columnAdapter.getPropertyName())
                    && existingPropertyNames.contains(columnAdapter.getPropertyName())) {
                columnAdapter.setUseQualifiedProperty(true);
            }
            if (StringUtils.isNotBlank(columnAdapter.getPropertyName())) {
                existingPropertyNames.add(columnAdapter.getPropertyName());
            }
            items.add(columnAdapter);
        }
    }

    private void removeDuplicatingPathColumn() {
        String firstSelector = getSelectors().get(0);
        getAdapters()
                .stream()
                .filter(adapter -> StringUtils.equals(adapter.getSelectorName(), firstSelector) && Constants.PROPERTY_PATH.equals(adapter.getPropertyName()))
                .findFirst()
                .ifPresent(items::remove);
    }

    /* -----------------
       Interface methods
       ----------------- */

    @Override
    public List<String> getSelectors() {
        return items
                .stream()
                .map(Column::getSelectorName)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getPropertyNames() {
        return getAdapters()
                .stream()
                .map(ColumnAdapter::getUniquePropertyName)
                .filter(StringUtils::isNotBlank)
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
            List<ColumnAdapter> matchingColumns = this.getItems()
                    .stream()
                    .filter(col -> col instanceof ColumnAdapter)
                    .filter(col -> selector.contains(col.getSelectorName()))
                    .map(ColumnAdapter.class::cast)
                    .collect(Collectors.toList());
            ColumnAdapter wildcardColumn = matchingColumns
                    .stream()
                    .filter(ColumnAdapter::isWildcard)
                    .findFirst()
                    .orElse(null);
            matchingColumns.forEach(col -> propertiesToInsert.remove(col.getPropertyName()));
            if (wildcardColumn == null || propertiesToInsert.isEmpty()) {
                continue;
            }
            int insertionPosition = this.getItems().indexOf(wildcardColumn) + 1;
            for (String  propertyToInsert : propertiesToInsert) {
                this.getItems().add(insertionPosition++, new ColumnAdapter(selector, propertyToInsert));
            }
            this.getItems().remove(wildcardColumn);
        }
    }

    private List<ColumnAdapter> getAdapters() {
        return items.stream().skip(1).map(ColumnAdapter.class::cast).collect(Collectors.toList());
    }

    /* ------
       Output
       ------ */

    @Override
    public JsonElement toJson(JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        JsonArray itemArray = new JsonArray();
        for (Column item : items) {
            if (item instanceof ColumnAdapter && ((ColumnAdapter) item).isWildcard()) {
                continue;
            }
            itemArray.add(context.serialize(item));
        }
        result.add("items", itemArray);
        return result;
    }

    /* ---------------
       Service classes
       --------------- */

    private static class PathColumn implements Column, JsonExportable {

        @Override
        public String getSelectorName() {
            return StringUtils.EMPTY;
        }

        @Override
        public String getPropertyName() {
            return Constants.PROPERTY_PATH;
        }

        @Override
        public String getColumnName() {
            return Constants.TITLE_PATH;
        }

        @Override
        public JsonElement toJson(JsonSerializationContext context) {
            JsonObject jsonObject = new  JsonObject();
            jsonObject.addProperty("title", Constants.TITLE_PATH);
            jsonObject.addProperty("property", Constants.PROPERTY_PATH);
            return jsonObject;
        }
    }
}

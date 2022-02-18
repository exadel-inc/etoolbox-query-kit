package com.exadel.etoolbox.querykit.core.utils.serialization;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.experimental.UtilityClass;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.qom.ChildNodeJoinCondition;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.DescendantNodeJoinCondition;
import javax.jcr.query.qom.EquiJoinCondition;
import javax.jcr.query.qom.Length;
import javax.jcr.query.qom.LowerCase;
import javax.jcr.query.qom.NodeLocalName;
import javax.jcr.query.qom.NodeName;
import javax.jcr.query.qom.SameNodeJoinCondition;
import javax.jcr.query.qom.UpperCase;
import java.lang.reflect.Type;

@UtilityClass
class StandardTypesHelper {

    public static void feedToGsonBuilder(GsonBuilder builder) {
        addForColumn(builder);
        addForChildNodeJoinCondition(builder);
        addForDescendantNodeJoinCondition(builder);
        addForEquiJoinCondition(builder);
        addForLength(builder);
        addForLowerCase(builder);
        addForNodeLocalName(builder);
        addForNodeName(builder);
        addForSameNodeJoinCondition(builder);
        addForUpperCase(builder);
        addForValue(builder);
    }

    private static void addForColumn(GsonBuilder builder) {
        builder.registerTypeAdapter(
                Column.class,
                (JsonSerializer<Column>) (value, type, context) ->
                        getJsonObject(
                        "selector", value.getSelectorName(),
                        "property", value.getPropertyName()
                ));
    }

    private static void addForChildNodeJoinCondition(GsonBuilder builder) {
        builder.registerTypeHierarchyAdapter(
                ChildNodeJoinCondition.class,
                (JsonSerializer<ChildNodeJoinCondition>) (value, type, context) ->
                        getJsonObject(
                        "childSelector", value.getChildSelectorName(),
                        "parentSelector", value.getParentSelectorName(),
                        "type", "ISCHILDNODE"
                ));
    }

    private static void addForDescendantNodeJoinCondition(GsonBuilder builder) {
        builder.registerTypeHierarchyAdapter(
                DescendantNodeJoinCondition.class,
                (JsonSerializer<DescendantNodeJoinCondition>) (value, type, context) ->
                        getJsonObject(
                        "descendantSelector", value.getDescendantSelectorName(),
                        "ancestorSelector", value.getAncestorSelectorName(),
                        "type", "ISDESCENDANTNODE"
                ));
    }

    private static void addForEquiJoinCondition(GsonBuilder builder) {
        builder.registerTypeHierarchyAdapter(
                EquiJoinCondition.class,
                (JsonSerializer<EquiJoinCondition>) (value, type, context) ->
                        getJsonObject(
                        "selector1", value.getSelector1Name(),
                        "property1", value.getProperty1Name(),
                        "selector2", value.getSelector2Name(),
                        "property2", value.getProperty2Name(),
                        "type", "EQUAL"
                ));
    }

    private static void addForLength(GsonBuilder builder) {
        builder.registerTypeAdapter(
                Length.class,
                (JsonSerializer<Length>) (value, type, context) ->
                        getJsonObject(
                        "selector", value.getPropertyValue().getSelectorName(),
                        "property", value.getPropertyValue().getPropertyName(),
                        "type", "LENGTH"
                ));
    }

    private static void addForLowerCase(GsonBuilder builder) {
        builder.registerTypeHierarchyAdapter(
                LowerCase.class,
                (JsonSerializer<LowerCase>) (value, type, context) -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("operand", context.serialize(value.getOperand()));
                    jsonObject.addProperty("type", "LOWER");
                    return jsonObject;
                });
    }

    private static void addForNodeLocalName(GsonBuilder builder) {
        builder.registerTypeAdapter(
                NodeLocalName.class,
                (JsonSerializer<NodeLocalName>) (value, type, context) ->
                        getJsonObject(
                        "selector", value.getSelectorName(),
                        "type", "LOCAL_NAME"
                ));
    }

    private static void addForNodeName(GsonBuilder builder) {
        builder.registerTypeAdapter(
                NodeName.class,
                (JsonSerializer<NodeLocalName>) (value, type, context) ->
                        getJsonObject(
                        "selector", value.getSelectorName(),
                        "type", "NAME"
                ));
    }

    private static void addForSameNodeJoinCondition(GsonBuilder builder) {
        builder.registerTypeHierarchyAdapter(
                SameNodeJoinCondition.class,
                (JsonSerializer<SameNodeJoinCondition>) (value, type, context) ->
                        getJsonObject(
                        "selector1", value.getSelector1Name(),
                        "selector2", value.getSelector2Name(),
                        "path", value.getSelector2Path(),
                        "type", "ISSAMENODE"
                ));
    }

    private static void addForUpperCase(GsonBuilder builder) {
        builder.registerTypeHierarchyAdapter(
                UpperCase.class,
                (JsonSerializer<UpperCase>) (value, type, context) -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("operand", context.serialize(value.getOperand()));
                    jsonObject.addProperty("type", "UPPER");
                    return jsonObject;
                });
    }

    private static void addForValue(GsonBuilder builder) {
        builder.registerTypeAdapter(
                Value.class,
                (JsonSerializer<Value>) (value, type, context) -> {
                    String valueArgument;
                    try {
                        valueArgument = value.getString();
                    } catch (RepositoryException e) {
                        return null;
                    }
                    return getJsonObject(
                            "value", valueArgument,
                            "type", PropertyType.nameFromValue(value.getType()));
                });
    }

    /* ---------------
       Utility methods
       --------------- */

    private static JsonObject getJsonObject(String ... arguments) {
        JsonObject result = new JsonObject();
        for (int i = 1; i < arguments.length; i+=2) {
            result.addProperty(arguments[i-1], arguments[i]);
        }
        return result;
    }
}

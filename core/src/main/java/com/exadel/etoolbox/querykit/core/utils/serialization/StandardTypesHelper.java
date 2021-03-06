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
package com.exadel.etoolbox.querykit.core.utils.serialization;

import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.UpperCase;

/**
 * Contains utility methods for facilitating the export of standard query entities into formatted JSON
 */
@UtilityClass
class StandardTypesHelper {

    /**
     * Feeds the prepared JSON serializers (formatters) into the given JSON builder
     * @param builder {@link GsonBuilder} instance
     */
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
        addForSelector(builder);
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
                        Constants.PROPERTY_TYPE, "ISCHILDNODE"
                ));
    }

    private static void addForDescendantNodeJoinCondition(GsonBuilder builder) {
        builder.registerTypeHierarchyAdapter(
                DescendantNodeJoinCondition.class,
                (JsonSerializer<DescendantNodeJoinCondition>) (value, type, context) ->
                        getJsonObject(
                                "descendantSelector", value.getDescendantSelectorName(),
                                "ancestorSelector", value.getAncestorSelectorName(),
                                Constants.PROPERTY_TYPE, "ISDESCENDANTNODE"
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
                                Constants.PROPERTY_TYPE, "EQUAL"
                        ));
    }

    private static void addForLength(GsonBuilder builder) {
        builder.registerTypeAdapter(
                Length.class,
                (JsonSerializer<Length>) (value, type, context) ->
                        getJsonObject(
                                "selector", value.getPropertyValue().getSelectorName(),
                                "property", value.getPropertyValue().getPropertyName(),
                                Constants.PROPERTY_TYPE, "LENGTH"
                        ));
    }

    private static void addForLowerCase(GsonBuilder builder) {
        builder.registerTypeHierarchyAdapter(
                LowerCase.class,
                (JsonSerializer<LowerCase>) (value, type, context) -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("operand", context.serialize(value.getOperand()));
                    jsonObject.addProperty(Constants.PROPERTY_TYPE, "LOWER");
                    return jsonObject;
                });
    }

    private static void addForNodeLocalName(GsonBuilder builder) {
        builder.registerTypeAdapter(
                NodeLocalName.class,
                (JsonSerializer<NodeLocalName>) (value, type, context) ->
                        getJsonObject(
                                "selector", value.getSelectorName(),
                                Constants.PROPERTY_TYPE, "LOCAL_NAME"
                        ));
    }

    private static void addForNodeName(GsonBuilder builder) {
        builder.registerTypeAdapter(
                NodeName.class,
                (JsonSerializer<NodeLocalName>) (value, type, context) ->
                        getJsonObject(
                                "selector", value.getSelectorName(),
                                Constants.PROPERTY_TYPE, "NAME"
                        ));
    }

    private static void addForSameNodeJoinCondition(GsonBuilder builder) {
        builder.registerTypeHierarchyAdapter(
                SameNodeJoinCondition.class,
                (JsonSerializer<SameNodeJoinCondition>) (value, type, context) ->
                        getJsonObject(
                                "selector1", value.getSelector1Name(),
                                "selector2", value.getSelector2Name(),
                                Constants.PROPERTY_PATH, value.getSelector2Path(),
                                Constants.PROPERTY_TYPE, "ISSAMENODE"
                        ));
    }

    private static void addForSelector(GsonBuilder builder) {
        builder.registerTypeHierarchyAdapter(
                Selector.class,
                (JsonSerializer<Selector>) (value, type, context) ->
                        getJsonObject(
                                "selector", value.getSelectorName(),
                                "nodeType", value.getNodeTypeName()
                        ));
    }

    private static void addForUpperCase(GsonBuilder builder) {
        builder.registerTypeHierarchyAdapter(
                UpperCase.class,
                (JsonSerializer<UpperCase>) (value, type, context) -> {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("operand", context.serialize(value.getOperand()));
                    jsonObject.addProperty(Constants.PROPERTY_TYPE, "UPPER");
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
                            Constants.PROPERTY_VALUE, valueArgument,
                            Constants.PROPERTY_TYPE, PropertyType.nameFromValue(value.getType()));
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

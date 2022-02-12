package com.exadel.etoolbox.querykit.core.models.serialization;

import com.exadel.etoolbox.querykit.core.models.qom.QomAdapter;
import com.exadel.etoolbox.querykit.core.models.qom.constraints.JunctionConstraint;
import com.exadel.etoolbox.querykit.core.utils.ResponseUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

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
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;

@UtilityClass
public class QomSerializationHelper {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(
                    QomAdapter.class,
                    new JsonSerializer<QomAdapter>() {
                        @Override
                        public JsonElement serialize(QomAdapter value, Type type, JsonSerializationContext context) {
                            JsonObject result = new JsonObject();
                            result.add("source", GSON.toJsonTree(value.getSource()));
                            result.add("constraint", GSON.toJsonTree(value.getConstraintAdapter()));
                            result.add("orderings", GSON.toJsonTree(value.getOrderings()));
                            result.add("columns", GSON.toJsonTree(value.getColumns()));
                            return result;
                        }
                    })

            .registerTypeHierarchyAdapter(
                    JunctionConstraint.class,
                    new JsonSerializer<JunctionConstraint>() {
                        @Override
                        public JsonElement serialize(JunctionConstraint value, Type type, JsonSerializationContext context) {
                            JsonObject result = new JsonObject();
                            if (value.isCascade()) {
                                result.add("constraints", GSON.toJsonTree(value.flatten()));
                            } else {
                                result.add(
                                        "constraints",
                                        GSON.toJsonTree(Arrays.asList(value.getConstraint1(), value.getConstraint2())));
                            }
                            result.addProperty("type", value.getType());
                            return result;
                        }
                    })

            .registerTypeAdapter(
                    Column.class,
                    (JsonSerializer<Column>) (value, type, context) -> getJsonObject(
                            "selector", value.getSelectorName(),
                            "property", value.getPropertyName()
                    ))

            .registerTypeAdapter(
                    Value.class,
                    (JsonSerializer<Value>) (value, type, context) -> {
                        String valueArgument = null;
                        try {
                            valueArgument = value.getString();
                        } catch (RepositoryException e) {
                           return null;
                        }
                        return getJsonObject(
                                "value", valueArgument,
                                "type", PropertyType.nameFromValue(value.getType()));
                    })

            .registerTypeHierarchyAdapter(
                    ChildNodeJoinCondition.class,
                    (JsonSerializer<ChildNodeJoinCondition>) (value, type, context) -> getJsonObject(
                            "childSelector", value.getChildSelectorName(),
                            "parentSelector", value.getParentSelectorName(),
                            "type", "ISCHILDNODE"
                    ))

            .registerTypeHierarchyAdapter(
                    DescendantNodeJoinCondition.class,
                    (JsonSerializer<DescendantNodeJoinCondition>) (value, type, context) -> getJsonObject(
                            "descendantSelector", value.getDescendantSelectorName(),
                            "ancestorSelector", value.getAncestorSelectorName(),
                            "type", "ISDESCENDANTNODE"
                    ))

            .registerTypeHierarchyAdapter(
                    EquiJoinCondition.class,
                    (JsonSerializer<EquiJoinCondition>) (value, type, context) -> getJsonObject(
                            "selector1", value.getSelector1Name(),
                            "property1", value.getProperty1Name(),
                            "selector2", value.getSelector2Name(),
                            "property2", value.getProperty2Name(),
                            "type", "EQUAL"
                    ))

            .registerTypeHierarchyAdapter(
                    SameNodeJoinCondition.class,
                    (JsonSerializer<SameNodeJoinCondition>) (value, type, context) -> getJsonObject(
                            "selector1", value.getSelector1Name(),
                            "selector2", value.getSelector2Name(),
                            "path", value.getSelector2Path(),
                            "type", "ISSAMENODE"
                    ))

            .registerTypeHierarchyAdapter(
                    Length.class,
                    (JsonSerializer<Length>) (value, type, context) -> getJsonObject(
                            "selector", value.getPropertyValue().getSelectorName(),
                            "property", value.getPropertyValue().getPropertyName(),
                            "type", "LENGTH"
                    ))

            .registerTypeHierarchyAdapter(
                    LowerCase.class,
                    new JsonSerializer<LowerCase>() {
                        @Override
                        public JsonElement serialize(LowerCase value, Type type, JsonSerializationContext context) {
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.add("operand", GSON.toJsonTree(value.getOperand()));
                            jsonObject.addProperty("type", "LOWER");
                            return jsonObject;
                        }
                    })

            .registerTypeHierarchyAdapter(
                    UpperCase.class,
                    new JsonSerializer<UpperCase>() {
                        @Override
                        public JsonElement serialize(UpperCase value, Type type, JsonSerializationContext context) {
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.add("operand", GSON.toJsonTree(value.getOperand()));
                            jsonObject.addProperty("type", "UPPER");
                            return jsonObject;
                        }
                    })

            .registerTypeHierarchyAdapter(
                    NodeLocalName.class,
                    (JsonSerializer<NodeLocalName>) (value, type, context) -> getJsonObject(
                            "selector", value.getSelectorName(),
                            "type", "LOCAL_NAME"
                    ))

            .registerTypeHierarchyAdapter(
                    NodeName.class,
                    (JsonSerializer<NodeLocalName>) (value, type, context) -> getJsonObject(
                            "selector", value.getSelectorName(),
                            "type", "NAME"
                    ))

            .registerTypeAdapter(
                    String.class,
                    new TypeAdapter<String>() {
                        @Override
                        public void write(JsonWriter jsonWriter, String value) throws IOException {
                            if (StringUtils.isEmpty(value)) {
                                jsonWriter.nullValue();
                            } else {
                                jsonWriter.value(value);
                            }
                        }
                        @Override
                        public String read(JsonReader jsonReader)  {
                            return null;
                        }
                    })

            .create();


    public static String serialize(QomAdapter value) {
        try {
            return GSON.toJson(value);
        } catch (IllegalArgumentException | IllegalStateException | StackOverflowError e) {
            return ResponseUtil.getJsonMessage("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static JsonObject getJsonObject(String ... arguments) {
        JsonObject result = new JsonObject();
        for (int i = 1; i < arguments.length; i+=2) {
            result.addProperty(arguments[i-1], arguments[i]);
        }
        return result;
    }

}

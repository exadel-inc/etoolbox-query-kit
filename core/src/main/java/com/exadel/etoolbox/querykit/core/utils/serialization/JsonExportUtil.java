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

import com.exadel.etoolbox.querykit.core.utils.ResponseUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

@UtilityClass
public class JsonExportUtil {

    private static final Gson GSON;

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(
                        JsonExportable.class,
                        (JsonSerializer<JsonExportable>) (value, type, context) -> value.toJson(context));
        // Do not serialize empty strings
        gsonBuilder.registerTypeAdapter(
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
                });
        StandardTypesHelper.feedToGsonBuilder(gsonBuilder);
        GSON = gsonBuilder.create();
    }

    public static String export(Object value) {
        try {
            return GSON.toJson(value);
        } catch (IllegalArgumentException | IllegalStateException | StackOverflowError e) {
            return ResponseUtil.getJsonMessage("error", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    public static void submitValue(JsonObject target, String name, Object value) {
        if (value != null && value.getClass().isArray()) {
            target.add(name, getArray(value));
        } else if (value != null) {
            target.addProperty(name, getString(value));
        }
    }

    private JsonArray getArray(Object value) {
        Object[] array = (Object[]) value;
        JsonArray result = new JsonArray();
        Arrays.stream(array).map(JsonExportUtil::getString).filter(StringUtils::isNotEmpty).forEach(result::add);
        return result;
    }

    private String getString(Object value) {
        if (value instanceof Calendar) {
            return DATE_FORMATTER.format(((Calendar) value).getTime());
        }
        return String.valueOf(value);
    }

}

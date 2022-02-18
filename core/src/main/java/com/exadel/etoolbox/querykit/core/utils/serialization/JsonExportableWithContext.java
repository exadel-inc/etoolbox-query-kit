package com.exadel.etoolbox.querykit.core.utils.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

public interface JsonExportableWithContext<T> extends JsonExportable {

    @Override
    default JsonElement toJson(JsonSerializationContext serializer) {
        return toJson(serializer, null);
    }

    JsonElement toJson(JsonSerializationContext serializer, T data);
}

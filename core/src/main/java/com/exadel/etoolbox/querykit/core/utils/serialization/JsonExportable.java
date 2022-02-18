package com.exadel.etoolbox.querykit.core.utils.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

public interface JsonExportable {

    JsonElement toJson(JsonSerializationContext serializer);
}

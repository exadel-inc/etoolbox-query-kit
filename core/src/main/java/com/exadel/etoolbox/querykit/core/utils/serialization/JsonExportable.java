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

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

/**
 * Manifests an entity that can be exported to JSON via an augmented routine
 */
public interface JsonExportable {

    /**
     * Retrieves the JSON representation of the current object
     * @param serializer {@link JsonSerializationContext} object used to produce JSON
     * @return {@link JsonElement} representing the current entity
     */
    JsonElement toJson(JsonSerializationContext serializer);
}

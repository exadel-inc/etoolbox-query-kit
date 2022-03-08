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
package com.exadel.etoolbox.querykit.core.models.qom.constraints;

import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportable;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.apache.commons.lang3.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface JunctionConstraint extends JsonExportable {

    ConstraintAdapter getConstraint1();

    ConstraintAdapter getConstraint2();

    String getType();

    default boolean isCascade() {
        return isCascade(false);
    }

    default boolean isCascade(boolean nested) {
        if (getConstraint1() == null || getConstraint2() == null) {
            return false;
        }
        boolean leftIsJunction = ClassUtils.isAssignable(getConstraint1().getClass(), JunctionConstraint.class);
        boolean leftFits = !leftIsJunction || getConstraint1().getClass().equals(getClass());
        boolean rightIsJunction = ClassUtils.isAssignable(getConstraint2().getClass(), JunctionConstraint.class);
        boolean rightFits = !rightIsJunction || getConstraint2().getClass().equals(getClass());

        return leftFits && rightFits && (leftIsJunction || rightIsJunction || nested);
    }

    default List<ConstraintAdapter> flatten() {
        List<ConstraintAdapter> result = new ArrayList<>();
        for (ConstraintAdapter item : Arrays.asList(getConstraint1(), getConstraint2())) {
            if (ClassUtils.isAssignable(item.getClass(), JunctionConstraint.class)
                    && ((JunctionConstraint) item).isCascade(true)) {
                List<ConstraintAdapter> addenda = ((JunctionConstraint) item).flatten();
                result.addAll(addenda);
            } else {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    default JsonElement toJson(JsonSerializationContext serializer) {
        JsonObject result = new JsonObject();
        if (isCascade()) {
            result.add("constraints", serializer.serialize(flatten()));
        } else {
            result.add(
                    "constraints",
                    serializer.serialize(Arrays.asList(getConstraint1(), getConstraint2())));
        }
        result.addProperty(Constants.PROPERTY_TYPE, getType());
        return result;
    }
}

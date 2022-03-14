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
package com.exadel.etoolbox.querykit.core.models.query.helpers;

import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;

/**
 * Provides the ability to specify an inline JSON structure is HTL in the form of a value array, and simplifies JSON
 * entities escaping
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class InlineJsonHelper {

    private static final String COLON = ":";
    private static final String OPENING_BRACKET = "{";
    private static final String CLOSING_BRACKET = "}";

    @Inject
    private Object[] args;

    /**
     * Retrieves the resulting JSON string
     */
    @Getter(lazy = true)
    private final String json = prepareJson();

    private String prepareJson() {
        return prepareJson(args);
    }

    private String prepareJson(Object[] args) {
        if (args == null || ArrayUtils.isEmpty(args)) {
            return OPENING_BRACKET + CLOSING_BRACKET;
        }
        boolean commaNeeded = false;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(OPENING_BRACKET);
        for (int i = 0; i < args.length - 1; i += 2) {
            if (args[i] == null) {
                continue;
            }
            if (commaNeeded) {
                stringBuilder.append(Constants.COMMA);
            }
            stringBuilder.append(Constants.QUOTE).append(args[i].toString()).append(Constants.QUOTE).append(COLON);
            if (args[i + 1] == null) {
                stringBuilder.append(Constants.QUOTE).append(Constants.QUOTE);
            } else if (args[i + 1].getClass().isArray()) {
                stringBuilder.append(prepareJson((Object[]) args[i + 1]));
            } else {
                stringBuilder.append(Constants.QUOTE).append(args[i + 1].toString()).append(Constants.QUOTE);
            }
            commaNeeded = true;
        }
        stringBuilder.append(CLOSING_BRACKET);
        return stringBuilder.toString();
    }
}

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
package com.exadel.etoolbox.querykit.core.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains utility methods for escaping and unescaping strings
 */
@UtilityClass
public class EscapingUtil {

    private static final Pattern ESCAPED_CHAR_PATTERN = Pattern.compile("__(\\d+)__");

    public static String escape(String value, String... unwanted) {
        if (StringUtils.isEmpty(value) || ArrayUtils.isEmpty(unwanted)) {
            return value;
        }
        String result = value;
        for (String token : unwanted) {
            if (result.contains(token)) {
                result = result.replace(token, prepareEscapedSequence(token));
            }
        }
        return result;
    }

    /**
     * Unescapes parts of the provided string according to a specific routine
     * @param value String to process
     * @return String value
     */
    public static String unescape(String value) {
        StringBuilder result = new StringBuilder(value);
        Matcher matcher = ESCAPED_CHAR_PATTERN.matcher(result);
        while (matcher.find()) {
            result.replace(matcher.start(), matcher.end(), Character.toString((char) Integer.parseInt(matcher.group(1))));
            matcher.reset(result);
        }
        return result.toString();
    }

    private static String prepareEscapedSequence(String token) {
        return token.chars()
                .mapToObj(String::valueOf)
                .map(chr -> "__" + chr + "__")
                .collect(Collectors.joining());
    }
}

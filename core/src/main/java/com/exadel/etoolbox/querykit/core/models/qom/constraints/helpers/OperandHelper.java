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
package com.exadel.etoolbox.querykit.core.models.qom.constraints.helpers;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.Literal;
import javax.jcr.query.qom.Operand;

@UtilityClass
@Slf4j
public class OperandHelper {

    public static String getLiteralValue(Operand operand) {
        if (!(operand instanceof Literal)) {
            return StringUtils.EMPTY;
        }
        try {
            return ((Literal) operand).getLiteralValue().getString();
        } catch (RepositoryException e) {
            log.error("Could not retrieve a value for the static operand", e);
        }
        return StringUtils.EMPTY;
    }
}

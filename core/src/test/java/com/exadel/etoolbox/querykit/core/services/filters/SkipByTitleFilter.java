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
package com.exadel.etoolbox.querykit.core.services.filters;

import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemFilter;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.Row;

public class SkipByTitleFilter implements SearchItemFilter {

    @Override
    public String getName() {
        return "skip-by-title";
    }

    @Override
    public Class<?> getTargetClass() {
        return Row.class;
    }

    @Override
    public boolean test(SearchRequest request, Object row) {
        String title;
        try {
            title = ((Row) row).getNode("content").getProperty("jcr:title").getString();
            String ordinal = title.startsWith("Page ") ? title.substring(5) : StringUtils.EMPTY;
            int number = StringUtils.isNumeric(ordinal) ? Integer.parseInt(ordinal) : -1;
            return number >= 0 && number % 2 == 0;
        } catch (RepositoryException e) {
            return false;
        }
    }
}

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

import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemFilterFactory;

import javax.jcr.RepositoryException;
import javax.jcr.query.Row;
import java.util.function.Predicate;

public class SkipByRankingFilterFactory implements SearchItemFilterFactory {

    @Override
    public String getName() {
        return "skip-by-ranking";
    }

    @Override
    public Predicate<Row> getFilter(SearchRequest request, ColumnCollection columns) {
        return row -> {
            try {
                long ranking = row.getNode("content").getProperty("ranking").getLong();
                return ranking != 42;
            } catch (RepositoryException e) {
                return true;
            }
        };
    }
}

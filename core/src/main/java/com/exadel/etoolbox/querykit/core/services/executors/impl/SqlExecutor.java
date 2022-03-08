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
package com.exadel.etoolbox.querykit.core.services.executors.impl;

import com.exadel.etoolbox.querykit.core.models.qom.QomAdapter;
import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterBundle;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ColumnCollection;
import com.exadel.etoolbox.querykit.core.models.qom.columns.ModifiableColumnCollection;
import com.exadel.etoolbox.querykit.core.models.query.ParsedQueryInfo;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.syntax.WordModel;
import com.exadel.etoolbox.querykit.core.services.executors.ExecutorType;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemConverterFactory;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemFilterFactory;
import com.exadel.etoolbox.querykit.core.services.executors.Executor;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import javax.jcr.query.Query;
import javax.jcr.query.qom.QueryObjectModel;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component(service = Executor.class)
public class SqlExecutor extends QueryBasedExecutor  {

    private static final String KEYWORD_SELECT = "select";
    private static final String KEYWORD_FROM = "from";

    @Reference(target = "(converter.output=QOM)", policyOption = ReferencePolicyOption.GREEDY)
    private QueryConverter queryConverterService;

    @Getter(AccessLevel.PACKAGE)
    @Reference
    private List<SearchItemFilterFactory> itemFilters;

    @Getter(AccessLevel.PACKAGE)
    @Reference
    private List<SearchItemConverterFactory> itemConverters;

    @Override
    public ExecutorType getType() {
        return ExecutorType.SQL;
    }

    @Override
    public ParsedQueryInfo parse(SearchRequest request) throws Exception {

        if (request.getUserParameters().size() > 0
                && containsInterpolateableValues(request.getStatement(), request.getUserParameters().keySet())) {

            return queryConverterService.convert(
                            request.getStatement(),
                            request.getResourceResolver(),
                            QomAdapterBundle.class)
                    .buildWith(
                            request.getQueryManager().getQOMFactory(),
                            request.getUserParameters());
        }

        return new ParsedQueryInfo() {
            @Override
            public String toJson() {
                return StringUtils.EMPTY;
            }

            @Override
            public String toSqlString() {
                return request.getStatement();
            }
        };
    }

    @Override
    Query compile(SearchRequest request) throws Exception {
        return request.getQueryManager().createQuery(parse(request).toSqlString(), Query.JCR_SQL2);
    }

    @Override
    ColumnCollection getColumnCollection(SearchRequest request, Query source) throws ConverterException {
        QueryObjectModel qom = queryConverterService.convert(
                prepareSelectExtract(source.getStatement()),
                request.getResourceResolver(),
                QomAdapter.class)
                .getModel();
        return new ModifiableColumnCollection(qom);
    }

    private static boolean containsInterpolateableValues(String statement, Set<String> variableNames) {
        WordModel wordModel = new WordModel(statement);
        for (String name : variableNames) {
            boolean match = wordModel.hasToken(elt -> elt.startsWith("'") && elt.endsWith("'") && elt.contains("$" + name));
            if (match) {
                return true;
            }
        }
        return false;
    }

    private static String prepareSelectExtract(String statement) {
        WordModel wordModel = new WordModel(statement);
        Set<String> selectors = new LinkedHashSet<>();
        WordModel selectorString = wordModel.extractBetween(KEYWORD_SELECT, KEYWORD_FROM);
        while (selectorString != null) {
            selectors.addAll(selectorString.split(Constants.COMMA)
                    .stream()
                    .map(WordModel::toString)
                    .map(String::trim)
                    .collect(Collectors.toList()));
            selectorString = wordModel.extractBetween(
                    KEYWORD_SELECT,
                    KEYWORD_FROM,
                    selectorString.getEndPosition());
        }
        return String.format(
                "SELECT %s FROM [%s]",
                String.join(Constants.COMMA, selectors),
                Constants.NODE_TYPE_PLACEHOLDER);
    }
}

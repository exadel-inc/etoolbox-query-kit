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
package com.exadel.etoolbox.querykit.core.services.converters.impl;

import com.exadel.etoolbox.querykit.core.models.qom.QomAdapter;
import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterBundle;
import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterContext;
import com.exadel.etoolbox.querykit.core.models.search.QueryType;
import com.exadel.etoolbox.querykit.core.models.syntax.WordModel;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import com.exadel.etoolbox.querykit.core.utils.EscapingUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.query.sql2.Parser;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link QueryConverter implementation} used to transform {@code JCR-SQL2} queries query object models, model adapters,
 * and bundles
 */
@Component(service = QueryConverter.class, property = "converter.output=QOM")
public class SqlToQomConverter implements QueryConverter {

    private static final String PSEUDO_EQUALITY_FORMAT = "= '$in$(%s)'";

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryType getSourceType() {
        return QueryType.JCR_SQL2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T convert(String statement, ResourceResolver resourceResolver, Class<T> type) throws ConverterException {
        Session session = resourceResolver.adaptTo(Session.class);
        Workspace workspace = Optional.ofNullable(session).map(Session::getWorkspace).orElse(null);
        if (workspace == null) {
            throw new ConverterException("Could not retrieve workspace");
        }

        try {
            QueryManager queryManager = workspace.getQueryManager();
            QueryObjectModelFactory queryObjectModelFactory = queryManager.getQOMFactory();
            ValueFactory queryValueFactory = session.getValueFactory();
            QomAdapterContext qomAdapterContext = QomAdapterContext.from(queryObjectModelFactory, queryValueFactory);

            Parser parser = new Parser(queryObjectModelFactory, queryValueFactory);
            List<QomAdapter> qomAdapters = new ArrayList<>();

            for (String statementPart : prepareCompliantStatementParts(statement)) {
                QueryObjectModel qom = parser.createQueryObjectModel(statementPart);
                QomAdapter qomAdapter = QomAdapter.from(qom, qomAdapterContext);
                qomAdapters.add(qomAdapter);
            }
            return output(qomAdapters, type);

        } catch (RepositoryException | ClassCastException e) {
            throw new ConverterException(XPathToSqlConverter.ERROR_MESSAGE_PARSE + statement, e);
        }
    }

    private <T> T output(List<QomAdapter> qomAdapters, Class<T> type) {
        if (String.class.equals(type) && qomAdapters.size() == 1) {
            return type.cast(qomAdapters.get(0).toSqlString());
        } else if (String.class.equals(type) && qomAdapters.size() > 1) {
            type.cast(new QomAdapterBundle(qomAdapters).toSqlString());
        } else if (String[].class.equals(type)) {
            return type.cast(qomAdapters.stream().map(QomAdapter::toSqlString).toArray(String[]::new));

        } else if (QueryObjectModel.class.equals(type)) {
            return type.cast(qomAdapters.get(0).getModel());
        } else if (QueryObjectModel[].class.equals(type)) {
            return type.cast(qomAdapters.stream().map(QomAdapter::getModel).toArray(QueryObjectModel[]::new));

        } else if (type.equals(QomAdapter.class)) {
            return type.cast(qomAdapters.get(0));
        } else if (type.equals(QomAdapter[].class)) {
            return type.cast(qomAdapters.toArray(new QomAdapter[0]));
        } else if (type.equals(QomAdapterBundle.class)) {
            return type.cast(new QomAdapterBundle(qomAdapters));
        }

        throw new ClassCastException("Unsupportable return type " + type);
    }

    private static List<String> prepareCompliantStatementParts(String source) {
        WordModel wordModel = new WordModel(source);
        replaceUnparseableParts(wordModel);
        if (!wordModel.hasToken(Constants.OPERATOR_UNION)) {
            return Collections.singletonList(wordModel.toString());
        }
        return wordModel
                .split(Constants.OPERATOR_UNION)
                .stream()
                .map(WordModel::toString)
                .collect(Collectors.toList());
    }

    private static void replaceUnparseableParts(WordModel wordModel) {
        WordModel inFunction = wordModel.extractFunction(Constants.OPERATOR_IN);
        while (inFunction != null) {
            String argument = StringUtils.substringBetween(
                    inFunction.toString(),
                    Constants.OPENING_BRACKET, Constants.CLOSING_BRACKET);
            argument = EscapingUtil.escape(argument, Constants.SINGLE_QUOTE);
            wordModel.replace(inFunction, String.format(PSEUDO_EQUALITY_FORMAT, argument));
            inFunction = wordModel.extractFunction(Constants.OPERATOR_IN);
        }
    }
}

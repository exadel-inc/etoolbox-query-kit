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

import com.exadel.etoolbox.querykit.core.models.search.QueryType;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.oak.query.xpath.XPathToSQL2Converter;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import java.text.ParseException;

/**
 * {@link QueryConverter implementation} used to transform {@code XPath} queries into {@code JCR-SQL2} format
 */
@Component(service = QueryConverter.class, property = "converter.output=SQL")
public class XPathToSqlConverter implements QueryConverter {
    static final String ERROR_MESSAGE_PARSE = "Could not parse statement ";

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryType getSourceType() {
        return QueryType.XPATH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T convert(String statement, ResourceResolver resourceResolver, Class<T> type) throws ConverterException {
        XPathToSQL2Converter converter = new XPathToSQL2Converter();
        String sql2;
        try {
            sql2 = converter.convert(statement);
            return type.cast(StringUtils.substringBefore(sql2, " /* xpath: "));
        } catch (ParseException | ClassCastException e) {
            throw new ConverterException(ERROR_MESSAGE_PARSE + statement, e);
        }
    }
}

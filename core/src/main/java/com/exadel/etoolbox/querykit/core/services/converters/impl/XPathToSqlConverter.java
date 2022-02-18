package com.exadel.etoolbox.querykit.core.services.converters.impl;

import com.exadel.etoolbox.querykit.core.models.search.QueryType;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.oak.query.xpath.XPathToSQL2Converter;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import java.text.ParseException;

@Component(service = QueryConverter.class, property = "converter.output=String")
public class XPathToSqlConverter implements QueryConverter {
    static final String ERROR_MESSAGE_PARSE = "Could not parse statement ";

    @Override
    public QueryType getSourceType() {
        return QueryType.XPATH;
    }

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

package com.exadel.etoolbox.querykit.core.services.converters.impl;

import com.day.cq.search.PredicateConverter;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.text.Text;
import com.exadel.etoolbox.querykit.core.models.search.QueryType;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.Session;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

@Component(service = QueryConverter.class, property = "converter.output=SQL")
public class QueryBuilderToSqlConverter extends XPathToSqlConverter {

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public QueryType getSourceType() {
        return QueryType.QUERY_BUILDER;
    }

    @Override
    public <T> T convert(String statement, ResourceResolver resourceResolver, Class<T> type) throws ConverterException {
        Properties props;
        try {
            props = getProperties(statement);
        } catch (IOException e) {
            throw new ConverterException(ERROR_MESSAGE_PARSE + statement, e);
        }
        PredicateGroup root = PredicateConverter.createPredicates(props);
        // Avoid slow //* queries
        if (root.isEmpty()) {
            throw new ConverterException("Predicate group cannot be empty");
        }

        Query queryBuilderQuery = queryBuilder.createQuery(root, resourceResolver.adaptTo(Session.class));
        queryBuilderQuery.setHitsPerPage(0);
        try {
            return type.cast(super.convert(queryBuilderQuery.getResult().getQueryStatement(), resourceResolver, type));
        } catch (ClassCastException e) {
            throw new ConverterException(ERROR_MESSAGE_PARSE + statement, e);
        }
    }

    private static Properties getProperties(String value) throws IOException {
        Properties properties = new Properties();
        try (StringReader reader = new StringReader(Text.unescape(StringUtils.replace(value, "&", "\n")))) {
            properties.load(reader);
            return properties;
        }
    }
}

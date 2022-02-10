package com.exadel.etoolbox.query.core.services.impl;

import com.day.cq.search.PredicateConverter;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.text.Text;
import com.exadel.etoolbox.query.core.services.QueryConverterService;
import com.exadel.etoolbox.query.core.models.QueryResultModel;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.query.sql2.Parser;
import org.apache.jackrabbit.oak.query.xpath.XPathToSQL2Converter;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Properties;

@Component(service = QueryConverterService.class)
public class QueryConverterServiceImpl implements QueryConverterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryConverterServiceImpl.class);

    private static final String XPATH_STARTER = "/";
    private static final String SQL_STARTER = "select";

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public QueryObjectModel convertQueryToJqom(ResourceResolver resourceResolver, QueryResultModel queryResultModel) {
        String query = queryResultModel.getQuery();
        try {
            Session session = resourceResolver.adaptTo(Session.class);
            if (session == null) {
                return null;
            }

            if (queryResultModel.getQuery().startsWith(XPATH_STARTER)) {
                query = convertXPathToSql2Query(queryResultModel.getQuery());
            } else if (!queryResultModel.getQuery().toLowerCase().startsWith(SQL_STARTER)) {
                query = convertQueryBuilderQueryToSql2(queryResultModel.getQuery(), session);
            }
            QueryObjectModelFactory qomFactory = session.getWorkspace().getQueryManager().getQOMFactory();
            ValueFactory valueFactory = session.getValueFactory();
            Parser parser = new Parser(qomFactory, valueFactory);
            QueryObjectModel queryObjectModel = parser.createQueryObjectModel(query);
            queryResultModel.setResultCount(IteratorUtils.size(queryObjectModel.execute().getRows()));
            if (queryResultModel.getLimit() != null) {
                queryObjectModel.setLimit(queryResultModel.getLimit());
            }
            if (queryResultModel.getOffset() != null) {
                queryObjectModel.setOffset(queryResultModel.getOffset());
            }
            return queryObjectModel;
        } catch (Exception e) {
            LOGGER.warn("Cannot create queryObjectModel", e);
        }
        return null;
    }

    private String convertQueryBuilderQueryToSql2(String query, Session session) throws ParseException, IOException {
        Properties props = convertToProperties(query);
        PredicateGroup root = PredicateConverter.createPredicates(props);
        // avoid slow //* queries
        if (root.isEmpty()) {
            LOGGER.debug("Predicate group cannot be empty");
            return null;
        }

        Query queryBuilderQuery = queryBuilder.createQuery(root, session);
        queryBuilderQuery.setHitsPerPage(0);
        return convertXPathToSql2Query(queryBuilderQuery.getResult().getQueryStatement());
    }

    private String convertXPathToSql2Query(String xPathQuery) throws ParseException {
        XPathToSQL2Converter converter = new XPathToSQL2Converter();
        String sql2 = converter.convert(xPathQuery);
        return StringUtils.substringBefore(sql2, " /* xpath: ");
    }

    private Properties convertToProperties(String query) throws IOException {
        Properties properties = new Properties();
        try (StringReader reader = new StringReader(Text.unescape(StringUtils.replace(query, "&", "\n")))) {
            properties.load(reader);
            return properties;
        }
    }
}
package com.exadel.etoolbox.query.core.services.impl;

import com.exadel.etoolbox.query.core.services.QueryConverterService;
import com.exadel.etoolbox.query.core.servlets.model.QueryResultModel;
import org.apache.jackrabbit.commons.query.sql2.Parser;
import org.apache.jackrabbit.oak.query.xpath.XPathToSQL2Converter;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.text.ParseException;

@Component(service = QueryConverterService.class)
public class QueryConverterServiceImpl implements QueryConverterService {

    private static final String X_PATH_LANGUAGE = "XPath";

    @Override
    public QueryObjectModel convertQueryToJQOM(ResourceResolver resourceResolver, QueryResultModel queryResultModel) {
        String query = queryResultModel.getQuery();
        try {
            if (queryResultModel.getLanguage().equals(X_PATH_LANGUAGE)) {
                XPathToSQL2Converter converter = new XPathToSQL2Converter();
                query = converter.convert(queryResultModel.getQuery());
            }
            Session session = resourceResolver.adaptTo(Session.class);
            if (session == null) {
                return null;
            }
            QueryObjectModelFactory qomFactory = session.getWorkspace().getQueryManager().getQOMFactory();
            ValueFactory valueFactory = session.getValueFactory();
            Parser parser = new Parser(qomFactory, valueFactory);
            QueryObjectModel queryObjectModel = parser.createQueryObjectModel(query);
            queryResultModel.setResultCount(queryObjectModel.execute().getRows().getSize());
            if (queryResultModel.getLimit() != null) {
                queryObjectModel.setLimit(queryResultModel.getLimit());
            }
            if (queryResultModel.getOffset() != null) {
                queryObjectModel.setOffset(queryResultModel.getOffset());
            }
            return queryObjectModel;
        } catch (UnsupportedRepositoryOperationException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
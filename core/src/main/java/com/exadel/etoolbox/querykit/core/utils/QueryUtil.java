package com.exadel.etoolbox.querykit.core.utils;

import com.exadel.etoolbox.querykit.core.models.qom.QomAdapter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.jackrabbit.commons.query.sql2.Parser;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.Optional;

@UtilityClass
@Slf4j
public class QueryUtil {

    public static QomAdapter parseSql2(String statement, ResourceResolver resourceResolver) throws ParseException {
        Session session = resourceResolver.adaptTo(Session.class);
        QueryManager queryManager = getQueryManager(resourceResolver);
        if (session == null || queryManager == null) {
            throw new ParseException("Could not retrieve a session or query manager");
        }
        try {
            Parser parser = new Parser(queryManager.getQOMFactory(), session.getValueFactory());
            return QomAdapter.from(parser.createQueryObjectModel(statement));
        } catch (RepositoryException e) {
            throw new ParseException(e);
        }
    }

    public static QueryObjectModelFactory getQomFactory(ResourceResolver resourceResolver) {
        return Optional.ofNullable(getQueryManager(resourceResolver))
                .map(QueryManager::getQOMFactory)
                .orElse(null);
    }

    private static QueryManager getQueryManager(ResourceResolver resourceResolver) {
        Session session = resourceResolver.adaptTo(Session.class);
        Workspace workspace = Optional.ofNullable(session).map(Session::getWorkspace).orElse(null);
        if (workspace == null) {
            log.error("Could not retrieve a session or workspace instance");
            return null;
        }
        try {
            return workspace.getQueryManager();
        } catch (RepositoryException e) {
            log.error("Could not retrieve a query manager", e);
        }
        return null;
    }
}

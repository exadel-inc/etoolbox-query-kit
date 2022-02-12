package com.exadel.etoolbox.querykit.core.services.query.impl;

import com.exadel.etoolbox.querykit.core.models.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.SearchResult;
import com.exadel.etoolbox.querykit.core.services.query.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Component;

import javax.jcr.Session;
import javax.jcr.Workspace;
import java.util.Optional;

@Component
@Slf4j
public class QueryServiceImpl implements QueryService {

    @Override
    public SearchResult execute(SearchRequest request) {

        Session session = request.getResourceResolver().adaptTo(Session.class);
        Workspace workspace = Optional.ofNullable(session).map(Session::getWorkspace).orElse(null);
        if (workspace == null) {
            return SearchResult.error(request, "Could not retrieve session");
        }
        try {
            return new Sql2Executor(request, workspace).execute();
        } catch (Exception e) {
            log.error("Could not execute query {}", request.getStatement(), e);
            return SearchResult.error(request, "Could not execute: " + e.getMessage());
        }
    }
}

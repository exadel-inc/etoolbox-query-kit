package com.exadel.etoolbox.querykit.core.servlets;

import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportUtil;
import com.exadel.etoolbox.querykit.core.services.query.QueryService;
import com.exadel.etoolbox.querykit.core.utils.ResponseUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

@Component(
        service = Servlet.class,
        property = {
                SLING_SERVLET_METHODS + "=[GET,POST]",
                SLING_SERVLET_PATHS + "=/apps/etoolbox-query-kit/services/query"
        })
public class QueryServlet extends SlingAllMethodsServlet {

    @Reference
    private QueryService queryService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        SearchRequest searchRequest = SearchRequest.from(request);
        if (!searchRequest.isValid()) {
            ResponseUtil.sendError(response, "Invalid request");
            return;
        }
        SearchResult searchResult = queryService.execute(searchRequest);
        switch (searchRequest.getFormat()) {
            case HTML:
            case CSV:
            default:
                ResponseUtil.sendJson(response, JsonExportUtil.export(searchResult));
        }
    }
}

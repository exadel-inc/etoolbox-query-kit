package com.exadel.etoolbox.query.core.servlets;

import com.exadel.etoolbox.query.core.services.QueryConverterService;
import com.exadel.etoolbox.query.core.services.QueryExecutorService;
import com.exadel.etoolbox.query.core.models.QueryResultModel;
import com.google.gson.Gson;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.query.qom.QueryObjectModel;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/services/etoolbox-query-kit/execute",
                "sling.servlet.methods=[post]"
        })
public class ExecuteQueryServlet extends SlingAllMethodsServlet {

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final Gson GSON = new Gson();

    @Reference
    private transient QueryConverterService queryConverterService;

    @Reference
    private transient QueryExecutorService queryExecutorService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        ResourceResolver resolver = request.getResourceResolver();
        QueryResultModel queryResultModel = new QueryResultModel(request);
        if (!queryResultModel.isValid()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        QueryObjectModel queryObjectModel = queryConverterService.convertQueryToJqom(resolver, queryResultModel);
        if (queryObjectModel != null) {
            queryExecutorService.executeJqomQuery(queryObjectModel, queryResultModel);
            String s = GSON.toJson(queryResultModel);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(CONTENT_TYPE_JSON);
            response.getWriter().write(s);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
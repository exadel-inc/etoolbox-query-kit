package com.exadel.etoolbox.query.core.servlets;

import com.exadel.etoolbox.query.core.services.ExecuteQueryService;
import com.exadel.etoolbox.query.core.services.QueryConverterService;
import com.exadel.etoolbox.query.core.servlets.model.QueryResultModel;
import com.google.gson.Gson;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.query.qom.QueryObjectModel;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/services/etoolbox-query-kit/execute",
                "sling.servlet.methods=[post]"
        })
public class ExecuteQueryServlet extends SlingAllMethodsServlet {

    private static final String APPLICATION_JSON = "application/json";

    @Reference
    private QueryConverterService queryConverterService;

    @Reference
    private ExecuteQueryService executeQueryService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        ResourceResolver resolver = request.getResourceResolver();
        QueryResultModel queryResultModel = new QueryResultModel(request);
        if (queryResultModel.isValid()) {
            QueryObjectModel queryObjectModel = queryConverterService.convertQueryToJQOM(resolver, queryResultModel);
            if (queryObjectModel != null) {
                executeQueryService.executeJQOMQuery(queryObjectModel, queryResultModel);
                String s = new Gson().toJson(queryResultModel);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(APPLICATION_JSON);
                response.getWriter().write(s);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
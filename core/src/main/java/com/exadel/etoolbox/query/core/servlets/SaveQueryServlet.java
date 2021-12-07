package com.exadel.etoolbox.query.core.servlets;

import com.exadel.etoolbox.query.core.services.SavedQueryService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/services/etoolbox-query-kit/save",
                "sling.servlet.methods=[post]"
        })
public class SaveQueryServlet extends SlingAllMethodsServlet {

    private static final String APPLICATION_JSON = "application/json";

    @Reference
    private SavedQueryService savedQueryService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        ResourceResolver resourceResolver = request.getResourceResolver();
        String query = savedQueryService.saveQuery(resourceResolver, request.getParameterMap());
        if (query != null) {
            response.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

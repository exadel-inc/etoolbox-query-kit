package com.exadel.etoolbox.query.core.servlets;

import com.exadel.etoolbox.query.core.services.SavedQueryService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/services/etoolbox-query-kit/edit",
                "sling.servlet.methods=[post]"
        })
public class EditQueriesServlet extends SlingAllMethodsServlet {

    @Reference
    private SavedQueryService savedQueryService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, List<String>> languageToQueries = savedQueryService.editQueries(request.getResourceResolver(), parameterMap);
        if (languageToQueries != null) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

package com.exadel.etoolbox.query.core.servlets;

import com.exadel.etoolbox.query.core.services.QueryConstructorService;
import com.exadel.etoolbox.query.core.servlets.model.QueryConstructorModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/services/etoolbox-query-kit/constructor",
                "sling.servlet.methods=[post]"
        })
public class ConstructorQueryServlet extends SlingAllMethodsServlet {

    @Reference
    private QueryConstructorService queryConstructorService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        QueryConstructorModel queryConstructorModel = new QueryConstructorModel(parameterMap);
        String query = queryConstructorService.convertConstructorToSql2Query(request.getResourceResolver(), queryConstructorModel);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(query);
    }
}

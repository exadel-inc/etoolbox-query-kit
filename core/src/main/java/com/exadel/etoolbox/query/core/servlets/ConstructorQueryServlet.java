package com.exadel.etoolbox.query.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/services/etoolbox-query-kit/constructor",
                "sling.servlet.methods=[post]"
        })
public class ConstructorQueryServlet extends SlingAllMethodsServlet {

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
    }
}

package com.exadel.etoolbox.querykit.core.servlets;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.List;

import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

@Component(
        service = Servlet.class,
        property = {
                SLING_SERVLET_METHODS + "=GET",
                SLING_SERVLET_PATHS + "=/apps/etoolbox-query-kit/datasources/dialog"
        })
public class DialogDatasourceServlet extends SlingSafeMethodsServlet {

    private static final String DIALOGS_ROOT = "/conf/etoolbox-query-kit/settings/dialogs";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {

        Resource resource = request.getResourceResolver().getResource(DIALOGS_ROOT);
        if (resource == null || !resource.hasChildren()) {
            return;
        }
        List<Resource> components = new ArrayList<>();
        for (Resource current : resource.getChildren()) {
            Resource menuItem = current.getChild("menuItem");
            if (menuItem == null || Boolean.TRUE.equals(menuItem.getValueMap().get("disabled", boolean.class))) {
                continue;
            }
            components.add(menuItem);
        }
        DataSource dataSource = new SimpleDataSource(components.iterator());
        request.setAttribute(DataSource.class.getName(), dataSource);
    }
}
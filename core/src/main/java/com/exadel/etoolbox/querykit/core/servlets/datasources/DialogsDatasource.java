package com.exadel.etoolbox.querykit.core.servlets.datasources;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.exadel.etoolbox.querykit.core.models.request.RequestAttributes;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
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
                SLING_SERVLET_PATHS + "=/apps/etoolbox-query-kit/datasources/dialogs"
        })
public class DialogsDatasource extends SlingSafeMethodsServlet {

    private static final String FOLDER_DIALOGS = "/conf/etoolbox-query-kit/settings/dialogs";
    private static final String FOLDER_CUSTOM_DIALOGS = "/conf/etoolbox-query-kit/settings/custom-dialogs";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        List<Resource> components = new ArrayList<>();
        feedFolderContent(request.getResourceResolver(), FOLDER_DIALOGS, components);
        feedFolderContent(request.getResourceResolver(), FOLDER_CUSTOM_DIALOGS, components);
        DataSource dataSource = new SimpleDataSource(components.iterator());
        request.setAttribute(DataSource.class.getName(), dataSource);
    }

    private static void feedFolderContent(ResourceResolver resourceResolver, String folder, List<Resource> accumulator) {
        Resource resource = resourceResolver.getResource(folder);
        if (resource == null || !resource.hasChildren()) {
            return;
        }
        for (Resource current : resource.getChildren()) {
            Resource menuItem = current.getChild("menuItem");
            if (menuItem == null || Boolean.TRUE.equals(menuItem.getValueMap().get("disabled", Boolean.class))) {
                continue;
            }
            accumulator.add(menuItem);
        }
    }
}
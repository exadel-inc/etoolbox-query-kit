package com.exadel.etoolbox.querykit.core.servlets;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=/apps/etoolbox-query-kit/services/edit-cell",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class EditorCellServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        ResourceResolver resourceResolver = request.getResourceResolver();

        String path = request.getParameter("data-path");
        String property = request.getParameter("data-name");
        String type = request.getParameter("data-type");
        Resource resource = resourceResolver.getResource(path);
        if (resource == null) {
            request.setAttribute(DataSource.class.getName(), EmptyDataSource.instance());
            return;
        }

        List<Resource> resources = new ArrayList<>();
        String value = resource.getValueMap().get(property, String.class);

        ValueMap vm = new ValueMapDecorator(new LinkedHashMap<>());
        vm.put(Constants.PROPERTY_NAME, "./" + property);
        vm.put(Constants.PROPERTY_VALUE, value);
        vm.put("fieldLabel", property);
        resources.add(new ValueMapResource(resourceResolver, "", type, vm));

        vm = new ValueMapDecorator(new LinkedHashMap<>());
        vm.put(Constants.PROPERTY_NAME, Constants.PROPERTY_PATH);
        vm.put(Constants.PROPERTY_VALUE, path);
        vm.put("granite:hidden", true);
        resources.add(new ValueMapResource(resourceResolver, "", "granite/ui/components/coral/foundation/form/textfield", vm));
        DataSource dataSource = new SimpleDataSource(resources.iterator());

        request.setAttribute(DataSource.class.getName(), dataSource);
    }
}

package com.exadel.etoolbox.query.core.servlets;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
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
                "sling.servlet.resourceTypes=/services/etoolbox-query-kit/edit-cell",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class EditorCellServlet extends SlingSafeMethodsServlet {

        @Override
        protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
                ResourceResolver resourceResolver = request.getResourceResolver();

                String path = request.getParameter("path");
                String property = request.getParameter("property");
                Resource resource = resourceResolver.getResource(path);
                if (resource == null) {
                        request.setAttribute(DataSource.class.getName(), EmptyDataSource.instance());
                        return;
                }

                List<Resource> resources = new ArrayList<>();
                String s = resource.getValueMap().get(property, String.class);

                ValueMap vm = new ValueMapDecorator(new LinkedHashMap<>());
                vm.put("name", "./" + property);
                vm.put("value", s);
                vm.put("fieldLabel", property);
                resources.add(new ValueMapResource(resourceResolver, "", "granite/ui/components/coral/foundation/form/textfield", vm));

                vm = new ValueMapDecorator(new LinkedHashMap<>());
                vm.put("name", "path");
                vm.put("value", path);
                vm.put("granite:hidden", true);
                resources.add(new ValueMapResource(resourceResolver, "", "granite/ui/components/coral/foundation/form/textfield", vm));
                DataSource dataSource = new SimpleDataSource(resources.iterator());

                request.setAttribute(DataSource.class.getName(), dataSource);
        }
}

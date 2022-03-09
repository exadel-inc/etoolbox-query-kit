/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Serves requests for rendering query results cell editor UI
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=/apps/etoolbox-query-kit/services/edit-cell",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class EditorCellServlet extends SlingSafeMethodsServlet {

    /**
     * Processes HTTP {@code GET} requests
     * @param request {@code SlingHttpServletRequest} object
     * @param response {@code slingHttpServletResponse} object
     * @throws IOException If the request processing failed
     */
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

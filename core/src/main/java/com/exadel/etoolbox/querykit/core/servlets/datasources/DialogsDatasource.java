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
package com.exadel.etoolbox.querykit.core.servlets.datasources;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
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

/**
 * Serves requests for a list of available query dialogs
 */
@Component(
        service = Servlet.class,
        property = {
                SLING_SERVLET_METHODS + "=GET",
                SLING_SERVLET_PATHS + "=/apps/etoolbox-query-kit/datasources/dialogs"
        })
public class DialogsDatasource extends SlingSafeMethodsServlet {

    private static final String FOLDER_DIALOGS = "/conf/etoolbox-query-kit/settings/dialogs";
    private static final String FOLDER_CUSTOM_DIALOGS = "/conf/etoolbox-query-kit/settings/custom-dialogs";

    /**
     * Processes HTTP {@code GET} requests. Sets the list of retrieved dialogs as a request attribute per the format of
     * Granite items datasource
     * @param request  {@code SlingHttpServletRequest} object
     * @param response {@code slingHttpServletResponse} object
     */
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
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
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Serves requests for a list of available profiles
 */
@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=/apps/etoolbox-query-kit/datasources/profiles",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class ProfilesDatasource extends SlingSafeMethodsServlet {

    private static final String PROFILES_ROOT = "/conf/etoolbox-query-kit/settings/profiles/jcr:content";

    /**
     * Processes HTTP {@code GET} requests. Sets the list of retrieved dialogs as a request attribute per the format of
     * Granite items datasource
     * @param request  {@code SlingHttpServletRequest} object
     * @param response {@code slingHttpServletResponse} object
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        ResourceResolver resourceResolver = request.getResourceResolver();
        List<Resource> profiles = getProfiles(resourceResolver);
        if (!profiles.isEmpty()) {
            DataSource dataSource = new SimpleDataSource(profiles.iterator());
            request.setAttribute(DataSource.class.getName(), dataSource);
        }
    }

    private static List<Resource> getProfiles(ResourceResolver resourceResolver) {
        Resource resource = resourceResolver.getResource(PROFILES_ROOT);
        if (resource == null) {
            return Collections.emptyList();
        }
        Iterable<Resource> children = resource.getChildren();
        return StreamSupport.stream(children.spliterator(), false)
                .map(profileNode -> {
                    String profileName = StringUtils.defaultString(
                            profileNode.getValueMap().get(Constants.PROPERTY_JCR_TITLE, String.class),
                            profileNode.getName());
                    return ImmutableMap.<String, Object>of(
                            Constants.PROPERTY_TEXT, profileName,
                            Constants.PROPERTY_VALUE, profileName.toLowerCase());
                })
                .map(ValueMapDecorator::new)
                .map(valueMap -> (Resource) new ValueMapResource(resourceResolver, StringUtils.EMPTY, StringUtils.EMPTY, valueMap))
                .collect(Collectors.toList());
    }
}
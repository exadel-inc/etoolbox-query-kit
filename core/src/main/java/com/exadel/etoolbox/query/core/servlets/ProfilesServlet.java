package com.exadel.etoolbox.query.core.servlets;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=/services/etoolbox-query-kit/profiles",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class ProfilesServlet extends SlingSafeMethodsServlet {

    private static final String PROFILES_NODE_PATH = "/etc/etoolbox-query-kit/profiles";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {

        DataSource dataSource = EmptyDataSource.instance();
        ResourceResolver resourceResolver = request.getResourceResolver();
        Resource resource = resourceResolver.getResource(PROFILES_NODE_PATH);
        if (resource != null) {
            Iterable<Resource> children = resource.getChildren();

            List<Resource> profilesResources = StreamSupport.stream(children.spliterator(), false)
                    .map(profile -> {
                        Map<String, Object> values = new HashMap<>();
                        values.put("value", profile.getValueMap().get("jcr:title", String.class));
                        values.put("text", profile.getValueMap().get("jcr:title", String.class));
                        return values;
                    })
                    .map(ValueMapDecorator::new)
                    .map(valueMap -> (Resource) new ValueMapResource(resourceResolver, StringUtils.EMPTY, StringUtils.EMPTY, valueMap))
                    .collect(Collectors.toList());

            dataSource = new SimpleDataSource(profilesResources.iterator());
        }

        request.setAttribute(DataSource.class.getName(), dataSource);
    }
}

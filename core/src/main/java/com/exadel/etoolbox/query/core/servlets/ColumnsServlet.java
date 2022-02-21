package com.exadel.etoolbox.query.core.servlets;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.EmptyDataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.exadel.etoolbox.query.core.models.QueryResultModel;
import com.exadel.etoolbox.query.core.services.QueryConverterService;
import com.exadel.etoolbox.query.core.services.QueryExecutorService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.query.qom.QueryObjectModel;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=/services/etoolbox-query-kit/columns",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class ColumnsServlet extends SlingSafeMethodsServlet {

    @Reference
    private transient QueryConverterService queryConverterService;

    @Reference
    private transient QueryExecutorService queryExecutorService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        ResourceResolver resourceResolver = request.getResourceResolver();
        DataSource dataSource = EmptyDataSource.instance();

        QueryResultModel queryResultModel = new QueryResultModel(request);
        QueryObjectModel queryObjectModel = queryConverterService.convertQueryToJqom(resourceResolver, queryResultModel);
        if (queryObjectModel == null) {
            return;
        }
        List<Resource> results = queryExecutorService.executeJqomQuery(resourceResolver, queryObjectModel);

        List<Resource> resources = new ArrayList<>();

        List<String> list = new ArrayList<>(results.get(0).getValueMap().keySet());
        list.forEach(column -> {
            ValueMap vm = new ValueMapDecorator(new LinkedHashMap<>());
            vm.put("jcr:title", column);
            resources.add(new ValueMapResource(resourceResolver, "", "", vm));
        });

        dataSource = new SimpleDataSource(resources.iterator());

        request.setAttribute(DataSource.class.getName(), dataSource);
    }
}

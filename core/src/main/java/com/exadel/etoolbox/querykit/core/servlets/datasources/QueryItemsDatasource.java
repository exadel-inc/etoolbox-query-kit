package com.exadel.etoolbox.querykit.core.servlets.datasources;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.services.query.QueryService;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=/apps/etoolbox-query-kit/datasources/queryItems",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class QueryItemsDatasource extends SlingSafeMethodsServlet {

    private static final String ITEM_RESOURCE_TYPE = "/apps/etoolbox-query-kit/components/console/tableRow";

    @Reference
    private transient QueryService queryService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        SearchRequest searchRequest = SearchRequest.from(request);
        SearchResult searchResult = queryService.execute(searchRequest);

        List<Resource> resources = searchResult
                .getItems()
                .stream()
                .map(item -> item.toVirtualResource(request.getResourceResolver(), searchResult.getColumns(), ITEM_RESOURCE_TYPE))
                .collect(Collectors.toList());

        DataSource dataSource = new SimpleDataSource(resources.iterator());
        request.setAttribute(DataSource.class.getName(), new MeasuredDatasourceAdapter(dataSource, searchResult.getTotal()));

        request.setAttribute(Constants.ATTRIBUTE_TOTAL, searchResult.getTotal());
        request.setAttribute(Constants.ATTRIBUTE_EXECUTION_TIME, searchResult.getExecutionTime());
        request.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, searchResult.getErrorMessage());
    }
}
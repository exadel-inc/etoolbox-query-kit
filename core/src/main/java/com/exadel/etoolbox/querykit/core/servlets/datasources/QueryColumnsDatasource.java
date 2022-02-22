package com.exadel.etoolbox.querykit.core.servlets.datasources;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.services.query.QueryService;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.util.List;
import java.util.stream.Collectors;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.resourceTypes=/apps/etoolbox-query-kit/datasources/queryColumns",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class QueryColumnsDatasource extends SlingSafeMethodsServlet {

    @Reference
    private transient QueryService queryService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        SearchRequest searchRequest = SearchRequest.from(request);
        SearchResult searchResult = queryService.dryRun(searchRequest);

        if (!searchResult.isSuccess() || searchResult.getColumns() == null) {
            return;
        }

        List<Resource> resources = searchResult
                .getColumns()
                .getColumnNames()
                .stream()
                .map(name -> ImmutableMap.<String, Object>of(Constants.PROPERTY_JCR_TITLE, name))
                .map(ValueMapDecorator::new)
                .map(valueMap -> new ValueMapResource(request.getResourceResolver(), StringUtils.EMPTY, StringUtils.EMPTY, valueMap))
                .collect(Collectors.toList());

        Resource selectableCell = new ValueMapResource(request.getResourceResolver(), StringUtils.EMPTY, StringUtils.EMPTY, new ValueMapDecorator(ImmutableMap.<String, Object>of("select", true)));
        resources.add(0, selectableCell);

        if (!resources.isEmpty()) {
            request.setAttribute(DataSource.class.getName(), new SimpleDataSource(resources.iterator()));
        }
    }
}
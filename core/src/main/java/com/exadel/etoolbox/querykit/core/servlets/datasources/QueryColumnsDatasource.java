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

/**
 * Serves requests for a collection of query results table columns
 */
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

    /**
     * Processes HTTP {@code GET} requests. Sets the list of retrieved dialogs as a request attribute per the format of
     * Granite Table columns datasource
     * @param request  {@code SlingHttpServletRequest} object
     * @param response {@code slingHttpServletResponse} object
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        SearchRequest searchRequest = SearchRequest.from(request);
        SearchResult searchResult = queryService.dryRun(searchRequest);

        if (!searchResult.isSuccess() || searchResult.getColumns() == null) {
            return;
        }

        List<Resource> allColumns = searchResult
                .getColumns()
                .getColumnNames()
                .stream()
                .map(name -> ImmutableMap.<String, Object>of(Constants.PROPERTY_JCR_TITLE, name))
                .map(ValueMapDecorator::new)
                .map(valueMap -> new ValueMapResource(request.getResourceResolver(), StringUtils.EMPTY, StringUtils.EMPTY, valueMap))
                .collect(Collectors.toList());

        Resource numberColumn = new ValueMapResource(
                request.getResourceResolver(),
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                new ValueMapDecorator(ImmutableMap.of(Constants.PROPERTY_JCR_TITLE, "#", "fixedWidth", Boolean.TRUE.toString())));
        allColumns.add(0, numberColumn);

        request.setAttribute(DataSource.class.getName(), new SimpleDataSource(allColumns.iterator()));
    }
}
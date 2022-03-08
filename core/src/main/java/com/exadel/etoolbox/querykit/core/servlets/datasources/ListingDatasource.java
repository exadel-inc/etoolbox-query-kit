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
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.models.search.SearchResultFormat;
import com.exadel.etoolbox.querykit.core.services.modifiers.impl.ListItemConverterFactory;
import com.exadel.etoolbox.querykit.core.services.query.QueryService;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.exadel.etoolbox.querykit.core.utils.ResponseUtil;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

@Component(
        service = Servlet.class,
        property = {
                SLING_SERVLET_METHODS + "=GET",
                SLING_SERVLET_PATHS + "=/apps/etoolbox-query-kit/datasources/itemlist"
        })
@Slf4j
public class ListingDatasource extends SlingSafeMethodsServlet {

    @Reference
    private QueryService queryService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        SearchRequest searchRequest = SearchRequest.from(request, request.getResource().getChild(Constants.NODE_DATASOURCE));
        searchRequest.getItemConverters().add(ListItemConverterFactory.NAME);

        if (!searchRequest.isValid()) {
            if (searchRequest.getResultFormat() == SearchResultFormat.JSON) {
                ResponseUtil.sendJsonError(response, "Invalid request");
            }
            return;
        }

        SearchResult searchResult = queryService.execute(searchRequest);
        if (!searchResult.isSuccess()) {
            if (searchRequest.getResultFormat() == SearchResultFormat.JSON) {
                ResponseUtil.sendJsonError(response, searchResult.getErrorMessage());
            }
            return;
        }

        if (searchRequest.getResultFormat() == SearchResultFormat.JSON) {
            outputJson(request, response, searchResult);
        } else {
            outputDatasource(request, searchResult);
        }
    }

    private static void outputJson(
            SlingHttpServletRequest request,
            SlingHttpServletResponse response,
            SearchResult result) throws IOException {

        List<Resource> itemResources = getItemResources(request, result);
        response.setContentType(Constants.CONTENT_TYPE_JSON);
        try (JsonWriter writer = new JsonWriter(response.getWriter())) {
            writer.beginArray();
            for (Resource resource : itemResources) {
                writer.beginObject()
                        .name(Constants.PROPERTY_TEXT).value(resource.getValueMap().get(Constants.PROPERTY_TEXT, String.class))
                        .name(Constants.PROPERTY_VALUE).value(resource.getValueMap().get(Constants.PROPERTY_VALUE, String.class))
                        .endObject();
            }
            writer.endArray();
        }
    }

    private static void outputDatasource(
            SlingHttpServletRequest request,
            SearchResult result) {

        List<Resource> itemResources = getItemResources(request, result);
        if (itemResources.isEmpty()) {
            return;
        }
        DataSource dataSource = new SimpleDataSource(itemResources.iterator());
        request.setAttribute(DataSource.class.getName(), dataSource);
    }

    private static List<Resource> getItemResources(
            SlingHttpServletRequest request,
            SearchResult result) {

        if (!result.isSuccess() || result.getItems().isEmpty()) {
            return Collections.emptyList();
        }
        return result
                .getItems()
                .stream()
                .map(item -> item.toVirtualResource(request.getResourceResolver()))
                .collect(Collectors.toList());
    }
}

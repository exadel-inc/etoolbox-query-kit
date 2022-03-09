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

import com.exadel.etoolbox.querykit.core.models.search.QueryRenderingFormat;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.query.QueryParserService;
import com.exadel.etoolbox.querykit.core.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

/**
 * Serves requests for parsing queries
 */
@Component(
        service = Servlet.class,
        property = {
                SLING_SERVLET_METHODS + "=[GET,POST]",
                SLING_SERVLET_PATHS + "=/apps/etoolbox-query-kit/services/parse"
        })
@Slf4j
public class QueryParsingServlet extends SlingAllMethodsServlet {

    @Reference
    private QueryParserService parserService;

    /**
     * Processes HTTP {@code GET} requests
     * @param request {@code SlingHttpServletRequest} object
     * @param response {@code slingHttpServletResponse} object
     * @throws IOException If the request processing failed
     */
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    /**
     * Processes HTTP {@code POST} requests
     * @param request {@code SlingHttpServletRequest} object
     * @param response {@code slingHttpServletResponse} object
     * @throws IOException If the request processing failed
     */
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        SearchRequest searchRequest = SearchRequest.from(request);
        if (!searchRequest.isValid()) {
            sendError(response, "Invalid request", searchRequest.getRenderingFormat());
            return;
        }

        try {
            String result = parserService.parse(searchRequest);
            if (searchRequest.getRenderingFormat() == QueryRenderingFormat.JSON) {
                ResponseUtil.sendJson(response, result);
            } else {
                ResponseUtil.sendString(response, result);
            }
        } catch (Exception e) {
            log.error("Could not parse statement {}", searchRequest.getStatement(), e);
            sendError(response, e.getMessage(), searchRequest.getRenderingFormat());
        }
    }

    private static void sendError(SlingHttpServletResponse response, String value, QueryRenderingFormat format) throws IOException {
        if (format == QueryRenderingFormat.JSON) {
            ResponseUtil.sendJsonError(response, value);
        } else {
            ResponseUtil.sendStringError(response, value);
        }
    }
}

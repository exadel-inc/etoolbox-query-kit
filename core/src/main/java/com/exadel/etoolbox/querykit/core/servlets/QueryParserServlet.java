package com.exadel.etoolbox.querykit.core.servlets;

import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterBundle;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import com.exadel.etoolbox.querykit.core.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.List;

import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;

@Component(
        service = Servlet.class,
        property = {
                SLING_SERVLET_METHODS + "=[GET,POST]",
                SLING_SERVLET_PATHS + "=/apps/etoolbox-query-kit/services/parse"
        })
@Slf4j
public class QueryParserServlet extends SlingAllMethodsServlet {

    @Reference(cardinality = ReferenceCardinality.MULTIPLE)
    private List<QueryConverter> queryConverters;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        SearchRequest searchRequest = SearchRequest.from(request);
        if (!searchRequest.isValid()) {
            ResponseUtil.sendError(response, "Invalid request");
            return;
        }

        QueryConverter queryConverter = pickConverter(searchRequest);
        if (queryConverter == null) {
            log.error("Could not find a converter for statement {}", searchRequest.getStatement());
            ResponseUtil.sendError(response, "Converter not found");
            return;
        }

        try {
            QomAdapterBundle qomAdapterBundle = queryConverter.convert(
                    searchRequest.getStatement(),
                    request.getResourceResolver(),
                    QomAdapterBundle.class);
            ResponseUtil.sendJson(response, qomAdapterBundle.toJson());
        } catch (ConverterException e) {
            log.error("Could not parse statement {}", searchRequest.getStatement(), e);
            ResponseUtil.sendError(response, e.getMessage());
        }
    }

    private QueryConverter pickConverter(SearchRequest searchRequest) {
        return CollectionUtils.emptyIfNull(queryConverters)
                .stream()
                .filter(converter -> converter.getSourceType() == searchRequest.getType())
                .findFirst()
                .orElse(null);
    }
}

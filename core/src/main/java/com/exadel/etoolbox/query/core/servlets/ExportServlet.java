package com.exadel.etoolbox.query.core.servlets;

import com.exadel.etoolbox.query.core.services.ExecuteQueryService;
import com.exadel.etoolbox.query.core.services.PDFExporterService;
import com.exadel.etoolbox.query.core.services.QueryConverterService;
import com.exadel.etoolbox.query.core.services.XLSXExporterService;
import com.exadel.etoolbox.query.core.servlets.model.QueryResultModel;
import com.google.gson.Gson;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.query.qom.QueryObjectModel;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/services/etoolbox-query-kit/export",
                "sling.servlet.methods=[post]"
        })
public class ExportServlet extends SlingAllMethodsServlet {

    private static final String CONTENT_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String CONTENT_TYPE_PDF = "application/pdf";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final Gson GSON = new Gson();

    @Reference
    private QueryConverterService queryConverterService;

    @Reference
    private ExecuteQueryService executeQueryService;

    @Reference
    private XLSXExporterService xlsxExporterService;

    @Reference
    private PDFExporterService pdfExporterService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        String format = request.getParameter("format");
        ResourceResolver resolver = request.getResourceResolver();
        QueryResultModel queryResultModel = new QueryResultModel(request);
        QueryObjectModel queryObjectModel = queryConverterService.convertQueryToJQOM(resolver, queryResultModel);
        executeQueryService.executeJQOMQuery(queryObjectModel, queryResultModel);
        //TODO little service for the end-user and think of giving files more descriptive names (instead result)
        switch (format) {
            case "XSLX": {
                response.setContentType(CONTENT_TYPE_EXCEL);
                response.setHeader(HEADER_CONTENT_DISPOSITION, "attachment; filename=result.xlsx");
                response.setStatus(200);
                xlsxExporterService.export(outputStream, "title", queryResultModel.getHeaders(), queryResultModel.getData());
                break;
            }
            case "PDF": {
                response.setContentType(CONTENT_TYPE_PDF);
                response.setHeader(HEADER_CONTENT_DISPOSITION, "attachment; filename=result.pdf");
                response.setStatus(200);
                pdfExporterService.export(outputStream, queryResultModel.getHeaders().keySet(), queryResultModel.getData());
                break;
            }
            case "JSON": {
                response.setContentType(CONTENT_TYPE_JSON);
                response.setHeader(HEADER_CONTENT_DISPOSITION, "attachment; filename=result.json");
                response.setStatus(200);
                outputStream.print(GSON.toJson(queryResultModel.getData()
                        .stream()
                        .map(stringMap -> stringMap.values().stream().findFirst())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList())
                ));
                break;
            }
            default: {
                throw new ServletException();
            }
        }
        outputStream.flush();
        outputStream.close();
    }
}
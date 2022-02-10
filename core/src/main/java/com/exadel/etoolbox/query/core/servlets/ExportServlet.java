package com.exadel.etoolbox.query.core.servlets;

import com.exadel.etoolbox.query.core.services.PdfExporterService;
import com.exadel.etoolbox.query.core.services.QueryConverterService;
import com.exadel.etoolbox.query.core.services.QueryExecutorService;
import com.exadel.etoolbox.query.core.services.XlsxExporterService;
import com.exadel.etoolbox.query.core.models.QueryResultModel;
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
    private transient QueryConverterService queryConverterService;

    @Reference
    private transient QueryExecutorService queryExecutorService;

    @Reference
    private transient XlsxExporterService xlsxExporterService;

    @Reference
    private transient PdfExporterService pdfExporterService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        ServletOutputStream outputStream = response.getOutputStream();
        String format = request.getParameter("format");
        ResourceResolver resolver = request.getResourceResolver();
        QueryResultModel queryResultModel = new QueryResultModel(request);
        QueryObjectModel queryObjectModel = queryConverterService.convertQueryToJqom(resolver, queryResultModel);
        queryExecutorService.executeJqomQuery(queryObjectModel, queryResultModel);
        //TODO little service for the end-user and think of giving files more descriptive names (instead result)
        switch (format) {
            case "XSLX": {
                response.setContentType(CONTENT_TYPE_EXCEL);
                response.setHeader(HEADER_CONTENT_DISPOSITION, "attachment; filename=result.xlsx");
                xlsxExporterService.export(outputStream, queryResultModel.getColumns(), queryResultModel.getData());
                break;
            }
            case "PDF": {
                response.setContentType(CONTENT_TYPE_PDF);
                response.setHeader(HEADER_CONTENT_DISPOSITION, "attachment; filename=result.pdf");
                pdfExporterService.export(outputStream, queryResultModel.getColumns().keySet(), queryResultModel.getData());
                break;
            }
            case "JSON": {
                response.setContentType(CONTENT_TYPE_JSON);
                response.setHeader(HEADER_CONTENT_DISPOSITION, "attachment; filename=result.json");
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
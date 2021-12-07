package com.exadel.etoolbox.query.core.servlets;

import com.exadel.etoolbox.query.core.services.ExecuteQueryService;
import com.exadel.etoolbox.query.core.services.PDFExporterService;
import com.exadel.etoolbox.query.core.services.QueryConverterService;
import com.exadel.etoolbox.query.core.services.XLSXExporterService;
import com.exadel.etoolbox.query.core.servlets.model.QueryResultModel;
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

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/services/etoolbox-query-kit/export",
                "sling.servlet.methods=[post]"
        })
public class ExportServlet extends SlingAllMethodsServlet {

    private static final String APPLICATION_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String APPLICATION_PDF = "application/pdf";

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
        switch (format) {
            case "XSLX" : {
                response.setContentType(APPLICATION_EXCEL);
                response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
                response.setStatus(200);
                xlsxExporterService.export(outputStream, "title", queryResultModel.getHeaders(), queryResultModel.getData());
                break;
            }
            case "PDF" : {
                response.setContentType(APPLICATION_PDF);
                response.setHeader("Content-Disposition", "attachment; filename=users.pdf");
                response.setStatus(200);
                pdfExporterService.export(outputStream, queryResultModel.getHeaders().keySet(), queryResultModel.getData());
                break;
            }
        }
        outputStream.flush();
        outputStream.close();
    }
}
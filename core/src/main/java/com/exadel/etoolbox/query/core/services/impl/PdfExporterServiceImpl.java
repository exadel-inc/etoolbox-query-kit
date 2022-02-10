package com.exadel.etoolbox.query.core.services.impl;

import com.exadel.etoolbox.query.core.services.PdfExporterService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.osgi.service.component.annotations.Component;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component(service = PdfExporterService.class)
public class PdfExporterServiceImpl implements PdfExporterService {

    @Override
    public void export(OutputStream out, Set<String> headers, List<Map<String, String>> data) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            PdfPTable table = new PdfPTable(headers.size());
            addTableHeader(table, headers);
            data.forEach(row -> addRows(table, row));

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void addTableHeader(PdfPTable table, Set<String> headers) {
        headers.forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table, Map<String, String> row) {
        row.forEach((key, value) -> table.addCell(value));
    }
}
package com.exadel.etoolbox.query.core.services.impl;

import com.exadel.etoolbox.query.core.services.PdfExporterService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Component(service = PdfExporterService.class)
public class PdfExporterServiceImpl implements PdfExporterService {

    @Override
    public void export(OutputStream out, List<Resource> resources) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawTable(page, contentStream, 700.0f, 100.0f, resources);
            }
            document.save(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawTable(PDPage page, PDPageContentStream contentStream,
                                 float y, float margin, List<Resource> resources) throws IOException {
        int rows = resources.size();
        int cols = resources.get(0).getValueMap().size();
        float rowHeight = 20.0f;
        float tableWidth = page.getMediaBox().getWidth() - 2.0f * margin;
        float tableHeight = rowHeight * (float) rows;
        float colWidth = tableWidth / (float) cols;

        //draw horizontal lines
        float nexty = y ;
        for (int i = 0; i <= rows; i++) {
            contentStream.moveTo(margin, nexty);
            contentStream.lineTo(margin + tableWidth, nexty);
            contentStream.stroke();
            nexty-= rowHeight;
        }

        //draw vertical lines
        float nextx = margin;
        for (int i = 0; i <= cols; i++) {
            contentStream.moveTo(nextx, y);
            contentStream.lineTo(nextx, y - tableHeight);
            contentStream.stroke();
            nextx += colWidth;
        }

        //now add the text
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12.0f);

        float cellMargin = 5.0f;
        float textx = margin + cellMargin;
        float texty = y - 15.0f;
        for (Resource resource : resources) {
            for (Object text : resource.getValueMap().values()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(textx, texty);
                contentStream.showText((String) text);
                contentStream.endText();
                textx += colWidth;
            }
            texty -= rowHeight;
            textx = margin + cellMargin;
        }
    }
}
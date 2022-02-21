package com.exadel.etoolbox.query.core.services.impl;

import com.exadel.etoolbox.query.core.services.ExporterService;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component(service = HtmlExporterServiceImpl.class)
public class HtmlExporterServiceImpl implements ExporterService {

    @Override
    public void export(OutputStream out, List<Resource> resources) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html lang=\"en\"><body><table border=\"1\"><thead>");

        List<List<String>> headers = new ArrayList<>();
        headers.add(new ArrayList<>(resources.get(0).getValueMap().keySet()));
        appendRows(builder, headers);

        builder.append("</thead><tbody>");

        List<List<String>> rows = resources.stream()
                .map(resource -> resource
                        .getValueMap()
                        .values()
                        .stream()
                        .map(String.class::cast)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        appendRows(builder,rows);

        builder.append("</tbody></table></body></html>");

        out.write(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void appendRows(StringBuilder builder, List<List<String>> rows) {
        rows.forEach(row -> {
            builder.append("<tr>");
            row.forEach(cell -> builder.append("<td>").append(cell).append("</td>"));
            builder.append("</tr>");
        });
    }
}

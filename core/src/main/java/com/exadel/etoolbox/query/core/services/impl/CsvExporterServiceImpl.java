package com.exadel.etoolbox.query.core.services.impl;

import com.exadel.etoolbox.query.core.services.ExporterService;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component(service = CsvExporterServiceImpl.class)
public class CsvExporterServiceImpl implements ExporterService {

    @Override
    public void export(OutputStream out, List<Resource> resources) throws IOException {

        String headers = resources.get(0)
                .getValueMap()
                .keySet()
                .stream()
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));

        String content = resources.stream()
                .map(resource -> resource
                        .getValueMap()
                        .values()
                        .stream()
                        .map(value -> escapeSpecialCharacters((String) value))
                        .collect(Collectors.joining(",")))
                .collect(Collectors.joining(System.getProperty("line.separator")));

        String result = String.join(System.getProperty("line.separator"), headers, content);
        out.write(result.getBytes(StandardCharsets.UTF_8));
    }

    private String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}

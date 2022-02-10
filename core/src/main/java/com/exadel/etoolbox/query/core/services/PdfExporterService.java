package com.exadel.etoolbox.query.core.services;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PdfExporterService {
    void export(OutputStream out, Set<String> headers, List<Map<String, String>> data);
}

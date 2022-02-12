package com.exadel.etoolbox.querykit.core.services;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface PdfExporterService {
    void export(OutputStream out, Map<String, List<String>> data);
}

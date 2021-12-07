package com.exadel.etoolbox.query.core.services;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface XLSXExporterService {
    void export(final OutputStream out, final String title, final Map<String, String> headers, final List<Map<String, String>> data);
}

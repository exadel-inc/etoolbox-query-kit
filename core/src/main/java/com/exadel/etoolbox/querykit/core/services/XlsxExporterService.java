package com.exadel.etoolbox.querykit.core.services;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface XlsxExporterService {
    void export(final OutputStream out, final Map<String, String> columns, final List<Map<String, String>> data);
}

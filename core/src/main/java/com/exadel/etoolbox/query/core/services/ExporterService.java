package com.exadel.etoolbox.query.core.services;

import org.apache.sling.api.resource.Resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ExporterService {
    void export(OutputStream out, List<Resource> resources) throws IOException;
}

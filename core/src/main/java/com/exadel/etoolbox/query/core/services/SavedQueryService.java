package com.exadel.etoolbox.query.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;
import java.util.Map;

public interface SavedQueryService {
    Map<String, List<String>> getSavedQueries(ResourceResolver resolver, Map<String, String[]> parameterMap);
    String saveQuery(ResourceResolver resolver, Map<String, String[]> parameterMap);
    Map<String, List<String>> editQueries(ResourceResolver resolver, Map<String, String[]> parameterMap);
}

package com.exadel.etoolbox.query.core.model;

import com.exadel.etoolbox.query.core.services.SavedQueryService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;

import java.util.List;
import java.util.Map;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class SavedQueriesModel {

    @Self
    private SlingHttpServletRequest request;

    @OSGiService
    private SavedQueryService savedQueryService;

    public Map<String, List<String>> getQueries() {
        return savedQueryService.getSavedQueries(request.getResourceResolver(), request.getParameterMap());
    }
}
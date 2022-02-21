package com.exadel.etoolbox.query.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import java.util.Map;

@Model(adaptables = SlingHttpServletRequest.class)
public class TableRowModel {

    @Self
    private SlingHttpServletRequest request;

    private Map<String, Object> properties;

    @PostConstruct
    protected void init() {
        Resource resource = request.getResource();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}

package com.exadel.etoolbox.querykit.core.models;

import com.day.crx.JcrConstants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;

import java.util.LinkedHashMap;

@RequiredArgsConstructor
public class SearchItem extends AbstractResource {

    private final String path;
    private final ResourceResolver resourceResolver;

    private final ValueMapDecorator properties = new ValueMapDecorator(new LinkedHashMap<>());

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getResourceType() {
        Object value = properties.get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY);
        return value != null
                ? StringUtils.defaultIfBlank(value.toString(), JcrConstants.NT_UNSTRUCTURED)
                :  JcrConstants.NT_UNSTRUCTURED;
    }

    @Override
    public String getResourceSuperType() {
        Object value = properties.get(JcrResourceConstants.SLING_RESOURCE_SUPER_TYPE_PROPERTY);
        return value != null
                ? StringUtils.defaultIfBlank(value.toString(), JcrConstants.NT_UNSTRUCTURED)
                :  JcrConstants.NT_UNSTRUCTURED;
    }

    @Override
    public ResourceMetadata getResourceMetadata() {
        return null;
    }

    @Override
    public ResourceResolver getResourceResolver() {
        return resourceResolver;
    }

    @Override
    public ValueMap getValueMap() {
        return properties;
    }

    public Resource toJcrResource() {
        return resourceResolver.getResource(path);
    }
}

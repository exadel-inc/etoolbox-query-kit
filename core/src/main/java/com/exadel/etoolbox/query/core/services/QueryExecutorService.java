package com.exadel.etoolbox.query.core.services;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.query.qom.QueryObjectModel;
import java.util.List;

public interface QueryExecutorService {
    List<Resource> executeJqomQuery(ResourceResolver resolver, QueryObjectModel queryObjectModel);
}

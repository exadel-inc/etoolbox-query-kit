package com.exadel.etoolbox.querykit.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.query.qom.QueryObjectModel;

public interface QueryConverterService {
    QueryObjectModel convertQueryToJqom(ResourceResolver resourceResolver, QueryResultModel queryResultModel);
}

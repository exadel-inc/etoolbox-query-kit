package com.exadel.etoolbox.query.core.services;

import com.exadel.etoolbox.query.core.servlets.model.QueryResultModel;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.query.qom.QueryObjectModel;

public interface QueryConverterService {
    QueryObjectModel convertQueryToJQOM(ResourceResolver resourceResolver, QueryResultModel queryResultModel);
}

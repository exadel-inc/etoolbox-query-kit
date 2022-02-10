package com.exadel.etoolbox.query.core.services;

import com.exadel.etoolbox.query.core.models.QueryResultModel;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.query.qom.QueryObjectModel;

public interface QueryConverterService {
    QueryObjectModel convertQueryToJqom(ResourceResolver resourceResolver, QueryResultModel queryResultModel);
}

package com.exadel.etoolbox.query.core.services;

import com.exadel.etoolbox.query.core.servlets.model.QueryConstructorModel;
import org.apache.sling.api.resource.ResourceResolver;

public interface QueryConstructorService {
    String convertConstructorToSql2Query(ResourceResolver resourceResolver, QueryConstructorModel queryConstructorModel);

}

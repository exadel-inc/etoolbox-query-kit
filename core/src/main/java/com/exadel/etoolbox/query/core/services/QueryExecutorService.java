package com.exadel.etoolbox.query.core.services;

import com.exadel.etoolbox.query.core.models.QueryResultModel;

import javax.jcr.query.qom.QueryObjectModel;

public interface QueryExecutorService {
    void executeJqomQuery(QueryObjectModel queryObjectModel, QueryResultModel queryResultModel);
}

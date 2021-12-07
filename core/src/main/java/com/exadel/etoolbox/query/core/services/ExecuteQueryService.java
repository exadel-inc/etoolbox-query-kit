package com.exadel.etoolbox.query.core.services;

import com.exadel.etoolbox.query.core.servlets.model.QueryResultModel;

import javax.jcr.query.qom.QueryObjectModel;

public interface ExecuteQueryService {
    void executeJQOMQuery(QueryObjectModel queryObjectModel, QueryResultModel queryResultModel);
}

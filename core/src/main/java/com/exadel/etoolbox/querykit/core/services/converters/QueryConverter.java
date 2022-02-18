package com.exadel.etoolbox.querykit.core.services.converters;

import com.exadel.etoolbox.querykit.core.models.search.QueryType;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import org.apache.sling.api.resource.ResourceResolver;

public interface QueryConverter {

    QueryType getSourceType();

    <T> T convert(String statement, ResourceResolver resourceResolver, Class<T> type) throws ConverterException;

}

package com.exadel.etoolbox.querykit.core.services.modifiers;

import com.exadel.etoolbox.querykit.core.models.search.SearchItem;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.function.UnaryOperator;

public interface SearchItemConverterFactory {

    String getName();

    UnaryOperator<SearchItem> getModifier(SearchRequest request);

}

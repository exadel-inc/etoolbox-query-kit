package com.exadel.etoolbox.querykit.core.services.modifiers.impl;

import com.exadel.etoolbox.querykit.core.models.search.SearchItem;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemConverterFactory;
import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;

import java.util.function.UnaryOperator;

@Component
public class ListItemConverterFactory implements SearchItemConverterFactory {
    public static final String NAME = "list-item";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public UnaryOperator<SearchItem> getModifier(SearchRequest request) {
        return new Modifier(request);
    }

    @RequiredArgsConstructor
    private static class Modifier implements UnaryOperator<SearchItem> {
        private final SearchRequest request;

        @Override
        public SearchItem apply(SearchItem searchItem) {
            String text = StringUtils.defaultString(searchItem.getProperty(Constants.PROPERTY_JCR_TITLE, String.class), "Component");
            searchItem.clearProperties();
            searchItem.putProperty(Constants.PROPERTY_TEXT, text);
            searchItem.putProperty(Constants.PROPERTY_VALUE, searchItem.getPath());
            return searchItem;
        }
    }
}

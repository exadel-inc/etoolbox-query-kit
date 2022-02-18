package com.exadel.etoolbox.querykit.core.services.modifiers.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.exadel.etoolbox.querykit.core.models.search.SearchItem;
import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.services.modifiers.SearchItemConverterFactory;
import lombok.RequiredArgsConstructor;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import java.util.function.UnaryOperator;

@Component
public class FindPageConverterFactory implements SearchItemConverterFactory {

    @Override
    public String getName() {
        return "find-page";
    }

    @Override
    public UnaryOperator<SearchItem> getModifier(SearchRequest request) {
        return new Modifier(request, request.getResourceResolver().adaptTo(PageManager.class));
    }

    @RequiredArgsConstructor
    private static class Modifier implements UnaryOperator<SearchItem> {
        private final SearchRequest request;
        private final PageManager pageManager;

        @Override
        public SearchItem apply(SearchItem searchItem) {
            Page page = pageManager != null ? pageManager.getContainingPage(searchItem.getPath()) : null;
            if (page == null) {
                return searchItem;
            }
            Resource contentResource = page.getContentResource();
            return SearchItem.newInstance(
                    request,
                    page.getPath(),
                    contentResource.getValueMap(),
                    contentResource.getPath());
        }
    }
}

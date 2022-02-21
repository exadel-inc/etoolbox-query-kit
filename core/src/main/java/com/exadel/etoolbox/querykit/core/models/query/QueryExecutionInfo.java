package com.exadel.etoolbox.querykit.core.models.query;

import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class QueryExecutionInfo {

    @SlingObject
    private SlingHttpServletRequest request;

    @Getter
    private long total;

    @Getter
    private long executionTime;

    @Getter
    private String errorMessage;

    @PostConstruct
    private void init() {
        total = getNumericValue(request, Constants.ATTRIBUTE_TOTAL);
        executionTime = getNumericValue(request, Constants.ATTRIBUTE_EXECUTION_TIME);
        errorMessage = getStringValue(request, Constants.ATTRIBUTE_ERROR_MESSAGE);
    }

    private static long getNumericValue(SlingHttpServletRequest request, String attribute) {
        Object value = request.getAttribute(attribute);
        if (value == null || !StringUtils.isNumeric(value.toString())) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }

    private static String getStringValue(SlingHttpServletRequest request, String attribute) {
        Object value = request.getAttribute(attribute);
        if (value == null) {
            return StringUtils.EMPTY;
        }
        return value.toString();
    }
}

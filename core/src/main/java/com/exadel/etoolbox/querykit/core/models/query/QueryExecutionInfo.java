package com.exadel.etoolbox.querykit.core.models.query;

import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import java.util.Map;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class QueryExecutionInfo {

    @SlingObject
    private SlingHttpServletRequest request;

    private long computedTotal;

    private long passedTotal;

    @Getter
    private int pageSize;

    @Getter
    private long executionTime;

    @Getter
    private String errorMessage;

    @Getter
    private Map<Integer, Integer> pages;

    @PostConstruct
    private void init() {
        computedTotal = getNumericAttribute(request, Constants.ATTRIBUTE_TOTAL);
        passedTotal = getNumericParameter(request,"-total");
        pageSize = (int) getNumericParameter(request,"-pageSize");
        executionTime = getNumericAttribute(request, Constants.ATTRIBUTE_EXECUTION_TIME);
        errorMessage = getStringAttribute(request, Constants.ATTRIBUTE_ERROR_MESSAGE);
    }

    public long getTotal() {
        return computedTotal > 0 ? computedTotal : passedTotal;
    }

    private static long getNumericAttribute(SlingHttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        if (value == null || !StringUtils.isNumeric(value.toString())) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }

    private static long getNumericParameter(SlingHttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || !StringUtils.isNumeric(value.toString())) {
            return 0L;
        }
        return Long.parseLong(value);
    }

    private static String getStringAttribute(SlingHttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        if (value == null) {
            return StringUtils.EMPTY;
        }
        return value.toString();
    }


}

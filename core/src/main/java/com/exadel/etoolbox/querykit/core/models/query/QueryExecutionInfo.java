/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Contains the metadata characterizing the retrieved query result
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class QueryExecutionInfo {

    @SlingObject
    private SlingHttpServletRequest request;

    private long computedTotal;

    private long passedTotal;

    /**
     * Retrieves the page size in UI
     */
    @Getter
    private int pageSize;

    /**
     * Retrieves the results offset
     */
    @Getter
    private int offset;

    /**
     * Retrieves the query execution time
     */
    @Getter
    private long executionTime;

    /**
     * Retrieves the optional error message received while executing the query
     */
    @Getter
    private String errorMessage;

    /**
     * Retrieves the {@link PaginationInfo} for the display of the current query
     */
    @Getter
    private PaginationInfo paginationInfo;

    /**
     * Retrieves the total number of entries for the current query
     * @return Long value
     */
    public long getTotal() {
        return computedTotal > 0 ? computedTotal : passedTotal;
    }

    @PostConstruct
    private void init() {
        computedTotal = getNumericAttribute(request, Constants.ATTRIBUTE_TOTAL);
        passedTotal = getNumericParameter(request,"-total");
        pageSize = (int) getNumericParameter(request,"-pageSize");
        offset = (int) getNumericParameter(request, "-offset");
        executionTime = getNumericAttribute(request, Constants.ATTRIBUTE_EXECUTION_TIME);
        errorMessage = getStringAttribute(request, Constants.ATTRIBUTE_ERROR_MESSAGE);

        paginationInfo = new PaginationInfo(getTotal(), offset, pageSize);
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
        if (!StringUtils.isNumeric(value)) {
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

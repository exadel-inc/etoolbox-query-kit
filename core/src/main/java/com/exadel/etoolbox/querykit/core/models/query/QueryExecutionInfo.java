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
import com.exadel.etoolbox.querykit.core.utils.RequestUtil;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;

/**
 * Contains the metadata characterizing the retrieved query result
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class QueryExecutionInfo {

    @SlingObject
    private SlingHttpServletRequest request;

    /**
     * Retrieves the total number of entries for the current query
     */
    @Getter
    private long total;

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
    private PaginationInfo pagination;

    @PostConstruct
    private void init() {
        total = RequestUtil.getNumericValue(request.getAttribute(Constants.ATTRIBUTE_TOTAL), 0);
        pageSize = (int) RequestUtil.getNumericValue(request.getParameter( "-pageSize"), 0);
        offset = (int) RequestUtil.getNumericValue(request.getParameter("-offset"), 0);
        executionTime = RequestUtil.getNumericValue(request.getAttribute(Constants.ATTRIBUTE_EXECUTION_TIME), 0);
        errorMessage = request.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE) != null
                ? request.getAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE).toString()
                : StringUtils.EMPTY;

        pagination = new PaginationInfo(total, offset, pageSize);
    }
}

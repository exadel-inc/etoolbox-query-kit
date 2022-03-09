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
package com.exadel.etoolbox.querykit.core.models.search;

import com.exadel.etoolbox.querykit.core.utils.RequestUtil;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.query.QueryManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.exadel.etoolbox.querykit.core.utils.RequestUtil.PARAMETER_PREFIX;

/**
 * Contains user-specified data used to compose and execute a query
 */
@Builder(access = AccessLevel.PRIVATE, builderClassName = "Builder")
@Getter
@Slf4j
public class SearchRequest {

    private static final String PARAMETER_ALL = PARAMETER_PREFIX + "allprops";
    private static final String PARAMETER_ITEM_FILTERS = PARAMETER_PREFIX + "filters";
    private static final String PARAMETER_ITEM_CONVERTERS = PARAMETER_PREFIX + "converters";
    private static final String PARAMETER_OFFSET = PARAMETER_PREFIX + "offset";
    private static final String PARAMETER_LIMIT = PARAMETER_PREFIX + "pageSize";
    private static final String PARAMETER_QUERY = PARAMETER_PREFIX + "query";
    private static final String PARAMETER_SHOW_TOTAL = PARAMETER_PREFIX + "measure";
    private static final String PARAMETER_TYPE_AWARE = PARAMETER_PREFIX + "typeaware";
    private static final String PARAMETER_TRAVERSE = PARAMETER_PREFIX + "traverse";

    private static final int DEFAULT_OFFSET = 0;
    public static final int DEFAULT_LIMIT = Integer.MAX_VALUE;

    private static final String PARAM_VALUE_ITERATING = "iterating";

    /**
     * Gets the {@link ResourceResolver} associated with the current request
     */
    private transient ResourceResolver resourceResolver;

    /**
     * Gets the query statement string
     */
    @With
    private String statement;

    /**
     * Gets the user parameters passed alongside the query statement
     */
    private Map<String, Object> userParameters;

    /**
     * Gets the names of item filters used to modify query results
     */
    private List<String> itemFilters;

    /**
     * Gets the user parameters passed alongside the query statement
     */
    private SearchResultFormat resultFormat;

    private boolean iterating;

    /**
     * Gets the limit of results per query run
     */
    private long limit;

    /**
     * Gets the names of item converters used to modify query results
     */
    private List<String> itemConverters;

    /**
     * Gets the offset applied to query results
     */
    private long offset;

    /**
     * Gets whether the query result should expose all properties when a wildcard selector is specified (by default,
     * only the {@code jcr:path} is exposed unless some particular columns are named)
     */
    private boolean showAllProperties;

    /**
     * Gets whether the total number of results should be computed alongside with the results per given {@code offset}
     * and {@code limit}
     */
    private boolean showTotal;

    /**
     * Gets whether the query result should contain metadata for particular properties, such as property path and data
     * type
     */
    private boolean storeDetails;

    /**
     * Gets whether the statement should be parsed executed in a "non-query" manner, i.e., via repository traversal
     */
    private boolean traverse;

    /* ------------------
       Instance accessors
       ------------------ */

    /**
     * Gets whether the current request is valid
     * @return True or false
     */
    public boolean isValid() {
        return !getType().equals(QueryType.UNSUPPORTED) && offset >= 0 && limit >= 0;
    }

    /**
     * Gets the {@link QueryType} associated with the current request
     * @return {@code QueryType} value
     */
    public QueryType getType() {
        return QueryType.from(statement);
    }

    /**
     * Gets the {@link QueryRenderingFormat} associated with the current request
     * @return {@code QueryRenderingFormat} value
     */
    public QueryRenderingFormat getRenderingFormat() {
        return this.getResultFormat() == SearchResultFormat.JSON ? QueryRenderingFormat.JSON : QueryRenderingFormat.SQL;
    }

    /**
     * Gets whether the query engine should perform complete results iteration to retrieve the total number of results
     */
    public boolean isIterating() {
        return traverse || iterating || !itemFilters.isEmpty();
    }

    /**
     * Retrieves the {@link QueryManager} associated with the current request
     * @return {@code QueryManager} object
     * @throws RepositoryException In the {@code QueryManager} cannot be retrieved
     */
    public QueryManager getQueryManager() throws RepositoryException {
        Session session = resourceResolver.adaptTo(Session.class);
        Workspace workspace = Optional.ofNullable(session).map(Session::getWorkspace).orElse(null);
        if (workspace == null) {
            throw new RepositoryException("Could not retrieve session/workspace");
        }
        return workspace.getQueryManager();
    }

    /**
     * Retrieves the {@link ValueFactory} associated with the current request
     * @return {@code ValueFactory} object
     * @throws RepositoryException In the {@code ValueFactory} cannot be retrieved
     */
    public ValueFactory getValueFactory() throws RepositoryException {
        Session session = resourceResolver.adaptTo(Session.class);
        if (session == null) {
            throw new RepositoryException("Could not retrieve session");
        }
        return session.getValueFactory();
    }

    /* ---------------
       Factory methods
       --------------- */

    /**
     * Creates a new {@link SearchRequest} instance by extracting parameters from the provided {@code
     * SlingHttpServletRequest}
     * @param request {@code SlingHttpServletRequest} object
     * @return New {@code SearchRequest} instance
     */
    public static SearchRequest from(SlingHttpServletRequest request) {
        return from(request, null);
    }

    /**
     * Creates a new {@link SearchRequest} instance by extracting parameters from the provided {@code
     * SlingHttpServletRequest} and Sling {@code Resource} that contains fallback parameters
     * @param request  {@code SlingHttpServletRequest} object
     * @param resource {@code Resource} object
     * @return New {@code SearchRequest} instance
     */
    public static SearchRequest from(SlingHttpServletRequest request, Resource resource) {
        return SearchRequest
                .builder()
                .resourceResolver(request.getResourceResolver())
                .statement(RequestUtil.getParameter(request, resource, PARAMETER_QUERY))
                .userParameters(collectUserParameters(request))

                .itemFilters(RequestUtil.getStringCollection(request, resource, PARAMETER_ITEM_FILTERS))
                .resultFormat(SearchResultFormat.from(request.getRequestPathInfo().getExtension()))
                .iterating(PARAM_VALUE_ITERATING.equals(RequestUtil.getParameter(request, resource, PARAMETER_SHOW_TOTAL)))
                .limit(RequestUtil.getNumericValue(RequestUtil.getParameter(request, resource, PARAMETER_LIMIT), DEFAULT_LIMIT))
                .itemConverters(RequestUtil.getStringCollection(request, resource, PARAMETER_ITEM_CONVERTERS))
                .offset(RequestUtil.getNumericValue(RequestUtil.getParameter(request, resource, PARAMETER_OFFSET), DEFAULT_OFFSET))
                .showAllProperties(Boolean.parseBoolean(RequestUtil.getParameter(request, resource, PARAMETER_ALL)))
                .showTotal(Boolean.parseBoolean(RequestUtil.getParameter(request, resource, PARAMETER_SHOW_TOTAL))
                        || PARAM_VALUE_ITERATING.equals(RequestUtil.getParameter(request, resource, PARAMETER_SHOW_TOTAL)))
                .storeDetails(Boolean.parseBoolean(RequestUtil.getParameter(request, resource, PARAMETER_TYPE_AWARE)))
                .traverse(Boolean.parseBoolean(RequestUtil.getParameter(request, resource, PARAMETER_TRAVERSE)))

                .build();
    }

    private static Map<String, Object> collectUserParameters(SlingHttpServletRequest request) {
        List<String> parameterKeys = request
                .getRequestParameterMap()
                .keySet()
                .stream()
                .filter(key -> !key.startsWith(PARAMETER_PREFIX))
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        for (String key : parameterKeys) {
            Object value = RequestUtil.getValueOrArray(request, key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }
}

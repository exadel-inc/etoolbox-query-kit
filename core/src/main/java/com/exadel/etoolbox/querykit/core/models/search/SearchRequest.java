package com.exadel.etoolbox.querykit.core.models.search;

import com.exadel.etoolbox.querykit.core.utils.RequestUtil;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

@Builder(access = AccessLevel.PRIVATE, builderClassName = "Builder")
@Getter
@Slf4j
public class SearchRequest {

    private static final String PARAMETER_ALL = PARAMETER_PREFIX + "allprops";
    private static final String PARAMETER_ITEM_FILTERS = PARAMETER_PREFIX + "filters";
    private static final String PARAMETER_ITEM_CONVERTERS = PARAMETER_PREFIX + "converters";
    private static final String PARAMETER_OFFSET = PARAMETER_PREFIX + "offset";
    private static final String PARAMETER_LIMIT = PARAMETER_PREFIX + "limit";
    private static final String PARAMETER_QUERY = PARAMETER_PREFIX + "query";
    private static final String PARAMETER_SHOW_TOTAL = PARAMETER_PREFIX + "total";
    private static final String PARAMETER_TYPE_AWARE = PARAMETER_PREFIX + "typeaware";
    private static final String PARAMETER_TRAVERSE = PARAMETER_PREFIX + "traverse";

    private static final int DEFAULT_OFFSET = 0;
    public static final int DEFAULT_LIMIT = Integer.MAX_VALUE;

    private static final String PARAM_VALUE_ITERATING = "iterating";

    private transient ResourceResolver resourceResolver;

    @With
    private String statement;

    private Map<String, Object> userParameters;

    private List<String> itemFilters;

    private SearchResultFormat format;

    private boolean iterating;

    private long limit;

    private List<String> itemConverters;

    private long offset;

    private boolean showAllProperties;

    private boolean showTotal;

    private boolean storeDetails;

    private boolean traverse;

    /* ----------------
       Instance methods
       ---------------- */

    public boolean isValid() {
        return StringUtils.isNotBlank(statement) && offset >= 0 && limit >= 0;
    }

    public QueryType getType() {
        return QueryType.from(statement);
    }

    public boolean isIterating() {
        return traverse || iterating || !itemFilters.isEmpty();
    }

    public QueryManager getQueryManager() throws RepositoryException {
        Session session = resourceResolver.adaptTo(Session.class);
        Workspace workspace = Optional.ofNullable(session).map(Session::getWorkspace).orElse(null);
        if (workspace == null) {
            throw new RepositoryException("Could not retrieve session/workspace");
        }
        return workspace.getQueryManager();
    }

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

    public static SearchRequest from(SlingHttpServletRequest request) {
        return from(request, null);
    }

    public static SearchRequest from(SlingHttpServletRequest request, Resource resource) {
        return SearchRequest
                .builder()
                .resourceResolver(request.getResourceResolver())
                .statement(RequestUtil.getParameter(request, resource, PARAMETER_QUERY))
                .userParameters(collectUserParameters(request))

                .itemFilters(RequestUtil.getStringCollection(request, resource, PARAMETER_ITEM_FILTERS))
                .format(SearchResultFormat.from(request.getRequestPathInfo().getExtension()))
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

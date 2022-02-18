package com.exadel.etoolbox.querykit.core.models.search;

import com.exadel.etoolbox.querykit.core.utils.RequestUtil;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
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

@Builder(access = AccessLevel.PRIVATE, builderClassName = "Builder")
@Getter
@Slf4j
public class SearchRequest {

    private static final String PARAMETER_ALL = "q_allprops";
    private static final String PARAMETER_ITEM_FILTERS = "q_filters";
    private static final String PARAMETER_ITEM_CONVERTERS = "q_converters";
    private static final String PARAMETER_OFFSET = "q_offset";
    private static final String PARAMETER_LIMIT = "q_limit";
    private static final String PARAMETER_QUERY = "q_query";
    private static final String PARAMETER_SHOW_TOTAL = "q_total";
    private static final String PARAMETER_TYPE_AWARE = "q_typeaware";
    private static final String PARAMETER_TRAVERSE = "q_traverse";

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
        return SearchRequest
                .builder()
                .resourceResolver(request.getResourceResolver())
                .statement(RequestUtil.decode(request.getParameter(PARAMETER_QUERY), request))
                .userParameters(collectUserParameters(request))

                .itemFilters(RequestUtil.getStringCollection(request, PARAMETER_ITEM_FILTERS))
                .format(SearchResultFormat.from(request.getRequestPathInfo().getExtension()))
                .iterating(PARAM_VALUE_ITERATING.equals(request.getParameter(PARAMETER_SHOW_TOTAL)))
                .limit(RequestUtil.getNumericValue(request.getParameter(PARAMETER_LIMIT), DEFAULT_LIMIT))
                .itemConverters(RequestUtil.getStringCollection(request, PARAMETER_ITEM_CONVERTERS))
                .offset(RequestUtil.getNumericValue(request.getParameter(PARAMETER_OFFSET), DEFAULT_OFFSET))
                .showAllProperties(Boolean.parseBoolean(request.getParameter(PARAMETER_ALL)))
                .showTotal(Boolean.parseBoolean(request.getParameter(PARAMETER_SHOW_TOTAL))
                        || PARAM_VALUE_ITERATING.equals(request.getParameter(PARAMETER_SHOW_TOTAL)))
                .storeDetails(Boolean.parseBoolean(request.getParameter(PARAMETER_TYPE_AWARE)))
                .traverse(Boolean.parseBoolean(request.getParameter(PARAMETER_TRAVERSE)))

                .build();
    }

    private static Map<String, Object> collectUserParameters(SlingHttpServletRequest request) {
        List<String> parameterKeys = request
                .getRequestParameterMap()
                .keySet()
                .stream()
                .filter(key -> !key.startsWith("q_"))
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        for (String key : parameterKeys) {
            Object value = RequestUtil.getValueOrStringifiedArray(request, key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

}

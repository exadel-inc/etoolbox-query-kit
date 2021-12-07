package com.exadel.etoolbox.query.core.services.impl;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.exadel.etoolbox.query.core.services.SavedQueryService;
import com.exadel.etoolbox.query.core.services.SessionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;

@Component(service = SavedQueryService.class)
public class SavedQueryServiceImpl implements SavedQueryService {

    private final static String QUERY_PARAMETER = "query";
    private final static String LANGUAGE_PARAMETER_DELIMITER = "-queries";
    private final static String LANGUAGE_PARAMETER = "language";
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList("XPath", "SQL2");
    private final static String USERS_PATH = "/var/etoolbox-query-kit/components/content/users/";

    @Reference
    private SessionService sessionService;

    @Override
    public Map<String, List<String>> getSavedQueries(ResourceResolver resolver, Map<String, String[]> parameterMap) {
        Session session = resolver.adaptTo(Session.class);
        if (session == null) {
            return null;
        }
        Resource resource = getUserNodeResource(resolver, session);
        if (resource == null) {
            return null;
        }
        ValueMap valueMap = resource.adaptTo(ValueMap.class);
        if (valueMap == null) {
            return null;
        }
        Map<String, List<String>> map = new HashMap<>();
        SUPPORTED_LANGUAGES.forEach(language -> {
            String[] strings = valueMap.get(language, String[].class);
            if (strings != null) {
                map.put(language, Arrays.asList(strings));
            } else {
                map.put(language, Collections.singletonList(StringUtils.EMPTY));
            }
        });
        return map;
    }

    @Override
    public String saveQuery(ResourceResolver resolver, Map<String, String[]> parameterMap) {
        Session session = resolver.adaptTo(Session.class);
        if (session == null) {
            return null;
        }
        try {
            Resource resource = getUserNodeResource(resolver, session);
            if (resource == null) {
                return null;
            }
            ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
            if (valueMap == null) {
                return null;
            }
            String language = parameterMap.get(LANGUAGE_PARAMETER)[0];
            String query = parameterMap.get(QUERY_PARAMETER)[0];
            String[] values = valueMap.get(language, String[].class);
            Set<String> set = values != null ? new HashSet<>(Arrays.asList(values)) : new HashSet<>();
            set.add(query);
            valueMap.put(language, set.toArray());
            session.save();
            return query;
        } catch (RepositoryException e) {
            e.printStackTrace();
        } finally {
            sessionService.closeSession(session);
        }
        return null;
    }

    @Override
    public Map<String, List<String>> editQueries(ResourceResolver resolver, Map<String, String[]> parameterMap) {
        Session session = resolver.adaptTo(Session.class);
        if (session == null) {
            return null;
        }
        try {
            Resource resource = getUserNodeResource(resolver, session);
            if (resource == null) {
                return null;
            }
            ModifiableValueMap valueMap = resource.adaptTo(ModifiableValueMap.class);
            if (valueMap == null) {
                return null;
            }
            Map<String, List<String>> languageToQueries = new HashMap<>();
            parameterMap.forEach((key, value) -> {
            if (key.contains(LANGUAGE_PARAMETER_DELIMITER)) {
                String language = key.split(LANGUAGE_PARAMETER_DELIMITER)[0];
                if (languageToQueries.containsKey(language)) {
                    Arrays.stream(value).forEach(query -> languageToQueries.get(language).add(query));
                }else {
                    languageToQueries.put(language, new ArrayList<>(Arrays.asList(value)));
                }
            }
        });
        languageToQueries.forEach((language, queries) -> valueMap.put(language, queries.toArray()));
        session.save();
        return languageToQueries;
        } catch (RepositoryException e) {
            e.printStackTrace();
        } finally {
            sessionService.closeSession(session);
        }
        return null;
    }

    private Resource getUserNodeResource(ResourceResolver resolver, Session session) {
        try {
            String userID = resolver.getUserID();
            String currentUserNodePath = String.join(StringUtils.EMPTY, USERS_PATH, userID);
            return session.nodeExists(currentUserNodePath)
                    ? resolver.getResource(currentUserNodePath)
                    : resolver.getResource(JcrUtil.createPath(currentUserNodePath, JcrConstants.NT_UNSTRUCTURED, session).getPath());
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }
}
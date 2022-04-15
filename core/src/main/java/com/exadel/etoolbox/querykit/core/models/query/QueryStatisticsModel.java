package com.exadel.etoolbox.querykit.core.models.query;

import com.exadel.etoolbox.querykit.core.utils.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Model(adaptables = SlingHttpServletRequest.class)
@Slf4j
public class QueryStatisticsModel {

    private static final Pattern PROPERTY_INDEX_PATTERN = Pattern.compile("/\\*\\sproperty\\s([^\\s=]+)[=\\s]");
    private static final Pattern LUCENE_INDEX_PATTERN = Pattern.compile("/\\*\\slucene:([^\\s*]+)[\\s*]");

    private static final String OAK_INDEX_PREFIX = "/oak:index";
    private static final String STRIP_CHARS = " /";

    @SlingObject
    private ResourceResolver resourceResolver;

    @Getter
    private List<QueryInfo> allQueries;
    @Getter
    private Map<String, Integer> indexesByUsage = new LinkedHashMap<>();
    @Getter
    private Map<String, List<QueryInfo>> queriesByIndex = new HashMap<>();
    @Getter
    private LinkedHashMap<String, List<QueryInfo>> queriesByIndexSorted = new LinkedHashMap<>();

    @PostConstruct
    private void init() throws IOException {

        QueryManager queryManager = getQueryManager(resourceResolver);
        MBeanServerConnection server = ManagementFactory.getPlatformMBeanServer();

        allQueries = getQueryInfo(server, queryManager);

        for (QueryInfo queryInfo : allQueries) {
            if (queryInfo.getIndexes().isEmpty()) {
                continue;
            }
            queryInfo.getIndexes().forEach(index -> queriesByIndex.computeIfAbsent(index, k -> new ArrayList<>()).add(queryInfo));
        }

        queriesByIndex.entrySet().stream()
                .sorted((a, b) -> Integer.compare(a.getValue().size(), b.getValue().size()) * -1)
                .forEach(entry -> queriesByIndexSorted.put(entry.getKey(), entry.getValue()));

        getIndexes(resourceResolver).forEach(index -> indexesByUsage.put(
                index,
                queriesByIndex.entrySet().stream()
                        .filter(entry -> {
                            String indexId = getIndexId(index);
                            return entry.getKey().equals(indexId) || entry.getKey().endsWith(indexId + Constants.CLOSING_BRACKET);
                        })
                        .map(entry -> entry.getValue().size())
                        .findFirst().orElse(0)));
    }

    private QueryManager getQueryManager(ResourceResolver resourceResolver) {
        Session session = resourceResolver.adaptTo(Session.class);
        if (session == null) {
            return null;
        }
        try {
            return session.getWorkspace().getQueryManager();
        } catch (RepositoryException e) {
            log.error("Can't get QueryManager from the session", e);
            return null;
        }
    }

    private List<QueryInfo> getQueryInfo(MBeanServerConnection server, QueryManager queryManager) {
        ObjectName queryStatMbean = getQueryStatMBean(server);
        if (queryStatMbean == null || queryManager == null) {
            return Collections.emptyList();
        }
        String jsonRaw;
        List<QueryInfo> result = new ArrayList<>();

        try {
            jsonRaw = server.invoke(queryStatMbean, "asJson", null, null).toString();

        } catch (Exception e) {
            return Collections.emptyList();
        }

        JsonParser jsonParser = new JsonParser();
        JsonArray queries = jsonParser.parse(jsonRaw).getAsJsonArray();

        for(int i = 0; i < queries.size(); i++) {
            JsonObject jsonObject = queries.get(i).getAsJsonObject();
            String statement = getAsString(jsonObject, "query");
            if (StringUtils.contains(statement, "oak-internal") || StringUtils.startsWithIgnoreCase(statement, "explain ")) {
                continue;
            }
            String language = getAsString(jsonObject, "language");
            String thread = getAsString(jsonObject, "lastThreadName");
            String lastExecuted = getAsString(jsonObject, "lastExecutedMillis");
            int executeCount = getAsInt(jsonObject, "executeCount");

            QueryInfo queryInfo = new QueryInfo(queryManager);
            queryInfo.setLanguage(language);
            queryInfo.setStatement(statement);
            queryInfo.setThreadName(thread);
            queryInfo.setExecuteCount(executeCount);
            queryInfo.setLastExecuted(lastExecuted);
            result.add(queryInfo);
        }
        result.sort((a, b) -> Integer.compare(a.getExecuteCount(), b.getExecuteCount()) * -1);
        return result;
    }

    private ObjectName getQueryStatMBean(MBeanServerConnection server) {
        try {
            Set<ObjectName> names = server.queryNames(new ObjectName("org.apache.jackrabbit.oak:type=QueryStats,*"), null);
            return names.iterator().next();
        } catch (IOException | MalformedObjectNameException | NoSuchElementException e) {
            log.warn("Can't get 'org.apache.jackrabbit.oak:type=QueryStats,*'", e);
            return null;
        }
    }

    private List<String> getIndexes(ResourceResolver resourceResolver) {
        Resource oakIndex = resourceResolver.getResource(OAK_INDEX_PREFIX);
        if (oakIndex == null || !oakIndex.hasChildren()) {
            return Collections.emptyList();
        }
        return StreamSupport.stream(oakIndex.getChildren().spliterator(), false)
                .map(Resource::getPath)
                .sorted()
                .collect(Collectors.toList());
    }

    private String getIndexId(String value) {
        if (value.contains(Constants.OPENING_BRACKET) && value.contains(Constants.CLOSING_BRACKET)) {
            return StringUtils.substringBefore(value, Constants.OPENING_BRACKET).trim();
        } else if (StringUtils.containsIgnoreCase(value, OAK_INDEX_PREFIX)) {
            return StringUtils.strip(StringUtils.substringAfter(value, OAK_INDEX_PREFIX), STRIP_CHARS);
        }
        return StringUtils.strip(value, STRIP_CHARS);
    }

    private String getAsString(JsonObject object, String property) {
        try {
            return object.get(property).getAsString();
        } catch (UnsupportedOperationException e) {
            return StringUtils.EMPTY;
        }
    }

    private int getAsInt(JsonObject object, String property) {
        try {
            return object.get(property).getAsInt();
        } catch (UnsupportedOperationException e) {
            return 0;
        }
    }

    public Map<String, Integer> getIndexesByUsage() {
        Map<String,Integer> map = new LinkedHashMap<>();
        indexesByUsage.forEach((key, value) -> map.put(StringUtils.removeStart(key, "/oak:index/"), value));
        return map;
    }
}

package com.exadel.etoolbox.querykit.core.models.query;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class QueryInfo {

    private static final Pattern PROPERTY_INDEX_PATTERN = Pattern.compile("/\\*\\sproperty\\s([^\\s=]+)[=\\s]");
    private static final Pattern LUCENE_INDEX_PATTERN = Pattern.compile("/\\*\\slucene:([^\\s*]+)[\\s*]");

    private QueryManager queryManager;
    @Setter
    @Getter
    private String statement;
    @Setter
    @Getter
    private String language;
    @Setter
    @Getter
    private String threadName;
    @Setter
    @Getter
    private String lastExecuted;
    @Setter
    @Getter
    private int executeCount;
    private Set<String> indexes;

    public QueryInfo(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    public Set<String> getIndexes() throws IOException {
        if (indexes != null) {
            return indexes;
        }
        try {
            indexes = new HashSet<>();
            Query query = queryManager.createQuery("explain " + statement, language);
            QueryResult queryResult = query.execute();
            Row row = queryResult.getRows().nextRow();
            String queryPlan = row.getValue("plan").getString();

            Stream.of(PROPERTY_INDEX_PATTERN, LUCENE_INDEX_PATTERN).forEach(pattern -> {
                Matcher matcher = pattern.matcher(queryPlan);
                while (matcher.find()) {
                    indexes.add(matcher.group(1).trim().replaceAll("\\(.+\\)", StringUtils.EMPTY));
                }
            });
        } catch (Exception e) {
            indexes = Collections.emptySet();
        }
        return indexes;
    }
}

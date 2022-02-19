package com.exadel.etoolbox.querykit.core.services.query.impl;

import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import com.exadel.etoolbox.querykit.core.models.search.SearchResult;
import com.exadel.etoolbox.querykit.core.services.executors.impl.QomExecutor;
import com.exadel.etoolbox.querykit.core.services.executors.impl.SqlExecutor;
import com.exadel.etoolbox.querykit.core.services.filters.SkipByRankingFilterFactory;
import com.exadel.etoolbox.querykit.core.services.filters.SkipByTitleFilterFactory;
import com.exadel.etoolbox.querykit.core.services.modifiers.impl.UnDuplicatingFilterFactory;
import com.exadel.etoolbox.querykit.core.services.modifiers.impl.FindPageConverterFactory;
import com.exadel.etoolbox.querykit.core.services.query.QueryService;
import com.exadel.etoolbox.querykit.core.services.converters.impl.SqlToQomConverter;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class QueryServiceTest {

    @Rule
    public AemContext aemContext = new AemContext(ResourceResolverType.JCR_OAK);

    private QueryService queryService;

    @Before
    public void init() {
        aemContext.load().json("/com/exadel/etoolbox/querykit/content/content.json", "/content");

        aemContext.registerInjectActivateService(new SkipByTitleFilterFactory());
        aemContext.registerInjectActivateService(new SkipByRankingFilterFactory());
        aemContext.registerInjectActivateService(new UnDuplicatingFilterFactory());
        aemContext.registerInjectActivateService(new FindPageConverterFactory());

        aemContext.registerInjectActivateService(new SqlToQomConverter());
        aemContext.registerInjectActivateService(new QomExecutor());
        aemContext.registerInjectActivateService(new SqlExecutor());

        queryService = aemContext.registerInjectActivateService(new QueryServiceImpl());
    }

    @Test
    public void shouldExecuteMeasurementQuery() {
        aemContext.request().addRequestParameter(
                "q_query",
                "SELECT * FROM [cq:PageContent] AS e WHERE ISDESCENDANTNODE(e, '/content')");
        aemContext.request().addRequestParameter("q_total", "true");
        aemContext.request().addRequestParameter("q_offset", "1");
        aemContext.request().addRequestParameter("q_limit", "1");

        SearchRequest request = SearchRequest.from(aemContext.request());
        SearchResult result = queryService.execute(request);

        Assert.assertTrue(result.isSuccess());
        Assert.assertTrue(result.getTotal() > 0);
        Assert.assertEquals(1, result.getItems().size());
        Assert.assertTrue(result.getInfo().contains(QomExecutor.class.getSimpleName()));
    }

    @Test
    public void shouldExecuteIteratingQuery() {
        aemContext.request().addRequestParameter(
                "q_query",
                "SELECT * FROM [cq:Page] AS e WHERE ISCHILDNODE(e, '/content/site')");
        aemContext.request().addRequestParameter("q_total", "iterating");
        aemContext.request().addRequestParameter("q_offset", "3");
        aemContext.request().addRequestParameter("q_limit", "5");

        SearchRequest request = SearchRequest.from(aemContext.request());
        SearchResult result = queryService.execute(request);

        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(10, result.getTotal());
        Assert.assertEquals(5, result.getItems().size());
        Assert.assertTrue(result.getInfo().contains(QomExecutor.class.getSimpleName()));
    }

    @Test
    public void shouldSwitchToSqlWhenUnsupportedKeywords() {
        aemContext.request().addRequestParameter(
                "q_query",
                "SELECT * FROM [cq:PageContent] AS e WHERE ISDESCENDANTNODE(e, '/content') " +
                        "UNION SELECT * FROM [cq:Page] AS e WHERE ISDESCENDANTNODE(e, '/$var')");
        aemContext.request().addRequestParameter("q_total", "true");
        aemContext.request().addRequestParameter("var", "content");

        SearchRequest request = SearchRequest.from(aemContext.request());
        SearchResult result = queryService.execute(request);

        Assert.assertTrue(result.isSuccess());
        Assert.assertTrue(result.getTotal() > 0);
        Assert.assertTrue(result.getInfo().contains(SqlExecutor.class.getSimpleName()));
    }

    @Test
    public void shouldBindVariables() {
        aemContext.request().addRequestParameter(
                "q_query",
                "SELECT * FROM [cq:PageContent] AS e WHERE e.[jcr:title] = $title AND e.ranking = $ranking");
        aemContext.request().addRequestParameter("q_total", "true");
        aemContext.request().addRequestParameter("title", "Page 1");
        aemContext.request().addRequestParameter("ranking", "42");

        SearchRequest request = SearchRequest.from(aemContext.request());
        SearchResult result = queryService.execute(request);

        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(1, result.getTotal());
    }

    @Test
    public void shouldSearchWithVariableInterpolationSql() {
        aemContext.request().addRequestParameter(
                "q_query",
                "SELECT * FROM [cq:PageContent] AS e WHERE e.[jcr:title] = '$title' " +
                        "UNION SELECT * FROM [cq:Page] AS e WHERE e.[jcr:title] = '$title'");
        aemContext.request().addRequestParameter("title", "Page 1");
        aemContext.request().addRequestParameter("title", "Page 2");

        SearchRequest request = SearchRequest.from(aemContext.request());
        SearchResult result = queryService.execute(request);

        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(2, result.getItems().size());
    }

    @Test
    public void shouldSearchWithVariableInterpolationQom() {
        aemContext.request().addRequestParameter(
                "q_query",
                "SELECT * FROM [cq:PageContent] AS e WHERE ISDESCENDANTNODE(e, '$path') AND e.[jcr:title] = '$title'");
        aemContext.request().addRequestParameter("path", "/content");
        aemContext.request().addRequestParameter("title", "Page 1");
        aemContext.request().addRequestParameter("title", "Page 2");

        SearchRequest request = SearchRequest.from(aemContext.request());
        SearchResult result = queryService.execute(request);

        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(2, result.getItems().size());
    }

    @Test
    public void shouldSearchWithExtraColumnsSql() {
        aemContext.request().addRequestParameter(
                "q_query",
                "SELECT e.* FROM [cq:PageContent] AS e WHERE e.[jcr:title] = 'Page 1' " +
                        "UNION SELECT * FROM [cq:Page] AS e WHERE e.[jcr:title] = 'Page 1'");
        aemContext.request().addRequestParameter("q_allprops", "true");

        SearchRequest request = SearchRequest.from(aemContext.request());
        SearchResult result = queryService.execute(request);

        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(1, result.getItems().size());
        Assert.assertEquals("Page 1", result.getItems().get(0).getProperty("jcr:title"));
    }

    @Test
    public void shouldSearchWithExtraColumnsQom() {
        aemContext.request().addRequestParameter(
                "q_query",
                "SELECT e.* FROM [cq:PageContent] AS e WHERE e.[jcr:title] = 'Page 1'");
        aemContext.request().addRequestParameter("q_allprops", "true");

        SearchRequest request = SearchRequest.from(aemContext.request());
        SearchResult result = queryService.execute(request);

        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(1, result.getItems().size());
        Assert.assertEquals("Page 1", result.getItems().get(0).getProperty("jcr:title"));
    }

    @Test
    public void shouldApplyCustomFilters() {
        aemContext.request().addRequestParameter(
                "q_query",
                "SELECT * FROM [cq:PageContent] AS content " +
                        "INNER JOIN [cq:Page] AS page ON ISCHILDNODE(content, page)" +
                        "WHERE ISCHILDNODE(page, '/content/site')");
        aemContext.request().addRequestParameter("q_filters", "skip-by-title, skip-by-ranking");
        aemContext.request().addRequestParameter("q_total", "true");

        SearchRequest request = SearchRequest.from(aemContext.request());
        SearchResult result = queryService.execute(request);

        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(4, result.getTotal());
    }

    @Test
    public void shouldApplyDuplicateFilter() {
        aemContext.request().addRequestParameter(
                "q_query",
                "SELECT * FROM [nt:unstructured] AS e WHERE ISDESCENDANTNODE(e, '/content/site') " +
                        "AND e.[sling:resourceType] LIKE '%type'");
        aemContext.request().addRequestParameter("q_filters", "no-duplicate-pages");
        aemContext.request().addRequestParameter("q_total", "true");
        aemContext.request().setMethod("POST");

        SearchRequest request = SearchRequest.from(aemContext.request());
        SearchResult result = queryService.execute(request);

        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(2, result.getTotal());
    }

    @Test
    public void shouldApplyModifiers() {
        aemContext.request().addRequestParameter(
                "q_query",
                "SELECT * FROM [nt:unstructured] AS e WHERE ISDESCENDANTNODE(e, '/content/site') " +
                        "AND e.[sling:resourceType] LIKE '%type'");
        aemContext.request().addRequestParameter("q_converters", "find-page");
        aemContext.request().addRequestParameter("q_total", "true");

        SearchRequest request = SearchRequest.from(aemContext.request());
        SearchResult result = queryService.execute(request);

        Assert.assertTrue(result.isSuccess());
        Assert.assertEquals(4, result.getTotal());
        Assert.assertTrue(result.getItems().stream().allMatch(item -> item.getProperty("jcr:title", String.class).startsWith("Page")));
    }
}

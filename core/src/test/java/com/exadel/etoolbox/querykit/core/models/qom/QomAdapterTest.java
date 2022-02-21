package com.exadel.etoolbox.querykit.core.models.qom;

import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.services.converters.impl.SqlToQomConverter;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import com.google.common.collect.ImmutableMap;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.QueryObjectModelFactory;

public class QomAdapterTest {

    @Rule
    public AemContext aemContext = new AemContext(ResourceResolverType.JCR_OAK);

    private QueryConverter sqlToQomConverter;

    @Before
    public void init() {
        sqlToQomConverter = aemContext.registerInjectActivateService(new SqlToQomConverter());
    }

    @Test
    public void shouldInterpolateVariables() throws ConverterException, RepositoryException {
        String statement = "SELECT * FROM [nt:unstructured] AS a " +
                "WHERE ISDESCENDANTNODE(a, '$var') " +
                "AND a.[sling:resourceType] = 'some/$var2' " +
                "AND a.[sling:resourceSuperType] = '$notFound' " +
                "AND a.[pageTitle] = 'mid$word value' " +
                "AND a.[jcr:title] LIKE $bind";

        QomAdapter qomAdapter = sqlToQomConverter.convert(statement, aemContext.resourceResolver(), QomAdapter.class);
        String result = qomAdapter
                .buildWith(
                        getQomFactory(),
                        ImmutableMap.of(
                                "var", "/content",
                                "var2", "type",
                                "word", "will not use this"
                        ))
                .toSqlString();
        Assert.assertEquals(
                "SELECT a.* FROM [nt:unstructured] AS a " +
                        "WHERE ISDESCENDANTNODE(a, '/content') " +
                        "AND a.[sling:resourceType] = 'some/type' " +
                        "AND a.[sling:resourceSuperType] = '$notFound' " +
                        "AND a.pageTitle = 'mid$word value' " +
                        "AND a.[jcr:title] LIKE $bind",
                result);
    }

    @Test
    public void shouldChangeExpressionWhenArrayInterpolated() throws ConverterException, RepositoryException {
        String statement = "SELECT * FROM [nt:unstructured] AS a INNER JOIN [nt:unstructured] AS b on ISCHILDNODE(b, a)" +
                "WHERE ISDESCENDANTNODE(a, '$var') AND ISDESCENDANTNODE(b, '$var2') OR a.[jcr:title] = $bind";

        QomAdapter qomAdapter = sqlToQomConverter.convert(statement, aemContext.resourceResolver(), QomAdapter.class);
        String result = qomAdapter
                .buildWith(
                        getQomFactory(),
                        ImmutableMap.of(
                                "var", new String [] {"/content", "/apps"},
                                "var2", new String[] {"/content/acme"},
                                "var3", "will not use this"
                        ))
                .toSqlString();
        Assert.assertEquals(
                "SELECT a.*, b.* " +
                        "FROM [nt:unstructured] AS a " +
                        "INNER JOIN [nt:unstructured] AS b ON ISCHILDNODE(b, a) " +
                        "WHERE (ISDESCENDANTNODE(a, '/content') OR ISDESCENDANTNODE(a, '/apps')) " +
                        "AND ISDESCENDANTNODE(b, '/content/acme') " +
                        "OR a.[jcr:title] = $bind",
                result);
    }


    @Test
    public void shouldChangeExpressionWhenArrayInterpolated2() throws ConverterException, RepositoryException {
        String statement = "SELECT * FROM [nt:unstructured] AS a INNER JOIN [nt:unstructured] AS b on ISCHILDNODE(b, a)" +
                "WHERE ISDESCENDANTNODE(a, '$var') AND a.[jcr:title] LIKE '$var2' AND a.[jcr:description] <> 'a$var3'";

        QomAdapter qomAdapter = sqlToQomConverter.convert(statement, aemContext.resourceResolver(), QomAdapter.class);
        String result = qomAdapter
                .buildWith(
                        getQomFactory(),
                        ImmutableMap.of(
                                "var", new String [] {"/content", "/apps"},
                                "var2", new String[] {"Hello", "Goodbye"},
                                "var3", "will not use this"
                        ))
                .toSqlString();

        Assert.assertEquals(
                "SELECT a.*, b.* FROM [nt:unstructured] AS a " +
                        "INNER JOIN [nt:unstructured] AS b ON ISCHILDNODE(b, a) " +
                        "WHERE (ISDESCENDANTNODE(a, '/content') OR ISDESCENDANTNODE(a, '/apps')) " +
                        "AND (a.[jcr:title] LIKE 'Hello' OR a.[jcr:title] LIKE 'Goodbye') " +
                        "AND a.[jcr:description] <> 'a$var3'",
                result);
    }

    @Test
    public void shouldHandleInFunction() throws ConverterException, RepositoryException {
        String statement = "SELECT * FROM [nt:unstructured] AS a WHERE a.[jcr:title] IN ('first', 'second', 'third') " +
                "AND (LOWER(a.[pageTitle]) IN('fourth', 'fifth', 'sixth') AND a.foo <> 'bar')";
        String result = sqlToQomConverter
                .convert(statement, aemContext.resourceResolver(), QomAdapter.class)
                .buildWith(getQomFactory())
                .toSqlString();
        Assert.assertEquals(
                "SELECT a.* FROM [nt:unstructured] AS a " +
                        "WHERE (a.[jcr:title] = 'first' OR a.[jcr:title] = 'second' OR a.[jcr:title] = 'third') " +
                        "AND (LOWER(a.pageTitle) = 'fourth' OR LOWER(a.pageTitle) = 'fifth' OR LOWER(a.pageTitle) = 'sixth') " +
                        "AND a.foo <> 'bar'",
                result);
    }


    @Test
    public void shouldSplitByUnion() throws ConverterException, RepositoryException {
        String statement = "SELECT * FROM [cq:Page] AS a " +
                "UNION SELECT a FROM [cq:PageContent] AS a WHERE a.[jcr:title] IN('1','2',$var) " +
                "ORDER BY a.ranking DESC";
        String result = sqlToQomConverter.convert(
                        statement,
                        aemContext.resourceResolver(),
                        QomAdapterBundle.class)
                .buildWith(getQomFactory(), ImmutableMap.of("var", 42))
                .toSqlString();
        Assert.assertEquals(
                "SELECT a.* FROM [cq:Page] AS a " +
                        "UNION SELECT a.a AS a FROM [cq:PageContent] AS a " +
                        "WHERE a.[jcr:title] = '1' OR a.[jcr:title] = '2' OR a.[jcr:title] = '42' " +
                        "ORDER BY a.ranking DESC",
                result);
    }

    private QueryObjectModelFactory getQomFactory() throws RepositoryException {
        Session session = aemContext.resourceResolver().adaptTo(Session.class);
        assert session != null;
        Workspace workspace = session.getWorkspace();
        QueryManager queryManager = workspace.getQueryManager();
        return queryManager.getQOMFactory();
    }
}

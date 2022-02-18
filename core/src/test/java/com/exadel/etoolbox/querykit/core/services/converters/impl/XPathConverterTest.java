package com.exadel.etoolbox.querykit.core.services.converters.impl;

import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class XPathConverterTest {

    @Rule
    public AemContext aemContext = new AemContext();

    private QueryConverter xPathConverter;

    @Before
    public void init() {
        xPathConverter = aemContext.registerInjectActivateService(new XPathToSqlConverter());
    }

    @Test
    public void shouldProcessSimpleQuery() throws ConverterException {
        String result = xPathConverter.convert(
                "/jcr:root/content//element(*, sling:Folder)[@sling:resourceType='x']",
                aemContext.resourceResolver(),
                String.class);
        Assert.assertTrue(StringUtils.equalsAnyIgnoreCase(
                "SELECT [jcr:path], [jcr:score], * FROM [sling:Folder] AS a " +
                        "WHERE [sling:resourceType] = 'x' AND ISDESCENDANTNODE(a, '/content')",
                result));
    }

    @Test
    public void shouldProcessJunctionQuery() throws ConverterException {
        String result = xPathConverter.convert(
                "/jcr:root/home//element(*, rep:Authorizable)[@rep:principalName != 'Joe' and @rep:principalName != 'Steve']",
                aemContext.resourceResolver(),
                String.class);
        Assert.assertTrue(StringUtils.equalsAnyIgnoreCase(
                "SELECT [jcr:path], [jcr:score], * FROM [rep:Authorizable] AS a " +
                        "WHERE [rep:principalName] <> 'Joe' AND [rep:principalName] <> 'Steve' AND ISDESCENDANTNODE(a, '/home')",
                result));
    }

    @Test
    public void shouldProcessUnionQuery() throws ConverterException {
        String result = xPathConverter.convert(
                "/jcr:root/content/dam/*[@hidden='hidden-folder' or not(@hidden)]",
                aemContext.resourceResolver(),
                String.class);
        Assert.assertTrue(StringUtils.equalsAnyIgnoreCase(
                "SELECT [jcr:path], [jcr:score], * FROM [nt:base] AS a " +
                        "WHERE ISCHILDNODE(a, '/content/dam') AND [hidden] = 'hidden-folder' " +
                        "UNION SELECT [jcr:path], [jcr:score], * FROM [nt:base] AS a " +
                        "WHERE ISCHILDNODE(a, '/content/dam') AND [hidden] IS NULL",
                result));
    }
}

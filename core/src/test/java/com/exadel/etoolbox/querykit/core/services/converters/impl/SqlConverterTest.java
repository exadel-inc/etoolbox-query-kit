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
package com.exadel.etoolbox.querykit.core.services.converters.impl;

import com.exadel.etoolbox.querykit.core.services.converters.QueryConverter;
import com.exadel.etoolbox.querykit.core.utils.ConverterException;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SqlConverterTest {

    @Rule
    public AemContext aemContext = new AemContext(ResourceResolverType.JCR_OAK);

    private QueryConverter sqlToQomConverter;

    @Before
    public void init() {
        sqlToQomConverter = aemContext.registerInjectActivateService(new SqlToQomConverter());
    }

    @Test
    public void shouldProcessSimpleQuery() throws ConverterException {
        String statement = "SELECT * FROM [nt:unstructured] AS a WHERE ISDESCENDANTNODE(a, '/content') AND a.[jcr:title] LIKE $bind";
        String result = sqlToQomConverter.convert(statement, aemContext.resourceResolver(), String.class);
        Assert.assertEquals(
                "SELECT a.* FROM [nt:unstructured] AS a WHERE ISDESCENDANTNODE(a, '/content') AND a.[jcr:title] LIKE $bind",
                result);
    }
}

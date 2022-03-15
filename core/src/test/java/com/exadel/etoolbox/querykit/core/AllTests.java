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
package com.exadel.etoolbox.querykit.core;

import com.exadel.etoolbox.querykit.core.models.qom.QomAdapterTest;
import com.exadel.etoolbox.querykit.core.models.syntax.WordModelTest;
import com.exadel.etoolbox.querykit.core.services.converters.impl.SqlConverterTest;
import com.exadel.etoolbox.querykit.core.services.converters.impl.XPathConverterTest;
import com.exadel.etoolbox.querykit.core.services.query.impl.QueryServiceTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Shortcut class for running all available test cases in a batch
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        QomAdapterTest.class,
        WordModelTest.class,

        SqlConverterTest.class,
        XPathConverterTest.class,

        QueryServiceTest.class,
})
public class AllTests {
}

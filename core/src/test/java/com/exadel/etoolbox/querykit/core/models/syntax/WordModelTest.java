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
package com.exadel.etoolbox.querykit.core.models.syntax;

import com.exadel.etoolbox.querykit.core.utils.Constants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class WordModelTest {

    private static final String SELECT_EXPRESSION = "SELECT page.[jcr:title], comp.[jcr:title] AS ['My Title']\nFROM [cq:PageContent] AS page\n" +
            "INNER JOIN [nt:unstructured] AS comp ON ISDESCENDANTNODE(comp, page)\n" +
            "WHERE (comp.[sling:resourceType] = 'some/comp''onent' OR comp.[type] IN ('1', 'in(2)', '3')) " +
            "AND ISDESCENDANTNODE(page, '/content/hpe/base/blueprint') " +
            "AND LOWER(comp.category) IN('some', 'cat')\n\n" +
            "UNION SELECT * FROM [cq:PageContent] AS page WHERE ISCHILDNODE(page2, '/content') " +
            "ORDER BY page.[jcr:description] DESC";

    private static final String IN_EXPR_1 = "IN ('1', 'in(2)', '3')";
    private static final String IN_EXPR_2 = "IN('some', 'cat')";

    private static final String WORD_1 = "page.[jcr:title]";
    private static final String WORD_2 = "comp.[type]";

    private static final String EMPTY_EQ_LITERAL = "= ''";

    private WordModel wordModel;

    @Before
    public void init() {
        wordModel = new WordModel(SELECT_EXPRESSION);
    }

    @Test
    public void shouldSearchKeywords() {
        Assert.assertTrue(wordModel.hasToken("isdescendantnode"));
        Assert.assertTrue(wordModel.hasToken("from", "to"));
        Assert.assertTrue(wordModel.hasToken("sling:resourceType"::equals));
        Assert.assertTrue(wordModel.hasToken("'some/comp''onent'"));
        Assert.assertFalse(wordModel.hasToken("comp.[sling:resourceType]"));
    }

    @Test
    public void shouldSplit() {
        List<WordModel> chunks = wordModel.split("union");
        Assert.assertEquals(2, chunks.size());
        Assert.assertTrue(chunks.get(0).toString().trim().endsWith(IN_EXPR_2));
        Assert.assertTrue(chunks.get(1).toString().trim().startsWith("SELECT *"));
        Assert.assertTrue(chunks.get(1).toString().trim().endsWith("DESC"));
    }

    @Test
    public void shouldExtractFunctions() {
        List<WordModel> inFunctions = new ArrayList<>();
        WordModel inFunction = wordModel.extractFunction("in");
        while (inFunction != null) {
            inFunctions.add(inFunction);
            inFunction = wordModel.extractFunction("in", inFunction.getEndPosition());
        }
        Assert.assertEquals(2, inFunctions.size());
        Assert.assertEquals(IN_EXPR_1, inFunctions.get(0).toString());
        Assert.assertEquals(IN_EXPR_2, inFunctions.get(1).toString());
    }

    @Test
    public void shouldSearchWithinCapturedGroup() {
        List<WordModel> inFunctionArgs = new ArrayList<>();
        WordModel inFunction = wordModel.extractFunction("in");
        while (inFunction != null) {
            List<WordModel> args = inFunction.extractBetween("(", ")").split(Constants.COMMA);
            inFunctionArgs.addAll(args);
            inFunction = wordModel.extractFunction("in", inFunction.getEndPosition());
        }
        Assert.assertEquals(5, inFunctionArgs.size());
        Assert.assertEquals("'1'", inFunctionArgs.get(0).toString().trim());
        Assert.assertEquals("'cat'", inFunctionArgs.get(4).toString().trim());
    }

    @Test
    public void shouldExtractWords() {
        WordModel word = wordModel.extractWord(1);
        Assert.assertEquals(WORD_1, word.toString());

        WordModel inFunc = wordModel.extractFunction("in");
        Assert.assertEquals(WORD_2, wordModel.extractWordBackwards(inFunc.getStartPosition() - 1).toString());
        Assert.assertEquals(WORD_2, wordModel.extractWordBackwards(inFunc.getStartPosition() - 2).toString());
    }

    @Test
    public void shouldExtractSubstringBetweenTokens() {
        StringBuilder stringBuilder = new StringBuilder();
        WordModel selectors = wordModel.extractBetween("SELECT", "FROM");
        selectors.inflate(-1, 0);
        while (selectors != null) {
            stringBuilder.append(selectors);
           selectors = wordModel.extractBetween("select", "from", selectors.getEndPosition());
        }
        Assert.assertEquals("SELECT page.[jcr:title], comp.[jcr:title] AS ['My Title']\n * ", stringBuilder.toString());
    }

    @Test
    public void shouldDoReplacement() {
        WordModel inFunction = wordModel.extractFunction("in");
        while (inFunction != null) {
            wordModel.replace(inFunction, EMPTY_EQ_LITERAL);
            inFunction = wordModel.extractFunction("in");
        }
        Assert.assertEquals(
                SELECT_EXPRESSION.replace(IN_EXPR_1, EMPTY_EQ_LITERAL).replace(IN_EXPR_2, EMPTY_EQ_LITERAL),
                wordModel.toString());
    }
}
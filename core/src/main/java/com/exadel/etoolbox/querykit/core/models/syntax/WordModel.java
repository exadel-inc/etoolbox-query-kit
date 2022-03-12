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
import com.exadel.etoolbox.querykit.core.utils.EscapingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents an arbitrary string as a sequence of delimited tokens. Used to extract particular sub-sequences honoring
 * spaces and other delimiter symbols, as well as escaped literals that may contain delimiters inside
 */
@Slf4j
public class WordModel {
    private static final int CHARACTER_COLON = ':';
    private static final int CHARACTER_DOUBLE_QUOTE = '"';
    private static final int CHARACTER_NEWLINE = '\n';
    private static final int CHARACTER_QUOTE = '\'';
    private static final int CHARACTER_RETURN = '\r';
    private static final int CHARACTER_SPACE = ' ';
    private static final int CHARACTER_UNDERSCORE = '_';

    private static final Predicate<String> IS_NON_WORD = str -> StringUtils.isBlank(str) || Constants.COMMA.equals(str);
    private static final Predicate<String> IS_WORD = IS_NON_WORD.negate();

    private static final String[] UNWANTED_SEQUENCES = new String[]{"''"};

    private final List<String> elements;

    private int startPosition;

    private int endPosition;

    /**
     * Creates a new {@link WordModel} instance
     * @param value String to build this world model from
     */
    public WordModel(String value) {
        this(getElements(value), 0, 0);
    }

    /**
     * Creates a new {@link WordModel} instance
     * @param elements      Existing tokens, probably originating from another word model
     * @param startPosition Start position in the list of tokens to pass into the new word model
     * @param endPosition   End position in the list of tokens to pass into the new word model
     */
    private WordModel(List<String> elements, int startPosition, int endPosition) {
        this.elements = elements;
        this.startPosition = startPosition;
        this.endPosition = endPosition > startPosition ? endPosition : elements.size();
    }

    /**
     * Gets the start position in the list of tokens associated with this word model
     * @return Int value
     */
    public int getStartPosition() {
        return Math.max(startPosition, 0);
    }

    /**
     * Gets the end position in the list of tokens associated with this word model
     * @return Int value
     */
    public int getEndPosition() {
        return Math.min(endPosition, elements.size());
    }

    /**
     * Moves the start and end positions in the list of tokens associated with this word model
     * @param startDelta Amount of steps to move the start position
     * @param endDelta   Amount of steps to move the end position
     */
    public void inflate(int startDelta, int endDelta) {
        startPosition = Math.max(startPosition + startDelta, 0);
        endPosition = Math.min(endPosition + endDelta, elements.size());
    }

    /**
     * Gets whether the current word model is valid
     * @return True or false
     */
    public boolean isValid() {
        return elements != null
                && getEndPosition() > getStartPosition();
    }

    /**
     * Gets whether the current word model contains <u>any</u> of the provided tokens
     * @param value A variadic array of words
     * @return True or false
     */
    public boolean hasToken(String... value) {
        return hasToken(elt -> StringUtils.equalsAnyIgnoreCase(elt, value));
    }

    /**
     * Gets whether the current word model contains <u>any</u> tokens that satisfy the provided filter
     * @param filter {@code Predicate} instance
     * @return True or false
     */
    public boolean hasToken(Predicate<String> filter) {
        return elements.subList(getStartPosition(), getEndPosition())
                .stream()
                .anyMatch(filter);
    }

    /**
     * Retrieves the nearest <u>word</u> (a non-blank string, or an escaped sling literal) situated at or <b>after</b>
     * the given position
     * @param position Int value
     * @return New {@link WordModel} instance
     */
    public WordModel extractWord(int position) {
        if (position >= getEndPosition() || position < getStartPosition()) {
            return null;
        }
        int currentPos = StringUtils.isBlank(elements.get(position))
                ? findToken(IS_WORD, position)
                : position;
        if (currentPos == -1) {
            return null;
        }
        int endPos = findToken(IS_NON_WORD, currentPos);
        if (endPos == -1) {
            endPos = getEndPosition();
        }
        return new WordModel(elements, currentPos, endPos);
    }

    /**
     * Retrieves the nearest <u>word</u> (a non-blank string, or an escaped sling literal) situated at or <b>before</b>
     * the given position
     * @param position Int value
     * @return New {@link WordModel} instance
     */
    public WordModel extractWordBackwards(int position) {
        if (position >= getEndPosition() || position <= getStartPosition()) {
            return null;
        }
        int currentPos = StringUtils.isBlank(elements.get(position))
                ? findTokenBackwards(IS_WORD, position)
                : position;
        if (currentPos == -1) {
            return null;
        }
        int endPos = findTokenBackwards(IS_NON_WORD, currentPos);
        if (endPos == -1) {
            endPos = 0;
        }
        return new WordModel(elements, endPos + 1, currentPos + 1);
    }

    /**
     * Extracts the sequence of words matching the pattern of a function situated anywhere within the current word
     * model
     * @param name Name of the function, a non-blank string is expected
     * @return New {@link WordModel} instance
     */
    public WordModel extractFunction(String name) {
        return extractFunction(name, getStartPosition());
    }

    /**
     * Extracts the sequence of words matching the pattern of a function situated within the current word model at or
     * <u>after</u> the given position
     * @param name     Name of the function, a non-blank string is expected
     * @param position Int value
     * @return New {@link WordModel} instance
     */
    public WordModel extractFunction(String name, int position) {
        if (elements.isEmpty() || StringUtils.isBlank(name) || position >= getEndPosition()) {
            return null;
        }
        int hitPos = findToken(name, position);
        if (hitPos == -1) {
            return null;
        }
        WordModel closestBrackets = extractBrackets(hitPos + 1);
        if (closestBrackets == null) {
            return null;
        }
        return new WordModel(elements, hitPos, closestBrackets.getEndPosition());
    }

    private WordModel extractBrackets(int position) {
        int currentPos = findToken(Constants.OPENING_BRACKET, position);
        if (currentPos == -1) {
            return null;
        }
        int openingCount = 1;
        int closingCount = 0;
        currentPos++;
        while (currentPos < getEndPosition()) {
            if (elements.get(currentPos).equals(Constants.OPENING_BRACKET)) {
                openingCount++;
            } else if (elements.get(currentPos).equals(Constants.CLOSING_BRACKET)) {
                closingCount++;
            }
            if (openingCount == closingCount) {
                return new WordModel(elements, position, currentPos + 1);
            }
            currentPos++;
        }
        return null;
    }

    /**
     * Extracts the sequence of tokens situated between the provided "delimiting" tokens (such as brackets)
     * @param opening Opening token
     * @param closing Closing token
     * @return New {@link WordModel} instance
     */
    public WordModel extractBetween(String opening, String closing) {
        return extractBetween(opening, closing, getStartPosition());
    }

    public WordModel extractBetween(String opening, String closing, int position) {
        if (position >= getEndPosition() || position < getStartPosition()) {
            return null;
        }
        int startPosition = findToken(opening, position);
        if (startPosition == -1) {
            return null;
        }
        int endPosition = findToken(closing, startPosition + 1);
        if (endPosition <= startPosition) {
            return null;
        }
        return new WordModel(elements, startPosition + 1, endPosition);
    }

    /**
     * Splits the sequence of tokens with the provided splitting token
     * @param splitter String value; a non-blank string is expected
     * @return New {@link WordModel} instance
     */
    public List<WordModel> split(String splitter) {
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }
        List<WordModel> result = new ArrayList<>();
        int startPos = getStartPosition();
        int hitPos = findToken(splitter, startPos);
        while (hitPos > -1) {
            if (hitPos > startPos) {
                result.add(new WordModel(elements, startPos, hitPos));
            }
            startPos = hitPos + 1;
            hitPos = findToken(splitter, startPos);
        }
        if (startPos > 0) {
            result.add(new WordModel(elements, startPos, getEndPosition()));
        } else {
            result.add(this);
        }
        return result;
    }

    /**
     * Substitutes within the current instance all entries of the provided "nested" {@link WordModel} with the given
     * replacement string
     * @param searchPart  {@code WordModel} object representing the replaceable inner part
     * @param replacement Replacement string
     */
    public void replace(WordModel searchPart, String replacement) {
        replace(searchPart, new WordModel(replacement));
    }

    /**
     * Substitutes within the current instance all entries of the provided "nested" {@link WordModel} with the given
     * replacement model
     * @param searchPart  {@code WordModel} object representing the replaceable inner part
     * @param replacement Replacement model
     */
    public void replace(WordModel searchPart, WordModel replacement) {
        if (searchPart == null || !Objects.equals(elements, searchPart.elements) || !searchPart.isValid()) {
            return;
        }
        boolean initial = isInitialSize();
        int length = searchPart.getEndPosition() - searchPart.getStartPosition();
        elements.addAll(searchPart.getEndPosition(), replacement.elements);
        for (int i = 0; i < length; i++) {
            elements.remove(searchPart.getStartPosition());
        }
        if (initial) {
            endPosition = elements.size();
        }
    }

    private int findToken(String value, int position) {
        return findToken(str -> str.equalsIgnoreCase(value), position);
    }

    private int findToken(Predicate<String> definition, int position) {
        for (int i = position; i < getEndPosition(); i++) {
            if (definition.test(elements.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findTokenBackwards(Predicate<String> definition, int start) {
        if (start >= getEndPosition() || start < getStartPosition()) {
            return -1;
        }
        for (int i = start; i >= getStartPosition(); i--) {
            if (definition.test(elements.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isInitialSize() {
        return startPosition == 0 && endPosition == elements.size();
    }

    /**
     * Provides the string representation of the current word model
     * @return String value
     */
    @Override
    public String toString() {
        return String.join(StringUtils.EMPTY, elements.subList(getStartPosition(), getEndPosition()));
    }

    /* --------------
       Initialization
       -------------- */

    private static List<String> getElements(String value) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(value)) {
            return result;
        }

        String escapedValue = EscapingUtil.escape(value, UNWANTED_SEQUENCES);
        boolean hasEscaping = !escapedValue.equals(value);

        StringReader reader = new StringReader(escapedValue);
        StreamTokenizer tokenizer = new StreamTokenizer(reader);
        tokenizer.ordinaryChar(CHARACTER_SPACE);
        tokenizer.ordinaryChar(CHARACTER_NEWLINE);
        tokenizer.ordinaryChar(CHARACTER_RETURN);
        tokenizer.wordChars(CHARACTER_COLON, CHARACTER_COLON);
        tokenizer.wordChars(CHARACTER_UNDERSCORE, CHARACTER_UNDERSCORE);

        int currentToken;
        try {
            currentToken = tokenizer.nextToken();
            while (currentToken != StreamTokenizer.TT_EOF) {
                appendToElements(tokenizer, currentToken, result);
                currentToken = tokenizer.nextToken();
            }

        } catch (IOException e) {
            log.error("Error parsing statement", e);
        } finally {
            reader.close();
        }

        if (hasEscaping) {
            for (int i = 0; i < result.size(); i++) {
                result.set(i, EscapingUtil.unescape(result.get(i)));
            }
        }
        return result;
    }

    private static void appendToElements(StreamTokenizer tokenizer, int token, List<String> collection) {
        if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
            collection.add(String.valueOf((int) tokenizer.nval));
        } else if (tokenizer.ttype == StreamTokenizer.TT_WORD
                || tokenizer.ttype == CHARACTER_QUOTE
                || tokenizer.ttype == CHARACTER_DOUBLE_QUOTE) {

            boolean isLiteral = tokenizer.ttype == CHARACTER_QUOTE || tokenizer.ttype == CHARACTER_DOUBLE_QUOTE;
            String content = isLiteral ? tokenizer.sval : tokenizer.sval.trim();
            if (isLiteral) {
                String terminator = String.valueOf((char) tokenizer.ttype);
                collection.add(terminator + content + terminator);
            } else if (StringUtils.isNotEmpty(content)) {
                collection.add(content);
            }
        } else {
            collection.add(String.valueOf((char) token));
        }
    }
}

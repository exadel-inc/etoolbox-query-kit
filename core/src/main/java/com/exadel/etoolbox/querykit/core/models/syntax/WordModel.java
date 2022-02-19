package com.exadel.etoolbox.querykit.core.models.syntax;

import com.exadel.etoolbox.querykit.core.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class WordModel {
    private static final int CHARACTER_COLON = ':';
    private static final int CHARACTER_DOUBLE_QUOTE = '"';
    private static final int CHARACTER_NEWLINE = '\n';
    private static final int CHARACTER_QUOTE = '\'';
    private static final int CHARACTER_RETURN = '\r';
    private static final int CHARACTER_SPACE = ' ';
    private static final int CHARACTER_UNDERSCORE = '_';

    private static final Pattern ESCAPED_CHAR_PATTERN = Pattern.compile("__(\\d+)__");

    private static final Predicate<String> IS_NON_WORD = str -> StringUtils.isBlank(str) || Constants.COMMA.equals(str);
    private static final Predicate<String> IS_WORD = IS_NON_WORD.negate();

    private static final String[] UNWANTED_SEQUENCES = new String[] {"''"};

    private final List<String> elements;

    private int startPosition;

    private int endPosition;

    public WordModel(String value) {
        this(getElements(value), 0, 0);
    }

    private WordModel(List<String> elements, int startPosition, int endPosition) {
        this.elements = elements;
        this.startPosition = startPosition;
        this.endPosition = endPosition > startPosition ? endPosition : elements.size();
    }

    public int getStartPosition() {
        return Math.max(startPosition, 0);
    }

    public int getEndPosition() {
        return Math.min(endPosition, elements.size());
    }

    public void inflate(int startDelta, int endDelta) {
        startPosition = Math.max(startPosition + startDelta, 0);
        endPosition = Math.min(endPosition + endDelta, elements.size());
    }

    public boolean isValid() {
        return elements != null
                && getEndPosition() > getStartPosition();
    }

    public boolean hasToken(String... value) {
        return hasToken(elt -> StringUtils.equalsAnyIgnoreCase(elt, value));
    }

    public boolean hasToken(Predicate<String> filter) {
        return elements.subList(getStartPosition(), getEndPosition())
                .stream()
                .anyMatch(filter);
    }

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

    public WordModel extractFunction(String name) {
        return extractFunction(name, getStartPosition());
    }

    public WordModel extractFunction(String name, int position) {
        if (elements.isEmpty() || StringUtils.isBlank(name) || position >= getEndPosition()) {
            return null;
        }
        int hitPos = findToken(name, position);
        if (hitPos == -1) {
            return null;
        }
        WordModel closestBrackets = extractBrackets( hitPos + 1);
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

    public void replace(WordModel searchPart, String replacement) {
        replace(searchPart, new WordModel(replacement));
    }

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

    @Override
    public String toString() {
        return String.join(StringUtils.EMPTY, elements.subList(getStartPosition(), getEndPosition()));
    }

    /* ---------------
       Utility methods
       --------------- */

    public static String escape(String value, String... unwanted) {
        if (org.apache.commons.lang.StringUtils.isEmpty(value) || ArrayUtils.isEmpty(unwanted)) {
            return value;
        }
        String result = value;
        for (String token : unwanted) {
            if (result.contains(token)) {
                result = result.replace(token, prepareEscapedSequence(token));
            }
        }
        return result;
    }

    public static String unescape(String value) {
        StringBuilder result = new StringBuilder(value);
        Matcher matcher = ESCAPED_CHAR_PATTERN.matcher(result);
        while (matcher.find()) {
            result.replace(matcher.start(), matcher.end(), Character.toString((char) Integer.parseInt(matcher.group(1))));
            matcher.reset(result);
        }
        return result.toString();
    }

    private static String prepareEscapedSequence(String token) {
        return token.chars()
                .mapToObj(String::valueOf)
                .map(chr -> "__" + chr + "__")
                .collect(Collectors.joining());
    }

    /* --------------
       Initialization
       -------------- */

    private static List<String> getElements(String value) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(value)) {
            return result;
        }

        String escapedValue = escape(value, UNWANTED_SEQUENCES);
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
                result.set(i, unescape(result.get(i)));
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

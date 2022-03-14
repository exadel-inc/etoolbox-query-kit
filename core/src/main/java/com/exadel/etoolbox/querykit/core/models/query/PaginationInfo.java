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
package com.exadel.etoolbox.querykit.core.models.query;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Contains the metadata used to render pagination controls for the current query in UI
 */
public class PaginationInfo {

    private final static int BUTTON_BLOCK_SIZE = 5;

    /**
     * Retrieves a list of {@link PageElement} objects representing navigation buttons
     */
    @Getter
    private final LinkedList<PageElement> elements;

    private final int currentPage;

    /**
     * Creates a new {@link PaginationInfo} instance
     * @param total    Total number of results used for the computation
     * @param offset   Results offset used for the computation
     * @param pageSize UI Page size used for the computation
     */
    PaginationInfo(long total, long offset, int pageSize) {
        int pageCount = total % pageSize > 0 ? (int) total / pageSize + 1 : (int) total / pageSize;
        currentPage = (int) offset / pageSize + 1;

        if (pageCount <= BUTTON_BLOCK_SIZE) {
            elements = calculatePageRange(1, pageCount, pageSize);
        } else if (currentPage < BUTTON_BLOCK_SIZE) {
            elements = calculatePageRange(1, BUTTON_BLOCK_SIZE, pageSize);
            if (pageCount > BUTTON_BLOCK_SIZE + 1) {
                elements.add(PageElement.ellipsis());
            }
            elements.add(PageElement.button(pageCount, pageSize));
        } else if (currentPage > pageCount - BUTTON_BLOCK_SIZE + 1) {
            elements = calculatePageRange(pageCount - BUTTON_BLOCK_SIZE + 1, pageCount, pageSize);
            if (pageCount > BUTTON_BLOCK_SIZE + 1) {
                elements.addFirst(PageElement.ellipsis());
            }
            elements.addFirst(PageElement.button(1, pageSize));
        } else {
            elements = calculatePageRange(currentPage - 1, currentPage + 1, pageSize);
            elements.addFirst(PageElement.ellipsis());
            elements.addFirst(PageElement.button(1, pageSize));
            elements.add(PageElement.ellipsis());
            elements.add(PageElement.button(pageCount, pageSize));
        }
        elements.stream().filter(elt -> elt.getNumber() == currentPage).findFirst().ifPresent(PageElement::setCurrent);
        elements.getLast().setMaxEndPosition(total);
    }

    /**
     * Retrieves the currently active navigation button
     * @return {@link PageElement} object representing a navigation button
     */
    public PageElement getCurrent() {
        return elements.stream().filter(elt -> elt.getNumber() == currentPage).findFirst().orElse(null);
    }

    private static LinkedList<PageElement> calculatePageRange(int startInclusive, int endInclusive, int size) {
        return IntStream.range(startInclusive, endInclusive + 1)
                .mapToObj(i -> PageElement.button(i, size))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Represents a page navigation element (a button or an ellipsis sign)
     */
    @Getter
    public static class PageElement {
        private static final String ELEMENT_TYPE_BUTTON = "button";
        private static final String ELEMENT_TYPE_ELLIPSIS = "ellipsis";

        private int number;
        private int size;
        private boolean current;

        @Setter(AccessLevel.PRIVATE)
        private long maxEndPosition;

        /**
         * Retrieves the type of the current element (either a button or an ellipsis sign)
         * @return String value
         */
        public String getType() {
            return number > 0 && size > 0 ? ELEMENT_TYPE_BUTTON : ELEMENT_TYPE_ELLIPSIS;
        }

        /**
         * Retrieves the query offset associated with the current navigation element
         * @return Int value
         */
        public long getOffset() {
            if (number == 0 || size == 0) {
                return 0;
            }
            return (long) (number - 1) * size;
        }

        /**
         * Retrieves the number of the starting query result (row) associated with the current navigation element
         * @return Int value
         */
        public long getStart() {
            if (number == 0 || size == 0) {
                return 0;
            }
            return getOffset() + 1;
        }

        /**
         * Retrieves the number of the ending query result (row; inclusive) associated with the current navigation element
         * @return Int value
         */
        public long getEnd() {
            if (number == 0 || size == 0) {
                return 0;
            }
            if (maxEndPosition > 0) {
                return Math.min(getStart() + size - 1, maxEndPosition);
            }
            return getStart() + size - 1;
        }

        private void setCurrent() {
            current = true;
        }

        private static PageElement ellipsis() {
            return new PageElement();
        }

        private static PageElement button(int number, int size) {
            PageElement result = new PageElement();
            result.number = number;
            result.size = size;
            return result;
        }
    }
}

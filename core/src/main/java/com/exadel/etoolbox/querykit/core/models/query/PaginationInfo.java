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

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains the metadata used to render pagination controls for the current query in UI
 */
public class PaginationInfo {

    private final long computedTotal;
    private final int pageSize;

    /**
     * Gets the number of the current page
     */
    @Getter
    private final int currentPage;

    /**
     * Gets whether the control for the "starting element" of pagination (like {@code [1]...}) should be rendered
     */
    @Getter
    private boolean startElementNeeded;

    /**
     * Gets whether the control for the "ending element" of pagination (like {@code ...[100]}) should be rendered
     */
    @Getter
    private boolean endElementNeeded;

    /**
     * Enumerates numbers to be rendered as the controls for the "middle elements" of pagination (like {@code ...[50]
     * [51]...})
     */
    @Getter
    private final List<Integer> middleElements;

    /**
     * Creates a new {@link PaginationInfo} instance
     * @param total    Total number of results used for the computation
     * @param offset   Results offset used for the computation
     * @param pageSize UI Page size used for the computation
     */
    PaginationInfo(long total, int offset, int pageSize) {
        this.computedTotal = total;
        this.pageSize = pageSize;

        currentPage = pageSize != 0 ? offset / pageSize + 1 : 1;

        if (getPagesCount() <= 5) {
            middleElements = calculateMiddleElements(1, getPagesCount());
        } else if (currentPage < 5) {
            endElementNeeded = true;
            middleElements = calculateMiddleElements(1, 5);
        } else if (currentPage > getPagesCount() - 6) {
            startElementNeeded = true;
            middleElements = calculateMiddleElements(getPagesCount() - 6, getPagesCount());
        } else {
            startElementNeeded = true;
            endElementNeeded = true;
            middleElements = calculateMiddleElements(currentPage - 1, currentPage + 1);
        }
    }

    /**
     * Retrieves the total number of pages
     * @return Int value
     */
    public int getPagesCount() {
        return (int) Math.ceil((double) computedTotal / pageSize);
    }

    private List<Integer> calculateMiddleElements(int start, int end) {
        return Stream.iterate(start, s -> ++s).limit(end - start + 1).collect(Collectors.toList());
    }
}

package com.exadel.etoolbox.querykit.core.models.query;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PaginationInfo {

    private final long computedTotal;
    private final int pageSize;

    @Getter
    private int currentPage;

    @Getter
    private boolean startPaginationNeeded;

    @Getter
    private boolean endPaginationNeeded;

    @Getter
    private List<Integer> rangePagesPagination = new ArrayList<>();

    public PaginationInfo(long computedTotal, int offset, int pageSize) {
        this.computedTotal = computedTotal;
        this.pageSize = pageSize;

        currentPage = pageSize != 0 ? offset / pageSize + 1: 1;

        if (getPagesCount() <= 5){
            rangePagesPagination = getRangesPages(1, getPagesCount());
        } else if (currentPage < 5) {
            endPaginationNeeded = true;
            rangePagesPagination = getRangesPages(1, 5);
        } else if (currentPage > getPagesCount() - 6) {
            startPaginationNeeded = true;
            rangePagesPagination = getRangesPages(getPagesCount() - 6, getPagesCount());
        } else {
            startPaginationNeeded = true;
            endPaginationNeeded = true;
            rangePagesPagination = getRangesPages(currentPage - 1, currentPage + 1);
        }
    }

    public int getPagesCount() {
        return (int) Math.ceil((double) computedTotal / pageSize);
    }

    public List<Integer> getRangesPages(int start, int end) {
        return Stream.iterate(start, s -> ++s).limit(end - start + 1).collect(Collectors.toList());
    }
}

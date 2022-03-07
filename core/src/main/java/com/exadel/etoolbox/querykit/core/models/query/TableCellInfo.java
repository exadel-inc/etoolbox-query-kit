package com.exadel.etoolbox.querykit.core.models.query;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public class TableCellInfo {

    private final Object value;

    @Setter(AccessLevel.PACKAGE)
    private String path;

    @Setter(AccessLevel.PACKAGE)
    private String type;
}
package com.exadel.etoolbox.querykit.core.models.query;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Setter(AccessLevel.PACKAGE)
public class TableCellInfo {
    private String path;
    private String type;
    private Object value;
}
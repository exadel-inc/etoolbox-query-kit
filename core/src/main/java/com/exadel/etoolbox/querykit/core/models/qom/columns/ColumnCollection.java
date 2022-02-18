package com.exadel.etoolbox.querykit.core.models.qom.columns;

import javax.jcr.query.qom.Column;
import java.util.List;

public interface ColumnCollection {

    List<Column> getItems();

    List<String> getSelectors();

    List<String> getPropertyNames();

}

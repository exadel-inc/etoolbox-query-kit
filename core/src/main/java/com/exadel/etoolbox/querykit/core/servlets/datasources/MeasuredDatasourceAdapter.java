package com.exadel.etoolbox.querykit.core.servlets.datasources;

import com.adobe.granite.ui.components.ds.DataSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.sling.api.resource.Resource;

import java.util.Iterator;

@RequiredArgsConstructor
@Getter
class MeasuredDatasourceAdapter implements DataSource {

    private final DataSource original;

    private final long total;

    @Override
    public Iterator<Resource> iterator() {
        return original.iterator();
    }

    @Override
    public Long getGuessTotal() {
        return total;
    }
}
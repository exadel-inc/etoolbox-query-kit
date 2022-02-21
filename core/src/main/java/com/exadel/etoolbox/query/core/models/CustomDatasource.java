package com.exadel.etoolbox.query.core.models;

import com.adobe.granite.ui.components.ds.DataSource;
import org.apache.commons.collections4.iterators.ListIteratorWrapper;
import org.apache.sling.api.resource.Resource;

import java.util.Iterator;

public class CustomDatasource implements DataSource {

    private Long total;
    private final ListIteratorWrapper<Resource> wrapper;

    public CustomDatasource(Iterator<Resource> iterator) {
        this.wrapper = new ListIteratorWrapper(iterator);
    }

    @Override
    public Long getGuessTotal() {
        return total;
    }

    @Override
    public Iterator<Resource> iterator() {
        this.wrapper.reset();
        return this.wrapper;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}

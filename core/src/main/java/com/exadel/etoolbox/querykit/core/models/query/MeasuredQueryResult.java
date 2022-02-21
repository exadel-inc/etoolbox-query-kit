package com.exadel.etoolbox.querykit.core.models.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

@RequiredArgsConstructor
@AllArgsConstructor
public class MeasuredQueryResult implements QueryResult {

    private final QueryResult original;

    @Getter
    private long total;

    @Override
    public String[] getColumnNames() throws RepositoryException {
        return original.getColumnNames();
    }

    @Override
    public RowIterator getRows() throws RepositoryException {
        return original.getRows();
    }

    @Override
    public NodeIterator getNodes() throws RepositoryException {
        return original.getNodes();
    }

    @Override
    public String[] getSelectorNames() throws RepositoryException {
        return original.getSelectorNames();
    }
}

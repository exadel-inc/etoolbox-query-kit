package com.exadel.etoolbox.querykit.core.models.qom;

import com.exadel.etoolbox.querykit.core.models.search.SearchRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.qom.QueryObjectModelFactory;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class QomAdapterContext {

    private final QueryObjectModelFactory modelFactory;

    private final ValueFactory valueFactory;

    private boolean changed;

    public void reportChange() {
        changed = true;
    }

    public static QomAdapterContext from(QueryObjectModelFactory modelFactory, ValueFactory valueFactory) {
        return new QomAdapterContext(modelFactory, valueFactory);
    }

    public static QomAdapterContext from(SearchRequest request) throws RepositoryException {
        return new QomAdapterContext(request.getQueryManager().getQOMFactory(), request.getValueFactory());
    }

    public static QomAdapterContext from(QomAdapterContext original) {
        return new QomAdapterContext(original.getModelFactory(), original.getValueFactory());
    }
}

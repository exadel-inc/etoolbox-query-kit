package com.exadel.etoolbox.querykit.core.models.qom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.jcr.ValueFactory;
import javax.jcr.query.qom.QueryObjectModelFactory;

@RequiredArgsConstructor
@Getter
public class QomAdapterContext {

    private final QueryObjectModelFactory modelFactory;

    private final ValueFactory valueFactory;
}

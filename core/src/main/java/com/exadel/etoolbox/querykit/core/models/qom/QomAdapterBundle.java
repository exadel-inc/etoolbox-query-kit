package com.exadel.etoolbox.querykit.core.models.qom;

import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportable;
import com.exadel.etoolbox.querykit.core.utils.serialization.JsonExportUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class QomAdapterBundle implements JsonExportable {

    private final List<QomAdapter> adapters;

    public List<QueryObjectModel> getModels() {
        return CollectionUtils.emptyIfNull(adapters).stream().map(QomAdapter::getModel).collect(Collectors.toList());
    }

    public QomAdapterBundle buildWith(QueryObjectModelFactory factory) throws RepositoryException {
        return buildWith(factory, null);
    }

    public QomAdapterBundle buildWith(QueryObjectModelFactory factory, Map<String, Object> arguments) throws RepositoryException {
        if (CollectionUtils.isEmpty(adapters)) {
            return this;
        }
        List<QomAdapter> newAdapters = new ArrayList<>();
        for (QomAdapter adapter : adapters) {
            newAdapters.add(adapter.buildWith(factory, arguments));
        }
        return new QomAdapterBundle(newAdapters);
    }

    public String toJson() {
        return JsonExportUtil.export(this);
    }

    public String toFormattedString() {
        if (CollectionUtils.isEmpty(adapters)) {
            return StringUtils.EMPTY;
        }
        StringBuilder result = new StringBuilder();
        for (QomAdapter adapter : adapters) {
            String newChunk = adapter.toFormattedString();
            if (StringUtils.isEmpty(newChunk)) {
                continue;
            }
            result.append(result.length() > 0 ? " UNION " : StringUtils.EMPTY).append(newChunk);
        }
        return result.toString();
    }

    @Override
    public JsonElement toJson(JsonSerializationContext serializer) {
        return serializer.serialize(adapters);
    }
}

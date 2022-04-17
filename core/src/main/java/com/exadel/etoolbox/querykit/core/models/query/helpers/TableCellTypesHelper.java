package com.exadel.etoolbox.querykit.core.models.query.helpers;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.util.Map;

public class TableCellTypesHelper {
    @Getter
    private static final Map<String, String> typeToResourceType = ImmutableMap.of(
            "String", "granite/ui/components/coral/foundation/form/textfield",
            "longString", "granite/ui/components/coral/foundation/form/textarea",
            "Date", "granite/ui/components/coral/foundation/form/datepicker",
            "Boolean", "granite/ui/components/coral/foundation/form/checkbox",
            "Long", "granite/ui/components/foundation/form/numberfield");
}

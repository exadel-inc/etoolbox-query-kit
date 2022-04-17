<%--
  ~ Licensed under the Apache License, Version 2.0 (the "License").
  ~ You may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<%@ include file="/libs/granite/ui/global.jsp" %>
<%@ page session="false"
         import="org.apache.sling.api.request.RequestParameter,
         org.apache.sling.api.resource.Resource,
         org.apache.sling.api.SlingHttpServletRequest,
         org.apache.sling.api.resource.ValueMap,
         org.apache.sling.api.wrappers.ValueMapDecorator,
         com.adobe.granite.ui.components.ds.ValueMapResource,
         com.google.common.collect.ImmutableMap,
         com.exadel.etoolbox.querykit.core.models.query.helpers.TableCellTypesHelper" %>

<%!
    private String getParameter(SlingHttpServletRequest request, String name) {
        RequestParameter requestParameter = request.getRequestParameterMap().getValue(name);
        if (requestParameter != null) {
            return requestParameter.getString();
        }
        return null;
    }

    private String getResourceType(String type) {
        return TableCellTypesHelper.getTypeToResourceType().get(type);
    }
%>
<%
    String name = getParameter(slingRequest, "name");
    String type = getParameter(slingRequest, "type");

    if (name == null || type == null) {
        return;
    }

    String effectiveResourceType = getResourceType(type);

    ValueMap valueMap = new ValueMapDecorator(ImmutableMap.<String, Object>builder()
            .put("sling:resourceType", effectiveResourceType)
            .put("name", name)
            .put("fieldLabel", "Edit value of [" + name + "]")
            .put("type", "datetime")
            .put("value", "true")
            .put("uncheckedValue", "false")
            .put("text", name)
            .build());
    Resource widgetResource = new ValueMapResource(resourceResolver, "", "nt:unstructured", valueMap);
    cmp.include(widgetResource, effectiveResourceType, cmp.getOptions());

    if (type.equals("Boolean")) {
        ValueMap valueMapTypeHint = new ValueMapDecorator(ImmutableMap.<String, Object>builder()
                .put("sling:resourceType", "granite/ui/components/foundation/form/hidden")
                .put("ignoreData", "true")
                .put("name", name + "@TypeHint")
                .put("value", type)
                .build());
        Resource typeHintResource = new ValueMapResource(resourceResolver, "", "nt:unstructured", valueMapTypeHint);
        cmp.include(typeHintResource, "granite/ui/components/foundation/form/hidden", cmp.getOptions());
    }
%>
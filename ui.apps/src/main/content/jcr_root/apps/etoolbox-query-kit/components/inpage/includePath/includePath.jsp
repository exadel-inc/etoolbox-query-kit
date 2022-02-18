<%@ include file="/libs/granite/ui/global.jsp" %>
<%@ page session="false"
         import="org.apache.sling.api.request.RequestParameter,
         org.apache.sling.api.resource.Resource" %>
<%

    String path = null;
    RequestParameter requestParameter = slingRequest.getRequestParameterMap().getValue("path");
    if (requestParameter != null) {
        path = requestParameter.getString();
    }

    Resource targetResource = path != null ? resourceResolver.getResource(path) : null;
    if (targetResource != null) {
        cmp.include(targetResource, "granite/ui/components/coral/foundation/container", cmp.getOptions());
    }

%>
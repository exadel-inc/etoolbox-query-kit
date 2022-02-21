<%@ include file="/libs/granite/ui/global.jsp" %>
<%@ page session="false"
           import="com.adobe.granite.ui.components.AttrBuilder,
                  com.adobe.granite.ui.components.Config,
                  com.adobe.granite.ui.components.Tag" %>
<%@ page import="org.apache.commons.lang3.ArrayUtils" %>
<%

    if (!cmp.getRenderCondition(resource, false).check()) {
        return;
    }

    Config cfg = cmp.getConfig();
    String name = cfg.get("name", String.class);
    String[] values = cmp.getValue().getContentValue(name, String[].class);
    if (ArrayUtils.isEmpty(values)) {
        return;
    }
%>
    <ul class="readonly-value-list">
<%
    for (String value : values) {
        out.println("<li>"+  value + "</li>");
        out.println("<input type='hidden' name='" + name + "' value='" +  value + "'/>");
    }
%>
    </ul>

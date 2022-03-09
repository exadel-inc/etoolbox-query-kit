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

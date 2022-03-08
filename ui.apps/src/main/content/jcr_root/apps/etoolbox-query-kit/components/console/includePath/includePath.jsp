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
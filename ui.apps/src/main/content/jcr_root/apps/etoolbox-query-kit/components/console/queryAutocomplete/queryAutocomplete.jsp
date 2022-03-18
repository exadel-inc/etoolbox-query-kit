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
<%
    Config cfg = cmp.getConfig();
    Tag tag = cmp.consumeTag();

    AttrBuilder attrs = tag.getAttrs();
    cmp.populateCommonAttrs(attrs);
    attrs.add("name", cfg.get("name", String.class));
    attrs.add("placeholder", cfg.get("emptyText", String.class));
    attrs.add("match", cfg.get("match", "contains"));

    String fieldLabel = cfg.get("fieldLabel", String.class);

    if (cfg.get("forceSelection", false)) {
        attrs.add("forceSelection", true);
    }
    if (cfg.get("required", false)) {
        attrs.add("required", true);
        attrs.add("aria-required", true);
    }
%>
<div class="coral-Form-fieldwrapper">
    <label class="coral-Form-fieldlabel"><%= fieldLabel%></label>
    <coral-autocomplete <%= attrs.build() %> class="coral-Form-field">
    </coral-autocomplete>
</div>
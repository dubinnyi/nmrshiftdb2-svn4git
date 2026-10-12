<%--
Copyright 2004 The Apache Software Foundation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ page import = "org.apache.jetspeed.portal.portlets.GenericMVCPortlet" %>

<%
String portletId = (String) request.getAttribute(GenericMVCPortlet.PORTLET_ID);
String docUrl = (String) request.getAttribute(GenericMVCPortlet.DOC_URL);
String iframeStyle = "position:absolute;top:-100;left:-100;height:0px;width:0px;visibility:hidden";
%>
<div id="div-<%= portletId %>">
<br>
... loading ...<br>
<br>
</div>
<script type="text/javascript">
function handleOnLoad(portletId, html)
{
//	alert('top.handleOnLoad: portletId=' + portletId + ', html="' + html + '" (len=' + html.length + ')');
	var div = document.getElementById('div-' + portletId);
	div.innerHTML = html;
}
</script>
<iframe src="loadDoc.jsp?<%= GenericMVCPortlet.DOC_URL %>=<%= docUrl %>&<%= GenericMVCPortlet.PORTLET_ID %>=<%= portletId %>" style="<%= iframeStyle %>"></iframe>

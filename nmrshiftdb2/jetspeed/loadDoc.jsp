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
<%@ page import="java.net.URLDecoder" %>
<%@ page import="org.apache.jetspeed.portal.portlets.GenericMVCPortlet" %>

<%
String portletId = request.getParameter(GenericMVCPortlet.PORTLET_ID);
String docUrl = request.getParameter(GenericMVCPortlet.DOC_URL);
docUrl = URLDecoder.decode(docUrl);
%>

<script type="text/javascript">
var timerId;
var waitCount = 0;
function handleLoadDocOnLoad(portletId)
{
	// get frame
	var frame = window.frames[portletId];
	if (!frame) frame = document.body.firstChild;

	// get delayed content
	var delayedContent = '';
	if (frame.contentDocument)
	{
		delayedContent = frame.contentDocument.body.innerHTML;
	}
	else if (frame.document)
	{
		delayedContent = frame.document.body.innerHTML;
	}

	// handle content
	if (timerId) window.clearTimeout(timerId);
	if (delayedContent.length > 0)
	{
		top.handleOnLoad(portletId, delayedContent);
	}
	else	// content not loaded yet: Mozilla bug
	{
		// window.status = 'frame.contentDocument missing; waitCount=' + (waitCount++);
		// alert('delayedContent missing; waitCount=' + (waitCount++));
		timerId = window.setTimeout('handleLoadDocOnLoad("' + portletId + '")', 200);
	}
}
</script>

<frameset rows="1" onLoad="handleLoadDocOnLoad('<%= portletId %>')">
<frame name="<%= portletId %>" src="<%= docUrl %>"></frame>
</frameset>

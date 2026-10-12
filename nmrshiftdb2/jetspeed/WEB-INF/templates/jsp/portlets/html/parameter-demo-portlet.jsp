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
<%@ taglib uri='/WEB-INF/templates/jsp/tld/template.tld' prefix='jetspeed' %>

<p>
This is a demo of <code>parameter styles</code>. Parameter styles are custom "widgets"
which allow to present portlet parameter using something other than just input text
box. These "widgets" can be as simple as text area or check box or as complex as pop
up date calendar. 
<p>
The following table lists ALL parameters defined for this portlet. Click customize to 
see the viewable parameters displayed in various parameter styles. You may change the 
parameter settings, save, and see your changes reflected in the table below:

<%@ page import = "org.apache.turbine.util.Log" %>
<%@ page import = "org.apache.turbine.util.RunData" %>

<%@ page import = "org.apache.jetspeed.portal.Portlet" %>

<%@ page import = "java.util.Hashtable" %>
<%@ page import = "java.util.Enumeration" %>

<TABLE>
    <TR>
        <TH>Parameter</TH>
        <TH>Value</TH>
    </TR>
<% 

RunData data = (RunData)request.getAttribute("rundata");
Portlet portlet = (Portlet)request.getAttribute("portlet");
if (portlet != null)
{
    Hashtable parms = new Hashtable();
    parms.putAll(portlet.getPortletConfig().getInitParameters());
    for (Enumeration e = parms.keys(); e.hasMoreElements(); )
    {
        String key = (String) e.nextElement();
        String value = (String) parms.get(key);
%>
    <TR>
        <TD><%=key%></TD>
        <TD><%=value%></TD>
    </TR>
<%
    }
}

%>
</TABLE>

<P>
Here's an example of using parameter styles within the body of a portlet:
<P>
Available skins: <jetspeed:parameterStyle name="skin-list" style="RegistryEntryListBox">registry=Skin;</jetspeed:parameterStyle>
Available controls: <jetspeed:parameterStyle name="control-list" value="TabControl" style="RegistryEntryListBox">registry=PortletControl;</jetspeed:parameterStyle>


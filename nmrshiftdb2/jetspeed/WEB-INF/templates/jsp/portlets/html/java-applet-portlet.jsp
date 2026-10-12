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

<%@ page import = "org.apache.turbine.util.RunData" %>
<%@ page import = "org.apache.turbine.util.ContentURI" %>

<%@ page import = "org.apache.jetspeed.portal.Portlet" %>

<%@ page import = "java.util.Hashtable" %>
<%@ page import = "java.util.Enumeration" %>

<% 
RunData data = (RunData) request.getAttribute("rundata");
Portlet portlet = (Portlet) request.getAttribute("portlet");
String name = null;
String archive = null;
String border = null;
String align = null;
String height = null;
String width = null;
String codebase = null;
String code = null;
if (portlet != null)
{
    name = portlet.getPortletConfig().getInitParameter("name");
    archive = portlet.getPortletConfig().getInitParameter("archive");
    border = portlet.getPortletConfig().getInitParameter("border");
    align = portlet.getPortletConfig().getInitParameter("align");
    height = portlet.getPortletConfig().getInitParameter("height");
    width = portlet.getPortletConfig().getInitParameter("width");
    codebase = portlet.getPortletConfig().getInitParameter("codebase", ".");
    if (codebase.equals("."))
    {
        codebase = new ContentURI(data).getURI("").toString();
    }
    code = portlet.getPortletConfig().getInitParameter("code");
%>
        
<applet <%=name == null ? "" : "name=\"" + name + "\""%> 
        <%=archive == null ? "" : "archive=\"" + archive + "\""%> 
        <%=border == null ? "" : "border=\"" + border + "\""%> 
        <%=align == null ? "" : "align=\"" + align + "\""%> 
        <%=height == null ? "" : "height=\"" + height + "\""%> 
        <%=width == null ? "" : "width=\"" + width + "\""%>             
        <%=codebase == null ? "" : "codebase=\"" + codebase + "\""%>         
        <%=code == null ? "" : "code=\"" + code + "\""%>>
<%
    Hashtable parms = new Hashtable();
    parms.putAll(portlet.getPortletConfig().getInitParameters());
    for (Enumeration e = parms.keys(); e.hasMoreElements(); )
    {
        String key = (String) e.nextElement();
        String value = (String) parms.get(key);
        if ("name,archive,border,align,height,width,codebase,code,template,action".indexOf(key) < 0)
        {
%>
        <param name="<%=key%>" value="<%=value%>">
<% 
        }
    }
%>
</applet>
<%
}
%>


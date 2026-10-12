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

<%@ page import = "org.apache.jetspeed.services.rundata.JetspeedRunData" %> 
<%@ page import = "org.apache.jetspeed.util.template.JetspeedTool" %> 

<% 
JetspeedRunData rundata = (JetspeedRunData) request.getAttribute("rundata"); 
if (rundata.getJs_peid() != null)
{
    JetspeedTool jt = new JetspeedTool(rundata);
%>
    <%=jt.getPortalElement(rundata.getJs_peid())%>
<%
} 
else
{
%>
    <p>Portal Element Id missing</p>
<%    
}
%>


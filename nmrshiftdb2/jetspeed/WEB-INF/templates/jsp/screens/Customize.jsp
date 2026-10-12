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
<%@ page import="org.apache.jetspeed.util.template.JetspeedTool" %>
<%@ page import="org.apache.jetspeed.services.rundata.JetspeedRunData" %>
<%@ page import="org.apache.turbine.services.jsp.JspService" %>
<%@ page import="org.apache.ecs.ConcreteElement" %>
<%@ page import="org.apache.jetspeed.portal.Portlet" %>

<%
    JetspeedRunData   data = (JetspeedRunData)pageContext.getAttribute(JspService.RUNDATA, PageContext.REQUEST_SCOPE);
    data.setMode(data.CUSTOMIZE);
    JetspeedTool   jetspeed = new JetspeedTool(data);
    Portlet customized = (Portlet)data.getCustomized();
    // The above is returning null every time. Why?? --wab 23 feb 2002

    if (customized != null)
    {
        ConcreteElement   result  =  jetspeed.getCustomizer(customized).getContent(data);
    
        //The ECS element must serialize in the character encoding
        // of the response
        result.setCodeSet( data.getResponse().getCharacterEncoding() );
        result.output( data.getResponse().getWriter() );
    }
    else 
    {
        data.getResponse().getWriter().println("No portlet to customize (null instance)");
    }
%>




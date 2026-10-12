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

<%@ page import = "org.apache.turbine.util.Log" %>
<%@ page import = "org.apache.turbine.util.RunData" %>

<%@ page import = "java.util.Enumeration" %>
<%@ page import = "java.util.Hashtable" %>

<% 
RunData data = (RunData) request.getAttribute("rundata");
String jspeid = (String) request.getAttribute("js_peid");
%>

<p>
Dear Jetspeed User:
<P>
Thank you for taking the time to filling out this questionnaire - think of it as a form contributing back to the project. 
It will allow us to get a better picture of how Jetspeed is used and, in turn, respond better to Jetspeed community's needs. 
Also, we hope that it will provide some concrete real world examples to convince you, your bosses or clients that Jetspeed is 
the right choice for a portal product.
<P>
Please be as specific as your particular situation allows you to be. We realize that you may have to get special permission 
to release information about intranet projects - just do your best.
<p> 
The following table lists your current responses. Click <a href="<jetspeed:portletlink jspeid="<%=jspeid%>" action="controls.Customize"/>">here</A> 
to change your responses (or customize this portlet) and see your changes reflected in the table below:
<P>
<TABLE BORDER="1">
    <TR>
        <TH>Question</TH>
        <TH>Response</TH>
    </TR>
<%
Hashtable questions = (Hashtable) request.getAttribute("questions");
if (questions != null)
{
    Enumeration i = questions.keys();

    while(i.hasMoreElements())
    {
        String title = (String) i.nextElement();
        String value = (String) questions.get(title);
        value = value == null || value.length() == 0 ? "NOT PROVIDED" : value;
%>
    <TR>
        <TD><B><%=title%></B></TD>
        <TD><%=(value.equalsIgnoreCase("") ? "&nbsp;" : value) %></TD>
    </TR>
<%
    }
}

%>
    <TR>
        <TD COLSPAN="2">
            <FORM METHOD="POST">
                <INPUT TYPE="hidden" NAME="js_peid" VALUE="<%=jspeid%>">              
                <p>&nbsp;&nbsp;File containing image of the site: 
                <INPUT TYPE="FILE" SIZE="30" NAME="emailAttachment" VALUE="">                
                <p>&nbsp;&nbsp;
                <INPUT TYPE="SUBMIT" NAME="eventSubmit_doEmail" VALUE="E-Mail Questionnaire"> to 
                <INPUT TYPE="TEXT" SIZE="30" NAME="emailTo" VALUE="jetspeed-dev@jakarta.apache.org">
                &nbsp;&nbsp;<font COLOR="RED"><B><%=data.getMessage() == null ? "" : data.getMessage()%></B></FONT>
            </FORM>
        </TD>
    </TR>
</TABLE>
<P>
Thank you again you for your contribution!
<P>
Jetspeed Development Team
<P>
<IMG SRC="<jetspeed:contentUri href="images/jetspeed-powered.gif"/>">

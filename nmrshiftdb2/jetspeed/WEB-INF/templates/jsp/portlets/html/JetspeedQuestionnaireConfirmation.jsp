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

<%@ page import = "java.util.Enumeration" %>
<%@ page import = "java.util.Hashtable" %>

<% 
RunData data = (RunData) request.getAttribute("rundata");
String jspeid = (String) request.getAttribute("js_peid");
String email = (String) request.getAttribute("email");
%>

<p>
The following email has been successfully sent:
<P>
<B><%=email%></B>
<P>
<FORM METHOD="POST">
    <INPUT TYPE="hidden" NAME="js_peid" VALUE="<%=jspeid%>">              
    <INPUT TYPE="SUBMIT" NAME="eventSubmit_doContinue" VALUE="Continue">
</FORM>
<P>
Thank you again you for your contribution!
<P>
Jetspeed Development Team
<P>
<IMG SRC="<jetspeed:contentUri href="images/jetspeed-powered.gif"/>">

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

<%@ page import = "org.apache.jetspeed.services.resources.JetspeedResources" %> 

<% 
String screenJsp = (String)request.getAttribute("screenJsp");
String logoFile = null;
String portalTitle = null;
if (JetspeedResources.getString("topnav.logo.file") != null) {
  logoFile = JetspeedResources.getString("topnav.logo.file");
}
if (JetspeedResources.getString("portal.title") != null) {
  portalTitle = JetspeedResources.getString("portal.title"); 
}
%>


<html>

<head>
 <base href="<jetspeed:uriLookup type="BaseURL" />">
 <link href="<%= JetspeedResources.getString( "site.stylesheet" ) %>" type="text/css" rel="stylesheet"> 
<% if(portalTitle != null){ %> 
   <title><%=portalTitle%></title>
<%}%>   
</head>


<body bgcolor="#ffffff">

<%if (JetspeedResources.getBoolean("topnav.enable") == true) {%>                                     
<table cellspacing="0" width="100%" border="0" cellpadding="0">
  <tr>
      <%
      //
      // Display the company logo with link back to the the portal home page
      //
      %>  
    <td align="left" width="30%">
      <a href="<jetspeed:uriLookup type="Home"/>">
        <img border="0" alt="<jetspeed:l10n key="HOME"/>" src="<jetspeed:contentUri href="<%=logoFile%>"/>">
      </a>
    </td>
    <td>
      <jetspeed:navigation  defaultTemplate="top_default.jsp"  loggedInTemplate="top_loggedIn.jsp" />
    </td>
  </tr>
</table>
<%}%>

<table cellspacing="0" width="100%" cellpadding="0" border="0">
  <tr>
<%if (JetspeedResources.getBoolean("leftnav.enable") == true) {%>                          
    <td width="<%=JetspeedResources.getString("leftnav.width")%>" valign="top" bgcolor="#ffffff">
   
      <jetspeed:navigation defaultTemplate="left.jsp" />
    </td>
<%}%>    
    <TD>
<%-- Enable for Debugging ("Included servlet Error: 500")
Including:<%= screenJsp %> <p>
 --%>
      <jsp:include page="<%= screenJsp %>" flush="true" />  
    </TD>
  </tr>
</table>

<%if (JetspeedResources.getBoolean("bottomnav.enable") == true) {%>                        
<table cellspacing="0" width="100%" cellpadding="0" border="0">
  <tr>
    <td valign="bottom" bgcolor="#ffffff">
      <jetspeed:navigation  defaultTemplate="bottom.jsp" />
    </td>
  </tr>
</table>
<%}%>
</body>

</html>
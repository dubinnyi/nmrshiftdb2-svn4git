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

<br/>
<table width="100%">
  <tr>
    <td align="left">
     <small>
       <%=JetspeedResources.getString("jetspeed.name")%> - Version <%=JetspeedResources.getString("jetspeed.version")%><br/>
       &copy; Apache Software Foundation 1999-2003
     </small>
    </td>
     <%if (JetspeedResources.getString("mail.support") != null) {%>
     <td align="center">
       <a href="mailto:<%=JetspeedResources.getString("mail.support")%>"><jetspeed:l10n key="BOTTOM_SUPPORT_MSG"/></a>
     </td>
     <%}%>
     <td align="right">
       <a href="http://www.apache.org">
         <img align="right" alt="Apache Software Foundation" border="0" src="<jetspeed:contentUri href="images/feather.gif"/>">
       </a>
     </td>
  </tr>
</table>


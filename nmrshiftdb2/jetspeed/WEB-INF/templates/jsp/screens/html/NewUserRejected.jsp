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


<div>
  <center>
    <table cellpadding="2" cellspacing="0" border="1" width="50%">
      <tr>
        <td align="center" colspan="3" class="INPUTFORMTITLE">
          <jetspeed:l10n key="LOGIN_TITLE"/>
        </td>
      </tr>
      <tr>      
        <td class="INPUTFORM">
          <p><jetspeed:l10n key="JLOGINUSER_REQUEST_REJECTED"/></p>
        </td>
      </tr>
      <tr>
        <td align="left" colspan="3" class="INPUTFORM">
          <p>
            <jetspeed:l10n key="LOGINHELP_NOTICE"/>
          </p>
          <center>
           <a href="mailto:<%=JetspeedResources.getString("mail.support")%>"><%=JetspeedResources.getString("mail.support")%></a>
          </center>
        </td>
      </tr>
    </table>
  </center>
</div>

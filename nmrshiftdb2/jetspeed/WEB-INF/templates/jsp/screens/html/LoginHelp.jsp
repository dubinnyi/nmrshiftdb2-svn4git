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
<%@ page import = "org.apache.turbine.util.RunData" %>

<% 
RunData data = (RunData) request.getAttribute("rundata");                 
%>

<div>
  <center>
    <table cellpadding="2" cellspacing="0" border="1" width="50%">
      <tr>
        <td align="center" colspan="3" class="INPUTFORMTITLE">
          <jetspeed:l10n key="LOGINHELP_TITLE"/>
        </td>
      </tr>
      <tr>      
        <td class="INPUTFORM">
          <%if (data.getMessage() != null) {
            // Message include account creation failure messages.
           %>
            <p><b><%=data.getMessage()%></b></p>
          <%}
          if (JetspeedResources.getBoolean("password.reminder.enable") == true ) {
          %>
          <form accept-charset="UTF-8, ISO-8859-1" method="POST" action="<jetspeed:link template="ConfirmRegistration"/>" enctype="application/x-www-form-urlencoded">
            <input name="action" type="hidden" value="<%=JetspeedResources.getString("action.login")%>">
            <p>
              <jetspeed:l10n key="USERFORM_FORGOTPASSWORD_NOTICE"/>
            </p>
            <center>
              <table cellpadding="0" cellspacing="1" border="0">
                <tr>
                  <td align="right" valign="middle">
                    <jetspeed:l10n key="USERFORM_USERNAMEMSG"/>
                  </td>
                  <td>&nbsp;</td>
                  <td align="right" valign="middle">
                    <input name="username"  type="TEXT" value="<%=data.getParameters().getString("username", "")%>">
                  </td>
                  <td align="right" valign="middle">
                    <input name="eventSubmit_doReminder" type="submit" tabindex="5" value="<jetspeed:l10n key="USERFORM_PASSWORDSEND"/>" style="font-size:10" >
                  </td>
                </tr>
              </table>
            </center>
          </form>
          <br>
          <%}%>
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

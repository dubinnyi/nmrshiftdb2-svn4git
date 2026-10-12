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
<%@ page import = "org.apache.jetspeed.services.resources.JetspeedResources" %> 

<% 
RunData data = (RunData)request.getAttribute("rundata"); 
String username = data.getParameters().getString("username");
if (username == null)
{
    username = data.getUser().getUserName();
}
if (!username.equals(JetspeedResources.getString("services.JetspeedSecurity.user.anonymous"))) {
%>
<div>
  <center>
    <table cellpadding="2" cellspacing="0" border="1" width="50%">
      <tr>
        <td align="center" colspan="3" class="INPUTFORMTITLE">
          <jetspeed:l10n key="PASSWORDFORM_TITLE"/>
        </td>
      </tr>
      <tr>
        <td class="INPUTFORM">
          <% if (data.getMessage() != null) {
            // Message include account creation failure messages. %>
            <p><b><%=data.getMessage()%></b></p>
          <%}%>
          <form accept-charset="UTF-8, ISO-8859-1" 
              method="POST" 
              action="<jetspeed:dynamicUri/>" 
              enctype="application/x-www-form-urlencoded">
          <input name="action" type="hidden" value="ChangePassword">
          <input name="username" type="hidden" value="<%=username%>">
          <center>
              <table cellpadding="0" cellspacing="1" border="0">
                <tr>
                  <td><jetspeed:l10n key="PASSWORDFORM_USERNAMEMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                    <%=username%>
                  </td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="PASSWORDFORM_OLDPASSWORDMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                    <input name="old_password" type="PASSWORD" value="">
                  </td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="PASSWORDFORM_PASSWORDMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                    <input name="password" type="PASSWORD" value="">
                  </td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="PASSWORDFORM_PASSWORDCONFIRMMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                    <input name="password_confirm" type="PASSWORD" value="">
                  </td>
                </tr>
                <tr>
                  <td colspan="3">&nbsp;</td>
                </tr> 
                <tr>
                  <td colspan="3">
                    <table width="100%">
                      <tr>
                        <td align="right" width="40%">
                          <input name="<jetspeed:l10n key="PASSWORDFORM_CANCEL"/>" type="submit" value="<jetspeed:l10n key="PASSWORDFORM_CANCEL"/>">
                        </td>
                        <td width="10%">&nbsp;</td>
                        <td align="left" width="40%">
                          <input name="<jetspeed:l10n key="PASSWORDFORM_CHANGE"/>" type="submit" value="<jetspeed:l10n key="PASSWORDFORM_CHANGE"/>">
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </center>
          </form>
        </td>
      </tr>
    </table>
  </center>
</div>
<%} else {%>
<h3>Sorry, cannot change password for anonymous user.<h3>
<%}%>
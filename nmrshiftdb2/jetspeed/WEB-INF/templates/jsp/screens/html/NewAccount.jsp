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
          <jetspeed:l10n key="NEWACCOUNT_TITLE"/>
        </td>
      </tr>
      <tr>      
        <td class="INPUTFORM">
          <p><jetspeed:l10n key="NEWACCOUNT_NOTICE"/></p>
          <%if ( JetspeedResources.getBoolean("newuser.approval.enable") == false ) {
              if ( JetspeedResources.getBoolean("newuser.confirm.enable") == true ) {
            %>
              <p><jetspeed:l10n key="NEWACCOUNT_CONFIRMATION"/></p>
            <%}
          }
          else {
            %>
            <p><jetspeed:l10n key="NEWACCOUNT_APPROVAL"/></p>
          <%}
          if (data.getMessage() != null) {
            // Message include account creation failure messages.
           %>
            <p><b><%=data.getMessage()%></b></p>
          <%}%>
          <form accept-charset="UTF-8, ISO-8859-1" method="POST" action="<jetspeed:link template="ConfirmRegistration"/>" enctype="application/x-www-form-urlencoded">
            <input name="action" type="hidden" value="CreateNewUserAndConfirm">
            <center>
              <table cellpadding="0" cellspacing="1" border="0">
                <tr>
                  <td><jetspeed:l10n key="USERFORM_USERNAMEMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="right">
                    <input name="username" type="TEXT" value="<%=data.getParameters().getString("username", "")%>">
                  </td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="USERFORM_PASSWORDMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="right">
                    <input name="password" type="PASSWORD" value="">
                  </td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="USERFORM_PASSWORDCONFIRMMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="right">
                    <input name="password_confirm" type="PASSWORD" value="">
                  </td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="USERFORM_FIRSTNAMEMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="right">
                    <input name="firstname" type="TEXT" value="<%=data.getParameters().getString("firstname", "")%>">
                  </td> 
                </tr>
                <tr>
                  <td><jetspeed:l10n key="USERFORM_LASTNAMEMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="right">
                   <input name="lastname" type="TEXT" value="<%=data.getParameters().getString("lastname", "")%>">
                  </td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="USERFORM_EMAILMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="right">
                    <input name="email" type="TEXT" value="<%=data.getParameters().getString("email", "")%>">
                  </td>
                </tr> 
                <tr>
                  <td colspan="3"><jetspeed:l10n key="NEWACCOUNT_INFOEMAILMSG"/></td>
                </tr> 
                <tr>
                  <td colspan="3">&nbsp;</td>
                </tr> 
                <tr>
                  <td colspan="3">
                    <table width="100%">
                      <tr>
                        <td align="right" width="40%">
                          <input name="submit1" type="reset" value="<jetspeed:l10n key="USERFORM_RESET"/>">
                        </td>
                        <td width="10%">&nbsp;</td>
                        <td align="left" width="40%">
                          <input name="submit2" type="submit" value="<jetspeed:l10n key="USERFORM_CREATE"/>">
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

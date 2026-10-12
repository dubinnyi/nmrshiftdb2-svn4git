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
<%@ page import = "java.util.Hashtable" %> 
<%@ page import = "org.apache.jetspeed.services.resources.JetspeedResources" %> 

<% 
    RunData data = (RunData) request.getAttribute("rundata");
    String username  = "";
    String firstname = "";
    String lastname  = "";
    String email     = "";

    try {
        Hashtable screenData = (Hashtable)request.getAttribute( "ScreenDataEditAccount" );

        username  = (String)screenData.get( "username" );
        firstname = (String)screenData.get( "firstname" );
        lastname  = (String)screenData.get( "lastname" );
        email     = (String)screenData.get( "email" );
    } catch (Exception e) {
%>
        <%@ include file="ErrorNoScreenData.inc" %>
<%
        return;
    }
%>

<div>
  <center>
    <table cellpadding="2" cellspacing="0" border="1" width="50%">
      <tr>
        <td align="center" colspan="3" class="INPUTFORMTITLE">
          <jetspeed:l10n key="EDITACCOUNT_TITLE"/>
        </td>
      </tr>
      <tr>
        <td class="INPUTFORM">
          <%if (data.getMessage() != null) {
            // Message include account creation failure messages. %>
            <p><b><%=data.getMessage()%></b></p>
          <%}%>
          <form accept-charset="UTF-8, ISO-8859-1" 
              method="POST" 
              action="<jetspeed:dynamicUri/>" 
              enctype="application/x-www-form-urlencoded">
          <input name="action" type="hidden" value="UpdateAccount">
          <input name="username" type="hidden" value="<%=username%>">
          <center>
              <table cellpadding="0" cellspacing="1" border="0">
                <tr>
                  <td><jetspeed:l10n key="USERFORM_USERNAMEMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                    <%=username%>
                  </td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="USERFORM_OLDPASSWORDMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                    <input name="old_password" type="PASSWORD" value="">
                  </td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="USERFORM_PASSWORDMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                    <input name="password" type="PASSWORD" value="">
                  </td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="USERFORM_PASSWORDCONFIRMMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                    <input name="password_confirm" type="PASSWORD" value="">
                  </td>
                </tr>
<%if ( JetspeedResources.getBoolean("automatic.logon.enable") == true ) {%>
                <tr>
                  <td colspan="3">
                    <input name="rememberme" value="true" type="checkbox" 
<%if ( data.getCookies().get("logincookie") != null ) {%>
                        checked
<%}%>
                    /><jetspeed:l10n key="USERFORM_REMEMBERME"/>
                  </td>
                </tr>
<%}%>
                <tr>
                  <td><jetspeed:l10n key="USERFORM_FIRSTNAMEMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                    <input name="firstname" type="TEXT" value="<%=firstname%>">
                  </td> 
                </tr>
                <tr>
                  <td><jetspeed:l10n key="USERFORM_LASTNAMEMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                   <input name="lastname" type="TEXT" value="<%=lastname%>">
                  </td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="USERFORM_EMAILMSG"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                    <input name="email" type="TEXT" value="<%=email%>">
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
                          <input name="cancelBtn" type="submit" value="<jetspeed:l10n key="USERFORM_CANCEL"/>">
                        </td>
                        <td width="10%">&nbsp;</td>
                        <td align="left" width="40%">
                          <input name="submit2" type="submit" value="<jetspeed:l10n key="USERFORM_UPDATE"/>">
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

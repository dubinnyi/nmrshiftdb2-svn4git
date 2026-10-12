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

<% RunData data = (RunData)request.getAttribute("rundata"); %>

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
          <%if (data.getMessage() != null) {
            // Message include account creation failure messages.%>
            <p><b><%=data.getMessage()%></b></p>
          <%}%>
          <form method="POST" 
               action="<jetspeed:dynamicUri/>" 
               enctype="application/x-www-form-urlencoded">
            <input name="action" type="hidden" value="<%=JetspeedResources.getString("action.login")%>" />
            <center>
              <table border="0" cellspacing="2" cellpadding="1">
                <tr>
                  <td><jetspeed:l10n key="LOGIN_USERNAME"/></td>
                  <td><input size="12" value="<%=data.getParameters().getString("username", "")%>" name="username" maxlength="25" type="text" /></td>
                </tr>
                <tr>
                  <td><jetspeed:l10n key="LOGIN_PASSWORD"/></td>
                  <td><input size="12" value="" name="password" maxlength="25" type="password" /></td>
                </tr>
                <%if (JetspeedResources.getBoolean("automatic.logon.enable") == true ) {%>
                <tr>
                  <td colspan="2">
                    <input name="rememberme" value="true" type="checkbox">
                    <jetspeed:l10n key="LOGIN_REMEMBERME"/>
                  </td>
                </tr>
                <%}%>
                <tr>
                  <td colspan="2" align="center">
                    <input name="submit" type="submit" value="<jetspeed:l10n key="LOGIN_LOGIN"/>">
                  </td>
                </tr>
                <tr>
                  <td colspan="2" align="center">
                  <%if (JetspeedResources.getBoolean("topnav.user_creation.enable") == true ) {%>
                    <a href="<jetspeed:link template="NewAccount"/>" style="font-size:10"><jetspeed:l10n key="LOGIN_NEW_ACCOUNT"/></a>
                    &nbsp;|&nbsp;
                  <%}%>                    
                    <a href="<jetspeed:link template="LoginHelp"/>" style="font-size:10"><jetspeed:l10n key="TOP_LOGINHELP"/></a>
                  </td>
                </tr>
              </table>
            </form>
          </center>
        </td>
      </tr>
    </table>
  </center>
</div>

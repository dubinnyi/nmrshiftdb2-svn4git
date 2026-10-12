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

<% RunData rundata = (RunData)request.getAttribute("rundata"); %>

<div>
  <center>
    <table cellpadding="2" cellspacing="0" border="1" width="50%">
      <tr>
        <td align="center" colspan="3" class="INPUTFORMTITLE">
          <jetspeed:l10n key="CONFIRMREGISTRATION_TITLE"/>
        </td>
      </tr>
      <tr>
        <td class="INPUTFORM">
          <p><jetspeed:l10n key="CONFIRMREGISTRATION_MESSAGE"/></p>
          <%if (rundata.getMessage() != null) {
            // Message include account creation failure messages. %>
            <p><b><%=rundata.getMessage()%></b></p>
          <%}%>
          <form accept-charset="UTF-8, ISO-8859-1" 
                method="POST" 
                action="<jetspeed:dynamicUri/>" 
                enctype="application/x-www-form-urlencoded">
            <input name="username" type="hidden" value="<%= rundata.getParameters().getString("username", "") %>">
            <input name="password" type="hidden" value="<%= rundata.getParameters().getString("password", "") %>">
            <input name="action" type="hidden" value="<%= JetspeedResources.getString( "action.login" ) %>">
            <center>
              <table cellpadding="0" cellspacing="1" border="0">
                <tr>
                  <td><jetspeed:l10n key="CONFIRMREGISTRATION_SECRETKEYTITLE"/></td>
                  <td>&nbsp;</td>
                  <td align="left">
                    <input name="secretkey" type="TEXT" value="<%= rundata.getParameters().getString("secretkey", "") %>">
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
                          <input name="reset" type="reset" value="<jetspeed:l10n key="CONFIRMREGISTRATION_CANCEL"/>">
                        </td>
                        <td width="10%">&nbsp;</td>
                        <td align="left" width="40%">
                          <input name="submit2" type="submit" value="<jetspeed:l10n key="CONFIRMREGISTRATION_CONFIRM"/>">
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

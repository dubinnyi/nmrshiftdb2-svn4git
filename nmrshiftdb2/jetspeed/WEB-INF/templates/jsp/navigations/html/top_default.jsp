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
  String BgImage = JetspeedResources.getString("topnav.bg.image");
  String BgColor = JetspeedResources.getString("topnav.bg.color");
  String FontFace = JetspeedResources.getString("topnav.font.face");
  String FontSize = JetspeedResources.getString("topnav.font.size");
  String FontColor = JetspeedResources.getString("topnav.font.color");
  String FontStyle = null;
  
  if (BgImage == null || BgImage.equals("")) {
    BgImage = null;
  }  
  if (BgColor == null || BgColor.equals("")) {
    BgColor = null;
  }  
  if (FontFace == null || FontFace.equals("")) {
    FontFace = null;
  }  
  if (FontSize == null || FontSize.equals("")) {
    FontSize = null;
  }  
  if (FontColor == null || FontColor.equals("")) {
    FontColor = null;
  }  
  
  if(FontColor != null) {
    FontStyle="style=color:"+FontColor+"; ";
  }

  if(FontSize != null  && FontColor!=null){
   FontStyle="style=color:"+FontColor+"; font-size:"+FontSize+";";
  }else if(FontSize != null) {
   FontStyle="style=font-size:"+FontSize+";";
  }
  else {
   FontStyle="style=font-size:10;";
  }
  
%>



<div>
  <table cellspacing="0" border="0" width="100%" <%if(BgColor !=null){%>bgcolor="<%=BgColor%>" <%}%> <%if(BgImage !=null){%>background="<%=BgImage%>" <%}%> >
    <tr valign="middle">
      <%
      //
      // Display the welcome message.  This is typicaly where a Banner AD is displayed
      //
      %>
      <td align="center">
        <h2><jetspeed:l10n key="TOP_TITLE"/></h2>
      </td>
      <%
      //
      // Right column of top navigation bar.
      // Optionally allow user login. Required for user creation
      // Optionally allow user creation.
      //
      %>
      <%if( JetspeedResources.getBoolean("topnav.user_login.enable") == true) { %>
      <td align="right" valign="bottom" width="30%">
        <form method="POST" action="<jetspeed:dynamicUri/>" enctype="application/x-www-form-urlencoded">
          <input name="action" type="hidden" value="<%=JetspeedResources.getString("action.login")%>">
          <table border="0" cellspacing="2" cellpadding="0">
            <tr>
              <td <%=FontStyle%>><jetspeed:l10n key="USERFORM_USERNAMEMSG"/></td>
              <td>
                <input value="" name="username" maxlength="25" type="text" tabindex="1" <%=FontStyle%>>
              </td>
              <td rowspan="2" align="center">
                <input name="submit" type="submit" value="<jetspeed:l10n key="USERFORM_LOGIN"/>" tabindex="4"  <%=FontStyle%> />
              </td>
              <td>&nbsp;</td>
            </tr>
            <tr>
              <td <%=FontStyle%>><jetspeed:l10n key="USERFORM_PASSWORDMSG"/></td>
              <td>
                <input value="" name="password" maxlength="25" type="password" tabindex="2" style="font-size:10">
              </td>
            </tr>
            <%if ( JetspeedResources.getBoolean("automatic.logon.enable") == true ) {%>
            <tr>
              <td <%=FontStyle%> colspan="2">
                <input name="rememberme" value="true" type="checkbox" tabindex="3" /><jetspeed:l10n key="USERFORM_REMEMBERME"/>
              </td>
            </tr>
            <%}%>
            <tr>
              <td align="center" colspan="3">
                <%if( JetspeedResources.getBoolean("topnav.user_creation.enable") == true &&
                      JetspeedResources.getBoolean("newuser.confirm.enable") == true ) { %>
                <a href="<jetspeed:link template="NewAccount"/>" <%=FontStyle%>><jetspeed:l10n key="TOP_CREATENEWACCOUNT"/></a>
                &nbsp;|&nbsp;
                <%}%>
                <a href="<jetspeed:link template="LoginHelp"/>" <%=FontStyle%>><jetspeed:l10n key="TOP_LOGINHELP"/></a>
              </td>
            </tr>
            <%
            // Leave space between last line and content
            %>
            <tr>
              <td>&nbsp;</td>
            </tr>
          </table>
        </form>
      </td>
      <%}%>
    </tr>
  </table>
 </div>

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
  RunData data = (RunData) request.getAttribute("rundata");  
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
      // Right column of top navigation bar. Here we are displaying the
      // user's name, link(s) to page customizers, link to account editor, logout
      //
      %>
      <td align="right" valign="bottom" width="30%" >
        <table cellpadding="0" cellspacing="0" border="0" >
          <tr>
            <td align="right" <%=FontStyle%>>
              <jetspeed:l10n key="TOP_USERWELCOMEMSG"/> <b><%=data.getUser().getFirstName()%> <%=data.getUser().getLastName()%></b>
            </td>
            <td>&nbsp;</td>
          </tr>
          <%if( JetspeedResources.getBoolean("topnav.my_pages.enable") == true) { %>          
          <tr>
            <td align="right" <%=FontStyle%>>
              <jetspeed:l10n key="PSML_MANAGER_MY_PAGES"/> <jetspeed:parameterStyle name="mypages" style="MyPagesListBox"/>
            </td>
          </tr>
          <%}%>
          <tr>
            <td align="right"  <%=FontStyle%>>
              <jetspeed:l10n key="TOP_CUSTOMIZE"/>: 
              <a href="<jetspeed:uriLookup type="Customize"/>" style="font-size:10">HTML</a>
              <a href="<jetspeed:uriLookup type="Customize"/>&mtype=wml"  style="font-size:10">WML</a>
            </td>
          </tr>
          <tr>
            <td align="right" <%=FontStyle%>>
              <a href="<jetspeed:uriLookup type="EditAccount"/>" <%=FontStyle%>><jetspeed:l10n key="TOP_EDITACCOUNT"/> <%=data.getUser().getUserName()%></a>
            </td>
          </tr>
          <tr>
            <td align="right" <%=FontStyle%>>
              <a href="<jetspeed:uriLookup type="Logout"/>" <%=FontStyle%>><jetspeed:l10n key="TOP_LOGOUT"/></a>
            </td>
          </tr>
          <%
          // Leave space between last line and content
          %>
          <tr>
            <td>&nbsp;</td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</div>
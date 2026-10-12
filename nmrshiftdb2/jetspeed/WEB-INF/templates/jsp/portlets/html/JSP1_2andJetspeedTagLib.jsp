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
<%@ page language="java"
         import="java.util.Enumeration"
         session="false"
%>
<%@ taglib uri='/WEB-INF/templates/jsp/tld/template.tld' prefix='jetspeed' %>
    <html>
<!--
  Copyright (c) 2001 The Apache Software Foundation.  All rights 
  reserved.
-->

<!--
 A JSP portlet example that displays Jetspeed TagLib and servlet request
 data. 

 @author <a href="mailto:paulsp@apache.org">Paul Spencer</a>
-->
  <body>
    <center><h1>Jetspeed Tag Library</h1></center>
    <center><h2>jetspeed:info</h2></center>
    <table>
      <tr>
        <th>requestedInfo</th>
        <th>Returned Value</th>
      </tr>
      <tr>
        <td>Email</td>
        <td><jetspeed:info requestedInfo="Email" /></td>
      </tr>
      <tr>
        <td>FirstName</td>
        <td><jetspeed:info requestedInfo="FirstName" /></td>
      </tr>
      <tr>
        <td>LastName</td>
        <td><jetspeed:info requestedInfo="LastName" /></td>
      </tr>
      <tr>
        <td>UserName</td>
        <td><jetspeed:info requestedInfo="UserName" /></td>
      </tr>
      <tr>
        <td>ServerDate</td>
        <td><jetspeed:info requestedInfo="ServerDate" /></td>
      </tr>
    </table>

    <center><h2>jetspeed:uriLookup</h2></center>
    <table>
      <tr>
        <th>Type</th>
        <th>Returned Value</th>
      </tr>
      <tr>
        <td>Customize</td>
        <td><jetspeed:uriLookup type="Customize" /></td>
      </tr>
      <tr>
        <td>EditAccount</td>
        <td><jetspeed:uriLookup type="EditAccount" /></td>
      </tr>
      <tr>
        <td>Login</td>
        <td><jetspeed:uriLookup type="Login" /></td>
      </tr>
      <tr>
        <td>Logout</td>
        <td><jetspeed:uriLookup type="Logout" /></td>
      </tr>
      <tr>
        <td>Home</td>
        <td><jetspeed:uriLookup type="Home" /></td>
      </tr>
    </table>

    <center><h2>jetspeed:contentUri</h2></center>
    <table>
      <tr>
        <th>href</th>
        <th>Returned Value</th>
      </tr>
      <tr>
        <td>apidocs/</td>
        <td><jetspeed:contentUri href="apidocs/" /></td>
      </tr>
      <tr>
        <td>docs/</td>
        <td><jetspeed:contentUri href="docs/" /></td>
      </tr>
      <tr>
        <td>rss/</td>
        <td><jetspeed:contentUri href="rss/" /></td>
      </tr>
    </table>

    <center><h2>jetspeed:link</h2></center>
    <table>
      <tr>
        <th>template</th>
        <th>action</th>        
        <th>Returned Value</th>
      </tr>
      <tr>
        <td>Login</td>
        <td>JLogoutUser</td>        
        <td><jetspeed:link template="Login" action="JLogoutUser"/></td>
      </tr>
      <tr>
        <td>ChangePassword</td>
        <td></td>                
        <td><jetspeed:link template="ChangePassword" /></td>
      </tr>
    </table>

    <center><h2>jetspeed:portlet</h2></center>
    <table>
      <tr>
        <th>name</th>
        <th>psml</th>        
        <th>Returned Value</th>
      </tr>
      <tr>
        <td>HelloVelocity</td>
        <td>user/turbine/media-type/html/page/default.psml</td>        
        <td><jetspeed:portlet name="HelloVelocity" psml="user/turbine/media-type/html/page/default.psml"/></td>
      </tr>
      <tr>
        <td>JetspeedFramed <SMALL><p>(you will need JetspeedFramed in your profile in order for this to work)</P></SMALL></td>
        <td>&nbsp;</td>                
        <td><jetspeed:portlet name="JetspeedFramed"/></td>
      </tr>      
    </table>

    <center><h2>jetspeed:portletlink</h2></center>
    <table>
      <tr>
        <th>name</th>
        <th>psml</th>        
        <th>action</th>                        
        <th>Returned Value</th>
      </tr>
      <tr>
        <td>HelloVelocity</td>
        <td>user/turbine/media-type/html/page/default.psml</td>        
        <td>controls.Print</td>                
        <td><a href="<jetspeed:portletlink name="HelloVelocity" psml="user/turbine/media-type/html/page/default.psml" action="controls.Print"/>"><jetspeed:portletlink name="HelloVelocity" psml="user/turbine/media-type/html/page/default.psml" action="controls.Print"/></A></td>
      </tr>
      <tr>
        <td>DatabaseBrowserTest</td>
        <td></td>        
        <td></td>                
        <td><a href="<jetspeed:portletlink name="DatabaseBrowserTest" />"><jetspeed:portletlink name="DatabaseBrowserTest" /></A></td>
      </tr>
    </table>

    <center><h2>jetspeed:l10n</h2></center>
    <table>
      <tr>
        <th>key</th>
        <th>alt</th>        
        <th>Returned Value</th>
      </tr>
      <tr>
        <td>LOGIN_USERNAME</td>
        <td>&nbsp;</td>        
        <td><jetspeed:l10n key="LOGIN_USERNAME" /></td>
      </tr>
      <tr>
        <td>NOT_LOCALIZED</td>
        <td>&nbsp;</td>        
        <td><jetspeed:l10n key="NOT_LOCALIZED" /></td>
      </tr>
      <tr>
        <td>NOT_LOCALIZED</td>
        <td>Alternative translation</td>        
        <td><jetspeed:l10n key="NOT_LOCALIZED" alt="Alternative translation" /></td>
      </tr>
    </table>

    <center><h2>jetspeed:parameterStyle</h2></center>
    <table>
      <tr>
        <th>name</th>
        <th>style</th>        
        <th>value</th>                
        <th>options</th>                
        <th>Returned Value</th>
      </tr>
      <tr>
        <td>portlet-list</td>
        <td>RegistryEntryListBox</td>  
        <td>&nbsp;</td>                              
        <td>&nbsp;</td>                
        <td><jetspeed:parameterStyle name="porlet-list" style="RegistryEntryListBox"/></td>
      </tr>
      <tr>
        <td>skin-list</td>
        <td>RegistryEntryListBox</td>  
        <td>&nbsp;</td>                              
        <td>registry=Skin;set-label=true</td>                
        <td><jetspeed:parameterStyle name="skin-list" style="RegistryEntryListBox">registry=Skin</jetspeed:parameterStyle></td>
      </tr>
      <tr>
        <td>control-list</td>
        <td>RegistryEntryListBox</td>  
        <td>TabControl</td>                              
        <td>registry=PortletControl;set-label=true</td>                
        <td><jetspeed:parameterStyle name="control-list" style="RegistryEntryListBox" value="TabControl">registry=PortletControl;set-label=true</jetspeed:parameterStyle></td>
      </tr>
    </table>

    <hr/>
    <center><h1>HTTP Request Header</h1></center>
    <table>
      <tr>
        <th>Name</th>
        <th>Returned Value</th>
      </tr>
      <tr>
        <td>Auth Type</td>
        <td><%= request.getAuthType() %></td>
      </tr>
      <tr>
        <td>Character Encoding</td>
        <td><%= request.getCharacterEncoding() %></td>
      </tr>
      <tr>
        <td>Content Length</td>
        <td><%= request.getContentLength() %></td>
      </tr>
      <tr>
        <td>Content Type</td>
        <td><%= request.getContentType() %></td>
      </tr>
      <tr>
        <td>Context Path</td>
        <td><%= request.getContextPath() %></td>
      </tr>
      <tr>
        <td>Method</td>
        <td><%= request.getMethod() %></td>
      </tr>
      <tr>
        <td>Path Info</td>
        <td><%= request.getPathInfo() %></td>
      </tr>
      <tr>
        <td>Path Translated</td>
        <td><%= request.getPathTranslated() %></td>
      </tr>
      <tr>
        <td>Protocol</td>
        <td><%= request.getProtocol() %></td>
      </tr>
      <tr>
        <td>Query String</td>
        <td><%= request.getQueryString() %></td>
      </tr>
      <tr>
        <td>Remote Address</td>
        <td><%= request.getRemoteAddr() %></td>
      </tr>
      <tr>
        <td>Remote Host</td>
        <td><%= request.getRemoteHost() %></td>
      </tr>
      <tr>
        <td>Remote User</td>
        <td><%= request.getRemoteUser() %></td>
      </tr>
      <tr>
        <td>Requested Session Id</td>
        <td><%= request.getRequestedSessionId() %></td>
      </tr>
      <tr>
        <td>Requested Session Id from Cookie</td>
        <td><%= request.isRequestedSessionIdFromCookie() %></td>
      </tr>
      <tr>
        <td>Requested Session Id from URL</td>
        <td><%= request.isRequestedSessionIdFromURL() %></td>
      </tr>
      <tr>
        <td>Request URI</td>
        <td><%= request.getRequestURI() %></td>
      </tr>
      <tr>
        <td>Request URL</td>
        <td><%= request.getRequestURL() %></td>
      </tr>
      <tr>
        <td>Scheme</td>
        <td><%= request.getScheme() %></td>
      </tr>
      <tr>
        <td>Secure</td>
        <td><%= request.isSecure() %></td>
      </tr>
      <tr>
        <td>Server Name</td>
        <td><%= request.getServerName() %></td>
      </tr>
      <tr>
        <td>Server Port</td>
        <td><%= request.getServerPort() %></td>
      </tr>
      <tr>
        <td>Servlet Path</td>
        <td><%= request.getServletPath() %></td>
      </tr>
    </table>

    <center><h2>HTTP Headers</h2></center>
    <table>
      <tr>
        <th>Name</th>
        <th>Value</th>
      </tr>
<%
      Enumeration e = request.getHeaderNames();

      while (e.hasMoreElements()) {
        String name = (String)e.nextElement();
        String value = request.getHeader(name);
        out.println("<tr><td>" + name + "</td><td>" + value + "</td></tr>");
      }
%>
    </table>

    <center><h2>Attributes</h2></center>
    <table>
      <tr>
        <th>Name</th>
      </tr>
<%
      e = request.getAttributeNames();

      while (e.hasMoreElements()) {
        String name = (String)e.nextElement();
        out.println("<tr><td>" + name + "</td></tr>");
      }
%>
    </table>

    <center><h2>Parameters</h2></center>
    <table>
      <tr>
        <th>Name</th>
        <th>Value</th>
      </tr>
<%
      e = request.getParameterNames();

      while (e.hasMoreElements()) {
        String name = (String)e.nextElement();
        String value = request.getParameter(name);
        out.println("<tr><td>" + name + "</td><td>" + value + "</td></tr>");
      }
%>
    </table>
    <center><h2>Cookies</h2></center>
    <table>
      <tr>
        <th>Name</th>
        <th>Value</th>
      </tr>
<%
      Cookie[] cookies = request.getCookies();
      if (cookies != null)
      {
        for (int i = 0; i < cookies.length; i++)
        {
          out.println("<tr><td>" + cookies[i].getName() + "</td><td>" + cookies[i].getValue() + "</td></tr>");
        }
      }
%>
    </table>
  </body>
</html>
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
         session="false"
%>
<%@ taglib uri='/WEB-INF/templates/jsp/tld/template.tld' prefix='jetspeed' %>
<p>
Jetspeed JSP Hello World Example
<br/><br/>
    <b><center>Examples of jetspeed:info</center></b>
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
<br/><br/>
    <b><center>Examples of jetspeed:contentUri</center></b>
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

</p>
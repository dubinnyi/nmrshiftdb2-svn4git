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

<table width="100%" border="0" cellspacing="0" cellpadding="2">
  <tr>
    <td>
      <a href="<jetspeed:uriLookup type="Home" />">
        <small><jetspeed:l10n key="HOME"/></small>
      </a>
    </td>
  </tr>
  <tr>
    <td>
      <a href="<jetspeed:contentUri href="docs/" />">
        <small><jetspeed:l10n key="LEFT_DOCUMENTATION"/></small>
      </a>
    </td>
  </tr>
  <tr>
    <td>
      <a href="<jetspeed:contentUri href="apidocs/" />">
        <small>API</small>
      </a>
    </td>
  </tr>
</table>
    
  </td>
  <td valign="top" rowspan="2">


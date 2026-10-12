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
<%@ page import = "java.util.Map" %>

<%
/**
 * Returns texarea control intialized with the value parameter using the following options:
 * <UL>
 * <LI><code>rows</code>: number of rows (default = 3)</LI>
 * <LI><code>cols</code>: number of columns (default = 80)</LI> 
 *</UL>
 * 
 * @author <a href="mailto:morciuch@apache.org">Mark Orciuch</a>
 * @version $Id: TextArea.jsp,v 1.3 2005/01/18 17:08:46 morciuch Exp $
 */

Map parms = (Map) request.getAttribute("parms");          
String rows = (String) parms.get("rows"); rows = rows != null ? rows : "3";
String cols = (String) parms.get("cols"); cols = cols != null ? cols : "80";
%>

<textarea name="<%=request.getAttribute("name")%>" rows="<%=rows%>" cols="<%=cols%>"><%=request.getAttribute("value")%></textarea>

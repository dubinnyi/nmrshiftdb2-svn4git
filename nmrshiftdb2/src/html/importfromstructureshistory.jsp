<%
/*  This file is part of nmrshiftdb2, an NMR database package.
 *  Copyright (C) 2010,2013 Stefan Kuhn
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */%>
 
<%@ page import="org.openscience.nmrshiftdb.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.awt.Color" %>

<script>
  function getKetcher()
  {
    var ketcherFrame = window.opener.document.getElementById('ketcherFrame');
    var ketcher = null;
    if ('contentDocument' in ketcherFrame)
      ketcher = ketcherFrame.contentWindow.ketcher;
    else // IE7
      ketcher = document.frames['ketcherFrame'].window.ketcher;
    return ketcher;
  }

  function loadMol(mol){
    var ketcher = getKetcher();
    if (ketcher)
      ketcher.setMolecule(mol);
  }
</script>
<html>
<head>
<title>Import from structures history</title>
<%  String CLEAR_HISTORY="clearstructureshistory";
    if(request.getParameter("nmrshiftdbaction")!=null && request.getParameter("nmrshiftdbaction").equals(CLEAR_HISTORY))
      session.removeAttribute("structureshistory");
    List v=GeneralUtils.getVectorWithSessionBindingAttributeFromSession(session,NmrshiftdbConstants.STRUCTURESHISTORY);
    String[] files=new String[v.size()];
    for(int i=0;i<v.size();i++){
      String mol = (String)v.get(i);
      String filename=System.currentTimeMillis() + "import.jpg";
      GeneralUtils.makeJpg(mol, "nmrshiftdbhtml/" + filename,config,100,100,false,null,null,  Color.LIGHT_GRAY);
      files[i]=filename+";jsessionid="+session.getId();
    }%>
</head>
<body>
<% if(files.length==0){%>
  You did not yet enter a structure during your current session!
<% } else { %>
  Click on a structure to import:
  <%for(int i=0;i<files.length;i++){
      if(GeneralUtils.getNmrshiftdbProperty("structureeditor").equals("MARVINJS")) { %>
	    <a href="javascript:window.opener.marvinSketcherInstance.importAsMol('<%=GeneralUtils.replace((String)v.get(i),"\n","\\n")%>');self.close();"><img src="<%= files[i] %>"></a>
      <% } else if(GeneralUtils.getNmrshiftdbProperty("structureeditor").equals("GGAKETCHER")) { %>
	    <a href="javascript:loadMol('<%=GeneralUtils.replace((String)v.get(i),"\n","\\n")%>');self.close();"><img src="<%= files[i] %>"></a>
      <% } else { %>
      	<a href="javascript:window.opener.document.JcpEditor.setMolFile('<%=GeneralUtils.replace((String)v.get(i),"\n","\\n")%>');self.close();"><img src="<%= files[i] %>"></a>
	  <% }     
    }
  %>
  </form>
  <form action="importfromstructureshistory.jsp" name=myForm2>
    <input type="hidden" name="nmrshiftdbaction" value="<%=CLEAR_HISTORY%>">
    <input type="submit" value="Clear history">
  </form>
<% } %>
</body>
</html>

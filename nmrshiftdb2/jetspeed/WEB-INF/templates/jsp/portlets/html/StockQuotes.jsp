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
<%@ page import = "org.apache.turbine.util.Log" %> 
<%@ page import = "org.apache.jetspeed.webservices.finance.stockmarket.StockQuote" %> 

<%
try{ 
    RunData rundata = (RunData) request.getAttribute("rundata"); 
    StockQuote[] quotes = (StockQuote[]) request.getAttribute("quotes");
    String[] columns = (String[]) request.getAttribute("columns");
    String jspeid = (String) request.getAttribute("js_peid");
    String currentSort = (String) request.getAttribute("sort");
    if (currentSort == null)
    {
        currentSort = "";
    }
    String selectedColumns = (String) request.getAttribute("selected-columns");
%>

<FORM METHOD="get">
  <INPUT TYPE="hidden" NAME="js_peid" VALUE="<%=jspeid%>">  
  Enter symbol(s) separated with commas: <input name="symbols" type="TEXT"><INPUT TYPE="SUBMIT" NAME="eventSubmit_doRefresh" VALUE="Get Quotes"><BR>
</FORM>      
<table>
  <tr>
    <td>
      <table border="true" cellspacing="1" cellpadding="3">
        <tr>
          <%for (int i = 0; columns != null && i < columns.length; i++) {
              String sortInd = "";
              if (columns[i].equals(currentSort))
              {
                  sortInd = "sort";
              }%>
            <TH><A HREF="<jetspeed:dynamicUri/>?eventSubmit_doSort=sort&sort=<%=columns[i]%>&js_peid=<%=jspeid%>"><%=columns[i]%></A><SUP><%=sortInd%></SUP></TH>
          <%}%>
        </tr>

          <%for (int j = 0; quotes != null && j < quotes.length; j++) {%>
        <tr>
          <%if (selectedColumns.indexOf("Symbol") >= 0) {%><TD><%=quotes[j].getSymbol()%></TD><%}%>
          <%if (selectedColumns.indexOf("Price") >= 0) {%><TD><%=quotes[j].getPrice()%></TD><%}%>
          <%if (selectedColumns.indexOf("Change") >= 0) {%><TD><%=quotes[j].getChange()%></TD><%}%>
          <%if (selectedColumns.indexOf("Volume") >= 0) {%><TD><%=quotes[j].getVolume()%></TD><%}%>
        </tr>
          <%}%>
      </table>
    </td>
  </tr>
</table>
<%} catch (Exception e) {
    Log.error(e);
    return;
}%>

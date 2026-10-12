<html><body>
<%@page import="java.util.*,java.io.*,org.openscience.nmrshiftdb.om.*,org.apache.torque.util.*,org.apache.turbine.util.*" %>
<%        String nmrpropsFile = ServletUtils.expandRelative(config, "/WEB-INF/conf/NMRShiftDB.properties");
          Properties nmrprops = new Properties();
          FileInputStream fis=new FileInputStream(nmrpropsFile);
          nmrprops.load(fis);
          fis.close();
          List v=DBSessionsPeer.doSelect(new Criteria(), config, request);
          String url=javax.servlet.http.HttpUtils.getRequestURL(request).toString();
          if(nmrprops.getProperty("useloadbalancing").equals("false") || ((DBSessions)v.get(0)).getLowestLoadServer()==null || url.indexOf(((DBSessions)v.get(0)).getLowestLoadServer())!=-1){
            %><jsp:forward page="nmrshiftdb/"/><%
          }else{
            String protocol="http";
            if(url.indexOf("https")>-1)
              protocol="https";
            response.sendRedirect(protocol+"://"+((DBSessions)v.get(0)).getLowestLoadServer()+"/nmrshiftdb");
          }
%>
<p>nmrshiftdb2 is a web database for organic structures and their nuclear 
magnetic resonance (nmr) spectra. It allows for spectrum prediction 
(currently only for carbon) as well as for searching spectra, structures 
and other properties. Last not least, it features peer-reviewed 
submission of datasets by its users. The nmrshiftdb2 software is open 
source, the data is published under an open content license. Please 
consult the documentation for more detailed information.</p></body></html>

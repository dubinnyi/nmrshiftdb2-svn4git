<html>
<body>
<%  String referer=null;
    if(request.getHeader("referer")!=null)
        referer=org.openscience.nmrshiftdb.util.GeneralUtils.replace(request.getHeader("referer"),"/nmrshiftdb;","/portal;");
    if(request.getHeader("referer")!=null && (new org.openscience.nmrshiftdb.util.UrlTool().getBaseUrl(request).startsWith(referer) || referer.startsWith(new org.openscience.nmrshiftdb.util.UrlTool().getBaseUrl(request)))){
        response.sendRedirect(request.getParameter("url"));
    }else{
        out.println("You may have clicked a spam link!");
    }
%>
</body>
</html>

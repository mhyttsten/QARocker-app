<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.fl.be.extract.D_DB" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>

<html>

<head><title>Indexes</title></head>
<body>
  <h1>Indexes</h1>
  <%
    for (String index: D_DB._indexes) {
        String href = "JSP_Index_DisplayFundsForIndex.jsp?" +
           JSP_Constants.ARG_ID + "=" + URLEncoder.encode(index, "UTF-8");
  %>
  <a href="<%=href%>"><%=index%></a><br>
  <% } %>

</body>
</html>


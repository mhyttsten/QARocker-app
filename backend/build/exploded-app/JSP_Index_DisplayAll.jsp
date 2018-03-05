<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.fl.be.extract.GCSWrapper" %>
<%@ page import="com.pf.shared.datamodel.DB_FundInfo" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>

<html>

<head><title>Indexes</title></head>
<body>
  <h1>Indexes</h1>
  <%
    JSP_Helper.initialize();
    for (String index: DB_FundInfo.getAllIndexes()) {
        String href = "JSP_Index_DisplayFundsForIndex.jsp?" +
           JSP_Constants.ARG_ID + "=" + URLEncoder.encode(index, "UTF-8");
  %>
  <a href="<%=href%>"><%=index%></a><br>
  <% } %>

</body>
</html>


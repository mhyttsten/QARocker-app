<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.fl.be.extract.GCSWrapper" %>
<%@ page import="com.pf.shared.datamodel.DB_FundInfo" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="com.pf.shared.utils.OTuple2G" %>
<html>

<head><title>Indexes</title></head>
<body>
  <h1>Indexes</h1>
  <%
    JSP_Helper.initialize();
    for (OTuple2G<String, Integer> e: DB_FundInfo.getIndexesByFundCount()) {
        String href = "JSP_Index_DisplayFundsForIndex.jsp?" +
           JSP_Constants.ARG_ID + "=" + URLEncoder.encode(e._o1, "UTF-8");
  %>
  <a href="<%=href%>"><%=e._o1%></a>: <%=e._o2%><br>
  <% } %>

</body>
</html>


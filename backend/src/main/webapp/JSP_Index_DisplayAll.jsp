<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundInfo" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundIndex" %>
<%@ page import="com.pf.fl.be.util.EE" %>
<%@ page import="com.pf.shared.MM" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="com.pf.fl.be.util.Constants" %>
<!-- page import="com.google.gson.Gson" -->

<% EE ee = EE.getEE(); %>

<html>

<head><title>Indexes</title></head>
<body>
  <%
    Iterator<FLA_FundIndex> fundIndexes = ofy().load().type(FLA_FundIndex.class).iterator();
  %>
  <h1>Indexes</h1>
  <%
    while (fundIndexes.hasNext()) {
        FLA_FundIndex fundIndex = fundIndexes.next();
        String indexName = fundIndex.mKey_IndexName;
        String href = "JSP_Index_DisplayFundsForIndex.jsp?" +
           Constants.ARG_ID + "=" + URLEncoder.encode(indexName, "UTF-8");
  %>
  <a href="<%=href%>"><%=indexName%></a><br>
  <% } %>

</body>
</html>


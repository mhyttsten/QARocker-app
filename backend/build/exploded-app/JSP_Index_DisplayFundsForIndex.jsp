<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.googlecode.objectify.Key" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundInfo" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundIndex" %>
<%@ page import="com.pf.fl.be.util.EE" %>
<%@ page import="com.pf.shared.MM" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.pf.fl.be.util.Constants" %>
<!-- page import="com.google.gson.Gson" -->

<% EE ee = EE.getEE(); %>

<html>

<head><title>Funds in an Index</title></head>
<body>
  <%
    String id = request.getParameter(Constants.ARG_ID);
    id = URLDecoder.decode(id, "UTF-8");
    FLA_FundIndex fundIndex = ofy().load().type(FLA_FundIndex.class).id(id).now();
    Key<FLA_FundIndex> idKey = Key.create(FLA_FundIndex.class, id);
    List<FLA_FundInfo> fundInfoList = ofy().load().type(FLA_FundInfo.class).filter("mIndexCompare", idKey).list();
  %>
  <h1>Funds for Index: <%=id%> (<%=fundInfoList.size()%>)</h1>
  <%
    for (int i=0; i < fundInfoList.size(); i++) {
        FLA_FundInfo fundInfo = fundInfoList.get(i);
  %>
  <%=fundInfo.mType%>.<%=fundInfo.mName%><br>
  <% } %>

</body>
</html>


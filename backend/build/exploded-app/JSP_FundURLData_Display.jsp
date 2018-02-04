<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.googlecode.objectify.Key" %>
<%@ page import="com.pf.fl.be.datastore.DS" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_Cache_FundInfo" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_Cache" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundInfo" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundIndex" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundPortfolio" %>
<%@ page import="com.pf.fl.be.util.EE" %>
<%@ page import="com.pf.shared.MM" %>
<%@ page import="com.googlecode.objectify.Key" %>
<%@ page import="com.googlecode.objectify.Ref" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="com.pf.shared.OTuple2G" %>
<%@ page import="com.pf.shared.OTuple3G" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper_fundURLData" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.pf.fl.be.util.Constants" %>

<!-- PARAM_TYPE is null, INVALID, or fund Type -->

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Funds in an Index</title>
<link rel="stylesheet" type="text/css" href="CSSMain.css">
</head>
<body>

<%
    final Logger log = Logger.getLogger("JSP_FundURLData_Display");
    final String TAG = MM.getClassName("JSP_FundURLData_Display");

    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    EE ee = EE.getEE();

    String argOperation = request.getParameter(Constants.ARG_OPERATION);

    String typeStr = request.getParameter(Constants.PARAM_TYPE);
    if (typeStr == null || typeStr.trim().length()== 0) {
       typeStr = null;
    }
    // ee.dinfo(log, TAG, "typeStr is: " + typeStr);
    List<FLA_Cache_FundInfo> list = FLA_Cache.cacheFundInfosByTypeOrNull(typeStr);

// COMMIT
if (argOperation != null && argOperation.equals(Constants.OP_COMMIT)) {
    JSP_Helper_fundURLData.urlControl_VerifyOrExecute(false, request);
    response.sendRedirect("JSP_FundURLData_Display.jsp");
}

// VERIFY
if (argOperation != null && argOperation.equals(Constants.OP_VERIFY)) { %>
<% String tableData = JSP_Helper_fundURLData.urlControl_VerifyOrExecute(true, request);
   if (tableData == null || tableData.trim().length() == 0) { %>
      <h1>No operations to perform</h1>
<% } else { %>
<form action="JSP_FundURLData_Display.jsp" method="POST" accept-charset="utf-8">
<input type="hidden" name="<%=Constants.ARG_OPERATION%>" value="<%=Constants.OP_COMMIT%>">
<div class="selectList3">
<table width="1400">
<%= tableData %>
</table>
</div>
<input type="submit" value="Perform Updates">
</form>
<% } }

// DISPLAY
else { %>
<form action="JSP_FundURLData_Display.jsp" method="POST" accept-charset="utf-8">
<input type="hidden" name="<%=Constants.ARG_OPERATION%>" value="<%=Constants.OP_VERIFY%>">
<div class="selectList3">
<table width="1400">
<%= JSP_Helper_fundURLData.urlControl_DisplayFundEmptiesAndList(list) %>
</table>
</div>
<input type="submit" value="Update">
</form>
<% } %>

</body>
</html>


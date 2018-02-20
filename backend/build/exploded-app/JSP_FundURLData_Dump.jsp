<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.googlecode.objectify.Key" %>
<%@ page import="com.pf.fl.be.datastore.DS" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_Cache_FundInfo" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_Cache" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundInfo" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundIndex" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundPortfolio" %>
<%@ page import="com.pf.fl.be.util.EE" %>
<%@ page import="com.pf.shared.utils.MM" %>
<%@ page import="com.googlecode.objectify.Key" %>
<%@ page import="com.googlecode.objectify.Ref" %>
<%@ page import="java.util.logging.Logger" %>
<%@ page import="com.pf.shared.utils.OTuple2G" %>
<%@ page import="com.pf.shared.utils.OTuple3G" %>
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

    for (FLA_Cache_FundInfo fi: list) {
%>
    <%=fi.getTypeAndName()%>,<%=fi.mURL%><br>
<% } %>

</body>
</html>


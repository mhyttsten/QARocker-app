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
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
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
    final Logger log = Logger.getLogger("JSP_Report02_Weekly_Display");
    final String TAG = MM.getClassName("JSP_Report02_Weekly_Display");

    EE ee = EE.getEE();
    String s = JSP_Helper.fundReport_WeeklyTable(request, response);
%>
<%=s%>

</body>
</html>


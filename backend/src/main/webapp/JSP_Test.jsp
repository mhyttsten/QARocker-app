<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.pf.fl.be.util.EE" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper_Test" %>
<%@ page import="com.pf.fl.be.util.Constants" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundInfo" %>
<%@ page import="java.util.Iterator" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Main</title>
</head>
<body>

<!-- Display all fund names -->
<%= JSP_Helper_Test.displayAllFundNames() %>

<!-- Test Euro Character -->
<!--
<%= JSP_Helper_Test.showEuroCharacterTest() %>
<% byte[] t = new byte[] { (byte)0xe2, (byte)0x82, (byte)0xac };
String s = new String(t, "UTF-8"); %>
Here is the string: <%= s %>
<%
   response.setContentType("text/html");
   response.setCharacterEncoding("UTF-8");
%>
-->


</body>
</html>

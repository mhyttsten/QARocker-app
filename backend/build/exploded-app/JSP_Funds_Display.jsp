<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.pf.fl.be.extract.GCSWrapper" %>
<%@ page import="com.pf.shared.datamodel.DB_FundInfo" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.shared.datamodel.D_FundInfo" %>

<html>

<head><title>Main</title></head>
<body>
  <h3>All the Funds</h3>

<%
    JSP_Helper.initialize();
    String s = JSP_Helper.fundsDisplayAll();
%>

<%= s %>

</body>
</html>

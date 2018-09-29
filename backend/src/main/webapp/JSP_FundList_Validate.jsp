<%@ page import="com.pf.fl.be.extract.GCSWrapper" %>
<%@ page import="com.pf.shared.datamodel.DB_FundInfo" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.shared.datamodel.D_FundInfo" %>

<%
    String type = request.getParameter(JSP_Constants.PARAM_ID);
%>

<html>

<head><title>Validate Funds</title></head>
<body>
<%=JSP_Helper.validateFunds(type)%>
<%=type%><br>
</body>
</html>

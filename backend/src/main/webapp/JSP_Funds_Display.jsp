<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.pf.fl.be.extract.D_DB" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.shared.datamodel.D_FundInfo" %>

<html>

<head><title>Main</title></head>
<body>
  <h3>All the Funds</h3>

<%
    for (D_FundInfo fi: D_DB._fis) {
%>
<%= fi.getOneLiner() %> <br>
<%    }  %>

</body>
</html>

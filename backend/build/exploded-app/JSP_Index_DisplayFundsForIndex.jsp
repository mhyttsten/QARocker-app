<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Constants" %>
<%@ page import="com.pf.shared.datamodel.D_FundInfo" %>
<%@ page import="com.pf.fl.be.extract.D_DB" %>

<html>

<head><title>Funds in an Index</title></head>
<body>
  <%
    String id = request.getParameter(JSP_Constants.ARG_ID);
    id = URLDecoder.decode(id, "UTF-8");
    List<D_FundInfo> l = D_DB._fisByIndexHM.get(id);
  %>
  <h1>Funds for Index: <%=id%> (<%=l.size()%>)</h1>
  <%
    for (int i=0; i < l.size(); i++) {
        D_FundInfo fundInfo = l.get(i);
  %>
  <%=fundInfo.getTypeAndName()%><br>
  <% } %>

</body>
</html>


<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundInfo" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundPortfolio" %>
<%@ page import="com.pf.fl.be.util.EE" %>
<%@ page import="com.pf.shared.MM" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="com.pf.fl.be.util.Constants" %>
<!-- page import="com.google.gson.Gson" -->

<% EE ee = EE.getEE(); %>

<html>

<head><title>Indexes</title>
</head>
<body>
  <%
    String hrefCreateAll = "JSP_Portfolio_Display.jsp" +
        "?" + Constants.ARG_OPERATION + "=" + Constants.OP_UPDATE +
        "&" + Constants.PARAM_TYPE + "=" + FLA_FundInfo.TYPE_ALL;
    String hrefCreateSEB = "JSP_Portfolio_Display.jsp" +
        "?" + Constants.ARG_OPERATION + "=" + Constants.OP_UPDATE +
        "&" + Constants.PARAM_TYPE + "=" + FLA_FundInfo.TYPE_SEB;
    String hrefCreatePPM = "JSP_Portfolio_Display.jsp" +
        "?" + Constants.ARG_OPERATION + "=" + Constants.OP_UPDATE +
        "&" + Constants.PARAM_TYPE + "=" + FLA_FundInfo.TYPE_PPM;
    String hrefCreateSPP = "JSP_Portfolio_Display.jsp" +
        "?" + Constants.ARG_OPERATION + "=" + Constants.OP_UPDATE +
        "&" + Constants.PARAM_TYPE + "=" + FLA_FundInfo.TYPE_SPP;
  %>
  <h1>Indexes</h1>
  <a href="<%=hrefCreateAll%>">Create New</a><br>
  <a href="<%=hrefCreateSEB%>">Create New SEB</a><br>
  <a href="<%=hrefCreatePPM%>">Create New PPM</a><br>
  <a href="<%=hrefCreateSPP%>">Create New SPP</a><br>
  <br>
  <br>
  <table width=\"1000\">
  <%
    Iterator<FLA_FundPortfolio> list = ofy().load().type(FLA_FundPortfolio.class).iterator();
    while (list.hasNext()) {
        FLA_FundPortfolio elem = list.next();
        String hrefView = "JSP_Portfolio_Display.jsp?" +
           Constants.ARG_OPERATION + "=" + Constants.OP_VIEW
           + "&" + Constants.ARG_ID + "=" + elem.mId;
        String hrefUpdate = "JSP_Portfolio_Display.jsp?" +
           Constants.ARG_OPERATION + "=" + Constants.OP_UPDATE
           + "&" + Constants.ARG_ID + "=" + elem.mId;
        String hrefDelete = "JSP_Portfolio_Display.jsp?" +
           Constants.ARG_OPERATION + "=" + Constants.OP_DELETE
           + "&" + Constants.ARG_ID + "=" + elem.mId;
  %>
  <tr>
  <td width=\"200\" align=\"left\"><%=elem.mName%></td>
  <td width=\"200\" align=\"left\"><a href="<%=hrefView%>">View</a></td>
  <td width=\"200\" align=\"left\"><a href="<%=hrefUpdate%>">Update</a></td>
  <td width=\"200\" align=\"left\"><a href="<%=hrefDelete%>">Delete</a></td>
  </tr>
  <% } %>
  </table>
</body>
</html>

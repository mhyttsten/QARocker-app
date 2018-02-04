<%@ page import="static com.googlecode.objectify.ObjectifyService.ofy" %>
<%@ page import="com.pf.fl.be.util.EE" %>
<%@ page import="com.pf.fl.be.util.Constants" %>
<%@ page import="com.pf.fl.be.jsphelper.JSP_Helper" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundInfo" %>
<%@ page import="com.pf.fl.be.datamodel.FLA_FundPortfolio" %>
<%@ page import="java.util.Iterator" %>

<% EE ee = EE.getEE();
    String hrefFundData_all = "JSP_FundURLData_Display.jsp";
    String hrefFundData_invalid = "JSP_FundURLData_Display.jsp?" +
        Constants.PARAM_TYPE + "=" + FLA_FundInfo.TYPE_INVALID;
    String hrefFundData_ppm = "JSP_FundURLData_Display.jsp?" +
        Constants.PARAM_TYPE + "=" + FLA_FundInfo.TYPE_PPM;
    String hrefFundData_seb = "JSP_FundURLData_Display.jsp?" +
        Constants.PARAM_TYPE + "=" + FLA_FundInfo.TYPE_SEB;
    String hrefFundData_spp = "JSP_FundURLData_Display.jsp?" +
        Constants.PARAM_TYPE + "=" + FLA_FundInfo.TYPE_SPP;
    String hrefFundData_ppm_dump = "JSP_FundURLData_Dump.jsp?" +
        Constants.PARAM_TYPE + "=" + FLA_FundInfo.TYPE_PPM;
    String hrefFundData_vg_addNames = "JSP_FundURLData_VGAddNames.jsp?";

    String hrefWeekly_all = "JSP_Report02_Weekly_Display.jsp" +
        "?" + Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_TYPE +
        "&" + Constants.PARAM_ID + "=" + FLA_FundInfo.TYPE_ALL;
    String hrefWeekly_seb = "JSP_Report02_Weekly_Display.jsp" +
        "?" + Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_TYPE +
        "&" + Constants.PARAM_ID + "=" + FLA_FundInfo.TYPE_SEB;
    String hrefWeekly_vanguard = "JSP_Report02_Weekly_Display.jsp" +
        "?" + Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_TYPE +
        "&" + Constants.PARAM_ID + "=" + FLA_FundInfo.TYPE_VANGUARD;
    String hrefWeekly_spp = "JSP_Report02_Weekly_Display.jsp" +
        "?" + Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_TYPE +
        "&" + Constants.PARAM_ID + "=" + FLA_FundInfo.TYPE_SPP;
    String hrefWeekly_ppm = "JSP_Report02_Weekly_Display.jsp" +
        "?" + Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_TYPE +
        "&" + Constants.PARAM_ID + "=" + FLA_FundInfo.TYPE_PPM;
    String hrefWeekly_report_seb = "JSP_Report02_Weekly_Display.jsp" +
        "?" + Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_REPORT_OPPORTUNITY +
        "&" + Constants.PARAM_ID + "=" + FLA_FundInfo.TYPE_SEB;

%>

<html>

<head><title>Main</title></head>
<body>
  <h1>These are the available alternatives</h2>
  <a href="JSP_Report01_DisplayAll.jsp">Show Reports 01</a><br>
  <a href="JSP_Index_DisplayAll.jsp">Show Indexes</a><br>
  <a href="JSP_Portfolio_DisplayAll.jsp">Show Portfolios</a><br>
  <br>
  <a href=<%=hrefFundData_all%>>Show Fund URL Data</a><br>
  <a href=<%=hrefFundData_invalid%>>Show Invalid Fund URL Data</a><br>
  <a href=<%=hrefFundData_ppm%>>Show PPM Fund URL Data</a><br>
  <a href=<%=hrefFundData_seb%>>Show SEB Fund URL Data</a><br>
  <a href=<%=hrefFundData_spp%>>Show SPP Fund URL Data</a><br>
  <a href=<%=hrefFundData_ppm_dump%>>Dump PPM Name </a><br>
  <a href=<%=hrefFundData_vg_addNames%>>Add VG names </a><br>
  <br>

  <a href=<%=hrefWeekly_all%>>Weekly: Type ALL</a><br>
  <a href=<%=hrefWeekly_seb%>>Weekly: Type SEB</a><br>
  <a href=<%=hrefWeekly_vanguard%>>Weekly: Type Vanguard</a><br>
  <a href=<%=hrefWeekly_spp%>>Weekly: Type SPP</a><br>
  <a href=<%=hrefWeekly_ppm%>>Weekly: Type PPM</a><br>
  <a href=<%=hrefWeekly_report_seb%>>Weekly: Report Opportunity SEB</a><br>
  <br>
  <%
    Iterator<FLA_FundPortfolio> list = ofy().load().type(FLA_FundPortfolio.class).iterator();
    while (list.hasNext()) {
        FLA_FundPortfolio elem = list.next();
        String hrefWeekly_display = "JSP_Report02_Weekly_Display.jsp" +
           "?" + Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_PORTFOLIO +
           "&" + Constants.PARAM_ID + "=" + elem.mId;
        String hrefWeekly_sebalt = "JSP_Report02_Weekly_Display.jsp" +
           "?" + Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_PORTFOLIO_MATCH_SEB +
           "&" + Constants.PARAM_ID + "=" + elem.mId;
   %>
   <a href=<%=hrefWeekly_display%>>Weekly: Portfolio <%=elem.mName%></a><br>
   <a href=<%=hrefWeekly_sebalt%>>Weekly: Portfolio <%=elem.mName%>_SEBAlt</a><br>
   <% } %>

  <br>
  <a href="JSP_Test.jsp">TEST</a><br>

</body>
</html>

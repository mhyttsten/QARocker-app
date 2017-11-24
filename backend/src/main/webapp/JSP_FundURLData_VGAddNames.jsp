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
<%@ page import="java.util.HashMap" %>
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

    List<FLA_Cache_FundInfo> list = FLA_Cache.cacheFundInfosByTypeOrNull(Constants.ACCOUNT_TYPE_VANGUARD);
    HashMap<String, Void> hm = new HashMap<>();
    StringBuffer strb = new StringBuffer();
    for (FLA_Cache_FundInfo fi: list) {
       hm.put(fi.mName, null);
       strb.append(fi.mName + "<br>");
    }

// COMMIT
if (argOperation != null && argOperation.equals(Constants.OP_COMMIT)) {
String fundsWLF = request.getParameter(Constants.PARAM_NAME);
String[] funds = fundsWLF.split("\n");


List<String> fundsSaved = new ArrayList<>();
List<String> fundsDups = new ArrayList<>();

boolean didit = false;
for (String s: funds) {
    s = s.trim();
    if (hm.containsKey(s)) {
       fundsDups.add(s);
       continue;
    }

    String url = Constants.url_getVanguard(s);
    FLA_FundInfo fi = new FLA_FundInfo();
    fi.mType = FLA_FundInfo.TYPE_VANGUARD;
    fi.mName = s;
    fi.mURL = url;
    fi.mMSRating = -1;
    fi.mPPMNumber = -1;
    fi.mDateYYMMDD_Updated = MM.getNowAs_YYMMDD(null);
    fi.mDateYYMMDD_Update_Attempted = fi.mDateYYMMDD_Updated;
    ofy().save().entity(fi).now();
    FLA_Cache.updateCacheAdd(fi);

    hm.put(s, null);
    fundsSaved.add(s);
}

response.getWriter().print("<h2>Funds added</h2><br>");
for (String s: fundsSaved) {
   response.getWriter().print(s + "<br>");
}

response.getWriter().print("<h2>Funds that already existed</h2><br>");
for (String s: fundsDups) {
   response.getWriter().print(s + "<br>");
}

// response.getWriter().print("<h2>Already existed dump</h2><br>");
// response.getWriter().print(strb.toString());
}
else {
%>

<form action="JSP_FundURLData_VGAddNames.jsp" method="POST" accept-charset="utf-8" id="usrform">
<input type="hidden" name="<%=Constants.ARG_OPERATION%>" value="<%=Constants.OP_COMMIT%>">

<br>
<input type="submit" value="Add Vanguard Funds">
</form>
<textarea rows="50" cols="80" name="<%=Constants.PARAM_NAME%>" form="usrform"></textarea>

<% } %>

</body>
</html>


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
<!-- page import="com.google.gson.Gson" -->

<!--
ARG_ID = The portfolio to view / edit
ARG_OP = OP_VIEW, OP_UPDATE

PARAM_NAME
PARAM_DATEC
PARAM_DATEM
PARAM_FUND_

DisplayAll

ARG_OP == OP_VIEW
   ARG_ID should be a readable entity
   Read DB and display all values - everything should be readonly
   Dont show the checkbox list
   Submit should be 'BACK' that goes back to DisplayAll

ARG_OP == OP_UPDATE && ARG_ID == null
   Fill in creation and modified date to now
   Fill in checkbox list with all fund names
   Submit should save, then go back to ARG_OP == OP_VIEW

ARG_OP == OP_UPDATE && ARG_ID != null
   Read from db and allows all fields (except textarea to be edited)
   Fill in checkbox list with all fund names, check the ones selected
   Submit should save, then go back to ARG_OP == OP_VIEW

argOperation;
argId; // set to key or null

-->

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Funds in an Index</title>
<link rel="stylesheet" type="text/css" href="CSSMain.css">
</head>
<body>

<%
    final Logger log = Logger.getLogger("JSP_Portfolio_Display");
    final String TAG = MM.getClassName("JSP_Portfolio_Display");

    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");

    EE ee = EE.getEE();

    String idStr = request.getParameter(Constants.ARG_ID);
    Long argId = null;
    if (idStr != null && idStr.trim().length() > 0) {
        argId = new Long(Long.parseLong(idStr));
    }
    String argOperation = request.getParameter(Constants.ARG_OPERATION);

    String name = "";
    String datec = MM.getNowAs_YYMMDD(null);
    String datem = MM.getNowAs_YYMMDD(null);
    String type = request.getParameter(Constants.PARAM_TYPE);
    String fundList = "";
    FLA_FundPortfolio existingPortfolio = null;
    String targetURL = "JSP_Portfolio_DisplayAll.jsp";
    String targetURLText = "Back";

String readOnlyName  = "";
String readOnlyDateC = "";
String readOnlyDateM = "";
String readOnlyFunds = "readOnly";
if (argOperation.equals(Constants.OP_VIEW)) {
   readOnlyName = "readonly";
   readOnlyDateC = "readonly";
   readOnlyDateM = "readonly";
}

if (argId != null) {
   existingPortfolio = ofy().load().type(FLA_FundPortfolio.class).id(argId).now();
   name = existingPortfolio.mName;
   datec = existingPortfolio.mDateYYMMDD_Created;
   datem = existingPortfolio.mDateYYMMDD_Modified;
   type = existingPortfolio.mType;
   List<FLA_Cache_FundInfo> cfis = FLA_Cache.getCacheVersions(existingPortfolio.mFunds);
   fundList = Constants.getTextAreaStringForFundInfos(cfis);
}

if (argOperation.equals(Constants.OP_COMMIT)) {
   if (existingPortfolio == null) {
      existingPortfolio = new FLA_FundPortfolio();
   }
   existingPortfolio.mName = request.getParameter(Constants.PARAM_NAME);
   existingPortfolio.mDateYYMMDD_Created = request.getParameter(Constants.PARAM_DATEC);
   existingPortfolio.mDateYYMMDD_Modified = request.getParameter(Constants.PARAM_DATEM);
   existingPortfolio.mType = request.getParameter(Constants.PARAM_TYPE);
   existingPortfolio.mFunds = new ArrayList<Ref<FLA_FundInfo>>();
   int i=0;
   String s = null;
   do {
      s = request.getParameter(Constants.PARAM_FUND_ + String.valueOf(i));
      if (s != null && s.trim().length() > 0) {
         Long l = new Long(Long.parseLong(s.trim()));
         Ref<FLA_FundInfo> ref = Ref.create(Key.create(FLA_FundInfo.class, l));
         existingPortfolio.mFunds.add(ref);
      }
      i++;
   } while(i < 2000);
   ofy().save().entity(existingPortfolio).now();
   response.sendRedirect("JSP_Portfolio_DisplayAll.jsp");
   return;
}

List<OTuple2G<FLA_Cache_FundInfo, Boolean>> selectFundList = new ArrayList<>();
if (argOperation.equals(Constants.OP_DELETE)) {
   ofy().delete().type(FLA_FundPortfolio.class).id(existingPortfolio.mId).now();
   response.sendRedirect("JSP_Portfolio_DisplayAll.jsp");
   return;
}
if (argOperation.equals(Constants.OP_UPDATE)) {
   targetURL = "JSP_Portfolio_Display.jsp";
   targetURLText = "Update";
   datem = MM.getNowAs_YYMMDD(null);

   List<FLA_Cache_FundInfo> fundInfos = FLA_Cache.cacheFundInfosByTypeOrNull(type);
   for (int i=0; i < fundInfos.size(); i++) {
      FLA_Cache_FundInfo fundInfo = fundInfos.get(i);
      OTuple2G<FLA_Cache_FundInfo, Boolean> ot2g = new OTuple2G<>();
      ot2g._o1 = fundInfo;
      ot2g._o2 = new Boolean(false);
      Key<FLA_FundInfo> fundInfoKey = Key.create(FLA_FundInfo.class, fundInfo.mFundInfoId.longValue());
      if (existingPortfolio != null && existingPortfolio.mFunds != null) {
         for (int j=0; j < existingPortfolio.mFunds.size(); j++) {
            Key<FLA_FundInfo> fiKeyExisting = existingPortfolio.mFunds.get(j).getKey();
            if (fundInfoKey.equals(fiKeyExisting)) {
               ot2g._o2 = new Boolean(true);
            }
         }
      }
      selectFundList.add(ot2g);
   }
}
%>

<form action="<%=targetURL%>" method="POST" accept-charset="utf-8">

<% if (argOperation.equals(Constants.OP_UPDATE) || argOperation.equals(Constants.OP_DELETE)) { %>
<input type="hidden" name="<%=Constants.ARG_OPERATION%>" value="<%=Constants.OP_COMMIT%>">
<% if (argId != null) { %>
<input type="hidden" name="<%=Constants.ARG_ID%>" value="<%=String.valueOf(argId)%>">
<% } } %>

<table width="800">
	<tr>
		<td width="200"><label>Name</label></td>
		<td width="200"><input type="text" name="<%=Constants.PARAM_NAME%>" value="<%=name%>"<%=readOnlyName%>></td>
		<td width="200"></td>
		<td width="200"></td>
	</tr>
	<tr>
		<td><label>Type</label></td>
		<td><input type="text" name="<%=Constants.PARAM_TYPE%>" value="<%=type%>" readonly></td>
		<td></td>
		<td></td>
	</tr>
	<tr>
		<td><label>Creation Date</label></td>
		<td><input type="text" name="<%=Constants.PARAM_DATEC%>" value="<%=datec%>" <%=readOnlyDateC%>></td>
		<td></td>
		<td></td>
	</tr>
	<tr>
		<td><label>Modified Date</label></td>
		<td><input type="text" name="<%=Constants.PARAM_DATEM%>" value="<%=datem%>" <%=readOnlyDateM%>></td>
		<td></td>
		<td></td>
	</tr>
	<tr>
<% if (argOperation.equals(Constants.OP_UPDATE)) { %>
		<td colspan="2"height="400">
        <textarea rows="25" cols="40" <%=readOnlyFunds%>><%=fundList%></textarea>
		</td>
		<td colspan="2">
<div class="selectList">
<% for (int i=0; i < selectFundList.size(); i++) {
    OTuple2G<FLA_Cache_FundInfo, Boolean> ot2g = selectFundList.get(i);
    String checked = "";
    if (ot2g._o2.booleanValue()) {
       checked = "checked";
    }
%>
    <input type="checkbox"
           name="<%=Constants.PARAM_FUND_+String.valueOf(i)%>"
           value="<%=String.valueOf(ot2g._o1.mFundInfoId)%>"
           <%=checked%>
           /><%=ot2g._o1.mType + "." + ot2g._o1.mName + " [" + String.valueOf(i) + "]"%><br />
<% } %>
</div>
</td>
<% } else { // End of update block
// Now starting view block %>
<td colspan="4">
<table>
<tr>
   <th>Type</th>
   <th>Fund</th>
   <th>Data</th>
</tr>
<%
List<FLA_Cache_FundInfo> cfis = FLA_Cache.getCacheVersions(existingPortfolio.mFunds);
for (int i=0; i < cfis.size(); i++) {
   FLA_Cache_FundInfo cfi = cfis.get(i);
   String fundName = cfi.mName;
   if (cfi.mPPMNumber > 0) {
      fundName += "&nbsp;(" + String.valueOf(cfi.mPPMNumber) + ")";
   }
%>
<tr>
   <td width="50"><%=cfi.mType%></td>
   <td width="400"><%=fundName%></td>
   <td>Data</td>
</tr>
<% } %>
</table>
</td>
<% } // end of view block%>

</tr>
<tr><td colspan="4"><input type="submit" value="<%=targetURLText%>"></td></tr>
</table>


</form>

</body>
</html>


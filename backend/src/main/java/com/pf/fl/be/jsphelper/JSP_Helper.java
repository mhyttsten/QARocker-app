package com.pf.fl.be.jsphelper;

import com.pf.fl.be.datamodel.FLA_Cache;
import com.pf.fl.be.datamodel.FLA_Cache_FundDPWeek;
import com.pf.fl.be.datamodel.FLA_Cache_FundInfo;
import com.pf.fl.be.datamodel.FLA_FundIndex;
import com.pf.fl.be.datamodel.FLA_FundPortfolio;
import com.pf.fl.be.util.Constants;
import com.pf.fl.be.util.EE;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;
import com.pf.shared.utils.OTuple3G;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class JSP_Helper {
    private static final Logger log = Logger.getLogger(JSP_Helper.class.getName());
    private static final String TAG = MM.getClassName(JSP_Helper.class.getName());

    /**
     *
     */
//    public static String createTrendReport(String title, List<FLA_Report_Element_Analysis> l) {
//
//        StringBuilder strb = new StringBuilder();
//        strb.append("<h3>" + title + ". Number of entries: " + l.size() + "</h3>\n");
//        strb.append("<table style=\"width:100%\">\n");
//        strb.append("<tr>\n");
//        strb.append("<th>Name</th>\n");
//        strb.append("<th>T/D</th>\n");
//        strb.append("<th>Dur</th>\n");
//        strb.append("<th>Avg</th>\n");
//        strb.append("<th>Med</th>\n");
//        strb.append("<th>Sum</th>\n");
//        strb.append("<th>SDev</th>\n");
//        strb.append("<th>YVtl</th>\n");
//        strb.append("<th>DPs</th>\n");
//        strb.append("</tr>\n");
//        for (FLA_Report_Element_Analysis rea : l) {
//            String name = null;
//            if (rea.mKeyFundIndex != null) {
//                name = rea.mKeyFundIndex;
//            } else {
//                FLA_FundInfo fi = ofy().load().type(FLA_FundInfo.class).id(rea.mKeyFundInfo).now();
//                name = fi.mType + "." + fi.mName;
//            }
//
//            strb.append("<tr>\n");
//            strb.append("<td>" + name + "</td>\n");
//            strb.append("<td>" + rea.mCountTotal + "/" + rea.mCountDirty + "</td>\n");
//            strb.append("<td>" + FLA_Report_Element_Analysis.getTrendDurationString(rea.mTrendDurationCategory) + "</td>\n");
//            strb.append("<td>" + String.format("%.2f", rea.mR1WAvg) + "</td>\n");
//            strb.append("<td>" + String.format("%.2f", rea.mR1WMed) + "</td>\n");
//            strb.append("<td>" + String.format("%.2f", rea.mR1WSum) + "</td>\n");
//            strb.append("<td>" + String.format("%.2f", rea.mR1WStdDev) + "</td>\n");
//            strb.append("<td>" + String.format("%d", rea.mR1WVolatilityYear) + "</td>\n");
//            strb.append("<td>" + rea.getDPsAsString() + "</td>\n");
//            strb.append("</tr>\n");
//        }
//        strb.append("</table>\n");
//        return strb.toString();
//    }

    /**
     *
     */
    private static List<FLA_Cache_FundDPWeek> getDPWeeks(String fundName, List<String> dates, List<FLA_Cache_FundDPWeek> dpws) throws Exception {
        EE ee = EE.getEE();
        List<FLA_Cache_FundDPWeek> r = new ArrayList<>();
        for (int i=0; i < dates.size(); i++) {
            String date = dates.get(i);
            boolean found = false;
            for (int j=0; j < dpws.size(); j++) {
                FLA_Cache_FundDPWeek fdpw = dpws.get(j);
                if (!MM.tgif_isFriday(fdpw.mDateYYMMDD)) {
                    ee.dsevere(log, TAG, "For fund: " + fundName + ", dpw: " + fdpw.mDateYYMMDD + ", is not a friday");
                }
                if (fdpw != null && fdpw.mDateYYMMDD != null && date.trim().equals(fdpw.mDateYYMMDD.trim())) {
                    r.add(fdpw);
                    found = true;
                }
            }
            if (!found) {
                r.add(null);
            }
        }
        return r;
    }

    /**
     *
     */
    private static List<String> generateDPWeekDates(int count, List<FLA_Cache_FundInfo> l) throws Exception {
        List<String> dates = new ArrayList<>();
        String fridayBefore = MM.tgif_getLastFridayTodayIncl(MM.getNowAs_YYMMDD(null));
        dates.add(fridayBefore);
        for (int i=1; i < count; i++) {
            fridayBefore = MM.tgif_getLastFridayTodayExcl(fridayBefore);
            dates.add(fridayBefore);
        }
        return dates;

        /* Retired code
        // Find first dpw entry that is a friday and the calculate back count fridays
        String fridayLast = null;
        for (FLA_Cache_FundInfo cfi: l) {
            List<FLA_Cache_FundDPWeek> dpws = cfi.getDPWeeks();
            if (dpws != null && dpws.size() > 0) {
                FLA_Cache_FundDPWeek dpw = dpws.get(0);
                if (dpw != null && MM.tgif_isFriday(dpw.mDateYYMMDD)) {
                    if (fridayLast != null && fridayLast.compareTo)
                    fridayLast = dpw.mDateYYMMDD;
                    break;
                }
            }
        }
        List<String> dates = new ArrayList<>();
        dates.add(fridayLast);
        for (int i=1; i < count; i++) {
            fridayLast = MM.tgif_getLastFridayTodayExcl(fridayLast);
            dates.add(fridayLast);
        }
        return dates;
        */
    }

    /**
     *
     */
    public static final String WT_FILTER_TYPE = "TYPE";
    public static final String WT_FILTER_INDEX = "INDEX";
    public static final String WT_FILTER_FUND = "FUND";
    public static final String WT_FILTER_PORTFOLIO = "PORTFOLIO";
    public static final String WT_FILTER_PORTFOLIO_MATCH_SEB = "PORTFOLIO_MATCH_SEB";
    public static final String WT_FILTER_REPORT_OPPORTUNITY = "REPORT_OPPORTUNITY";
    public static String fundReport_WeeklyTable(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        EE ee = EE.getEE();

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String paramType = request.getParameter(Constants.PARAM_TYPE);
        String paramId = request.getParameter(Constants.PARAM_ID);
        if (paramType == null || paramType.trim().length() == 0) {
            throw new Exception(Constants.PARAM_TYPE + " not set");
        }
        paramType = paramType.trim();
        if (paramId == null || paramId.trim().length() == 0) {
            throw new Exception(Constants.PARAM_ID + " not set");
        }
        paramId = paramId.trim();

        List<FLA_Cache_FundInfo> list = new ArrayList<>();
        String[] column1 = null;
        List<String> dates = null;
        if (paramType.equals(WT_FILTER_TYPE)) {
            list = FLA_Cache.cacheFundInfosByTypeOrNull(paramId);
            ee.dinfo(log, TAG, "paramId_1: " + paramId + ", size: " + list.size());
            dates = generateDPWeekDates(FLA_Cache_FundInfo.CACHED_DPS, list);
        } else if (paramType.equals(WT_FILTER_INDEX)) {
            list = FLA_Cache.cacheFundInfosByIndex(paramId);
            dates = generateDPWeekDates(FLA_Cache_FundInfo.CACHED_DPS, list);
        } else if (paramType.equals(WT_FILTER_FUND)) {
            list = FLA_Cache.cacheFundInfoById(paramId);
            dates = generateDPWeekDates(FLA_Cache_FundInfo.CACHED_DPS, list);
        } else if (paramType.equals(WT_FILTER_PORTFOLIO)) {
            Long id = Long.parseLong(paramId);
            FLA_FundPortfolio fp = ofy().load().type(FLA_FundPortfolio.class).id(id).now();
            list = FLA_Cache.getCacheVersions(fp.mFunds);
            dates = generateDPWeekDates(FLA_Cache_FundInfo.CACHED_DPS, list);
        } else if (paramType.equals(WT_FILTER_PORTFOLIO_MATCH_SEB)) {
            Long id = Long.parseLong(paramId);
            FLA_FundPortfolio fp = ofy().load().type(FLA_FundPortfolio.class).id(id).now();
            list = FLA_Cache.getCacheVersions(fp.mFunds);
            dates = generateDPWeekDates(FLA_Cache_FundInfo.CACHED_DPS, list);
            list = JSP_Helper_DataSorter.getSEBMatches(dates, list);
        } else if (paramType.equals(WT_FILTER_REPORT_OPPORTUNITY)) {
            list = FLA_Cache.cacheFundInfosByTypeOrNull(paramId);
            ee.dinfo(log, TAG, "paramId_2: " + paramId + ", size: " + list.size());
            dates = JSP_Helper_DataSorter.getDateSequence(0, FLA_Cache_FundInfo.CACHED_DPS); // Number of fridays ago, Count
        }

        StringBuilder s = new StringBuilder();
        s.append("Filter type: " + paramType);
        s.append("&nbsp;");
        s.append("Filter id: " + paramId);

        s.append("<div class=\"selectList3\">");
        s.append("<table>"); // width=\"1200\">");
        if (!paramType.equals(WT_FILTER_REPORT_OPPORTUNITY)) {
            fundReport_WeeklyTable_Headers(s, dates);
            fundReport_WeeklyTable_Rows(s, null, column1, dates, list);
        } else {
            fundReport_WeeklyTable_Headers(s, dates);
            List<OTuple3G<FLA_Cache_FundInfo, List<Double>, String>> rfull = new ArrayList<>();
            List<OTuple3G<FLA_Cache_FundInfo, List<Double>, String>> rfullTmp = new ArrayList<>();
            OTuple2G<String[], List<FLA_Cache_FundInfo>> rarg = null;
            JSP_Helper_DataSorter.getCachedFundInfoWithinDates(
                    dates,
                    list,
                    null,
                    rfull);
            // Best 10 funds by last week
            ee.dinfo(log, TAG, "Target1 for report is: " + rfull.size() + " number of elements");
            rfullTmp = JSP_Helper_DataSorter.dataSort_GetBestLastXWeeks(10, 1, rfull);
            ee.dinfo(log, TAG, "Target2 for report is: " + rfullTmp.size() + " number of elements");
            rarg = JSP_Helper_DataSorter.convertToTableRows(rfullTmp);
            ee.dinfo(log, TAG, "Converting them gave: " + rarg._o2.size());
            ee.dinfo(log, TAG, "And colcount: " + rarg._o1.length);
            fundReport_WeeklyTable_Rows(s, "Top This Week", rarg._o1, dates, rarg._o2);

            // Best 10 funds last 2 weeks
            rfullTmp = JSP_Helper_DataSorter.dataSort_GetBestLastXWeeks(10, 2, rfull);
            rarg = JSP_Helper_DataSorter.convertToTableRows(rfullTmp);
            fundReport_WeeklyTable_Rows(s, "Top Last 2 Weeks", rarg._o1, dates, rarg._o2);

            // Best 10 funds by last 2 weeks position
            rfullTmp = JSP_Helper_DataSorter.dataSort_GetBestScoreLast2Weeks(10, rfull);
            rarg = JSP_Helper_DataSorter.convertToTableRows(rfullTmp);
            fundReport_WeeklyTable_Rows(s, "Top Score Last 2 Weeks", rarg._o1, dates, rarg._o2);
        }

        s.append("</table>");
        s.append("</div>");
        return s.toString();
    }

    /**
     *
     */
    public static int fundReport_WeeklyTable_Headers(
            StringBuilder s,
            List<String> dates) throws Exception {
        int countColumns = 1;
        s.append("<tr>");
        s.append("<th align=\"left\" width=\"400\">Name</th>");
        for (int i = 0; i < dates.size(); i++) {
            s.append("<th align=\"right\">" + dates.get(i).substring(2) + "</th>");
            countColumns++;
        }
        s.append("</tr>");
        return countColumns;
    }

    /**
     *
     */
    public static void fundReport_WeeklyTable_Rows(
            StringBuilder s,
            String titleRow,
            String[] column1,
            List<String> dates,
            List<FLA_Cache_FundInfo> cfis) throws Exception {
        EE ee = EE.getEE();

        if (titleRow != null) {
            s.append("<tr>");
            s.append("<td align=\"left\" colspan=\"2\">");
            s.append("<b>" + titleRow + "</b>");
            s.append("</td>");
            s.append("</tr>");
        }

        for (int i = 0; i < cfis.size(); i++) {
            s.append("<tr>");
            FLA_Cache_FundInfo cfi = cfis.get(i);

            // Type, name and index
            s.append("<td align=\"left\">");
            if (cfi != null) {
                s.append("<a href=\"" + cfi.mURL + "\" target=\"_blank\">" + cfi.mType + ":</a>&nbsp;" + cfi.mName);
                if (cfi.mPPMNumber > 0) {
                    s.append("&nbsp;(" + String.valueOf(cfi.mPPMNumber) + ")");
                }
                String indexName = "-";
                if (cfi.mIndexCompare == null) {
                    ee.dinfo(log, TAG, cfi.mType + "." + cfi.mName + ": Had null as index");
                    s.append("&nbsp;(-)");
                } else {
                    FLA_FundIndex fundIndex = cfi.mIndexCompare.get();
                    indexName = fundIndex.mKey_IndexName;
                    if (indexName == null || indexName.trim().equals("-")) {
                        s.append("&nbsp;(-)");
                    } else {
                        String fundIndexEnc = URLEncoder.encode(indexName, "UTF-8");
                        String url = "JSP_Report02_Weekly_Display.jsp"
                                + "?" + Constants.PARAM_TYPE + "=" + WT_FILTER_INDEX
                                + "&" + Constants.PARAM_ID + "=" + fundIndexEnc;
                        s.append("&nbsp;(" + "<a href=\"" + url + "\" target=\"_blank\">index</a>)");
                    }
                }
            }
            s.append("</td>");

            // Get the weeks
            List<FLA_Cache_FundDPWeek> dpws = null;
            if (cfi != null) {
                dpws = cfi.getDPWeeks();
                dpws = getDPWeeks(cfi.mType + "." + cfi.mName, dates, dpws);
            }

            // Monthlys
            if (dpws != null) {
                int index = 0;
                while (index + 4 <= dpws.size()) {
                    String r1m = "-";
                    if (dpws.get(index) != null && dpws.get(index + 1) != null && dpws.get(index + 2) != null && dpws.get(index + 3) != null) {
                        Double[] ds = new Double[]{dpws.get(index).mR1w, dpws.get(index + 1).mR1w, dpws.get(index + 2).mR1w, dpws.get(index + 3).mR1w};
                        r1m = getR1M(ds);
                    }
                    index += 4;
                    s.append("<td align=\"right\">" + r1m + "</td>");
                    s.append("<td align=\"right\">" + "</td>");
                    s.append("<td align=\"right\">" + "</td>");
                    s.append("<td align=\"right\">" + "</td>");
                }
                s.append("</tr>");
            }

            // Weeklys
            s.append("<tr>");

            // First column, if given
            if (column1 != null
                    && column1.length > 0
                    && column1[i] != null) {
                s.append("<td align=\"left\">");
                // s.append("[34325, 5664, 34252, 3331]");
                s.append(column1[i]);
                s.append("</td>");
            } else {
                s.append("<td></td>"); // This is a new row so name cell is empty
            }

            // Then the weekly columns
            if (dpws != null) {
                for (int j = 0; j < dpws.size(); j++) {
                    FLA_Cache_FundDPWeek cfw = dpws.get(j);
                    if (cfw != null && cfw.mR1w != null) {
                        s.append("<td align=\"right\">" + cfw.mR1w + "</td>");
                    } else if (j == 0) {
                        s.append("<td align=\"right\">?</td>");
                    } else {
                        s.append("<td align=\"right\">-</td>");
                    }
                }
            }
            s.append("</tr>");

            // Create an empty row between each fund for readability
            s.append("<tr><td>&nbsp;</td></tr>");
        }
    }

    public static String getR1M(Double[] ds) {
        if (ds[0] == null || ds[1] == null || ds[2] == null || ds[3] == null) {
            return "-";
        }

        double r = 1.0D;
        for (int i=ds.length-1; i >= 0; i--) {
            Double d = ds[i];
            if (d.doubleValue() > 0.0D) {
                r *= (1.0D + d.doubleValue() / 100.0D);
            } else if (d.doubleValue() < 0.0D) {
                r *= (1.0D + d.doubleValue() / 100.0D);
            }
        }

        double delta = r - 1.0D;
        double newr = delta / 1.0D;
        newr *= 100;
        return String.format("%.1f", newr);
    }

}


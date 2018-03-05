package com.pf.fl.be.jsphelper;

import com.pf.fl.be.extract.GCSWrapper;
import com.pf.shared.Constants;
import com.pf.shared.datamodel.DB_FundInfo;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.MM;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JSP_Helper {
    private static final Logger log = Logger.getLogger(JSP_Helper.class.getName());
    private static final String TAG = MM.getClassName(JSP_Helper.class.getName());

    public static boolean _isInitialized;
    public static void initialize() throws IOException {
        log.info("JSP_Helper.initialize");
        if (_isInitialized) {
            log.info("...we are already initialized, returning");
            return;
        }
        byte[] data = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        log.info("...reading data: " + data.length + " bytes");
        DB_FundInfo._isInitialized = false;
        DB_FundInfo.initialize(data);
        _isInitialized = true;
    }

    public static void writeFundInfo(byte[] data) throws IOException {
        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
    }

    public static String fundsDisplayAll() {
        List<D_FundInfo> fisSEB = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_SEB);
        List<D_FundInfo> fisSPP = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_SPP);
        List<D_FundInfo> fisVGD = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_VANGUARD);
        List<D_FundInfo> fisPPM = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_PPM);

        StringBuffer sb = new StringBuffer();
        sb.append("<h2>Total SEB: " + fisSEB.size() + "</h2><br>");
        sb.append("<h2>Total SPP: " + fisSPP.size() + "</h2><br>");
        sb.append("<h2>Total VGD: " + fisVGD.size() + "</h2><br>");
        sb.append("<h2>Total PPM: " + fisPPM.size() + "</h2><br>");

        sb.append("<h2>Fund list for SEB</h2><br>");
        printFundType(sb, fisSEB);
        sb.append("<h2>Fund list for SPP</h2><br>");
        printFundType(sb, fisSPP);
        sb.append("<h2>Fund list for VGD</h2><br>");
        printFundType(sb, fisVGD);
        sb.append("<h2>Fund list for PPM</h2><br>");
        printFundType(sb, fisPPM);
        return sb.toString();
    }
    private static void printFundType(StringBuffer sb, List<D_FundInfo> l) {
        for (D_FundInfo fi: l) {
            sb.append(fi._nameMS + ", " + fi._nameOrig + ", " + fi._isUpdated + ", " + fi._url + "<br>");
        }
    }

    private static List<D_FundDPDay> getDPWeeks(
            String fundName,
            List<String> dates,
            List<D_FundDPDay> dpws) throws Exception {
        
        List<D_FundDPDay> r = new ArrayList<>();
        for (int i=0; i < dates.size(); i++) {
            String date = dates.get(i);
            boolean found = false;
            for (int j=0; j < dpws.size(); j++) {
                D_FundDPDay fdpw = dpws.get(j);
                if (!MM.tgif_isFriday(fdpw._dateYYMMDD)) {
                    log.severe("For fund: " + fundName + ", dpw: " + fdpw._dateYYMMDD + ", is not a friday");
                } else if (date.equals(fdpw._dateYYMMDD)) {
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

    private static List<String> generateDPWeekDates(int count, List<D_FundInfo> l) throws Exception {
        List<String> dates = new ArrayList<>();
        String fridayBefore = MM.tgif_getLastFridayTodayIncl(MM.getNowAs_YYMMDD(null));
        dates.add(fridayBefore);
        for (int i=1; i < count; i++) {
            fridayBefore = MM.tgif_getLastFridayTodayExcl(fridayBefore);
            dates.add(fridayBefore);
        }
        return dates;
    }

    public static final String WT_FILTER_TYPE = "TYPE";
    public static final String WT_FILTER_INDEX = "INDEX";
    public static final String WT_FILTER_FUND = "FUND";
    public static String fundReport_WeeklyTable(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String paramType = request.getParameter(JSP_Constants.PARAM_TYPE);
        String paramId = request.getParameter(JSP_Constants.PARAM_ID);
        if (paramType == null || paramType.trim().length() == 0) {
            throw new Exception(JSP_Constants.PARAM_TYPE + " not set");
        }
        paramType = paramType.trim();
        if (paramId == null || paramId.trim().length() == 0) {
            throw new Exception(JSP_Constants.PARAM_ID + " not set");
        }
        paramId = paramId.trim();

        List<D_FundInfo> list = new ArrayList<>();
        String[] column1 = null;
        List<String> dates = null;
        if (paramType.equals(WT_FILTER_TYPE)) {
            log.info("Getting funds of type: " + paramId);
            list = DB_FundInfo.getFundInfosByType(paramId);
            log.info("Number of funds: " + list.size());
            dates = generateDPWeekDates(JSP_Constants.CACHED_DPS, list);
        } else if (paramType.equals(WT_FILTER_INDEX)) {
            list = DB_FundInfo.getFundInfosByIndex(paramId);
            dates = generateDPWeekDates(JSP_Constants.CACHED_DPS, list);
        } else if (paramType.equals(WT_FILTER_FUND)) {
            throw new IOException("Not Converted to new code");
//            list = FLA_Cache.cacheFundInfoById(paramId);
//            dates = generateDPWeekDates(FLA_Cache_FundInfo.CACHED_DPS, list);
        }

        StringBuilder s = new StringBuilder();
        s.append("Filter type: " + paramType);
        s.append("&nbsp;");
        s.append("Filter id: " + paramId);

        s.append("<div class=\"selectList3\">");
        s.append("<table>"); // width=\"1200\">");

        fundReport_WeeklyTable_Headers(s, dates);

        log.info("Date count: " + dates.size());
//        List<OTuple2G<D_FundInfo, List<Double>>> rfull =
//                JSP_Helper_DataSorter.getCachedFundInfoWithinDates(
//                        dates,
//                        list);
//        log.info("Full data set: " + rfull.size());
//        rarg = JSP_Helper_DataSorter.convertToTableRows(rfull);

        fundReport_WeeklyTable_Rows(s, "All funds",null, dates, list);

//        // Best 10 funds by last week
//        rfullTmp = JSP_Helper_DataSorter.dataSort_GetBestLastXWeeks(10, 1, rfull);
//        rarg = JSP_Helper_DataSorter.convertToTableRows(rfullTmp);
//        fundReport_WeeklyTable_Rows(s, "Top This Week", rarg._o1, dates, rarg._o2);
//
//        // Best 10 funds last 2 weeks
//        rfullTmp = JSP_Helper_DataSorter.dataSort_GetBestLastXWeeks(10, 2, rfull);
//        rarg = JSP_Helper_DataSorter.convertToTableRows(rfullTmp);
//        fundReport_WeeklyTable_Rows(s, "Top Last 2 Weeks", rarg._o1, dates, rarg._o2);
//
//        // Best 10 funds by last 2 weeks position
//        rfullTmp = JSP_Helper_DataSorter.dataSort_GetBestScoreLast2Weeks(10, rfull);
//        rarg = JSP_Helper_DataSorter.convertToTableRows(rfullTmp);
//        fundReport_WeeklyTable_Rows(s, "Top Score Last 2 Weeks", rarg._o1, dates, rarg._o2);

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
            List<D_FundInfo> cfis) throws Exception {
        if (titleRow != null) {
            s.append("<tr>");
            s.append("<td align=\"left\" colspan=\"2\">");
            s.append("<b>" + titleRow + "</b>");
            s.append("</td>");
            s.append("</tr>");
        }

        log.info("fundReport_WeeklyTable_Rows, dates: " + dates.size() + ", fis: " + cfis.size());
        for (int i = 0; i < cfis.size(); i++) {
            s.append("<tr>");
            D_FundInfo cfi = cfis.get(i);

            // Type, name and index
            s.append("<td align=\"left\">");
            s.append("<a href=\"" + cfi._url + "\" target=\"_blank\">" + cfi._type + ":</a>&nbsp;" + cfi._nameMS);
            if (cfi._ppmNumber != null && cfi._ppmNumber.trim().length() > 0) {
                s.append("&nbsp;(" + String.valueOf(cfi._ppmNumber) + ")");
            }
            String indexName = "-";
            if (cfi._indexName == null) {
                log.info(cfi._type + "." + cfi._nameMS + ": Had null as index");
                s.append("&nbsp;(-)");
            } else {
                indexName = cfi._indexName;
                if (indexName == null || indexName.trim().equals("-")) {
                    s.append("&nbsp;(-)");
                } else {
                    String fundIndexEnc = URLEncoder.encode(indexName, "UTF-8");
                    String url = "JSP_Report02_Weekly_Display.jsp"
                            + "?" + JSP_Constants.PARAM_TYPE + "=" + WT_FILTER_INDEX
                            + "&" + JSP_Constants.PARAM_ID + "=" + fundIndexEnc;
                    s.append("&nbsp;(" + "<a href=\"" + url + "\" target=\"_blank\">index</a>)");
                }
            }
            s.append("</td>");

            // Get the weeks
            List<D_FundDPDay> dpws = null;
            if (cfi != null) {
                dpws = cfi._dpDays;
                dpws = getDPWeeks(cfi._type + "." + cfi._nameMS, dates, dpws);
            }

            // Monthlys
            if (dpws != null) {
                int index = 0;
                while (index + 4 <= dpws.size()) {
                    String r1m = "-";
                    if (dpws.get(index) != null && dpws.get(index + 1) != null && dpws.get(index + 2) != null && dpws.get(index + 3) != null) {
                        Double[] ds = {null, null, null, null};
                        if (dpws.get(index)._r1w != D_FundDPDay.FLOAT_NULL) {
                            ds[0] = new Double(dpws.get(index)._r1w);
                        }
                        if (dpws.get(index+1)._r1w != D_FundDPDay.FLOAT_NULL) {
                            ds[1] = new Double(dpws.get(index+1)._r1w);
                        }
                        if (dpws.get(index+2)._r1w != D_FundDPDay.FLOAT_NULL) {
                            ds[2] = new Double(dpws.get(index+2)._r1w);
                        }
                        if (dpws.get(index+3)._r1w != D_FundDPDay.FLOAT_NULL) {
                            ds[3] = new Double(dpws.get(index+3)._r1w);
                        }
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


            // Then the weekly columns
            s.append("<td align=\"right\"></td>");  // The magic 1st column which is not used anymore
            if (dpws != null) {
                for (int j = 0; j < dpws.size(); j++) {
                    D_FundDPDay cfw = dpws.get(j);
                    if (cfw != null && cfw._r1w != D_FundDPDay.FLOAT_NULL) {
                        s.append("<td align=\"right\">" + cfw._r1w + "</td>");
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


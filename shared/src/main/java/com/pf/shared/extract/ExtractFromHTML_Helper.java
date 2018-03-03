package com.pf.shared.extract;

import com.pf.shared.Constants;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.HtmlRetriever;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Logger;

public class ExtractFromHTML_Helper {
    private static final Logger log = Logger.getLogger(ExtractFromHTML_Helper.class.getName());
    private static final String TAG = "";

    public static final int RC_SUCCESS = 0;
    public static final int RC_SUCCESS_BUT_DATA_WAS_UPDATED = 1;
    public static final int RC_ERROR_REMOVE_FUND = 3;
    public static final int RC_ERROR_KEEP_FUND = 4;
    public static final int RC_WARNING_NO_DPDAY_FOUND = 5;

    private IndentWriter _iw = new IndentWriter();
    private IndentWriter _iwdb = new IndentWriter();

    public String _htmlPageContent = "";

    public String _dateNow_YYMMDD;
    public String _dateLastFriday_YYMMDD;

    public static void extractFund() {
        try {
            D_FundInfo fi = new D_FundInfo();

//            fi._type = "VGD";
//            fi._nameMS = "Vanguard High Dividend Yield Index Fund ETF Shares (VYM)";
//            fi._url = "http://performance.morningstar.com/perform/Performance/etf/trailing-total-returns.action?&t=VYM&region=usa&culture=en-US&cur=&ops=clear&s=0P00001MJB&ndec=2&ep=true&align=d&annlz=true&comparisonRemove=false&loccat=&taxadj=&benchmarkSecId=&benchmarktype=";

//            fi._type = "VGD";
//            fi._nameMS = "Royce Special Equity Fund Service Class (RSEFX)";
//            fi._url = "https://performance.morningstar.com/perform/Performance/fund/trailing-total-returns.action?&t=XNAS:RSEFX&region=usa&culture=en-US&cur=&ops=clear&s=0P00001MJB&ndec=2&ep=true&align=d&annlz=true&comparisonRemove=false&loccat=&taxadj=&benchmarkSecId=&benchmarktype=";

            fi._url = "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000PRDQ&programid=0000000000";

            ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
            log.warning("**** Will now extract fund: " + fi._url);
            OTuple2G<Integer, String> rc = eh.extractFundDetails(fi);

            if (rc._o1 == ExtractFromHTML_Helper.RC_SUCCESS) {
                log.warning("*** Success extracting fund");
            } else {
                log.info("*** Error");
            }
            log.warning("*** Returned message: " + rc._o2);
            log.warning("*** Fund information: " + fi.toString());
        } catch (Exception exc) {
            log.severe("Encountered exception while extracting fund");
            log.severe(exc.toString());
            log.severe(MM.getStackTraceString(exc));
        }
    }

    public OTuple2G<Integer, String> extractFundDetails(D_FundInfo fi) throws IOException {
        int rv = RC_SUCCESS;

        try {
            rv = extractFundDetailsImpl(fi);
        } catch(Exception exc) {
            StringBuffer strb = new StringBuffer();
            strb.append(fi.getTypeAndName() + "\n");
            strb.append("*** EXCEPTION CAUGHT WHEN extractFund\n");
            strb.append("...exception: " + exc.getMessage() + "\n");
            strb.append(MM.getStackTraceString(exc) + "\n");
            return new OTuple2G<>(RC_ERROR_KEEP_FUND, strb.toString());
        }

        if (rv != RC_SUCCESS) {
            IndentWriter iwr = new IndentWriter();
            iwr.setIndentChar('.');
            iwr.println("Error for: " + fi.getTypeAndName() + ", ec: " + rv);
            iwr.push();
            iwr.println(_iw.getString());
            iwr.pop();
            iwr.println("Debug information");
            iwr.push();
            iwr.println(_iwdb.getString());
            iwr.pop();
            return new OTuple2G<>(rv, iwr.getString());
        }

        return new OTuple2G<>(RC_SUCCESS, null);
    }

    private int extractFundDetailsImpl(D_FundInfo fi) throws Exception {

        _dateNow_YYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        _dateLastFriday_YYMMDD = MM.tgif_getLastFridayTodayExcl(_dateNow_YYMMDD);
        fi._dateYYMMDD_Update_Attempted = _dateNow_YYMMDD;

        // Get the raw HTML data
        IndentWriter iw_html_debug = new IndentWriter();
        byte[] htmlDataRaw = HtmlRetriever.htmlGet(
                iw_html_debug,
                fi._url,
                5000,
                6);

        boolean error = false;
        if (htmlDataRaw == null || htmlDataRaw.length == 0) {
            fi._errorCode = D_FundInfo.IC_COM_NO_URL_DATA;
            error = true;
        }
        String htmlDataString = MM.newString(htmlDataRaw, Constants.ENCODING_FILE_READ);
        if (htmlDataString == null || htmlDataString.length() == 0) {
            fi._errorCode = D_FundInfo.IC_COM_NO_DECODABLE_DATA;
            error = true;
        }
        if (error) {
            _iw.println("No URL data for Fund, html debug information:\n" + iw_html_debug.getString());
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        }

        _htmlPageContent = MM.stripHTMLComments(htmlDataString);

        int errorCode = ExtractFromHTML_Helper.RC_SUCCESS;
        if (fi._type.equals(D_FundInfo.TYPE_VANGUARD)) {
            errorCode = ExtractFromHTML_Vanguard.extractFundDetails(_iw, fi, _htmlPageContent);
        } else {
            errorCode = ExtractFromHTML_Morningstar.extractFundDetails(_iw, _iwdb, fi, _htmlPageContent);
        }
        if (errorCode != ExtractFromHTML_Helper.RC_SUCCESS) {
            return errorCode;
        }

        D_FundDPDay dpd = fi._dpDays.remove(0);  // Lets look at the things just added

        // A current DP was found
        if (dpd._dateYYMMDD == null) {
            _iw.println("Lastest DPDay had null for _dateYYMMDD");
            fi._errorCode = D_FundInfo.IC_HTML_MS_DPDAY_NULLDATE;
            return RC_WARNING_NO_DPDAY_FOUND;
        }
        if (dpd._dateYYMMDD_Actual == null) {
            _iw.println("Lastest DPDay had null for _dateYYMMDD_Actual");
            fi._errorCode = D_FundInfo.IC_HTML_MS_DPDAY_NULLDATE;
            return RC_WARNING_NO_DPDAY_FOUND;
        }

        // Weekly was null
        if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
            _iw.println("No update. r1w was null for good _dateYYMMDD: " + dpd._dateYYMMDD
                    + ", _dateYYMMDD_Actual: " + dpd._dateYYMMDD_Actual
                    + ", for friday: " + _dateLastFriday_YYMMDD);
            return RC_WARNING_NO_DPDAY_FOUND;
        }

        String dateNow_YYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        String dateLastFriday_YYMMDD   = MM.tgif_getLastFridayTodayExcl(dateNow_YYMMDD);
        String dateLastSaturday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.SATURDAY);
        String dateLastSunday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.SUNDAY);
        String dateLastMonday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.MONDAY);
        String dateLastTuesday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.TUESDAY);
        String dateLastThursday_YYMMDD = MM.tgif_getPrevWeekday(dateLastFriday_YYMMDD, Calendar.THURSDAY);

        // We have a valid extraction if actual date is last Friday or Thursday, or Mon/Tue week after
        if (dpd._dateYYMMDD_Actual.equals(dateLastFriday_YYMMDD)
                || dpd._dateYYMMDD_Actual.equals(dateLastThursday_YYMMDD)
                || dpd._dateYYMMDD_Actual.equals(dateLastSaturday_YYMMDD)
                || dpd._dateYYMMDD_Actual.equals(dateLastSunday_YYMMDD)
                || dpd._dateYYMMDD_Actual.equals(dateLastMonday_YYMMDD)
                || dpd._dateYYMMDD_Actual.equals(dateLastTuesday_YYMMDD)) {
            dpd._dateYYMMDD = dateLastFriday_YYMMDD;
            fi._dpDays.add(0, dpd);
        }
        // Otherwise, we should wait until next data point
        else {
            _iw.println("Want to extract for friday: " + dateLastFriday_YYMMDD);
            _iw.println("But found DP: " + dpd._dateYYMMDD_Actual);
            _iw.println("Which is not in expected range: " + dateLastThursday_YYMMDD + "-" + dateLastTuesday_YYMMDD);

            int daydiff = MM.tgif_dayCountDiff(dateLastFriday_YYMMDD, dpd._dateYYMMDD_Actual);
            if (daydiff > 14) {
                _iw.println("*** Setting fund invalid, number of days since last DPDay: " + daydiff);
                fi._errorCode = D_FundInfo.IC_NO_RECENT_DPDAY;
                fi._isValid = false;
                return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
            }

            return ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND;
        }

        fi._dateYYMMDD_Updated = _dateLastFriday_YYMMDD;
        return ExtractFromHTML_Helper.RC_SUCCESS;
    }

    public static OTuple2G<Boolean, Float> validFloat(String s) {
        if (s == null || s.trim().length() == 0) {
            return new OTuple2G<>(true, D_FundDPDay.FLOAT_NULL);
        }
        s = s.trim();
        if (s.equals("-")) {
            return new OTuple2G<>(true, D_FundDPDay.FLOAT_NULL);
        }
        if (s.equals("&mdash;")) {
            return new OTuple2G<>(true, D_FundDPDay.FLOAT_NULL);
        }
        s = s.replace(',', '.');
        try {
            float d = Float.parseFloat(s);
            return new OTuple2G<>(true, new Float(d));
        } catch(Exception exc) {
            return new OTuple2G<>(false, null);
        }
    }

}

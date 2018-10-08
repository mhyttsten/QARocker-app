package com.pf.shared.extract;

import com.pf.shared.Constants;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExtractStatistics {
    private static final Logger log = Logger.getLogger(ExtractStatistics.class.getName());

    public List<D_FundInfo> _fiAllFunds = new ArrayList<>();
    public int _fiExtractable = 0;

    public List<D_FundInfo> _fiToExtract = new ArrayList<>();
    public List<D_FundInfo> _fiExtractAttemptedToday = new ArrayList<>();

    // Mutually exclusive
    public List<D_FundInfo> _fiInvalids = new ArrayList<>();
    public List<D_FundInfo> _fiErrors = new ArrayList<>();

    public List<D_FundInfo> _fiExtracted = new ArrayList<>();
    public List<D_FundInfo> _fiExtracted_Thu = new ArrayList<>();
    public List<D_FundInfo> _fiExtracted_Fri = new ArrayList<>();
    public List<D_FundInfo> _fiExtracted_Mon = new ArrayList<>();
    public List<D_FundInfo> _fiExtracted_Tue = new ArrayList<>();
    // These are the non-extracted stats for all valid and non-errored funds
    public List<D_FundInfo> _fiNotExtracted_1F = new ArrayList<>();
    public List<D_FundInfo> _fiLastExtracted_2F = new ArrayList<>();
    public List<D_FundInfo> _fiLastExtracted_3F = new ArrayList<>();
    public List<D_FundInfo> _fiLastExtracted_GT3F = new ArrayList<>();

    public String _nowYYMMDD;
    public String _nowYYMMDD_HHMMSS;
    public String _fridayLastYYMMDD;
    public String _fridayLast2YYMMDD;
    public String _fridayLast3YYMMDD;

    //----------------------------------------------------------------------
    public ExtractStatistics(List<D_FundInfo> fiAllFunds) {
        _nowYYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        _nowYYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM);
        String nowYYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM);
        _fridayLastYYMMDD = MM.tgif_getLastFridayTodayExcl(_nowYYMMDD);
        _fridayLast2YYMMDD = MM.tgif_getLastFridayTodayExcl(_fridayLastYYMMDD);
        _fridayLast3YYMMDD = MM.tgif_getLastFridayTodayExcl(_fridayLast2YYMMDD);
        _fiAllFunds = fiAllFunds;
    }

    //----------------------------------------------------------------------
    private void reset() {
        _fiExtractable = 0;

        _fiToExtract = new ArrayList<>();
        _fiExtractAttemptedToday = new ArrayList<>();

        // Mutually exclusive
        _fiInvalids = new ArrayList<>();
        _fiErrors = new ArrayList<>();

        _fiExtracted = new ArrayList<>();
        _fiExtracted_Thu = new ArrayList<>();
        _fiExtracted_Fri = new ArrayList<>();
        _fiExtracted_Mon = new ArrayList<>();
        _fiExtracted_Tue = new ArrayList<>();
        // These are the non-extracted stats for all valid and non-errored funds
        _fiNotExtracted_1F = new ArrayList<>();
        _fiLastExtracted_2F = new ArrayList<>();
        _fiLastExtracted_3F = new ArrayList<>();
        _fiLastExtracted_GT3F = new ArrayList<>();
    }

    //----------------------------------------------------------------------
    public String extractStats() {
        reset();

        IndentWriter iw = new IndentWriter();
        iw.println("Entered ExtractStatistics.extractStats<br>");

        iw.println("Fund count: " + _fiAllFunds.size() + "<br>");
        for (int i=0; i < _fiAllFunds.size(); i++) {
            D_FundInfo fi = _fiAllFunds.get(i);

            if (i == 0) iw.println("Doing fund: " + fi.getTypeAndName());

            D_FundDPDay dpdAlreadyThere = null;
            if (fi._dpDays.size() > 0) {
                D_FundDPDay dpd = fi._dpDays.get(0);
                if (dpd._dateYYMMDD.equals(_fridayLastYYMMDD) && dpd._r1w != D_FundDPDay.FLOAT_NULL) {
                        dpdAlreadyThere = dpd;
                    if (i == 0) iw.println("Found dpd: " + dpd.toString());
                }
            }

            if (fi._isValid) {
                if (i == 0) iw.println("Extractable");
                _fiExtractable++;
            }
            if (fi._isValid && fi._errorCode != D_FundInfo.IC_NO_ERROR) {
                if (i == 0) iw.println("Errors");
                _fiErrors.add(fi);
            }

            // Obs, error funds should still be attempted to be extracted
            if (!fi._isValid) {
                if (i == 0) iw.println("Invalid");
                _fiInvalids.add(fi);
            }
            else if (dpdAlreadyThere != null) {
                _fiExtracted.add(fi);
                if (i == 0) iw.println("Extracted");
                String thu = MM.tgif_getPrevWeekday(_fridayLastYYMMDD, Calendar.THURSDAY);

                if (dpdAlreadyThere._dateYYMMDD_Actual.equals(thu)) {
                    _fiExtracted_Thu.add(fi);
                }
                if (dpdAlreadyThere._dateYYMMDD_Actual.equals(_fridayLastYYMMDD)) {
                    _fiExtracted_Fri.add(fi);
                }
                String mon = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.MONDAY);
                if (dpdAlreadyThere._dateYYMMDD_Actual.equals(mon)) {
                    _fiExtracted_Mon.add(fi);
                }
                String tue = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.TUESDAY);
                if (dpdAlreadyThere._dateYYMMDD_Actual.equals(tue)) {
                    _fiExtracted_Tue.add(fi);
                }
            }
            else if (fi._dateYYMMDD_Update_Attempted.compareTo(_nowYYMMDD) < 0) {
                if (i == 0) iw.println("To Extract");
                _fiToExtract.add(fi);
                _fiNotExtracted_1F.add(fi);
            }
            else if (fi._dateYYMMDD_Update_Attempted.compareTo(_nowYYMMDD) == 0) {
                if (i == 0) iw.println("Already Attempted Today");
                _fiExtractAttemptedToday.add(fi);
                _fiNotExtracted_1F.add(fi);
            }
            else {
                throw new AssertionError("Weird state for fund, "
                        + "\nnow: " + _nowYYMMDD
                        + "\nfridayLast: " + _fridayLastYYMMDD
                        + "\nfi.updated: " + fi._dateYYMMDD_Updated
                        + "\nfi.updateAttempted: " + fi._dateYYMMDD_Update_Attempted
                        + "\n" + fi.toString());
            }
        }

        for (D_FundInfo fi: _fiNotExtracted_1F) {
            for(D_FundDPDay dpd: fi._dpDays) {
                if (dpd._r1w != D_FundDPDay.FLOAT_NULL) {
                    if (dpd._dateYYMMDD.equals(_fridayLastYYMMDD)) {
                        throw new AssertionError("We should not have last friday in this list");
                    }
                    else if (dpd._dateYYMMDD.equals(_fridayLast2YYMMDD)) {
                        _fiLastExtracted_2F.add(fi);
                    }
                    else if (dpd._dateYYMMDD.equals(_fridayLast3YYMMDD)) {
                        _fiLastExtracted_3F.add(fi);
                    }
                    else if (dpd._dateYYMMDD.compareTo(_fridayLast3YYMMDD) < 0) {
                        _fiLastExtracted_GT3F.add(fi);
                    }
                    break;
                }
            }
        }
        iw.println("Exit ExtractStatistics.extractStats<br>");
        return iw.getString();
    }

    //--------------------------------------------------------------------------------------------
    public static String getExtractSummary(
            IndentWriter iw,
            boolean includeFullLists,
            String extractStart_YYMMDD_HHMMSS,
            List<D_FundInfo> fiNow) {

//        log.info("ExtractStatistics, entries in: " + fiNow.size());
//        log.info("...includeFullLists: " + includeFullLists);
        ExtractStatistics es_now = new ExtractStatistics(fiNow);
        es_now.extractStats();

        if (extractStart_YYMMDD_HHMMSS == null) {
            extractStart_YYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM);
        }

        iw.println("Report time: " + extractStart_YYMMDD_HHMMSS);
        iw.println("Now: " + MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM));
        iw.println("Current Friday: " + es_now._fridayLastYYMMDD);
        iw.println("Funds in DB");
        iw.println("...Total: " + es_now._fiAllFunds.size());
        iw.println("...Invalid: " + es_now._fiInvalids.size());
        iw.println("...Error: " + es_now._fiErrors.size() + (es_now._fiErrors.size() > 0 ? ", ***UNEXPECTED***": ""));
        iw.println("Extracted: " + es_now._fiExtracted.size() + ", out of: " + es_now._fiExtractable);
        iw.println("...Thursday: " + es_now._fiExtracted_Thu.size());
        iw.println("...Friday:   " + es_now._fiExtracted_Fri.size());
        iw.println("...Monday:   " + es_now._fiExtracted_Mon.size());
        iw.println("...Tuesday:  " + es_now._fiExtracted_Tue.size());
        iw.println("Not Extracted (1F): " + es_now._fiNotExtracted_1F.size());
        iw.println("...Extracted 2F:  " + es_now._fiLastExtracted_2F.size());
        iw.println("...Extracted 3F:  " + es_now._fiLastExtracted_3F.size());
        iw.println("...Extracted >3F: " + es_now._fiLastExtracted_GT3F.size());
        iw.println("...Attempted today: " + es_now._fiExtractAttemptedToday.size());

        iw.println();
        printListStr(iw, "Invalids", es_now._fiInvalids, 10000);

        iw.println();
        if (es_now._fiErrors.size() > 0) {
            iw.println("*** ERRORS EXISTED UNEXPECTEDLY ***");
        }
        printListStr(iw, "Errors", es_now._fiErrors, 10000);

        int limit = 4;
        if (includeFullLists) {
            limit = 10000;
        }

        iw.println();
        iw.println("...Thursday: " + es_now._fiExtracted_Thu.size());
        printListStr(iw, "Thursday", es_now._fiExtracted_Thu, limit);
        iw.println();
        printListStr(iw, "Not Extracted (1F)", es_now._fiNotExtracted_1F, limit);
        iw.println();
        printListStr(iw, "But extracted 2F", es_now._fiLastExtracted_2F, limit);
        iw.println();
        printListStr(iw, "But extracted 3F", es_now._fiLastExtracted_3F, limit);
        iw.println();
        printListStr(iw, "But extracted >3F", es_now._fiLastExtracted_GT3F, limit);

        iw.println();
        return "";
    }

    private static String printListStr(IndentWriter iw, String header, List<D_FundInfo> l, int limit) {
        if (iw == null) {
            iw = new IndentWriter();
        }
        iw.setIndentChar('.');
        iw.println(header + ". Count: " + l.size() + ".");
        for (int i = 0; i < l.size(); i++) {
            if (i >= limit) {
                break;
            }
            D_FundInfo fi = l.get(i);
            iw.print("[" + i + "]: "
                    + fi._type + "." + fi._nameMS
                    + ", updated: " + fi._dateYYMMDD_Updated
                    + ", attempted: " + fi._dateYYMMDD_Update_Attempted);
            if (fi._errorCode != D_FundInfo.IC_NO_ERROR) {
                iw.println(", ec: " + fi._errorCode + " (" + fi.error2str() + ")");
            } else {
                iw.println(", ec: SUCCESS");
            }

            iw.push();
            try {
                iw.println(URLEncoder.encode(fi._type + "." + fi._nameMS, Constants.ENCODING_FILE_WRITE));
            } catch(UnsupportedEncodingException exc) {
                throw new AssertionError("Unsupported encoding: " + exc.toString());
            }
            iw.println(fi._url);
            iw.pop();
        }
        return iw.getString();
    }
}


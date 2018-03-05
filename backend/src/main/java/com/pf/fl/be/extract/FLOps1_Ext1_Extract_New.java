package com.pf.fl.be.extract;

import com.google.cloud.storage.Blob;
import com.pf.shared.Constants;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo_Serializer;
import com.pf.shared.extract.ExtractFromHTML_Helper;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

public class FLOps1_Ext1_Extract_New {
    private static final Logger log = Logger.getLogger(FLOps1_Ext1_Extract_New.class.getName());
    private static final String TAG = MM.getClassName(FLOps1_Ext1_Extract_New.class.getName());

    private List<String> mIWDebugs = new ArrayList<>();
    private boolean mIgnoreSchedule;
    private String _nowYYMMDD;
    private String _fridayLastYYMMDD;
    private String _fridayLast2YYMMDD;

    public FLOps1_Ext1_Extract_New(boolean ignoreSchedule, IndentWriter iwErrors) {
        mIgnoreSchedule = ignoreSchedule;
    }

    public void doIt() throws IOException {
        MM.timeStart();

        // Set time variables
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_STOCKHOLM));
        int nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int nowHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        _nowYYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        String nowYYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM);
        _fridayLastYYMMDD  = MM.tgif_getLastFridayTodayExcl(_nowYYMMDD);
        _fridayLast2YYMMDD = MM.tgif_getLastFridayTodayExcl(_fridayLastYYMMDD);
        String sunYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.SUNDAY);
        String monYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.MONDAY);
        String tueYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.TUESDAY);
        String wedYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.WEDNESDAY);

        // Same algorithm for collection and recollecting
        //if ((nowDayOfWeek == Calendar.SATURDAY && nowHourOfDay > 5)

        // Check if it is time to extract
        if (!mIgnoreSchedule) {
            boolean scheduledPlay =
                    (nowDayOfWeek == Calendar.SATURDAY && nowHourOfDay >= 5)
                            || (_nowYYMMDD.equals(sunYYMMDD) && nowHourOfDay >= 5)
                            || (_nowYYMMDD.equals(monYYMMDD) && nowHourOfDay >= 5)
                            || (_nowYYMMDD.equals(tueYYMMDD) && nowHourOfDay >= 5)
                            || (_nowYYMMDD.equals(wedYYMMDD) && nowHourOfDay >= 5);
            if (!scheduledPlay) {
                log.info("Not time for extraction now: " + _nowYYMMDD + ", " + MM.tgif_getDayOfWeekStr(nowDayOfWeek) + "@" + nowHourOfDay + ", lastFriday: " + _fridayLastYYMMDD);
                return;
            }
        }

        // *** 1: It is time to extract
        log.info("Now: " + nowYYMMDD_HHMMSS + ", " + MM.tgif_getDayOfWeekStr(nowDayOfWeek) + "@" + nowHourOfDay + ", lastFriday: " + _fridayLastYYMMDD);

        IndentWriter iw_header = new IndentWriter();
        iw_header.println("\n*********************************************************************");
        iw_header.println("Extracting now: " + nowYYMMDD_HHMMSS + " (Stockholm time)");
        iw_header.println("Last Friday is: " + _fridayLastYYMMDD);

        // Get all Funds and Classify them
        byte[] data = GCSWrapper.gcsReadFile(Constants.PORTFOLIO_DB_MASTER_BIN);
        List<D_FundInfo> fiAllFunds = D_FundInfo_Serializer.decrunchFundList(data);
        printExtractStats(iw_header, fiAllFunds);
        List<D_FundInfo> fiToExtract = _fiToExtract;

        log.info(iw_header.getString());

        // Return if there is no work
        if (fiToExtract.size() == 0) {
            log.info("No entries to extract, extractList was empty: " + _nowYYMMDD + ", " + MM.tgif_getDayOfWeekStr(nowDayOfWeek) + "@" + nowHourOfDay + ", lastFriday: " + _fridayLastYYMMDD);
            iw_header.println("No entries to extract, extractList was empty");
            return;
        }
        iw_header.println();

        IndentWriter iw_fund_infos = new IndentWriter();
        iw_fund_infos.push();
        IndentWriter iw_fund_error_shorts = new IndentWriter();
        iw_fund_error_shorts.push();
        IndentWriter iw_fund_error_details = new IndentWriter();
        iw_fund_error_details.push();
        int countTotal = 0;
        List<D_FundInfo> fiRemoved = new ArrayList<>();
        while (fiToExtract.size() > 0) {
            D_FundInfo fi = fiToExtract.remove(0);

//            mEE.dinfo(log, TAG, "----------------------------------\nInformation before processing fund\n" + fi.toString());

            int ic_before = fi._errorCode;

            countTotal++;

            ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
            OTuple2G<Integer, String> rc = eh.extractFundDetails(fi);
            if (rc._o1 == ExtractFromHTML_Helper.RC_SUCCESS) {
                String s = rc._o2;
                if (s != null && s.trim().length() > 0) {
                    iw_fund_infos.println("- INFO: " + fi.getTypeAndName() + ", " + fi._url);
                    iw_fund_infos.push();
                    iw_fund_infos.println(s);
                    iw_fund_infos.pop();
                }

            } else {
                IndentWriter iwtmp = new IndentWriter();
                iwtmp.push();
                iwtmp.println("- ERROR: " + fi.getTypeAndName() + ", " + fi._url);
                iwtmp.push();

                if (ic_before != D_FundInfo.IC_NO_ERROR && fi._errorCode != D_FundInfo.IC_NO_ERROR) {
                    iwtmp.println("*** Became invalid, IC before: " + ic_before + ", now: " + fi._errorCode);
                    fi._isValid = false;
                }

                boolean should_log = true;
                switch(rc._o1) {
                    case ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND:
                        iwtmp.println("RC_ERROR_KEEP_FUND");
                        break;
                    case ExtractFromHTML_Helper.RC_ERROR_REMOVE_FUND:
                        iwtmp.println("RC_ERROR_REMOVE_FUND");
                        int countDel = 0;
                        while (countDel < fiAllFunds.size()) {
                            if (fi.getTypeAndName().equals(fiAllFunds.get(countDel).getTypeAndName())) {
                                fiAllFunds.remove(countDel);
                                break;
                            }
                            countDel++;
                        }
                        fiRemoved.add(fi);
                        break;
                    case ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED:
                        iwtmp.println("RC_SUCCESS_BUT_DATA_WAS_UPDATED");
                        break;
                    case ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND:
                        should_log = false;
                        break;
                    default:
                        iwtmp.println("ReturnCode: <UNEXPECTED RETURN CODE>");
                        break;
                }

                if (should_log) {
                    iwtmp.println("Message: " + rc._o2);

                    iw_fund_error_shorts.println(iwtmp.getString());

                    fi.dumpInfo(iwtmp);
                    log.severe( "\n" + iwtmp.getString());

                    // iwtmp.println("HTML content:\n" + eh._htmlPageContent);
                    iwtmp.pop();
                    iw_fund_error_details.println(iwtmp.getString());
                }
            }

            // If we are out of time, then break
            if (!MM.timerContinue(Constants.TIMEINS_BEFORE_DEADLINE)) {
                log.info("Maximum processing time reached for cron job, breaking");
                break;
            }

        }

        IndentWriter iwExtract = new IndentWriter();
        printExtractStats(iwExtract, fiAllFunds);

        log.info("---------------------------\nDone extracting, summary info:\n" + iwExtract.getString());
        iwExtract.println("");

        iw_header.println(iwExtract.getString());

        iw_header.println("------------");
        iw_header.println("Fund Extraction Information");
        iw_header.println(iw_fund_infos.getString());
        iw_header.println("------------");
        iw_header.println("Fund Extraction Errors - High Level");
        iw_header.println(iw_fund_error_shorts.getString());
//        mEE.dinfo(log, TAG, "---------------------------\nDone extracting, summary info:\n" + iw_header.getString());

        iw_header.println("------------");
        iw_header.println("Fund Extraction Errors - Details");
        iw_header.println(iw_fund_error_details.getString());

        // Write log file
        String logfileContent = "";
        String fundInfoLogsDebugFilename = Constants.PREFIX_FUNDINFO_LOGS_DEBUG + _fridayLastYYMMDD + Constants.EXT_TXT;
        byte[] logfileBA = GCSWrapper.gcsReadFile(fundInfoLogsDebugFilename);
        if (logfileBA != null && logfileBA.length > 0) {
            logfileContent = new String(logfileBA, Constants.ENCODING_FILE_READ);
        }
        logfileContent = iw_header.getString() + logfileContent;
        GCSWrapper.gcsWriteFile(fundInfoLogsDebugFilename, logfileContent.getBytes(Constants.ENCODING_FILE_WRITE));

        // Updated file with permanently deleted funds
        String fileName_fundInfoDeleted = Constants.PREFIX_FUNDINFO_DELETED + _fridayLastYYMMDD;
        if (fiRemoved.size() > 0) {
            List<D_FundInfo> fiRemovedOlds = new ArrayList<>();
            byte[] oldRMData = GCSWrapper.gcsReadFile(fileName_fundInfoDeleted);
            if (oldRMData != null && oldRMData.length > 0) {
                fiRemovedOlds = D_FundInfo_Serializer.decrunchFundList(oldRMData);
            }

            IndentWriter iw = new IndentWriter();
            iw.println("Funds that have been deleted from main DB");
            iw.push();
            for (int i = fiRemoved.size() - 1; i >= 0; i--) {
                D_FundInfo fi = fiRemoved.get(i);
                fiRemovedOlds.add(0, fi);
                iw.println(fi.getTypeAndName() + ", last dp: " + getLastestDate(fi._dpDays) + ", url: " + fi._url);
            }
            for (D_FundInfo fi: fiRemovedOlds) {
                iw.println(fi.getTypeAndName() + ", last dp: " + getLastestDate(fi._dpDays) + ", url: " + fi._url);
            }
            iw.pop();

            byte[] newRMData = D_FundInfo_Serializer.crunchFundList(fiRemovedOlds);
            GCSWrapper.gcsWriteBlob(fileName_fundInfoDeleted + Constants.EXT_BIN, newRMData);
            GCSWrapper.gcsWriteBlob(fileName_fundInfoDeleted + Constants.EXT_TXT, iw.getString().getBytes(Constants.ENCODING_FILE_WRITE));
        }

        // Write fund DB files
        byte[] fundListBA = D_FundInfo_Serializer.crunchFundList(fiAllFunds);
        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, fundListBA);
        GCSWrapper.gcsWriteFile(Constants.PREFIX_FUNDINFO_DB + _fridayLastYYMMDD + Constants.EXT_BIN, fundListBA);

        // Write Extract Summary File
        String extractInfo = getExtractSummaryFile();
        GCSWrapper.gcsWriteFile(Constants.PREFIX_FUNDINFO_LOGS_EXTRACT + _fridayLastYYMMDD + Constants.EXT_TXT, extractInfo.getBytes(Constants.ENCODING_FILE_WRITE));

        MM.sleepInMS(4000);
    }

    private String getExtractSummaryFile() throws IOException {
        byte[] thisRound = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        List<D_FundInfo> fiNow = D_FundInfo_Serializer.decrunchFundList(thisRound);
        byte[] prevRound = GCSWrapper.gcsReadFile(Constants.PREFIX_FUNDINFO_DB + _fridayLast2YYMMDD + Constants.EXT_BIN);
        List<D_FundInfo> fiPrev = null;

        String p_total = "***not available***";
        int p_totalI = -1;
        String p_extracted = "***not available***";
        int p_extractedI = -1;
        String p_invalids = "***not available***";
        String p_errors = "***not available***";
        int p_todayAlreadyAttempted = -1;
        int p_todayLeftToTry = -1;
        if (fiPrev != null && fiPrev.size() > 0) {
            extractStats(fiPrev);
            p_total = String.valueOf(_fiAllFunds.size());
            p_totalI = _fiAllFunds.size();
            p_extracted = String.valueOf(_fiAlreadyExtracted.size());
            p_extractedI = _fiAlreadyExtracted.size();
            p_invalids = String.valueOf(_fiInvalids.size());
            p_errors = String.valueOf(_fiErrors.size());
            p_todayAlreadyAttempted = _fiAlreadyAttemptedToday.size();
            p_todayLeftToTry = p_totalI - p_extractedI - p_todayAlreadyAttempted;
        }

        extractStats(fiNow);
        int todayLeftToTry = _fiAllFunds.size() - _fiAlreadyExtracted.size() - _fiAlreadyAttemptedToday.size();
        IndentWriter iw = new IndentWriter();
        iw.println("Current date: " + _fridayLastYYMMDD + ", (compared to: " + _fridayLast2YYMMDD + ")");
        iw.println("Total funds to extract: " + _fiAllFunds.size() + " (" + p_total + ")");
        iw.println("Current stats");
        iw.println("...Extracted: " + _fiAlreadyExtracted.size() + " (" + p_extracted + ")");
        iw.println("...Not Extracted: " + (_fiAllFunds.size() - _fiAlreadyExtracted.size()) + " (" + String.valueOf(p_totalI-p_extractedI) + ")");
        iw.println("...Today, attempted (no data): " + _fiAlreadyAttemptedToday.size() + " (" + String.valueOf(p_totalI-p_extractedI) + ")");
        iw.println("...Today, left to try: " + todayLeftToTry + " (" + String.valueOf(p_totalI-p_extractedI) + ")");

        iw.println("...Today, attempted (but no data): " + _fiAlreadyAttemptedToday.size());
        for (int i=0; i < _fiAlreadyAttemptedToday.size(); i++) {
            D_FundInfo fi = _fiAlreadyAttemptedToday.get(i);
            iw.println(".....[" + i + "]: " + "<a href=\"" + fi._url + "\">" + fi.getTypeAndName() + "</a>");
        }

        iw.println("Invalids: " + _fiInvalids.size() + " (" + p_invalids + ")<br>");
        for (int i=0; i < _fiInvalids.size(); i++) {
            D_FundInfo fi = _fiInvalids.get(i);
            iw.println(".....[" + i + "]: " + "<a href=\"" + fi._url + "\">" + fi.getTypeAndName() + "</a><br>");
        }

        iw.println("Errors: " + _fiErrors.size() + " (" + p_errors + ")<br>");
        for (int i=0; i < _fiErrors.size(); i++) {
            D_FundInfo fi = _fiErrors.get(i);
            iw.println(".....[" + i + "]: " + "<a href=\"" + fi._url + "\">" + fi.getTypeAndName() + "</a><br>");
        }
        iw.println();
        return iw.getString();
    }

    private static String getLastestDate(List<D_FundDPDay> dps) {
        if (dps == null) return "null";
        if (dps.size() == 0) return "";
        String date = dps.get(0)._dateYYMMDD;
        if (date == null) return "null_date";
        return date;
    }

    private static String printListStr(IndentWriter iw, String header, List<D_FundInfo> l) {
        if (iw == null) {
            iw = new IndentWriter();
        }
        iw.setIndentChar('.');
        iw.println(header + ". Count: " + l.size() + ".");
        iw.push();
        for (int i=0; i < l.size(); i++) {
            D_FundInfo fi = l.get(i);
            iw.print("[" + i + "]: "
                    + fi._type + "." + fi._nameMS
                    + ", updated: " + fi._dateYYMMDD_Updated
                    + ", attempted: " + fi._dateYYMMDD_Update_Attempted);
            if (fi._errorCode != D_FundInfo.IC_NO_ERROR) {
                iw.println(", ic: " + fi._errorCode);
            } else {
                iw.println(", valid");
            }
            iw.push();
            iw.println(fi._url);
            iw.pop();
        }
        iw.pop();
        return iw.getString();
    }

    List<D_FundInfo> _fiAllFunds;
    List<D_FundInfo> _fiToExtract;
    List<D_FundInfo> _fiInvalids;
    List<D_FundInfo> _fiErrors;
    List<D_FundInfo> _fiAlreadyExtracted;
    List<D_FundInfo> _fiAlreadyAttemptedToday;
    private void extractStats(List<D_FundInfo> fiAllFunds) {
        _fiAllFunds = fiAllFunds;
        _fiToExtract = new ArrayList<>();
        _fiInvalids = new ArrayList<>();
        _fiErrors = new ArrayList<>();
        _fiAlreadyExtracted = new ArrayList<>();
        _fiAlreadyAttemptedToday = new ArrayList<>();
        for (int i = 0; i < fiAllFunds.size(); i++) {
            D_FundInfo fi = fiAllFunds.get(i);
            if (!fi._isValid) {
                _fiInvalids.add(fi);
            } else if (fi._errorCode != D_FundInfo.IC_NO_ERROR) {
                _fiErrors.add(fi);
            } else if (fi._dateYYMMDD_Update_Attempted == null || fi._dateYYMMDD_Update_Attempted.length() != 6) {
                throw new AssertionError("Attempted field wrong: " + fi._type + "." + fi._nameMS);
            } else if (fi._dateYYMMDD_Updated.compareTo(_fridayLastYYMMDD) >= 0) {
                _fiAlreadyExtracted.add(fi);
            } else if (fi._dateYYMMDD_Update_Attempted.compareTo(_nowYYMMDD) < 0) {
                _fiToExtract.add(fi);
            } else if (fi._dateYYMMDD_Update_Attempted.compareTo(_nowYYMMDD) == 0) {
                _fiAlreadyAttemptedToday.add(fi);
            } else {
                throw new AssertionError("Weird state for fund: " + fi.toString());
            }
        }
    }

    private void printExtractStats(IndentWriter iw, List<D_FundInfo> fiAllFunds) {
        extractStats(fiAllFunds);

        iw.println("*** Fund report: " + MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM));
        iw.push();
        iw.println("Total funds in DB: " + fiAllFunds.size());
        iw.println("Extract done: " + _fiAlreadyExtracted.size());
        printListStr(iw, "Fund Invalids", _fiInvalids);
        printListStr(iw, "Fund Errors", _fiErrors);
        printListStr(iw, "Already Attempted To Extract Today (but without success)", _fiAlreadyAttemptedToday);
        iw.pop();
    }



}


package com.pf.fl.be.extract;

import com.google.cloud.storage.Blob;
import com.pf.shared.Constants;
import com.pf.shared.datamodel.DB_FundInfo;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo_Serializer;
import com.pf.shared.datamodel.D_FundInfo_Validator;
import com.pf.shared.extract.ExtractFromHTML_Helper;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.extract.ExtractStatistics;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

public class FLOps1_Ext1_Extract_New {
    private static final Logger log = Logger.getLogger(FLOps1_Ext1_Extract_New.class.getName());
    private static final String TAG = MM.getClassName(FLOps1_Ext1_Extract_New.class.getName());

    private IndentWriter _iwd = new IndentWriter();
    private boolean _debugSuccessful;
    private boolean _ignoreSchedule;
    private boolean _doPostProcessing = true;

    private List<D_FundInfo> _argFIToExtract = null;
    private List<D_FundInfo> _fiToExtract  = null;

    private String _nowYYMMDD;
    private String _nowYYMMDD_HHMMSS;
    private String _fridayLastYYMMDD;
    private String _fridayLast2YYMMDD;

    //------------------------------------------------------------------------
    public FLOps1_Ext1_Extract_New(List<D_FundInfo> argFIToExtract,
                                   boolean ignoreSchedule,
                                   boolean doPostProcessing,
                                   IndentWriter iwd,
                                   boolean debugSuccessful) {
        _argFIToExtract = argFIToExtract;
        _ignoreSchedule = ignoreSchedule;
        _doPostProcessing = doPostProcessing;
        if (iwd != null) {
            _iwd = iwd;
        }
        _iwd.println("\n*********************************************************************");
        if (iwd != null) {
            _iwd.println("This is a manual execution, iwd came in from constructor");
        }
        _debugSuccessful = debugSuccessful;
    }

    public void doIt() throws IOException {
        _iwd.println("FLOps1_Ext1_Extract_New, entered");
        MM.timerStart();

        // Set time variables
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_STOCKHOLM));
        int nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int nowHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        _nowYYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        _nowYYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM);
        String nowYYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(Constants.TIMEZONE_STOCKHOLM);
        _fridayLastYYMMDD  = MM.tgif_getLastFridayTodayExcl(_nowYYMMDD);
        _fridayLast2YYMMDD = MM.tgif_getLastFridayTodayExcl(_fridayLastYYMMDD);
        String sunYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.SUNDAY);
        String monYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.MONDAY);
        String tueYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.TUESDAY);
        String wedYYMMDD = MM.tgif_getNextWeekday(_fridayLastYYMMDD, Calendar.WEDNESDAY);

        // Check if it is time to extract
        if (!_ignoreSchedule) {
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

        _iwd.println("Extracting now: " + nowYYMMDD_HHMMSS + " (Stockholm time)");
        _iwd.println("Last Friday is: " + _fridayLastYYMMDD);

        // Get all Funds and Classify them
        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        DB_FundInfo.initialize(fundInfoBA);
        List<D_FundInfo> fiAllFunds = DB_FundInfo.getAllFundInfos();

        D_FundInfo_Validator fiv = new D_FundInfo_Validator(fiAllFunds);
        fiv.process();
        if (!fiv._error) {
            log.info("Fund database is valid");
        } else {
            log.severe("Fund database in invalid, will not initiate extraction\n" + fiv._iwErrors.getString());
            return;
        }

        if (_argFIToExtract != null && _argFIToExtract.size() > 0) {
            _fiToExtract = _argFIToExtract;
        } else {
            ExtractStatistics.getExtractSummary(_iwd, false, _nowYYMMDD_HHMMSS, fiAllFunds);
            ExtractStatistics es = new ExtractStatistics(fiAllFunds);
            es.extractStats();
            _fiToExtract = es._fiToExtract;
        }
        log.info(_iwd.getString());
        _iwd.println("Extracting: " + _fiToExtract.size() + " entries");

        // Return if there is no work
        if (_fiToExtract.size() == 0) {
            log.info("No entries to extract, extractList was empty: " + _nowYYMMDD + ", " + MM.tgif_getDayOfWeekStr(nowDayOfWeek) + "@" + nowHourOfDay + ", lastFriday: " + _fridayLastYYMMDD);
            _iwd.println("No entries to extract, extractList was empty");
            return;
        }

        int countTotal = 0;
        while (_fiToExtract.size() > 0) {
            D_FundInfo fi = _fiToExtract.remove(0);

            String fundBeforeStr = fi.getOneLiner();
            int ec_before = fi._errorCode;

            _iwd.println("[" + countTotal + "]. Extracting: " + fi.getOneLiner());

            IndentWriter iwdetails = new IndentWriter();
            iwdetails.setIndentChar('.');
            iwdetails.push();
            ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
            int errorCode = eh.extractFundDetails(fi, iwdetails);

            if (errorCode == ExtractFromHTML_Helper.RC_SUCCESS) {
                _iwd.println("...SUCCESS: " + fi.getOneLiner());
                if (_debugSuccessful) {
                    _iwd.println(iwdetails.getString());
                }
            } else {
                String s = "Error for: " + fi.getOneLiner();
                _iwd.println("...**ERROR: " + fi.getOneLiner());
                boolean is_error = false;
                switch(errorCode) {
                    case ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND:
                        s += "\nFund became invalid";
                        _iwd.println("...RC_ERROR_REMOVE_FUND, setting it to invalid");
                        fi._isValid = true;
                        break;
                    case ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED:
                        s += "\nSuccess, but data was updated";
                        _iwd.println("...RC_SUCCESS_BUT_DATA_WAS_UPDATED");
                        break;
                    case ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND:
                        s += "\nNo DPDay found";
                        _iwd.println("...NO DPDAY FOUND");
                        break;
                    default:
                        _iwd.println("...<UNEXPECTED RETURN CODE>");
                        break;
                }
                _iwd.println(iwdetails.getString());

                if (is_error) {
                    log.severe(s);
                } else {
                    log.info(s);
                }
            }

//            if (true) {
//                break;
//            }

            D_FundInfo_Validator fivSingleFund = new D_FundInfo_Validator();
            fivSingleFund.validateFund(fi);
            if (fivSingleFund._error) {
                String valError = "Encountered validation error for a single fund, will not continue\n" + "\n" + fivSingleFund._iwErrors.getString() + fi.toString()
                        + "Fund before extraction: " + fundBeforeStr;
                _iwd.println(valError);
                log.severe(valError);
                return;
            }

            countTotal++;
            if (countTotal == 1 || (countTotal % 50) == 0) {
                log.info("Processed: " + countTotal + ", latest: " + fi.getTypeAndName() + ", " + fi._url);
            }

            // If we are out of time, then break
            if (!MM.timerContinue(Constants.TIMEINS_BEFORE_DEADLINE)) {
                log.info("Maximum processing time reached for cron job, breaking");
                break;
            }

            // break;
        }
        log.info("Done, processed: " + countTotal);

        if (!_doPostProcessing) {
            log.warning("Extract session configured to not perform post-processing, returning");
            return;
        }

        fiv = new D_FundInfo_Validator(fiAllFunds);
        fiv.process();
        if (!fiv._error) {
            log.info("Extraction resulting in a valid fund database");
        } else {
            log.severe("Extraction resulted in invalid fund database. Refuse to save.\n" + fiv._iwErrors.getString());
            return;
        }

        // Get end summary
        IndentWriter iwExtract = new IndentWriter();
        ExtractStatistics.getExtractSummary(iwExtract, false, _nowYYMMDD_HHMMSS, fiAllFunds);
        log.info("Done extracting, summary info:\n" + iwExtract.getString());
        _iwd.println("\n Done extracting, summary info:\n" + iwExtract.getString());

        // Write extract file (used by the mobile app)
        log.info("Writing extract master file: " + Constants.FUNDINFO_LOGS_EXTRACT_MASTER_TXT);
        byte[] extractBA = iwExtract.getString().getBytes(Constants.ENCODING_FILE_WRITE);
        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_LOGS_EXTRACT_MASTER_TXT, extractBA);
        log.info("...Done writing extract master file, total bytes: " + extractBA.length);

        // Write fund DB files
        log.info("Writing master DB: " + Constants.FUNDINFO_DB_MASTER_BIN);
        byte[] fundListBA = D_FundInfo_Serializer.crunchFundList(fiAllFunds);
        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, fundListBA);
        String dayMasterDBFilename = Constants.PREFIX_FUNDINFO_DB + _fridayLastYYMMDD + Constants.EXT_BIN;
        log.info("Writing day master DB: " + dayMasterDBFilename);
        GCSWrapper.gcsWriteFile(dayMasterDBFilename, fundListBA);

        // Write debug file
        log.info("Writing debug file");
        String logfileContent = "";
        String fundInfoLogsDebugFilename = Constants.PREFIX_FUNDINFO_LOGS_DEBUG + _fridayLastYYMMDD + Constants.EXT_TXT;
        log.info("Writing debug file: " + fundInfoLogsDebugFilename);
        byte[] logfileBA = GCSWrapper.gcsReadFile(fundInfoLogsDebugFilename);
        if (logfileBA != null && logfileBA.length > 0) {
            logfileContent = new String(logfileBA, Constants.ENCODING_FILE_READ);
        }
        byte[] logfileContentBA =(_iwd.getString() + logfileContent).getBytes(Constants.ENCODING_FILE_WRITE);
        GCSWrapper.gcsWriteFile(Constants.PREFIX_FUNDINFO_LOGS_DEBUG + _fridayLastYYMMDD + Constants.EXT_TXT, logfileContentBA);
        GCSWrapper.gcsWriteFile(Constants.PREFIX_WHISTORIC_FUNDINFO_LOGS_DEBUG + _fridayLastYYMMDD + Constants.EXT_TXT, logfileContentBA);
        GCSWrapper.gcsDeleteFiles(Constants.PREFIX_FUNDINFO_LOGS_DEBUG, _fridayLastYYMMDD);  // Delete old files

        // Write Extract Summary File
        log.info("Writing extract details files");
        byte[] thisRound = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        List<D_FundInfo> fiNow = D_FundInfo_Serializer.decrunchFundList(thisRound);
        IndentWriter iwextract = new IndentWriter();
        ExtractStatistics.getExtractSummary(iwextract, false, _nowYYMMDD_HHMMSS, fiNow);
        String logFridayFilename = Constants.PREFIX_FUNDINFO_LOGS_EXTRACT + _fridayLastYYMMDD + Constants.EXT_TXT;
        byte[] fridayLogContent = GCSWrapper.gcsReadFile(logFridayFilename);
        String fridayLogContentStr = "";
        if (fridayLogContent != null) {
            fridayLogContentStr = new String(fridayLogContent, Constants.ENCODING_FILE_READ);
        }
        fridayLogContentStr = "-------------------------------------------------------------------\n" + iwextract.getString() + fridayLogContentStr;
        byte[] fridayLogContentBA = fridayLogContentStr.getBytes(Constants.ENCODING_FILE_WRITE);
        GCSWrapper.gcsWriteFile(Constants.PREFIX_FUNDINFO_LOGS_EXTRACT + _fridayLastYYMMDD + Constants.EXT_TXT, fridayLogContentBA);
        GCSWrapper.gcsWriteFile(Constants.PREFIX_WHISTORIC_FUNDINFO_LOGS_EXTRACT + _fridayLastYYMMDD + Constants.EXT_TXT, fridayLogContentBA);
        GCSWrapper.gcsDeleteFiles(Constants.PREFIX_FUNDINFO_LOGS_EXTRACT, _fridayLastYYMMDD);  // Delete old files

        log.info("Returning successfully from extract processing");
        MM.sleepInMS(4000);
    }
}

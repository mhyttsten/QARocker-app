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
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class FLOps1_Ext1_Extract_New {
    private static final Logger log = Logger.getLogger(FLOps1_Ext1_Extract_New.class.getName());
    private static final String TAG = MM.getClassName(FLOps1_Ext1_Extract_New.class.getName());

    private IndentWriter _iwd = new IndentWriter();
    private boolean _iwdAddSuccessful;
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
                                   boolean iwdAddSuccessful) {
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
        _iwdAddSuccessful = iwdAddSuccessful;
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
        DB_FundInfo.initialize(fundInfoBA, true);
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
            ExtractStatistics.getExtractSummary(_iwd, -1, -1, _nowYYMMDD_HHMMSS, fiAllFunds);
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
            log.info("Now processing fund: " + fi.getTypeAndName());

            String fundBeforeStr = fi.getOneLiner();
            int ec_before = fi._errorCode;

            _iwd.println("[" + countTotal + "]. Extracting: " + fi.getOneLiner());

            IndentWriter iwdetails = new IndentWriter();
            iwdetails.setIndentChar('.');
            iwdetails.push();
            ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
            int errorCode = eh.extractFundDetails(fi, iwdetails);
            fi._dateYYMMDD_Update_Attempted = eh._dateNow_YYMMDD;

            if (errorCode == ExtractFromHTML_Helper.RC_SUCCESS) {
                fi._isValid = true;
                fi._errorCode = D_FundInfo.IC_NO_ERROR;
                _iwd.println("...SUCCESS: " + fi.getOneLiner());
                if (_iwdAddSuccessful) {
                    _iwd.println(iwdetails.getString());
                }
            } else {
                String s = "Error for: " + fi.getOneLiner();
                iwdetails.println("...**ERROR: " + fi.getOneLiner());
                boolean is_error = false;
                switch(errorCode) {
                    case ExtractFromHTML_Helper.RC_ERROR_INVALID_FUND:
                        log.severe("*** INVALID: " + fi.getOneLiner());
                        s += "\nFund became invalid";
                        iwdetails.println("...RC_ERROR_REMOVE_FUND, setting it to invalid");
                        fi._isValid = false;
                        is_error = true;
                        break;
                    case ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED:
                        s += "\nSuccess, but data was updated";
                        iwdetails.println("...RC_SUCCESS_BUT_DATA_WAS_UPDATED");
                        break;
                    case ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND:
                        s += "\nNo DPDay found";
                        iwdetails.println("...NO DPDAY FOUND");
                        break;
                    default:
                        iwdetails.println("...<UNEXPECTED RETURN CODE>");
                        is_error = true;
                        break;
                }
                _iwd.println(iwdetails.getString());

                if (is_error) {
                    log.severe(s + "\n" + iwdetails.getString());
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

            log.info("...done processing: " + fi.getTypeAndName());
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

        // Write fund DB master
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

        // We only emit statistics once this entire day is finished
        // Finished meaning we've extracted or attempted extraction of all funds
        if (_fiToExtract.size() == 0) {
            log.info("Writing extract details files");
            byte[] thisRound = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
            List<D_FundInfo> fiNow = D_FundInfo_Serializer.decrunchFundList(thisRound);
            IndentWriter iwextract = new IndentWriter();
            ExtractStatistics.getExtractSummary(iwextract, 2, -1, _nowYYMMDD_HHMMSS, fiNow);
            String logFridayFilename = Constants.PREFIX_FUNDINFO_LOGS_EXTRACT + _fridayLastYYMMDD + Constants.EXT_TXT;
            byte[] fridayLogContentBA = GCSWrapper.gcsReadFile(logFridayFilename);
            String fridayLogContentStr = "";
            if (fridayLogContentBA != null) {
                fridayLogContentStr = new String(fridayLogContentBA, Constants.ENCODING_FILE_READ);
                fridayLogContentStr = "----------------\n" + fridayLogContentStr;
            }
            fridayLogContentStr = iwextract.getString() + fridayLogContentStr;
            fridayLogContentBA = fridayLogContentStr.getBytes(Constants.ENCODING_FILE_WRITE);
            GCSWrapper.gcsWriteFile(Constants.PREFIX_FUNDINFO_LOGS_EXTRACT + _fridayLastYYMMDD + Constants.EXT_TXT, fridayLogContentBA);
            GCSWrapper.gcsWriteFile(Constants.PREFIX_WHISTORIC_FUNDINFO_LOGS_EXTRACT + _fridayLastYYMMDD + Constants.EXT_TXT, fridayLogContentBA);
            GCSWrapper.gcsDeleteFiles(Constants.PREFIX_FUNDINFO_LOGS_EXTRACT, _fridayLastYYMMDD);  // Delete old files
            sendMail(_fridayLastYYMMDD, fridayLogContentStr);
            log.info("...Done writing extract details files");
        }
        log.info("Returning successfully from extract processing");
        MM.sleepInMS(4000);
    }

    private static void sendMail(String dpday, String content) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("extractor@ql-magnushyttsten.appspotmail.com", "Mother"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("magnus.hyttsten@gmail.com", "Master"));
            msg.setSubject("Your extraction report for: " + dpday);
            msg.setText(content);
            Transport.send(msg);
        } catch (Exception exc) {
            log.severe("Could not send mail, exception: " + exc.getMessage());
        }

    }
}


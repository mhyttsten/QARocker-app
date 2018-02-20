package com.pf.fl.be.extract;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.pf.shared.Constants;
import com.pf.shared.extract.ExtractFromHTML_Helper;
import com.pf.fl.be.util.EE;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.datamodel.D_FundInfo_Serializer;
import com.pf.shared.utils.Compresser;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

public class FLOps1_Ext1_Extract_New {
    private static final Logger log = Logger.getLogger(FLOps1_Ext1_Extract_New.class.getName());
    private static final String TAG = MM.getClassName(FLOps1_Ext1_Extract_New.class.getName());

    private EE mEE;
    private List<String> mIWDebugs = new ArrayList<>();
    private boolean mIgnoreSchedule;

    public FLOps1_Ext1_Extract_New(EE ee, boolean ignoreSchedule, IndentWriter iwErrors) {
        mEE = ee;
        mIgnoreSchedule = ignoreSchedule;
    }

    public void doIt() throws IOException {

        // Set time variables
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(EE.TIMEZONE_STOCKHOLM));
        int nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int nowHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        String nowYYMMDD = MM.getNowAs_YYMMDD(EE.TIMEZONE_STOCKHOLM);
        String nowYYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(EE.TIMEZONE_STOCKHOLM);
        String fridayLastYYMMDD = MM.tgif_getLastFridayTodayExcl(nowYYMMDD);
        String sunYYMMDD = MM.tgif_getNextWeekday(fridayLastYYMMDD, Calendar.SUNDAY);
        String monYYMMDD = MM.tgif_getNextWeekday(fridayLastYYMMDD, Calendar.MONDAY);
        String tueYYMMDD = MM.tgif_getNextWeekday(fridayLastYYMMDD, Calendar.TUESDAY);
        String wedYYMMDD = MM.tgif_getNextWeekday(fridayLastYYMMDD, Calendar.WEDNESDAY);

        // Same algorithm for collection and recollecting
        //if ((nowDayOfWeek == Calendar.SATURDAY && nowHourOfDay > 5)

        // Check if it is time to extract
        if (!mIgnoreSchedule) {
            boolean scheduledPlay =
                    (nowDayOfWeek == Calendar.SATURDAY && nowHourOfDay >= 5)
                            || (nowYYMMDD.equals(sunYYMMDD) && nowHourOfDay >= 5)
                            || (nowYYMMDD.equals(monYYMMDD) && nowHourOfDay >= 5)
                            || (nowYYMMDD.equals(tueYYMMDD) && nowHourOfDay >= 5)
                            || (nowYYMMDD.equals(wedYYMMDD) && nowHourOfDay >= 5);
            if (!scheduledPlay) {
                mEE.dinfo(log, TAG, "Not time for extraction now: " + nowYYMMDD + ", " + MM.tgif_getDayOfWeekStr(nowDayOfWeek) + "@" + nowHourOfDay + ", lastFriday: " + fridayLastYYMMDD);
                return;
            }
        }

        // *** 1: It is time to extract
        mEE.dinfo(log, TAG, "Now: " + nowYYMMDD_HHMMSS + ", " + MM.tgif_getDayOfWeekStr(nowDayOfWeek) + "@" + nowHourOfDay + ", lastFriday: " + fridayLastYYMMDD);

        IndentWriter iw_header = new IndentWriter();
        iw_header.println("\n*********************************************************************");
        iw_header.println("Extracting now: " + nowYYMMDD_HHMMSS + " (Stockholm time)");
        iw_header.println("Last Friday is: " + fridayLastYYMMDD);

        // Get all Funds and Classify them
        List<D_FundInfo> fiAllFunds = FLOps1_Ext1_Extract_New.readFundList();
        int fiAllFundsInitialCount = fiAllFunds.size();
        List<D_FundInfo> fiToExtract = new ArrayList<>();
        List<D_FundInfo> fiInvalid = new ArrayList<>();
        List<D_FundInfo> fiAlreadyExtracted = new ArrayList<>();
        List<D_FundInfo> fiAlreadyAttemptedToday = new ArrayList<>();
        for (int i=0; i < fiAllFunds.size(); i++) {
            D_FundInfo fi = fiAllFunds.get(i);
            if (!fi._isValid) {
                fiInvalid.add(fi);
            } else if (fi._dateYYMMDD_Update_Attempted == null || fi._dateYYMMDD_Update_Attempted.length() != 6) {
                throw new IOException("Attempted field wrong: " + fi._type + "." + fi._nameMS);
            } else if (fi._dateYYMMDD_Updated.compareTo(fridayLastYYMMDD) >= 0) {
                fiAlreadyExtracted.add(fi);
            } else if (fi._dateYYMMDD_Update_Attempted.compareTo(nowYYMMDD) < 0) {
                fiToExtract.add(fi);
            } else if (fi._dateYYMMDD_Update_Attempted.compareTo(nowYYMMDD) == 0) {
                fiAlreadyAttemptedToday.add(fi);
            } else {
                throw new IOException("Weird state for fund: " + fi.toString());
            }
        }

        // *** 2: It is time to extract
        iw_header.println("*** Extract list created");
        iw_header.push();
        iw_header.println("Total funds considered: " + fiAllFunds.size());
        iw_header.println("Already extracted this week: " + fiAlreadyExtracted.size());
//        printListStr(iw_header, "Already Extracted this week", fiAlreadyExtracted);
        // iw_header.println("Already attempted extract today: " + fiAlreadyAttemptedToday.size());
        printListStr(iw_header, "Already Attempted Extract Today", fiAlreadyAttemptedToday);
        iw_header.println("To extract: " + fiToExtract.size());
        printListStr(iw_header, "Funds Invalid", fiInvalid);
        iw_header.pop();
        mEE.dinfo(log, TAG, iw_header.getString());

        // Return if there is no work
        if (fiToExtract.size() == 0) {
            mEE.dinfo(log, TAG, "No entries to extract, extractList was empty: " + nowYYMMDD + ", " + MM.tgif_getDayOfWeekStr(nowDayOfWeek) + "@" + nowHourOfDay + ", lastFriday: " + fridayLastYYMMDD);
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
        List<D_FundInfo> fiNotUpdated = new ArrayList<>();
        List<D_FundInfo> fiUpdated = new ArrayList<>();
        fiInvalid = new ArrayList<D_FundInfo>();
        List<D_FundInfo> fiDeleted = new ArrayList<>();
        List<D_FundInfo> fiError = new ArrayList<>();
        List<D_FundInfo> fiKeep = new ArrayList<>();
        while (fiToExtract.size() > 0) {
            D_FundInfo fi = fiToExtract.remove(0);

//            mEE.dinfo(log, TAG, "----------------------------------\nInformation before processing fund\n" + fi.toString());

            int ic_before = fi._errorCode;

            countTotal++;

            ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
            OTuple2G<Integer, String> rc = eh.extractFundDetails(fi);
            if (rc._o1 == ExtractFromHTML_Helper.RC_SUCCESS) {
                fiUpdated.add(fi);
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
                    fiInvalid.add(fi);
                    fi._isValid = false;
                }

                boolean should_log = true;
                switch(rc._o1) {
                    case ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND:
                        iwtmp.println("RC_ERROR_KEEP_FUND");
                        fiError.add(fi);
                        break;
                    case ExtractFromHTML_Helper.RC_ERROR_REMOVE_FUND:
                        iwtmp.println("RC_ERROR_REMOVE_FUND");
                        fiDeleted.add(fi);
                        int countDel = 0;
                        while (countDel < fiAllFunds.size()) {
                            if (fi.getTypeAndName().equals(fiAllFunds.get(countDel).getTypeAndName())) {
                                fiAllFunds.remove(countDel);
                                break;
                            }
                            countDel++;
                        }
                        break;
                    case ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED:
                        iwtmp.println("RC_SUCCESS_BUT_DATA_WAS_UPDATED");
                        fiUpdated.add(fi);
                        break;
                    case ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND:
                        should_log = false;
                        fiNotUpdated.add(fi);
                        break;
                    default:
                        iwtmp.println("ReturnCode: <UNEXPECTED RETURN CODE>");
                        break;
                }

                if (should_log) {
                    iwtmp.println("Message: " + rc._o2);

                    iw_fund_error_shorts.println(iwtmp.getString());

                    fi.dumpInfo(iwtmp);
                    mEE.dwarning(log, TAG, "\n" + iwtmp.getString());

                    iwtmp.println("HTML content:\n" + eh._htmlPageContent);
                    iwtmp.pop();
                    iw_fund_error_details.println(iwtmp.getString());
                }
            }

//            mEE.dinfo(log, TAG, "\nExtract completed, result:\n" + fi.toString());
//            if (countTotal > 2) {
//                break;
//            }

            // If we are out of time, then break
            if (!EE.timerContinue()) {
                mEE.dinfo(log, TAG, "Maximum processing time reached for cron job, breaking");
                break;
            }

        }

        IndentWriter iwExtract = new IndentWriter();
        iwExtract.println("*** End information. Processed a total of: " + countTotal + " funds");
        iwExtract.push();

        iwExtract.println("Total in DB at start: " + fiAllFundsInitialCount);
        iwExtract.println("Keeping in DB: " + fiAllFunds.size());
        iwExtract.println("Updated: " + fiUpdated.size());
//        iwExtract.println("Not updated: " + fiNotUpdated.size());
        printListStr(iwExtract, "Not Updated", fiNotUpdated);
        printListStr(iwExtract, "Had Errors", fiError);
//        iwExtract.println("Became invalid: " + fiInvalid.size());
        printListStr(iwExtract, "Became Invalid", fiInvalid);
        printListStr(iwExtract, "Permanently Deleted", fiDeleted);
        mEE.dinfo(log, TAG, "---------------------------\nDone extracting, summary info:\n" + iwExtract.getString());
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
        byte[] logfileBA = gcsReadFile(Constants.PREFIX_FUNDINFO_LOGS, false);
        if (logfileBA != null && logfileBA.length > 0) {
            logfileContent = new String(logfileBA, EE.ENCODING_FILE_READ);
        }
        logfileContent = iw_header.getString() + logfileContent;
        gcsWriteFile(Constants.PREFIX_FUNDINFO_LOGS, Constants.EXT_TXT, logfileContent.getBytes(EE.ENCODING_FILE_WRITE), false);

        // Write fund file
        saveFundList(fiAllFunds);

        MM.sleepInMS(1000);
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

    // ******************************************************************

    public static List<D_FundInfo> readFundList() throws IOException {
        byte[] data = gcsReadFile(Constants.PREFIX_FUNDINFO_DB, true);

        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(bin);
        List<D_FundInfo> l = new ArrayList<>();
        while (din.available() > 0) {
            D_FundInfo fi = D_FundInfo_Serializer.decrunch_D_FundInfo(din);
            l.add(fi);
        }
        return l;
    }

    public static void saveFundList(List<D_FundInfo> l) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);

        for (D_FundInfo fi: l) {
            D_FundInfo_Serializer.crunch_D_FundInfo(dout, fi);
        }

        dout.flush();
        byte[] data = bout.toByteArray();
        gcsWriteFile(Constants.PREFIX_FUNDINFO_DB, Constants.EXT_BIN, data, true);
    }

    // ******************************************************************

    public static void gcsWriteFile(String prefix, String ext, byte[] data, boolean compress) throws IOException {

        Blob blob = gcsGetBlob(prefix);
        if (blob != null) {
            blob.delete();
        }

        Storage storage = null;
        storage = StorageOptions.getDefaultInstance().getService();
        String fname = prefix + MM.getNowAs_YYMMDD(null) + ext;
        try {
            if (compress) {
                byte[] cdata = Compresser.dataCompress(fname, data);
                blob = storage.create(BlobInfo.newBuilder(Constants.BUCKET, fname).build(), cdata);
            }
        } catch(Exception exc) {
            throw new AssertionError(exc);
        }
    }

    public static byte[] gcsReadFile(String prefix, boolean decompress) throws IOException {
        // Old school
        Storage storage = null;
        storage = StorageOptions.getDefaultInstance().getService();

        EE ee = EE.getEE();

        try {
            Blob blob = gcsGetBlob(prefix);
            if (blob == null) {
                return null;
            }
            ee.dinfo(log, TAG, "Reading file: " + blob.getName());
            storage = StorageOptions.getDefaultInstance().getService();
            byte[] dataBA = blob.getContent();
            if (decompress) {
                dataBA = Compresser.dataUncompress(dataBA);
            }
            return dataBA;
        } catch(Exception exc) {
            throw new AssertionError(exc);
        }
    }

    public static Blob gcsGetBlob(String prefix) throws IOException {
        Storage storage = null;
        storage = StorageOptions.getDefaultInstance().getService();

        EE ee = EE.getEE();

        try {
            Bucket bucket = storage.get(Constants.BUCKET);
            Page<Blob> pblob = bucket.list();
            Iterable<Blob> iterator = pblob.iterateAll();
            List<Blob> blobs = new ArrayList<>();
            for (Blob blob : iterator) {
                String bname = blob.getName();
                if (bname != null && bname.startsWith(prefix)) {
                    blobs.add(blob);
                }
            }

            Collections.sort(blobs, new Comparator<Blob>() {
                @Override
                public int compare(Blob o1, Blob o2) {
                    return -o1.getName().compareTo(o2.getName());
                }
            });

            if (blobs.size() == 0) {
                return null;
            }

            return blobs.get(0);
        } catch(Exception exc) {
            throw new AssertionError(exc);
        }
    }


}


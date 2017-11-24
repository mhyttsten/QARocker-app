package com.pf.fl.be.extract;

import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import com.pf.fl.be.datamodel.FLA_Cache;
import com.pf.fl.be.datamodel.FLA_Cache_FundInfo;
import com.pf.fl.be.datastore.DS;
import com.pf.fl.be.datamodel.FLA_FundInfo;
import com.pf.shared.MM;
import com.pf.fl.be.util.EE;
import com.pf.shared.IndentWriter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class FLOps1_Ext1_Extract {
    private static final Logger log = Logger.getLogger(FLOps1_Ext1_Extract.class.getName());
    private static final String TAG = MM.getClassName(FLOps1_Ext1_Extract.class.getName());

    private EE mEE;
    private List<String> mIWDebugs = new ArrayList<>();

    public FLOps1_Ext1_Extract(EE ee, IndentWriter iwErrors) {
        mEE = ee;
    }

    public static int SCH_WEEKLY = 1;
    public static int SCH_NOW = 2;
    public void doIt() throws Exception {
        // Production Line
        doItImpl(SCH_WEEKLY, null, false);
    }
    private void doItImpl(
            int schedule,
            List<FLA_FundInfo> extractList,
            boolean debug) throws Exception {

        // mEE.dinfo(log, TAG, "doItImpl");

        // If (Stockholm time)
        // This is a SATURDAY and it is after 16:00
        // Then fetch all that has Update < FRIDAY
        // And that does not have Update_Attempted today (SATURDAY)
        //
        // Else (Stockholm time)
        //    If  Update is < FRIDAY
        //    And Attempted is FRIDAY or Later
        //    And Current Time is TUESDAY 01:00, or WEDNESDAY 01:00, or THURSDAY 01:00
        //    And Attempted is not Current Day
        //    Then fetch all of those
        //
        // Anything not fitting this criteria is interesting (why is it that way?)

        // Set time variables
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(EE.TIMEZONE_STOCKHOLM));
        int nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int nowHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        String nowYYMMDD = MM.getNowAs_YYMMDD(EE.TIMEZONE_STOCKHOLM);
        String nowYYMMDD_HHMMSS = MM.getNowAs_YYMMDD_HHMMSS(EE.TIMEZONE_STOCKHOLM);
        String fridayLastYYMMDD = MM.tgif_getLastFridayTodayExcl(nowYYMMDD);
        String tueYYMMDD = MM.tgif_getNextWeekday(fridayLastYYMMDD, Calendar.TUESDAY);
        String wedYYMMDD = MM.tgif_getNextWeekday(fridayLastYYMMDD, Calendar.WEDNESDAY);
        String thuYYMMDD = MM.tgif_getNextWeekday(fridayLastYYMMDD, Calendar.THURSDAY);

        // Same algorithm for collection and recollecting
        //if ((nowDayOfWeek == Calendar.SATURDAY && nowHourOfDay > 5)
        List<FLA_FundInfo> fiExtractList = new ArrayList<>();

        // Check if it is time to extract
        boolean scheduledPlay =
                schedule == SCH_WEEKLY && (
                (nowDayOfWeek == Calendar.SATURDAY && nowHourOfDay >= 5)
                        || (nowYYMMDD.equals(tueYYMMDD) && nowHourOfDay >= 5)
                        || (nowYYMMDD.equals(wedYYMMDD) && nowHourOfDay >= 5));
        if (!scheduledPlay && schedule != SCH_NOW) {
            mEE.dinfo(log, TAG, "Not time for extraction, schedule: " + schedule + ", " + nowYYMMDD + ", " + MM.tgif_getDayOfWeekStr(nowDayOfWeek) + "@" + nowHourOfDay + ", lastFriday: " + fridayLastYYMMDD);
            return;
        }

        // *** 1: It is time to extract
        mEE.dinfo(log, TAG, "Now: " + nowYYMMDD_HHMMSS + ", " + MM.tgif_getDayOfWeekStr(nowDayOfWeek) + "@" + nowHourOfDay + ", lastFriday: " + fridayLastYYMMDD + ", s: " + schedule);

        // Total number of entries
        int totalCount = ofy()
                .load()
                .type(FLA_FundInfo.class)
                .count();
        ofy().clear();

        // Get fund list
        boolean foundAnything = false;
        int count = 0;
        List<FLA_FundInfo> fundInfoList = new ArrayList<>();
        do {
            foundAnything = false;
            // mEE.dinfo(log, TAG, "...Doing another round, we've fetched: " + count + " so far");
            Query<FLA_FundInfo> query = ofy()
                    .load()
                    .type(FLA_FundInfo.class)
                    .orderKey(false)
                    .offset(count)
                    .limit(50);
            QueryResultIterator<FLA_FundInfo> qri = query.iterator();
            while (qri.hasNext()) {
                count++;
                FLA_FundInfo fi = qri.next();
                fundInfoList.add(fi);
//                    if ((count % 100) == 0) {
//                        mEE.dinfo(log, TAG, "......[" + count + "]: " + fi.mType + "." + fi.mName);
//                    }
                foundAnything = true;
            }
            ofy().clear();
        } while (foundAnything);

        // Classify extract list
        int cntAlreadyExtractedList = 0;
        int cntAlreadyAttemptedList = 0;
        int cntElseList = 0;
        List<FLA_FundInfo> fiInvalidList = new ArrayList<>();
        List<FLA_FundInfo> fiAlreadyAttemptedToday = new ArrayList<>();
        int printedCount = 0;
        while (fundInfoList.size() > 0) {
            FLA_FundInfo fi = fundInfoList.remove(0);
            if (!fi.mIsValid) {
                fiInvalidList.add(fi);
            } else if (fi.mDateYYMMDD_Update_Attempted == null || fi.mDateYYMMDD_Update_Attempted.length() != 6) {
                throw new Exception("Attempted field wrong: " + fi.mType + "." + fi.mName + ", " + fi.mId);
            } else if (fi.mDateYYMMDD_Updated.compareTo(fridayLastYYMMDD) >= 0) {
                cntAlreadyExtractedList++;
            } else if (fi.mDateYYMMDD_Update_Attempted.compareTo(nowYYMMDD) < 0) {
                fiExtractList.add(fi);
            } else if (fi.mDateYYMMDD_Update_Attempted.compareTo(nowYYMMDD) == 0) {
                // fiExtractList.add(fi);
                fiAlreadyAttemptedToday.add(fi);
                cntAlreadyAttemptedList++;
            } else {
                cntElseList++;
            }

        }
        if (cntElseList > 0) {
            mEE.dwarning(log, TAG, "Found: " + cntElseList + " funds with unknown status");
        }

        // *** 2: It is time to extract
        IndentWriter iwExtract = new IndentWriter();
        iwExtract.setFlowChar('.');
        iwExtract.println("*** Extract list created");
        iwExtract.push();
        iwExtract.println("Total funds considered: " + count);
        iwExtract.println("Already extracted this week: " + cntAlreadyExtractedList);
        iwExtract.println("Already attempted extract today: " + cntAlreadyAttemptedList);
        iwExtract.println("To extract: " + fiExtractList.size());
        printListStr(iwExtract, "Funds Invalid", fiInvalidList);
        printListStr(iwExtract, "Funds Alread Attempted Extract Today", fiAlreadyAttemptedToday);
        mEE.dinfo(log, TAG, iwExtract.getString());
        fiAlreadyAttemptedToday.clear();
        fiAlreadyAttemptedToday = null;
        fiInvalidList.clear();
        fiInvalidList = null;

        // Return if there is no work
        if (fiExtractList.size() == 0) {
            mEE.dinfo(log, TAG, "No entries to extract, extractList was empty: " + nowYYMMDD + ", " + MM.tgif_getDayOfWeekStr(nowDayOfWeek) + "@" + nowHourOfDay + ", lastFriday: " + fridayLastYYMMDD);
            return;
        }

        // Iterate over elements to extract, and update
        count = 0;
        int countTotal = 0;
        int cntBecameInvalid = 0;
        int cntUpdated = 0;
        List<FLA_FundInfo> fiNotUpdatedList = new ArrayList<>();
        fiInvalidList = new ArrayList<>();
        boolean continueLoop = true;
        while (fiExtractList.size() > 0 && continueLoop) {
            FLA_FundInfo fi = fiExtractList.remove(0);
            countTotal++;

            FLOps1_Ext1_Extract_SingleFund ef = new FLOps1_Ext1_Extract_SingleFund();
            ef.extractFund(fi, true);
            if (ef.mError) {
                mEE.dwarning(log, TAG, "Encountered error while extracting fund\n" + ef.mIWE.getString());
            }

            switch (ef.mStatusCode) {
                case FLOps1_Ext1_Extract_SingleFund.SC_BECAME_INVALID:
                    cntBecameInvalid++;
                    mEE.dwarning(log, TAG, "Extracted: " + fi.getTypeAndName() + ", became invalid");
                    break;
                case FLOps1_Ext1_Extract_SingleFund.SC_NOT_UPDATED:
                    fiNotUpdatedList.add(fi);
                    break;
                case FLOps1_Ext1_Extract_SingleFund.SC_UPDATED_SUCCESS:
                    cntUpdated++;
                    break;
                default:
                    throw new AssertionError("Unexpected status code: " + ef.mStatusCode
                            + "\n" + ef.mIWE.getString());
            }

            // If we are out of time, then break
            if (!EE.timerContinue()) {
                mEE.dinfo(log, TAG, "Maximum processing time reached for cron job, breaking");
                break;
            }

        }
        iwExtract = new IndentWriter();
        iwExtract.setFlowChar('.');
        iwExtract.println("*** Finished, processed a total of: " + countTotal);
        iwExtract.push();
        iwExtract.println("Updated: " + cntUpdated);
        iwExtract.println("Became invalid: " + cntBecameInvalid);
        iwExtract.println("Not updated: " + fiNotUpdatedList.size());
        iwExtract.println("");

        mEE.dinfo(log, TAG, iwExtract.getString());
        MM.sleepInMS(1000);
    }

    /**
     *
     */
    private static String printListStr(IndentWriter iw, String header, List<FLA_FundInfo> l) throws Exception {
        if (iw == null) {
            iw = new IndentWriter();
        }
        iw.println(header + ". Count: " + l.size() + ".");
        iw.push();
        for (int i=0; i < l.size(); i++) {
            FLA_FundInfo fi = l.get(i);
            iw.print("[" + i + "]: "
                    + fi.mType + "." + fi.mName + " [" + fi.mId + "]"
                    + ", updated: " + fi.mDateYYMMDD_Updated
                    + ", attempted: " + fi.mDateYYMMDD_Update_Attempted);
            if (!fi.mIsValid) {
                iw.println(", invalid code: " + fi.mInvalidCode);
            } else {
                iw.println(", valid");
            }
            iw.push();
            iw.println(fi.mURL);
            iw.pop();
        }
        iw.pop();
        return iw.getString();
    }

    private static List<FLA_FundInfo> getFundInfosByKeys(long... ids) throws Exception {
        List<FLA_FundInfo> fis = new ArrayList<>();
        for (long id: ids) {
            FLA_FundInfo fi = DS.getFundInfo(Key.create(FLA_FundInfo.class, id));
            if (fi == null) {
                throw new Exception("Count not find fund with key: " + id);
            }
            fis.add(fi);
        }
        return fis;
    }

    private static List<FLA_FundInfo> getFundInfosByTypeAndName(String... typeAndNameList) throws Exception {
        List<FLA_FundInfo> fis = new ArrayList<>();
        for (String typeAndName : typeAndNameList) {
            int io = typeAndName.indexOf(".");
            String type = typeAndName.substring(0, io);
            String name = typeAndName.substring(io + 1);
            EE.getEE().dinfo(log, TAG, "getFundInfosByTypeAndName: " + type + "." + name);
            FLA_FundInfo fi = DS.getFundInfoByTypeAndName(type, name);
            if (fi == null) {
                throw new Exception("Count not find fund with type: " + type + ", and name: " + name);
            }
            EE.getEE().dinfo(log, TAG, "...Found");
            fis.add(fi);
        }
        return fis;
    }
}


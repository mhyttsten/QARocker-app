package com.pf.fl.be.servlet;

import com.googlecode.objectify.Ref;
import com.pf.fl.be.datamodel.FLA_FundDPDay;
import com.pf.fl.be.datamodel.FLA_FundInfo;
import com.pf.fl.be.datastore.DS;
import com.pf.fl.be.extract.FLOps1_Ext1_Extract;
import com.pf.fl.be.extract.FLOps1_Ext1_Extract_SingleFund;
import com.pf.fl.be.util.EE;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
//import com.pf.fl.be.FLOps1_Ext1_Extract;
import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CronExtractServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(CronExtractServlet.class.getSimpleName());
    private static final String TAG = CronExtractServlet.class.getSimpleName();

    private EE mEE;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // Test
//        log.info("Log test: info");
//        log.warning("Log test: warning");
//        log.severe("Log test: severe");
//        System.out.println("Log test: System.out");
//        resp.getOutputStream().write((CronExtractServlet.class.getName()
//                + ", executed at: " + new java.util.Date().toString()).getBytes());

        log.info("CronExtractServlet starting");

//        Queue q = QueueFactory.getQueue("getfunds");
//        q.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL)
//                .payload("Adding task from CronExtractServlet: " + new java.util.Date().toString()));

        try {
            mEE = EE.getEE();
            doExtract(req, resp);
            // deleteLastFriday(req, resp);
            // whyWontUpdate(req, resp);
            // printFund(req, resp);
            mEE.deinitialize();
        } catch(Exception exc) {
            if(mEE != null) {
                try {
                    System.out.println("Exception caught: " + exc);
                    exc.printStackTrace();
                    mEE.dsevere(log, TAG, "Exception caught");
                    mEE.dsevere(log, TAG, exc.toString());
                    mEE.dsevere(log, TAG, "Stack trace:\n" + MM.getStackTraceString(exc));
                }
                catch(Exception exc2) {
                    log.severe("Exception caught: " + exc);
                    System.out.println("Exception caught: " + exc2);
                }
            }
            try { mEE.deinitialize(); } catch(Exception exc2) {}
        }
    }

    /**
     *
     */
    public void doExtract(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        mEE.timerStart();

        IndentWriter iwError = new IndentWriter();
        FLOps1_Ext1_Extract extract = new FLOps1_Ext1_Extract(mEE, false, iwError);
        extract.doIt();
        String strError = iwError.getString().trim();
        if (strError.length() > 0) {
            mEE.dinfo(log, TAG, "Errors: " + iwError);
        }

    }

    /**
     *
     */
    public void printFund(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000ZNP7&programid=0000000000";
        FLA_FundInfo fi = DS.getFundInfoByURL(url);
        if (fi == null) {
            mEE.dsevere(log, TAG, "Could not find fund: null");
            return;
        }
//        else {
//            mEE.dinfo(log, TAG, "Found Fund");
//            DS.deleteFundInfo(fi);
//        }
//        mEE.dinfo(log, TAG, "Fund deleted");
//        fi = DS.getFundInfoByURL(url);
//        if (fi == null) {
//            mEE.dinfo(log, TAG, "Ok, fund not found anymore");
//            return;
//        }

        IndentWriter iw = new IndentWriter();
        fi.dumpInfo(iw);
        log.info("Fund found, here is the content\n" + iw.getString());
    }

    /**
     *
     */
    public void whyWontUpdate(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        log.info("*** We are in whyWontUpdate");

        String url = "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000TY9Z&programid=0000000000";
        //        String url = "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000ZNP7&programid=0000000000";
        FLA_FundInfo fi = DS.getFundInfoByURL(url);
        if (fi == null) {
            mEE.dsevere(log, TAG, "Could not find fund: null");
            return;
        }

        log.info("Working with fund: " + fi.getTypeAndName());
        log.info("...updated: " + fi.mDateYYMMDD_Updated);
        log.info("...update attempted: " + fi.mDateYYMMDD_Update_Attempted);

        fi.mDateYYMMDD_Updated = "160812";

        FLOps1_Ext1_Extract_SingleFund ef = new FLOps1_Ext1_Extract_SingleFund();
        ef.extractFund(fi, false);

        log.info("Done extracting");
        log.info("...error: " + ef.mError);
        log.info("...statusCode: " + ef.mStatusCode);
        log.info("...error info:\n" + ef.mIWE.getString());

        IndentWriter iw = new IndentWriter();
        fi.dumpInfo(iw);
        log.info("Updated FundInfo\n" + iw.getString());

        printFund(req, resp);

        log.info("DONE");
    }

    /**
     *
     */
    public void deleteLastFriday(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String nowYYMMDD = MM.getNowAs_YYMMDD(null);
        String lastFridayYYMMDD = MM.tgif_getLastFridayTodayExcl(nowYYMMDD);
        String lastlastFridayYYMMDD = MM.tgif_getLastFridayTodayIncl(lastFridayYYMMDD);

        mEE.dinfo(log, TAG, "Will now delete last Friday: " + lastFridayYYMMDD);
        mEE.dinfo(log, TAG, "......lastlastFriday: " + lastlastFridayYYMMDD);

        String url = "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000I3LA&programid=0000000000";
        FLA_FundInfo fi = DS.getFundInfoByURL(url);
        if (fi == null) {
            mEE.dsevere(log, TAG, "Could not find fund: null");
            return;
        }

        mEE.dinfo(log, TAG, "Found the fund: " + fi.getTypeAndName());
        if (fi.mDateYYMMDD_Updated.equals(lastFridayYYMMDD)) {
            mEE.dsevere(log, TAG, "Last friday: " + lastFridayYYMMDD + ", but fund had: " + fi.mDateYYMMDD_Updated);
            return;
        }

        fi.mDateYYMMDD_Updated = lastlastFridayYYMMDD;
        fi.mDateYYMMDD_Update_Attempted = lastlastFridayYYMMDD;

        // Delete that dpday
        int index = 0;
        while (index < fi.mDPDays.size()) {
            FLA_FundDPDay dpd = fi.mDPDays.get(index);
            if (dpd.mDateYYMMDD.equals(lastFridayYYMMDD)) {
                fi.mDPDays.remove(index);
            } else {
                index++;
            }
        }

        Ref<FLA_FundInfo> ref = DS.saveFundInfoAndUpdateFundIndex(null, fi);
        ofy().clear();
        fi = DS.getFundInfoByURL(url);
        mEE.dinfo(log, TAG, "Saved, and re-read gives my date: " + fi.mDateYYMMDD_Updated);



    }

}

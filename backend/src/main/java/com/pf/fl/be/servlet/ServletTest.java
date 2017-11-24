package com.pf.fl.be.servlet;

import com.pf.fl.be.datamodel.FLA_Cache;
import com.pf.fl.be.datamodel.FLA_Cache_FundInfo;
import com.pf.fl.be.datamodel.FLA_FundInfo;
import com.pf.fl.be.datamodel_raw.FL_MSExtractDetails;
import com.pf.fl.be.datamodel_raw.REFundInfo;
import com.pf.fl.be.datastore.DS;
import com.pf.fl.be.extract.FLOps1_Ext1_Extract;
import com.pf.fl.be.extract.FLOps1_Ext1_Extract_SingleFund;
import com.pf.fl.be.extract.FLOps1_Ext1_HTMLGet;
import com.pf.fl.be.util.Constants;
import com.pf.fl.be.util.EE;
import com.pf.shared.IndentWriter;
import com.pf.shared.MM;
import com.pf.shared.OTuple2G;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import com.pf.fl.be.FLOps1_Ext1_Extract;

public class ServletTest extends HttpServlet {
    private static final Logger log = Logger.getLogger(ServletTest.class.getSimpleName());
    private static final String TAG = ServletTest.class.getSimpleName();

    private EE mEE;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try {
            // collectAVanguard(req, resp);
            // deleteDuplicates(req, resp);
        } catch(Exception exc) {
            exc.printStackTrace();
            String stackTrace = MM.getStackTraceString(exc);
            System.out.println("Exception: " + exc);
            System.out.println("Stack trace:\n" + stackTrace);
            throw new IOException(exc.toString());
        }
    }

    public void deleteDuplicates(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        mEE = EE.getEE();

        long[] ids = new long[] { };
//                5762767671787520L,
//                5726828727631872L,
//                5705162228236288L,
//                5163878774210560L,
//                5133118755307520L,
//                4794601982394368L };

        for (long id: ids) {
            FLA_FundInfo fi = DS.getFundInfoById(id);
            if (fi == null) {
                resp.getWriter().println("No fund for id: " + id);
            } else {
                DS.deleteFundInfo(fi);
                resp.getWriter().println(fi.getTypeAndName() + " DELETED");
            }
        }
    }

    public void collectAVanguard(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        mEE = EE.getEE();

        // Test
        log.info("Log test: info");
        //log.warning("Log test: warning");
        //log.severe("Log test: severe");
        System.out.println("Log test: System.out");
        resp.getOutputStream().write((ServletTest.class.getName()
                + ", v5 executed at: " + new java.util.Date().toString()).getBytes());
        log.info("That was it from ServletTest");

        List<FLA_Cache_FundInfo> l = FLA_Cache.cacheFundInfosByTypeOrNull(Constants.ACCOUNT_TYPE_VANGUARD);
        log.info("Total # funds: " + l.size());
        for (FLA_Cache_FundInfo cfi: l) {
            if (!cfi.mName.toLowerCase().contains("etf")) {
                continue;
            }
//            if (!cfi.mName.contains("Vanguard Mega Cap Value Index Fund ETF Shares")) {
//                continue;
//            }
            FLA_FundInfo fi = DS.getFundInfoById(cfi.mId);

            // Collect the data
            FLOps1_Ext1_Extract_SingleFund esf = new FLOps1_Ext1_Extract_SingleFund();
            esf.extractFund(fi, true);
            if (esf.mError) {
                log.warning("ERROR WHILE EXTRACTING FUND: " + fi.getTypeAndName() + "\n"
                    + esf.mIWE.getString());
            } else {
                IndentWriter iw = new IndentWriter();
                fi.dumpInfo(iw);
                log.info("EXTRACTED SUCCESSFULLY: " + fi.getTypeAndName() + "\n" + iw.getString());
            }
//            break;
        }


//        checkURL();
    }



    public void checkURL() {
        try {
            checkURLImpl();
        } catch(Exception exc) {
            System.out.println("Exception: " + exc.getMessage());
            System.out.println(MM.getStackTraceString(exc));
        }
    }
    private void checkURLImpl() throws Exception {
        IndentWriter iw = new IndentWriter();

        String url = "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000I3LA&programid=0000000000";
        byte[] pageDataUncompressed = FLOps1_Ext1_HTMLGet.htmlGet(
                mEE,
                iw,
                url,
                5000,
                6);

        String htmlString = MM.newString(pageDataUncompressed, EE.ENCODING_FILE_READ);
        OTuple2G<Integer, REFundInfo> reFundInfo = FL_MSExtractDetails.extractFundDetails(
                Constants.ACCOUNT_TYPE_SEB,
                url,
                htmlString,
                iw);
        iw = new IndentWriter();
        reFundInfo._o2.addString(iw);
        System.out.println(iw.getString());
    }

//    private void checkURLImpl() throws Exception {
//        IndentWriter iw = new IndentWriter();
//
//        String url = "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000I3LA&programid=0000000000";
//
//        byte[] pageContent = MM.getURLContentBA(url);
//        // MM.fileWrite("pageContent.html", pageContent);
//        String htmlString = MM.newString(pageContent, EE.ENCODING_FILE_READ);
//        REFundInfo reFundInfo = FL_MSExtractDetails.extractFundDetails(
//                Constants.ACCOUNT_TYPE_SEB,
//                url,
//                htmlString,
//                iw);
//        iw = new IndentWriter();
//        reFundInfo.addString(iw);
//        System.out.println("**** RESULT IS:\n" + iw.getString());
//    }



}

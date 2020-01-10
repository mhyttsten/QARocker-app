package com.pf.fl.be.servlet;

import com.pf.fl.be.extract.FLOps1_Ext1_Extract_New;
import com.pf.mr.be.Constants;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CronExtractServlet_New extends HttpServlet {
    private static final Logger log = Logger.getLogger(CronExtractServlet_New.class.getSimpleName());
    private static final String TAG = CronExtractServlet_New.class.getSimpleName();

    //------------------------------------------------------------------------
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        log.info("CronExtractServlet starting");
        try {
//            IndentWriter iw = new IndentWriter();
//            iw.println("Parameters");
//            Enumeration e = req.getParameterNames();
//            while(e.hasMoreElements()) {
//                String p = e.nextElement().toString();
//                iw.println("..." + p + ": " + req.getParameter(p));
//            }
//            log.info(iw.getString());

            doExtract(req, resp);
        } catch(Exception exc) {
            log.severe("Exception caught: " + exc.getMessage());
            log.severe("Stack trace:\n" + MM.getStackTraceString(exc));
        }
    }

    //------------------------------------------------------------------------
    public static final String P_ignoreSchedule = "ignoreSchedule";
    public static final String P_doPostProcessing = "doPostProcessing";
    public static final String P_timeInSBeforeDeadline = "timeInSBeforeDeadline";

    public static void doExtract(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        boolean ignoreSchedule = false;
        boolean doPostProcessing = false;
        int timeInSBeforeDeadline = 0;

        String s = req.getParameter(P_ignoreSchedule);
        if (s != null && s.trim().toLowerCase().equals("true"))  {
            ignoreSchedule = true;
        }
        s = req.getParameter(P_doPostProcessing);
        if (s != null && s.trim().toLowerCase().equals("true"))  {
            doPostProcessing = true;
        }
        s = req.getParameter(P_timeInSBeforeDeadline);
        if (s != null)  {
            try {
                timeInSBeforeDeadline = Integer.parseInt(s);
            } catch(Exception exc) {
                log.severe("timeInSBeforeDeadline could not be converted to int: " + s);
                return;
            }
        }

        log.info("Parameters being passed");
        log.info("...ignoreSchedule: " + ignoreSchedule);
        log.info("...doPostProcessing: " + doPostProcessing);
        log.info("...timeInSBeforeDeadline: " + timeInSBeforeDeadline);

        if (ignoreSchedule) {
            String date = MM.getNowAs_YYMMDD(null);
            date = date.substring(2);
            log.info("The date is : " + date);
            if (date.equals("0101")) {
                log.info("It is the 1st of January and we're not doing the !cronInitiated");
                log.info("This execution only happens because we are forced to do 1 scheduled entry in cron.xml");
                return;
            }
        }

        log.info("Will now call extraction logic");
        FLOps1_Ext1_Extract_New extract = new FLOps1_Ext1_Extract_New(
                null,
                ignoreSchedule,
                doPostProcessing,
                timeInSBeforeDeadline,
                null,
                false);
        extract.doIt();
    }
}

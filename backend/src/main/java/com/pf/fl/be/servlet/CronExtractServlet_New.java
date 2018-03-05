package com.pf.fl.be.servlet;

import com.pf.fl.be.extract.FLOps1_Ext1_Extract_New;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CronExtractServlet_New extends HttpServlet {
    private static final Logger log = Logger.getLogger(CronExtractServlet_New.class.getSimpleName());
    private static final String TAG = CronExtractServlet_New.class.getSimpleName();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        log.info("CronExtractServlet starting");

        try {
            doExtract(req, resp);
        } catch(Exception exc) {
            System.out.println("Exception caught: " + exc);
            exc.printStackTrace();
            log.severe("Exception caught");
            log.severe(exc.toString());
            log.severe("Stack trace:\n" + MM.getStackTraceString(exc));
        }
    }

    /**
     *
     */
    public void doExtract(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        IndentWriter iwError = new IndentWriter();
        FLOps1_Ext1_Extract_New extract = new FLOps1_Ext1_Extract_New(true, iwError);
        extract.doIt();
        String strError = iwError.getString().trim();
        if (strError.length() > 0) {
            log.info("Errors: " + iwError);
        }

    }
}

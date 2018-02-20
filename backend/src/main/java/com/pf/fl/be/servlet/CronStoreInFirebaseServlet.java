package com.pf.fl.be.servlet;

import com.pf.fl.be.datamodel.GotoFirebase;
import com.pf.fl.be.util.EE;
import com.pf.shared.utils.MM;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import com.pf.fl.be.FLOps1_Ext1_Extract;

public class CronStoreInFirebaseServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(CronStoreInFirebaseServlet.class.getSimpleName());
    private static final String TAG = CronStoreInFirebaseServlet.class.getSimpleName();

    private EE mEE;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        log.info("CronStoreInFirebaseServlet starting");

        try {
            mEE = EE.getEE();
            doIt(req, resp);
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
        log.info("CronStoreInFirebaseServlet returning");

    }

    public void doIt(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        log.info("Now getting all funds");
        GotoFirebase g2Firebase = new GotoFirebase();
        g2Firebase.doIt();
    }
}

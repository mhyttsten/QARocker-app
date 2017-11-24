package com.pf.fl.be.servlet;

import com.pf.fl.be.util.EE;
import com.pf.shared.MM;
import com.pf.fl.be.datamodel.FLA_Cache;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// http://1.mhyttsten1.appspot.com/
// http://mhyttsten1.appspot.com/

public class CronMaintenanceServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(CronMaintenanceServlet.class.getName());
    private static final String TAG = MM.getClassName(CronMaintenanceServlet.class.getName());

    private EE mEE;

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("CronMaintenanceServlet, we are in doGet now");

        try {
            mEE = EE.getEE();
            FLA_Cache.performMaintenance();
        } catch(Exception exc) {
            log.severe("Exception caught: " + exc.toString());
            log.severe(FLA_Cache.updateCacheStatus());
            log.severe(MM.getStackTraceString(exc));
        }
    }
}

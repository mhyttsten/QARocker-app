package com.pf.fl.be.servlet;

import com.pf.fl.be.extract.GCSWrapper;
import com.pf.shared.Constants;
import com.pf.shared.datamodel.DB_FundInfo;
import com.pf.shared.datamodel.D_FundInfo_Serializer;
import com.pf.shared.extract.ExtractFromHTML_Helper;
import com.pf.shared.utils.MM;
import com.pf.shared.datamodel.D_FundInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletTest extends HttpServlet {
    private static final Logger log = Logger.getLogger(ServletTest.class.getSimpleName());
    private static final String TAG = ServletTest.class.getSimpleName();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            log.info("Now executing servletTest");
            doit();
        } catch(Exception exc) {
            exc.printStackTrace();
            String stackTrace = MM.getStackTraceString(exc);
            System.out.println("Exception: " + exc);
            System.out.println("Stack trace:\n" + stackTrace);
            log.severe("ERROR, STACK TRACE:\n" + stackTrace);
            throw new IOException(exc.toString());
        }
    }

    public void doit() throws Exception {
    }


    public void createSmallSet() throws Exception {
        log.info("createSmallSet");
        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        List<D_FundInfo> funds = D_FundInfo_Serializer.decrunchFundList(fundInfoBA);
        List<D_FundInfo> r = new ArrayList<>();
        int ppmC = 0;
        int ppmIC = 0;
        int sebC = 0;
        int sebIC = 0;
        int sppC = 0;
        int sppIC = 0;
        int vgdC = 0;
        int vgdIC = 0;

        for (D_FundInfo fi: funds) {
            if (fi._type.equals(D_FundInfo.TYPE_PPM) && ppmC <= 4) {
                ppmC++;
                if (ppmIC == 0) {
                    fi._errorCode = 998;
                    log.info("PPM, setting ec: 998, ppmIC is: " + ppmIC);
                }
                else if (ppmIC == 1) {
                    log.info("PPM, setting ec: 999, ppmIC is: " + ppmIC);
                    fi._errorCode = 999;
                    log.info("PPM, setting invalid, ppmIC is: " + ppmIC);
                    log.info("invalid fund is: " + fi.getTypeAndName());
                    fi._isValid = false;
                }
                ppmIC++;
                r.add(fi);
            }
            if (fi._type.equals(D_FundInfo.TYPE_SEB) && sebC <= 4) {
                sebC++;
                if (sebIC <= 1) {
                    fi._errorCode = D_FundInfo.IC_COM_NO_DECODABLE_DATA;
                    if (sebIC == 1) {
                        fi._isValid = false;
                    }
                    sebIC++;
                }
                r.add(fi);
            }
            if (fi._type.equals(D_FundInfo.TYPE_SPP) && sppC <= 4) {
                sppC++;
                if (sppIC <= 1) {
                    fi._errorCode = D_FundInfo.IC_COM_NO_DECODABLE_DATA;
                    if (sppIC == 1) {
                        fi._isValid = false;
                    }
                    sppIC++;
                }
                r.add(fi);
            }
            if (fi._type.equals(D_FundInfo.TYPE_VANGUARD) && vgdC <= 4) {
                vgdC++;
                if (vgdIC <= 1) {
                    fi._errorCode = D_FundInfo.IC_COM_NO_DECODABLE_DATA;
                    if (vgdIC == 1) {
                        fi._isValid = false;
                    }
                    vgdIC++;
                }
                r.add(fi);
            }
        }
        log.info("Result r has size: " + r.size());
        log.info("Now saving: " + Constants.FUNDINFO_DB_TEST_JSP);
        byte[] data = D_FundInfo_Serializer.crunchFundList(r);
        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_TEST_JSP, data);
        log.info("Done, exiting");
    }
}

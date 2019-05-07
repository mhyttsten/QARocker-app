package com.pf.fl.be.servlet;

import com.pf.fl.be.extract.GCSWrapper;
import com.pf.shared.Constants;
import com.pf.shared.datamodel.DB_FundInfo;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo_Serializer;
import com.pf.shared.datamodel.D_FundInfo_Validator;
import com.pf.shared.extract.ExtractFromHTML_Helper;
import com.pf.shared.utils.Compresser;
import com.pf.shared.utils.HtmlRetriever;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.OTuple2G;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletTest extends HttpServlet {
    private static final Logger log = Logger.getLogger(ServletTest.class.getSimpleName());
    private static final String TAG = ServletTest.class.getSimpleName();

    private static HttpServletRequest _req;
    private static HttpServletResponse _resp;

    public void println(String s) throws IOException {
        _resp.getOutputStream().write((s + "<br>").getBytes(Constants.ENCODING_FILE_WRITE));
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            log.info("Now executing servletTest");
            _req = req;
            _resp = resp;
            processFund();
        } catch(Exception exc) {
            exc.printStackTrace();
            String stackTrace = MM.getStackTraceString(exc);
            System.out.println("Exception: " + exc);
            System.out.println("Stack trace:\n" + stackTrace);
            log.severe("ERROR, STACK TRACE:\n" + stackTrace);
            throw new IOException(exc.toString());
        }
    }

    public void processFund() throws Exception {
        log.info("*** processFund started");

        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        DB_FundInfo.initialize(fundInfoBA, true);
        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();
        int count = 0;
        for (D_FundInfo fi : fis) {
//            count++;
        }
        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);

        log.info("*** processFund done, updated: " + count);
    }

//    public void setValidAndLastExtractionAttemptedToLastFriday(D_FundInfo fi) throws Exception {
//        String date = MM.getNowAs_YYMMDD()
//        fi._dateYYMMDD_Update_Attempted = MM.tgif_getLastFridayTodayIncl()
//
//    }

    public void processVGDNameChange() throws Exception {
        log.info("*** processFund started");

        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
        DB_FundInfo.initialize(fundInfoBA, true);
        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();
        List<D_FundInfo> fisVGD = DB_FundInfo.getFundInfosByType(D_FundInfo.TYPE_VANGUARD);

        int count = 0;
        for (D_FundInfo fi: fisVGD) {
            if (!fi._type.equals(D_FundInfo.TYPE_VANGUARD))
                throw new AssertionError("Not VGD type");
            int io1 = fi._nameMS.lastIndexOf("(");
            int io2 = fi._nameMS.lastIndexOf(")");
            if (io1 == -1 || io2 == -1 || io1 + 1 >= fi._nameMS.length()) {
                MM.writeBrowser(_resp.getOutputStream(),"Could not find ticker fog: " + fi._nameMS);
                return;
            }
            String ticker = fi._nameMS.substring(io1+1, io2);
            if (ticker.startsWith("V")) {
                count++;
                if (fi._nameMS.trim().startsWith("Vanguard")) {
                    MM.writeBrowser(_resp.getOutputStream(), "*** Already started with Vanguard: " + fi._nameMS + "<br>");
                } else {
                    fi._nameMS = "Vanguard " + fi._nameMS;
                    MM.writeBrowser(_resp.getOutputStream(), "New name: " + fi._nameMS + "<br>");
                }
            }
        }
        MM.writeBrowser(_resp.getOutputStream(), "Done with: " + count + " funds");

//        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
//        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
//        log.info("*** processFund done, updated: " + count);
    }

//    public void getURL() throws Exception {
//        String url = "http://www.google.com";
//        String url = "https://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000HLO5&programid=0000000000";
//        log.info("*** Get URL: ");
//        IndentWriter iw = new IndentWriter();
//        byte[] data = HtmlRetriever.htmlGet(iw, url, 5000, 4);
//        if (data == null) {
//            log.severe("data == null");
//        } else {
//            log.info("data.length: " + data.length);
//        }
//        log.info(iw.getString());
//    }
//
//    public void updateFields() throws Exception {
//        log.info("*** Fixing names starting");
//
//        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
//        DB_FundInfo.initialize(fundInfoBA);
//        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();
//        int found = 0;
//        int not_found = 0;
//        for (D_FundInfo fi: fis) {
//            List<D_FundDPDay> dpds = fi._dpDays;
//            fi._isValid = true;
//            fi._errorCode = D_FundInfo.IC_NO_ERROR;
//            fi._dateYYMMDD_Update_Attempted = "180317";
//            fi._dateYYMMDD_Updated = "180309";
//            if(fi._dpDays.size() > 0 && fi._dpDays.get(0)._dateYYMMDD.equals("180316")) {
//                fi._dpDays.remove(0);
//            }
//        }
////        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
////        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
////        log.info("*** RESULT, found: " + found + ", nf: " + not_found);
//    }
//
//    public void extractFund() throws Exception {
//        log.info("*** extractFund started");
//
//        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
//        DB_FundInfo.initialize(fundInfoBA);
//        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();
//
//        for (D_FundInfo fi: fis) {
//            if (fi._url.equals("http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00018UZ6&programid=0000000000")) {
//                log.info("...Found the fund");
//                IndentWriter iw = new IndentWriter();
//                ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
//                int rc = eh.extractFundDetails(fi, iw);
//                println(String.valueOf(rc));
//                println(iw.getString());
//            }
//        }
////        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
////        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
//    }
//
//    public void fixDates() throws Exception {
//        log.info("*** Fixing dates starting");
//
//        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
//        DB_FundInfo.initialize(fundInfoBA);
//        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();
//
//        log.info("Number of fis: " + fis.size());
//        D_FundInfo_Validator fiv = new D_FundInfo_Validator(fis);
//        fiv .process();
//        if (fiv._error) {
//            log.info(" X *** Error for funds\n" + fiv._iwErrors.getString());
//            return;
//        } else {
//            log.info("X *** No errors for funds");
//        }
//
//        for (D_FundInfo fi: fis) {
//            fi._dateYYMMDD_Update_Attempted = "180317";
//        }
//
//        D_FundInfo_Validator fiv2 = new D_FundInfo_Validator(fis);
//        fiv2.process();
//        if (fiv2._error) {
//            log.info(" XY *** Error for funds\n" + fiv2._iwErrors.getString());
//            return;
//        } else {
//            log.info("XY *** No errors for funds");
//        }
//
////        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
////        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
//    }
//
//    public void fixDates2() throws Exception {
//        log.info("*** Fixing dates starting");
//
//        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
//        DB_FundInfo.initialize(fundInfoBA);
//        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();
//
//        D_FundInfo_Validator fiv = new D_FundInfo_Validator(fis);
//        fiv.process();
//        if (fiv._error) {
//            log.info("1 *** Error for funds\n" + fiv._iwErrors.getString());
//        } else {
//            log.info("2 *** No errors for funds");
//        }
//
//        int dpds_09_dups = 0;
//        int dpds_09_single = 0;
//        int dpd_empty_rmed = 0;
//        for (D_FundInfo fi: fis) {
//            List<D_FundDPDay> dpds = fi._dpDays;
//            int index = 0;
//            List<D_FundDPDay> dpds_09 = new ArrayList<>();
//            while (index < dpds.size()) {
//                D_FundDPDay dpd = dpds.get(index);
//                if (dpd._dateYYMMDD == null || dpd._dateYYMMDD.length() != 6
//                        || dpd._dateYYMMDD_Actual == null || dpd._dateYYMMDD_Actual.length() != 6) {
//                    dpd_empty_rmed++;
//                    dpds.remove(index);
//                }
//                else if (dpd._dateYYMMDD.equals("180309")) {
//                    dpds_09.add(dpd);
//                    dpds.remove(index);
//                } else {
//                    index++;
//                }
//            }
//            if (dpds_09.size() == 1) {
//                dpds_09_single++;
//                dpds.add(dpds_09.get(0));
//            } else if (dpds_09.size() > 1){
//                log.info("Have multiple 180309 for: " + fi.getTypeAndName());
//                dpds_09_dups++;
//
//                D_FundDPDay closest = null;
//                for (D_FundDPDay dpd: dpds_09) {
//                    if (closest == null) {
//                        closest = dpd;
//                    } else {
//                        int diff1 = MM.tgif_dayCountDiff("180309", closest._dateYYMMDD_Actual);
//                        int diff2 = MM.tgif_dayCountDiff("180309", dpd._dateYYMMDD_Actual);
//                        if (Math.abs(diff1) > diff2) {
//                            closest = dpd;
//                        }
//                    }
//                }
//
//                if (closest == null) {
//                    log.info("*** Could not find 09 nor 12 solution for:\n" + fi.toString());
//                    for (D_FundDPDay dpdNotFound: dpds_09) {
//                        log.info("...d: " + dpdNotFound._dateYYMMDD + ", da: " + dpdNotFound._dateYYMMDD_Actual);
//                    }
//                    throw new AssertionError("Irrecoverable");
//                }
//
//                fi._dpDays.add(closest);
//                final D_FundInfo fi2 = fi;
//                Collections.sort(fi2._dpDays, new Comparator<D_FundDPDay>() {
//                    @Override
//                    public int compare(D_FundDPDay o1, D_FundDPDay o2) {
//                        if (o1 == null || o2 == null
//                                || o1._dateYYMMDD == null || o2._dateYYMMDD == null) {
//                            throw new AssertionError("Dpdays was null: " + fi2.toString());
//                        }
//                        return -o1._dateYYMMDD.compareTo(o2._dateYYMMDD);
//                    }
//                });
//                log.info("...Finished_with\n" + fi.toString());
//                log.info("\n\n");
//            }
//        }
//
//        log.info("2 *** Number of singles: " + dpds_09_single);
//        log.info("2 *** Number of 09 dups: " + dpds_09_dups);
//        log.info("2 *** Number of empties: " + dpd_empty_rmed);
//
//        D_FundInfo_Validator fiv2 = new D_FundInfo_Validator(fis);
//        fiv2.process();
//        if (fiv2._error) {
//            log.info("2 *** Error for funds\n" + fiv2._iwErrors.getString());
//        } else {
//            log.info("2 *** No errors for funds");
//        }
//
////        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
////        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
//    }
//
//
//    public void addNameOrig() throws Exception {
//        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
//        DB_FundInfo.initialize(fundInfoBA);
//        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();
//        for (D_FundInfo fi: fis) {
//            if (!fi._type.equals(D_FundInfo.TYPE_SEB)) {
//                fi._nameOrig = fi._nameMS;
//            }
//        }
//        byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
//        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
//    }
//
//
//    public void changeFundInfo() throws Exception {
//        String fund = "SEB.Vontobel Commodity H Hedged SEK";
//        log.info("Will now change name of fund: " + fund);
//        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
//        DB_FundInfo.initialize(fundInfoBA);
//        List<D_FundInfo> fis = DB_FundInfo.getAllFundInfos();
//        for (D_FundInfo fi: fis) {
//            if (fi.getTypeAndName().equals(fund)) {
//                log.info("Found it: " + fi.getTypeAndName());
//                fi._nameOrig = "Vontobel Harcourt Comm. H SEK";
//                byte[] data = D_FundInfo_Serializer.crunchFundList(fis);
//                GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
//                log.info("Saved and returning");
//                return;
//            }
//        }
//    }
//
//    public void extractAFund(String fundTypeAndName) throws Exception {
//        log.info("extractAFund. Will now extract fund: " + fundTypeAndName);
//        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
//        List<D_FundInfo> funds = D_FundInfo_Serializer.decrunchFundList(fundInfoBA);
//        for (D_FundInfo fi: funds) {
//            if (fi.getTypeAndName().equals(fundTypeAndName)) {
//                log.info("Before extract:\n" + fi.toString());
//                ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
//                IndentWriter iwd = new IndentWriter();
//                int rc = eh.extractFundDetails(fi, iwd);
//                log.info("RC: " + rc);
//                log.info("After extract, debug:\n" + iwd.getString());
//            }
//        }
//        log.info("extractAFund, all done will exit");
//
//    }
//
//
//    public void createSmallSet() throws Exception {
//        log.info("createSmallSet");
//        byte[] fundInfoBA = GCSWrapper.gcsReadFile(Constants.FUNDINFO_DB_MASTER_BIN);
//        List<D_FundInfo> funds = D_FundInfo_Serializer.decrunchFundList(fundInfoBA);
//        List<D_FundInfo> r = new ArrayList<>();
//        int ppmC = 0;
//        int ppmIC = 0;
//        int sebC = 0;
//        int sebIC = 0;
//        int sppC = 0;
//        int sppIC = 0;
//        int vgdC = 0;
//        int vgdIC = 0;
//
//        for (D_FundInfo fi: funds) {
//            if (fi._type.equals(D_FundInfo.TYPE_PPM) && ppmC <= 4) {
//                ppmC++;
//                if (ppmIC == 0) {
//                    fi._errorCode = 998;
//                    log.info("PPM, setting ec: 998, ppmIC is: " + ppmIC);
//                }
//                else if (ppmIC == 1) {
//                    log.info("PPM, setting ec: 999, ppmIC is: " + ppmIC);
//                    fi._errorCode = 999;
//                    log.info("PPM, setting invalid, ppmIC is: " + ppmIC);
//                    log.info("invalid fund is: " + fi.getTypeAndName());
//                    fi._isValid = false;
//                }
//                ppmIC++;
//                r.add(fi);
//            }
//            if (fi._type.equals(D_FundInfo.TYPE_SEB) && sebC <= 4) {
//                sebC++;
//                if (sebIC <= 1) {
//                    fi._errorCode = D_FundInfo.IC_COM_NO_DECODABLE_DATA;
//                    if (sebIC == 1) {
//                        fi._isValid = false;
//                    }
//                    sebIC++;
//                }
//                r.add(fi);
//            }
//            if (fi._type.equals(D_FundInfo.TYPE_SPP) && sppC <= 4) {
//                sppC++;
//                if (sppIC <= 1) {
//                    fi._errorCode = D_FundInfo.IC_COM_NO_DECODABLE_DATA;
//                    if (sppIC == 1) {
//                        fi._isValid = false;
//                    }
//                    sppIC++;
//                }
//                r.add(fi);
//            }
//            if (fi._type.equals(D_FundInfo.TYPE_VANGUARD) && vgdC <= 4) {
//                vgdC++;
//                if (vgdIC <= 1) {
//                    fi._errorCode = D_FundInfo.IC_COM_NO_DECODABLE_DATA;
//                    if (vgdIC == 1) {
//                        fi._isValid = false;
//                    }
//                    vgdIC++;
//                }
//                r.add(fi);
//            }
//        }
//        log.info("Result r has size: " + r.size());
//        log.info("Now saving: " + Constants.FUNDINFO_DB_TEST_JSP);
//        byte[] data = D_FundInfo_Serializer.crunchFundList(r);
//        GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_TEST_JSP, data);
//        log.info("Done, exiting");
//    }
}

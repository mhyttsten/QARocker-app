package com.pf.fl.be.jsphelper;

import com.pf.shared.datamodel.DB_FundInfo;
import com.pf.fl.be.extract.GCSWrapper;
import com.pf.shared.Constants;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;
import com.pf.shared.datamodel.D_FundInfo;

import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by magnushyttsten on 11/8/14.
 */
public class JSP_Helper_fundURLData {
    private static final Logger log = Logger.getLogger(JSP_Helper_fundURLData.class.getName());
    private static final String TAG = MM.getClassName(JSP_Helper_fundURLData.class.getName());

    /**
     *
     */
    public static final int FL_MODE_EMPTIES = 1;
    public static final int FL_MODE_FILL_EXISTING = 2;
    public static final int FL_MODE_FILL_REPORT = 3;

    public static String urlControl_DisplayFundEmptiesAndList(List<D_FundInfo> l) throws Exception {
        StringBuilder strb = new StringBuilder();

        int count = 0;
        for (int i = 0; i < 5; i++) {
            urlControl_DisplayFund(true, null, false, false, strb, FL_MODE_EMPTIES, null, count, -1);
            count++;
        }

        for (int i = 0; i < l.size(); i++) {
            D_FundInfo cfi = l.get(i);
            urlControl_DisplayFund(true, null, false, false, strb, FL_MODE_FILL_EXISTING, cfi, count, i+1);
            count++;
        }

        return strb.toString();
    }

    /**
     *
     */
    public static String urlControl_VerifyOrExecute(boolean isVerify, HttpServletRequest r) throws Exception {
        StringBuilder strb = new StringBuilder();
        List<D_FundInfo> allFunds = DB_FundInfo.getAllFundInfos();

        int count = 0;
        int i = -1;
        boolean shownInserts = false;
        boolean shownUpdates = false;
        while (true) {
            i++;
            // ee.dinfo(log, TAG, "At index: " + i + "................................................");

            StringBuilder tmpsb = new StringBuilder();
            Enumeration<String> pnames = r.getParameterNames();
            while (pnames.hasMoreElements()) {
                String s = pnames.nextElement();
                String[] sparts = s.split("_");
                if (sparts.length >= 2 && sparts[2].equals(String.valueOf(i))) {
                    tmpsb.append(s + "\n");
                }
            }
            // ee.dinfo(log, TAG, "Parameter names:\n" + tmpsb.toString());

            String p_idStr = JSP_Constants.PARAM2_ID_ + String.valueOf(i);
            String v_idStr = r.getParameter(p_idStr);
            String p_delete = JSP_Constants.PARAM2_DELETE_ + String.valueOf(i);
            String v_delete = r.getParameter(p_delete);
            String p_setvalid = JSP_Constants.PARAM2_SETVALID_ + String.valueOf(i);
            String v_setvalid = r.getParameter(p_setvalid);
            String p_type = JSP_Constants.PARAM2_TYPE_ + String.valueOf(i);
            String v_type = r.getParameter(p_type);
            String p_name = JSP_Constants.PARAM2_NAME_ + String.valueOf(i);
            String v_name = r.getParameter(p_name);
            String p_url = JSP_Constants.PARAM2_URL_ + String.valueOf(i);
            String v_url = r.getParameter(p_url);

            if (v_idStr == null && v_delete == null && v_type == null && v_setvalid == null && v_name == null && v_url == null) {
                return strb.toString();
            }

            /* ee.dinfo(log, TAG, "urlControl_VerifyOrExecute, input parameters\n"
                    + " id: " + v_idStr
                    + " delete: " + v_delete
                    + " setvalid: " + v_setvalid
                    + " type: " + v_type
                    + " name: " + v_name
                    + " url: " + v_url);
            */

            Long v_id = null;
            if (v_idStr != null && v_idStr.trim().length() > 0 && !v_idStr.trim().equals("null")) {
                try {
                    v_id = new Long(Long.parseLong(v_idStr.trim()));
                } catch (Exception exc) {
                    throw new Exception("Could not convert to number from parameter: " + p_idStr + ", with value: " + v_idStr);
                }
            }

            OTuple2G<D_FundInfo, D_FundInfo> fi = getD_FundInfo(allFunds, v_id, v_type, v_name, v_url, v_delete, v_setvalid);
            if (fi == null) {
                // ee.dinfo(log, TAG, "FundInfo is null, no more work to do at index: " + i);
                continue;
            }

            IndentWriter iw = new IndentWriter();
            fi._o1.dumpInfo(iw);
            // ee.dinfo(log, TAG, "FundInfo is: " + iw.getString());

            if (isVerify && v_id == null && !shownInserts) {
                strb.append("<tr><td colspan=\"6\"><b>New funds to be inserted</b></td></tr>\n");
                shownInserts = true;
            } else if (isVerify && v_id != null && !shownUpdates) {
                strb.append("<tr><td colspan=\"6\"><b>Updates to funds</b></td></tr>\n");
                shownUpdates = true;
            }

            if (v_id == null) {
                if (isVerify) {
                    D_FundInfo fiDB1 = DB_FundInfo.getFundInfosByTypeAndName(fi._o2._type, fi._o2._nameMS);
                    D_FundInfo fiDB2 = DB_FundInfo.getFundInfosByTypeAndURL(fi._o2._type, fi._o2._url);
                    if (fiDB1 != null || fiDB2 != null) {
                        String text = "";
                        if (fiDB1 != null) {
                            text = "Type.Name exists will not insert: ";
                        } else if (fiDB2 != null) {
                            text = "URL exists will not insert: ";
                        }
                        strb.append("<tr bgcolor=\"#FF0000\"><td colspan=\"5\">");
                        strb.append(text + fi._o2._type + "." + fi._o2._nameMS);
                        strb.append("</td></tr>\n");
                    } else {
                        urlControl_DisplayFund(true, "", false, false, strb, FL_MODE_FILL_EXISTING, fi._o2, count, -1);
                        count++;
                    }
                } else {
                    log.info("Will now save new entity: " + fi._o1 + "." + fi._o2);
                    byte[] data = DB_FundInfo.addFundInfo(fi._o1);
                    GCSWrapper.gcsWriteFile(Constants.FUNDINFO_DB_MASTER_BIN, data);
                }

            } else {
                String text = null;
                boolean isDelete = false;
                boolean isValid = false;
                if (v_delete != null && v_delete.trim().length() > 0) {
                    isDelete = true;
                    text = "Delete";
                    // ee.dinfo(log, TAG, "delete is requested");
                } else if (v_setvalid != null && v_setvalid.trim().length() > 0) {
                    isValid = true;
                    text = "Set Valid";
                    // ee.dinfo(log, TAG, "set valid is requested");
                } else {
                    text = "Updating name or URL";
                }

                if (isVerify) {
                    urlControl_DisplayFund(true, text, isValid, isDelete, strb, FL_MODE_FILL_EXISTING, fi._o2, count, -1);
                    count++;
                } else {
                    if (isDelete) {
                        log.warning("Will now delete entity: " + fi._o1._type + "." + fi._o1._nameMS);
                        DB_FundInfo.deleteFundInfo(fi._o1);
                        log.warning("...delete performed");
                    } else if (isValid) {
                        fi._o1._isValid = true;
                        fi._o1._errorCode = D_FundInfo.IC_NO_ERROR;
                        log.info("Will now set entity to valid: " + fi._o1._type + "." + fi._o1._nameMS);
                        GCSWrapper.gcsWriteBlob(Constants.FUNDINFO_DB_MASTER_BIN, DB_FundInfo.getFundInfosData());
                    } else {
                        log.info("Will update entity: " + fi._o1._type + "." + fi._o1._nameMS + ", url: " + fi._o1._url);
                        GCSWrapper.gcsWriteBlob(Constants.FUNDINFO_DB_MASTER_BIN, DB_FundInfo.getFundInfosData());
                    }
                }
            }
        }
    }

    /**
     *
     */
    private static void urlControl_DisplayFund(
            boolean doRow,
            String text,
            boolean hiddenSetValid,
            boolean hiddenDelete,
            StringBuilder strb,
            int mode,
            D_FundInfo elem,
            int count,
            int index) throws Exception {

        if (doRow) {
            if (elem != null && !elem._isValid && text == null) {
                strb.append("<tr bgcolor=\"#FF0000\">\n");
            } else {
                strb.append("<tr>\n");
            }
        }

        if (mode == FL_MODE_FILL_EXISTING) {
            if (elem == null) {
                log.severe("Elem is null!!!");
            }
            strb.append("<input type=\"hidden\" name=\"" + JSP_Constants.PARAM2_ID_ + String.valueOf(count) + "\" value=\"" + elem.getTypeAndName().hashCode() + "\"/>");
        }

        if (text != null) {
            strb.append("<td colspan=\"4\">" + text + "</td>");
            if (hiddenDelete) {
                strb.append("<input type=\"hidden\" name=\"" + JSP_Constants.PARAM2_DELETE_ + String.valueOf(count) + "\" value=\"" + elem.getTypeAndName().hashCode() + "\"/>");
            }
            if (hiddenSetValid) {
                strb.append("<input type=\"hidden\" name=\"" + JSP_Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + elem.getTypeAndName().hashCode() + "\"/>");
            }
        } else if (mode == FL_MODE_FILL_EXISTING) {
            strb.append("<td width=\"50\" align=\"right\">" +  String.valueOf(index) + ".</td>");
            strb.append("<td width=\"30\" align=\"left\"><input type=\"checkbox\" name=\"" + JSP_Constants.PARAM2_DELETE_ + String.valueOf(count) + "\" value=\"" + elem.getTypeAndName().hashCode() + "\"/>D</td>");
            if (elem._isValid) {
                strb.append("<td width=\"70\" align=\"left\"><input type=\"checkbox\" disabled name=\"" + JSP_Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + elem.getTypeAndName().hashCode() + "\"/>SV</td>");
            } else {
                strb.append("<td width=\"70\" align=\"left\"><input type=\"checkbox\" name=\"" + JSP_Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + elem.getTypeAndName().hashCode() + "\"/>SV/" + elem._errorCode + "</td>");
            }
            strb.append("<td width=\"30\" align=\"left\">[" +  elem._dpDays.size() + "]</td>\n");
        } else if (mode == FL_MODE_EMPTIES) {
            strb.append("<td width=\"50\"></td>\n");
            strb.append("<td align=\"left\" width=\"30\"></td>\n");
            strb.append("<td align=\"left\" width=\"70\"></td>\n");
            strb.append("<td align=\"left\" width=\"30\"></td>\n");
        }

        // Type
        if (mode == FL_MODE_FILL_EXISTING) {
            strb.append("<td width=\"40\">" + elem._type + "</td>\n");
            strb.append("<input type=\"hidden\" name=\"" + JSP_Constants.PARAM2_TYPE_ + String.valueOf(count) + "\" value=\"" + elem._type + "\"/>");
        } else if (mode == FL_MODE_EMPTIES) {
            strb.append("<td width=\"40\">\n");
            strb.append("<select name=\"" + JSP_Constants.PARAM2_TYPE_ + String.valueOf(count) + "\">\n");
            for (int j = 0; j < D_FundInfo.TYPES.length; j++) {
                strb.append("<option value=\"" + D_FundInfo.TYPES[j] + "\">" +
                        D_FundInfo.TYPES[j] + "</option>\n");
            }
            strb.append("</select>\n");
            strb.append("</td>");
        }

        String valueName = "";
        String valueURL = "";
        if (mode == FL_MODE_FILL_EXISTING) {
            valueName = elem._nameMS;
            valueURL = elem._url;
        }

        strb.append("<td width=\"300\">" +
                "<input style=\"width:300px;\" type=\"text\"" +
                " name=\"" + JSP_Constants.PARAM2_NAME_ + String.valueOf(count) + "\"" +
                " value=\"" + valueName + "\"" +
                " size=\"150\" readonly></td>\n");

        strb.append("<td width=\"600\">" +
                "<input style=\"width:600px;\" type=\"text\"" +
                " name=\"" + JSP_Constants.PARAM2_URL_ + String.valueOf(count) + "\"" +
                " value=\"" + valueURL + "\"" +
                " size=\"150\" readonly></td>\n");

        if (mode == FL_MODE_FILL_EXISTING) {
            String href = "JSP_Report02_Weekly_Display.jsp" +
                    "?" + JSP_Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_FUND +
                    "&" + JSP_Constants.PARAM_ID + "=" + elem.getTypeAndName().hashCode();
            strb.append("<td><a href=\"" + elem._url + "\"" + " target=\"_blank\">ms</a></td>");
            strb.append("<td><a href=\"" + href + "\"" + " target=\"_blank\">us</a></td>");
//            strb.append("<td><p style=\"font-size:11px\">[id:N/A]</p></td>");
        } else if (mode == FL_MODE_EMPTIES) {
            strb.append("<td width=\"20\"></td>");
            strb.append("<td width=\"20\"></td>");
            strb.append("<td width=\"115\"></td>");
        }
        strb.append("</tr>\n");
    }

    /**
     *
     */
    private static OTuple2G<D_FundInfo, D_FundInfo> getD_FundInfo(
            List<D_FundInfo> allFunds,
            Long v_id,
            String v_type,
            String v_name,
            String v_url,
            String v_delete,
            String v_setvalid) throws Exception {

        log.info("Entering with, type: " + v_type + ", name: " + v_name + ", url: " + v_url + ", setvalid: " + v_setvalid + ", delete: " + v_delete);

        if (v_name != null && v_name.startsWith("T. Rowe")) {
            log.info("getD_FundInfo, arguments"
                    + "\nv_type:     " + v_type
                    + "\nv_name:     " + v_name
                    + "\nv_url:      " + v_url
                    + "\nv_delete:   " + v_delete
                    + "\nv_setvalid: " + v_setvalid);
        }

        if (v_name == null || v_name.trim().length() == 0 || v_type == null || v_type.trim().length() == 0) {
            return null;
        }

        if (v_type.equals(D_FundInfo.TYPE_VANGUARD)
                && (v_url == null || v_url.length() == 0 || v_url.equals("null"))) {
            v_url = JSP_Constants.url_getVanguard(v_name);
            log.info("Vanguard with null URL\nname: " + v_name + "\n" + "url: " + v_url);
        }

        if (v_url == null || v_url.trim().length() == 0) {
            return null;
        }


        D_FundInfo fi = null;
        for (D_FundInfo fitmp: DB_FundInfo.getAllFundInfos()) {
            if (v_type.equals(fitmp._type) && v_name.equals(fitmp._nameMS) && v_url.equals(fitmp._url)) {
                fi = fitmp;
            }
        }

        if (v_delete != null || v_setvalid != null) {
            return new OTuple2G<>(fi, fi);
        }
        return null;   // No update

        // This was back in the day when we supported updating fund information
//        D_FundInfo fi = new D_FundInfo();
//        fi._type = v_type;
//        fi._nameMS = v_name;
//        fi._url = v_url;
//        fi._msRating = -1;
//        fi._ppmNumber = "";
//        String lfriday = MM.tgif_getLastFridayTodayExcl(MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM));
//        fi._dateYYMMDD_Updated = lfriday;
//        fi._dateYYMMDD_Update_Attempted = lfriday;
//
//        D_FundInfo fic = fi.duplicate();
//        return new OTuple2G<>(fi, fic);
    }
}


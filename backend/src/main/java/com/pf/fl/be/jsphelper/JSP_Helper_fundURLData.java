package com.pf.fl.be.jsphelper;

import com.pf.fl.be.datastore.DS;
import com.pf.fl.be.datamodel.FLA_Cache;
import com.pf.fl.be.datamodel.FLA_Cache_FundInfo;
import com.pf.fl.be.datamodel.FLA_FundInfo;
import com.pf.fl.be.util.Constants;
import com.pf.fl.be.util.EE;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;

import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import static com.googlecode.objectify.ObjectifyService.ofy;

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

    public static String urlControl_DisplayFundEmptiesAndList(List<FLA_Cache_FundInfo> l) throws Exception {
        StringBuilder strb = new StringBuilder();

        int count = 0;
        for (int i = 0; i < 5; i++) {
            urlControl_DisplayFund(true, null, false, false, strb, FL_MODE_EMPTIES, null, count, -1);
            count++;
        }

        for (int i = 0; i < l.size(); i++) {
            FLA_Cache_FundInfo cfi = l.get(i);
            urlControl_DisplayFund(true, null, false, false, strb, FL_MODE_FILL_EXISTING, cfi, count, i+1);
            count++;
        }

        return strb.toString();
    }

    /**
     *
     */
    public static String urlControl_VerifyOrExecute(boolean isVerify, HttpServletRequest r) throws Exception {
        EE ee = EE.getEE();
        ee.dinfo(log, TAG, "JSP_Helper_fundURLData.urlControl_VerifyOrExecute");

        StringBuilder strb = new StringBuilder();
        List<FLA_Cache_FundInfo> allFunds = FLA_Cache.cacheFundInfosByTypeOrNull(null);

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

            String p_idStr = Constants.PARAM2_ID_ + String.valueOf(i);
            String v_idStr = r.getParameter(p_idStr);
            String p_delete = Constants.PARAM2_DELETE_ + String.valueOf(i);
            String v_delete = r.getParameter(p_delete);
            String p_setvalid = Constants.PARAM2_SETVALID_ + String.valueOf(i);
            String v_setvalid = r.getParameter(p_setvalid);
            String p_type = Constants.PARAM2_TYPE_ + String.valueOf(i);
            String v_type = r.getParameter(p_type);
            String p_name = Constants.PARAM2_NAME_ + String.valueOf(i);
            String v_name = r.getParameter(p_name);
            String p_url = Constants.PARAM2_URL_ + String.valueOf(i);
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

            OTuple2G<FLA_FundInfo, FLA_Cache_FundInfo> fi = getFLA_FundInfo(allFunds, v_id, v_type, v_name, v_url, v_delete, v_setvalid);
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
                    FLA_FundInfo fiDB1 = DS.getFundInfoByTypeAndName(fi._o2.mType, fi._o2.mName);
                    FLA_FundInfo fiDB2 = DS.getFundInfoByTypeAndURL(fi._o2.mType, fi._o2.mURL);
                    if (fiDB1 != null || fiDB2 != null) {
                        String text = "";
                        if (fiDB1 != null) {
                            text = "Type.Name exists will not insert: ";
                        } else if (fiDB2 != null) {
                            text = "URL exists will not insert: ";
                        }
                        strb.append("<tr bgcolor=\"#FF0000\"><td colspan=\"5\">");
                        strb.append(text + fi._o2.mType + "." + fi._o2.mName + " [" + fi._o2.mId + "]");
                        strb.append("</td></tr>\n");
                    } else {
                        urlControl_DisplayFund(true, "", false, false, strb, FL_MODE_FILL_EXISTING, fi._o2, count, -1);
                        count++;
                    }
                } else {
                    ee.dinfo(log, TAG, "Will now save new entity: " + fi._o1.mType + "." + fi._o1.mName);
                    ofy().save().entity(fi._o1).now();
                    FLA_Cache.updateCacheAdd(fi._o1);
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
                        ee.dwarning(log, TAG, "Will now delete entity: " + fi._o1.mType + "." + fi._o1.mName + " [" + fi._o1.mId + "]");
                        DS.deleteFundInfo(fi._o1);
                        FLA_Cache.updateCacheDelete(fi._o1);
                        ee.dwarning(log, TAG, "...delete performed");
                    } else if (isValid) {
                        fi._o1.mIsValid = true;
                        fi._o1.mInvalidCode = FLA_FundInfo.IC_NONE;
                        ee.dinfo(log, TAG, "Will now set entity to valid: " + fi._o1.mType + "." + fi._o1.mName + " [" + fi._o1.mId + "]");
                        ofy().save().entity(fi._o1).now();
                        FLA_Cache.updateCacheUpdate(fi._o1);
                    } else {
                        ee.dinfo(log, TAG, "Will update entity: " + fi._o1.mType + "." + fi._o1.mName + " [" + fi._o1.mId + "], url: " + fi._o1.mURL);
                        ofy().save().entity(fi._o1).now();
                        FLA_Cache.updateCacheUpdate(fi._o1);
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
            FLA_Cache_FundInfo elem,
            int count,
            int index) throws Exception {

        if (doRow) {
            if (elem != null && !elem.mIsValid && text == null) {
                strb.append("<tr bgcolor=\"#FF0000\">\n");
            } else {
                strb.append("<tr>\n");
            }
        }

        if (mode == FL_MODE_FILL_EXISTING && elem.mId != null) {
            strb.append("<input type=\"hidden\" name=\"" + Constants.PARAM2_ID_ + String.valueOf(count) + "\" value=\"" + elem.mId + "\"/>");
        }

        if (text != null) {
            strb.append("<td colspan=\"4\">" + text + "</td>");
            if (hiddenDelete) {
                strb.append("<input type=\"hidden\" name=\"" + Constants.PARAM2_DELETE_ + String.valueOf(count) + "\" value=\"" + elem.mId + "\"/>");
            }
            if (hiddenSetValid) {
                strb.append("<input type=\"hidden\" name=\"" + Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + elem.mId + "\"/>");
            }
        } else if (mode == FL_MODE_FILL_EXISTING) {
            strb.append("<td width=\"50\" align=\"right\">" +  String.valueOf(index) + ".</td>");
            strb.append("<td width=\"30\" align=\"left\"><input type=\"checkbox\" name=\"" + Constants.PARAM2_DELETE_ + String.valueOf(count) + "\" value=\"" + elem.mId + "\"/>D</td>");
            if (elem.mIsValid) {
                strb.append("<td width=\"70\" align=\"left\"><input type=\"checkbox\" disabled name=\"" + Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + elem.mId + "\"/>SV</td>");
            } else {
                strb.append("<td width=\"70\" align=\"left\"><input type=\"checkbox\" name=\"" + Constants.PARAM2_SETVALID_ + String.valueOf(count) + "\" value=\"" + elem.mId + "\"/>SV/" + elem.mInvalidCode + "</td>");
            }
            strb.append("<td width=\"30\" align=\"left\">[" +  elem.getDPWeeks().size() + "]</td>\n");
        } else if (mode == FL_MODE_EMPTIES) {
            strb.append("<td width=\"50\"></td>\n");
            strb.append("<td align=\"left\" width=\"30\"></td>\n");
            strb.append("<td align=\"left\" width=\"70\"></td>\n");
            strb.append("<td align=\"left\" width=\"30\"></td>\n");
        }

        if (mode == FL_MODE_FILL_EXISTING) {
            strb.append("<td width=\"40\">" + elem.mType + "</td>\n");
            strb.append("<input type=\"hidden\" name=\"" + Constants.PARAM2_TYPE_ + String.valueOf(count) + "\" value=\"" + elem.mType + "\"/>");
        } else if (mode == FL_MODE_EMPTIES) {
            strb.append("<td width=\"40\">\n");
            strb.append("<select name=\"" + Constants.PARAM2_TYPE_ + String.valueOf(count) + "\">\n");
            for (int j = 0; j < FLA_FundInfo.TYPES.length; j++) {
                strb.append("<option value=\"" + FLA_FundInfo.TYPES[j] + "\">" +
                        FLA_FundInfo.TYPES[j] + "</option>\n");
            }
            strb.append("</select>\n");
            strb.append("</td>");
        }

        String valueName = "";
        String valueURL = "";
        if (mode == FL_MODE_FILL_EXISTING) {
            valueName = elem.mName;
            valueURL = elem.mURL;
        }

        strb.append("<td width=\"300\">" +
                "<input style=\"width:300px;\" type=\"text\"" +
                " name=\"" + Constants.PARAM2_NAME_ + String.valueOf(count) + "\"" +
                " value=\"" + valueName + "\"" +
                " size=\"150\"></td>\n");

        strb.append("<td width=\"600\">" +
                "<input style=\"width:600px;\" type=\"text\"" +
                " name=\"" + Constants.PARAM2_URL_ + String.valueOf(count) + "\"" +
                " value=\"" + valueURL + "\"" +
                " size=\"150\"></td>\n");

        if (mode == FL_MODE_FILL_EXISTING) {
            String href = "JSP_Report02_Weekly_Display.jsp" +
                    "?" + Constants.PARAM_TYPE + "=" + JSP_Helper.WT_FILTER_FUND +
                    "&" + Constants.PARAM_ID + "=" + elem.mFundInfoId;
            strb.append("<td><a href=\"" + elem.mURL + "\"" + " target=\"_blank\">ms</a></td>");
            strb.append("<td><a href=\"" + href + "\"" + " target=\"_blank\">us</a></td>");
            strb.append("<td><p style=\"font-size:11px\">[" + elem.mId + "]</p></td>");
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
    private static OTuple2G<FLA_FundInfo, FLA_Cache_FundInfo> getFLA_FundInfo(
            List<FLA_Cache_FundInfo> allFunds,
            Long v_id,
            String v_type,
            String v_name,
            String v_url,
            String v_delete,
            String v_setvalid) throws Exception {

        EE ee = EE.getEE();
        // ee.dinfo(log, TAG, "Entering with id: " + v_id + ", type: " + v_type + ", name: " + v_name + " [" + v_id + "], url: " + v_url + ", setvalid: " + v_setvalid + ", delete: " + v_delete);

        if (v_name != null && v_name.startsWith("T. Rowe")) {
            log.info("getFLA_FundInfo, arguments"
                    + "\nv_id:       " + v_id
                    + "\nv_type:     " + v_type
                    + "\nv_name:     " + v_name
                    + "\nv_url:      " + v_url
                    + "\nv_delete:   " + v_delete
                    + "\nv_setvalid: " + v_setvalid);
        }

        if (v_name == null || v_name.trim().length() == 0 || v_type == null || v_type.trim().length() == 0) {
            return null;
        }

        if (v_type.equals(Constants.ACCOUNT_TYPE_VANGUARD)
                && (v_url == null || v_url.length() == 0 || v_url.equals("null"))) {
            v_url = Constants.url_getVanguard(v_name);
            log.info("Vanguard with null URL\nname: " + v_name + "\n" + "url: " + v_url);
        }

        if (v_url == null || v_url.trim().length() == 0) {
            return null;
        }

        FLA_FundInfo fi = new FLA_FundInfo();
        if (v_id != null) {
            for (FLA_Cache_FundInfo fiElem : allFunds) {
                if (v_id.equals(fiElem.mFundInfoId)
                        && v_type.equals(fiElem.mType)
                        && v_name.equals(fiElem.mName)
                        && v_url.equals(fiElem.mURL)
                        && v_setvalid == null
                        && v_delete == null) {
                    // ee.dinfo(log, TAG, "Fund existed in exactly this form already, so wont update it!");
                    return null;
                }
            }
            fi = ofy().load().type(FLA_FundInfo.class).id(v_id.longValue()).now();
            fi.mName = v_name;
            fi.mURL = v_url;
        } else {
            fi.mId = v_id;
            fi.mType = v_type;
            fi.mName = v_name;
            fi.mURL = v_url;
            fi.mMSRating = -1;
            fi.mPPMNumber = -1;
            fi.mDateYYMMDD_Updated = MM.getNowAs_YYMMDD(null);
            fi.mDateYYMMDD_Update_Attempted = fi.mDateYYMMDD_Updated;
        }

        FLA_Cache_FundInfo fic = FLA_Cache_FundInfo.instantiate(fi);
        return new OTuple2G<>(fi, fic);
    }
}


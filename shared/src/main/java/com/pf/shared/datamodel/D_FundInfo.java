package com.pf.shared.datamodel;

import com.pf.shared.utils.IndentWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class D_FundInfo {

    public static final int IC_NO_ERROR = 0;

    public static final int IC_COM_NO_URL_DATA = 101;
    public static final int IC_COM_NO_DECODABLE_DATA = 102;
    public static final int IC_INVALID_URL_IRRECOVERABLE = 103;
    public static final int IC_NO_RECENT_DPDAY = 104;

    public static final int IC_HTML_MS_DAILY_TABLE = 201;
    public static final int IC_HTML_MS_DAILY_SEK_CURRENCY = 202;
    public static final int IC_HTML_MS_RATING = 203;
    public static final int IC_HTML_MS_CATEGORY = 204;
    public static final int IC_HTML_MS_YEARLY_TABLE_YEAR = 205;
    public static final int IC_HTML_MS_YEARLY_TABLE_FUND = 206;
    public static final int IC_HTML_MS_YEARLY_TABLE_INDEX = 207;
    public static final int IC_HTML_MS_YEARLY_TABLE_CATEGORY = 208;
    public static final int IC_HTML_MS_DPDAY_NULLDATE = 209;

    public static final int IC_VG_NOT_MF_NOR_ETF = 301;
    public static final int IC_HTML_VG_DATA_TICKER_MISMATCH = 302;
    public static final int IC_HTML_VG_TOTAL_RETURNS_NOT_FOUND = 303;
    public static final int IC_HTML_VG_DATE_NOT_FOUND = 304;
    public static final int IC_HTML_VG_DP_PARSING = 305;
    public static final int IC_HTML_VG_DP_ROWS_NO_INDEX = 306;
    public static final int IC_HTML_VG_DP_ROWS_INCONSISTENCY = 307;

    public static final String TYPE_PPM = "PPM";
    public static final String TYPE_SEB = "SEB";
    public static final String TYPE_SPP = "SPP";
    public static final String TYPE_VANGUARD = "VGD";
    public static final String TYPE_ALL = "ALL";
    public static final String TYPE_INVALID = "INVALID";
    public static final String[] TYPES = new String[] { TYPE_SEB, TYPE_SPP, TYPE_VANGUARD, TYPE_PPM };

    public boolean _isUpdated;
    public String _url = "";
    public boolean _isValid = true;
    public int _errorCode;
    public String _type = "";
    public String _nameMS = "";
    public String _nameOrig = "";
    public String _dateYYMMDD_Updated = "";
    public String _dateYYMMDD_Update_Attempted = "";
    public int _msRating = -1;
    public String _ppmNumber = "";
    public String _categoryName = "";
    public String _indexName = "";
    public String _currencyName = "";
    public List<D_FundDPDay>  _dpDays = new ArrayList<>();
    public List<D_FundDPYear> _dpYears = new ArrayList<>();

    public String getTypeAndName() { return _type + "." + _nameMS; }

    public void dumpInfo(IndentWriter iw) {
        iw.println("Fund: " + _type + "." + _nameMS);
        iw.push();
        iw.println("url: " + _url);
        iw.println("isValid: " + _isValid);
        iw.println("errorCode: " + _errorCode);
        iw.println("type: " + _type);
        iw.println("nameMS: " + _nameMS);
        iw.println("nameOrig: " + _nameOrig);
        iw.println("dateYYMMDD_Updated: " + _dateYYMMDD_Updated);
        iw.println("dateYYMMDD_Update_Attempted: " + _dateYYMMDD_Update_Attempted);
        iw.println("msRating: " + _msRating);
        iw.println("ppmNumber: " + _ppmNumber);
        iw.println("categoryName: " + _categoryName);
        iw.println("indexName: " + _indexName);
        iw.println("currencyName: " + _currencyName);

        iw.println("DPYears, length: " + _dpYears.size());
        iw.push();
        for (D_FundDPYear dpy: _dpYears) {
            dpy.dumpInfo(iw);
            iw.println();
        }
        iw.pop();

        iw.println("DPDays, length: " + _dpDays.size());
        iw.push();
        if (_dpDays.size() > 13) {
            for (int i=0; i < 12; i++) {
                D_FundDPDay dpd = _dpDays.get(i);
                dpd.dumpInfo(iw);
                iw.println();
            }
            D_FundDPDay dpd = _dpDays.get(_dpDays.size()-1);
            dpd.dumpInfo(iw);
            iw.println();
        } else {
            for (D_FundDPDay dpd : _dpDays) {
                dpd.dumpInfo(iw);
                iw.println();
            }
        }
        iw.pop();
    }

    public String toString() {
        IndentWriter iw = new IndentWriter();
        dumpInfo(iw);
        return iw.toString();
    }

    public String getOneLiner() {
        String dp1 = "none";
        String dp2 = "none";
        if (_dpDays.size() > 0) {
            D_FundDPDay dpd = _dpDays.get(0);
            dp1 = dpd._dateYYMMDD + "," + dpd._dateYYMMDD_Actual + "," + dpd._r1w;
        }
        if (_dpDays.size() > 1) {
            D_FundDPDay dpd = _dpDays.get(_dpDays.size()-1);
            dp2 = dpd._dateYYMMDD + "," + dpd._dateYYMMDD_Actual + "," + dpd._r1w;
        }

        StringBuffer strb = new StringBuffer();
        strb.append(getTypeAndName()
                + ", ec: " + _errorCode
                + ", iv: " + _isValid
                + ", du: " + _dateYYMMDD_Updated
                + ", dua: " + _dateYYMMDD_Update_Attempted
                + ", on: " + _nameOrig
                + ", mr: " + _msRating
                + ", p#: " + _ppmNumber
                + ", cur: " + _currencyName
                + ", in: " + _indexName
                + ", cn: " + _categoryName
                + ", dpd1: " + dp1
                + ", dpd2: " + dp2);
        return strb.toString();
    }

    public D_FundInfo duplicate() throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        D_FundInfo_Serializer.crunch_D_FundInfo(dout, this);
        dout.flush();
        byte[] ba = bout.toByteArray();

        ByteArrayInputStream bin = new ByteArrayInputStream(ba);
        DataInputStream din = new DataInputStream(bin);
        return D_FundInfo_Serializer.decrunch_D_FundInfo(din);
    }
}

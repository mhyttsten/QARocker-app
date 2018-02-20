package com.pf.fl.be.util;

import com.pf.fl.be.datamodel.FLA_Cache_FundInfo;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class Constants {

    public static final String url_funddata = "https://ql-magnushyttsten.firebaseio.com/funddata.json";
    public static final String url_extractdata = "https://ql-magnushyttsten.firebaseio.com/extractdata";

    public static final String url_getVanguard(String name) {
        int tEnd = name.lastIndexOf(")");
        int tStart = name.lastIndexOf("(");
        if (tEnd == -1 || tStart == -1) {
            return null;
        }
        String ticker = name.substring(tStart+1, tEnd);
        String vgURL = null;

        if (name.toLowerCase().contains("etf")) {
            vgURL = "https://performance.morningstar.com/perform/Performance/etf/trailing-total-returns.action?"
                    + "&t=" + ticker
                    + "&region=usa&culture=en-US&cur=&ops=clear&s=0P00001MJB&ndec=2&ep=true&align=d&annlz=true&comparisonRemove=false&loccat=&taxadj=&benchmarkSecId=&benchmarktype=";
        } else {
            vgURL = "https://performance.morningstar.com/perform/Performance/fund/trailing-total-returns.action?&t=XNAS:"
                    + ticker
                    + "&region=usa&culture=en-US&cur=&ops=clear&s=0P00001MJB&ndec=2&ep=true&align=d&annlz=true&comparisonRemove=false&loccat=&taxadj=&benchmarkSecId=&benchmarktype=";
        }
        return vgURL;
    }

    public static final String ACCOUNT_TYPE_PPM = "PPM";
    public static final String ACCOUNT_TYPE_SEB = "SEB";
    public static final String ACCOUNT_TYPE_SPP = "SPP";
    public static final String ACCOUNT_TYPE_VANGUARD = "VGD";

    public static final String PARAM_TYPE = "p_type";
    public static final String PARAM_NAME = "p_name";
    public static final String PARAM_ID = "p_id";
    public static final String PARAM2_ID_ = "p2_id_";
    public static final String PARAM2_TYPE_ = "p2_type_";
    public static final String PARAM2_NAME_ = "p2_name_";
    public static final String PARAM2_URL_ = "p2_url_";
    public static final String PARAM2_DELETE_ = "p2_delete_";
    public static final String PARAM2_SETVALID_ = "p2_setvalid_";

    // JSP Stuff
    public static final String ARG_OPERATION = "operation";
    public static final String OP_COMMIT = "op_commit";
    public static final String OP_VERIFY = "op_verify";
    public static final String OP_VIEW = "op_view";
    public static final String OP_DELETE = "op_delete";
    public static final String OP_UPDATE = "op_update";
    public static final String ARG_ID = "id";
    public static final String PARAM_DATEC = "p_datec";
    public static final String PARAM_DATEM = "p_datem";
    public static final String PARAM_FUND_ = "p_fund_";
    public static String getTextAreaStringForFundInfos(List<FLA_Cache_FundInfo> cfis) {
        StringBuffer strb = new StringBuffer();
        for (int i=0; i < cfis.size(); i++) {
            strb.append(cfis.get(i).mName + "\n");
        }
        ofy().clear();
        return strb.toString();
    }

}

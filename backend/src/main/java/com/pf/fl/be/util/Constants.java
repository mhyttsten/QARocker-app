package com.pf.fl.be.util;

import com.pf.fl.be.datamodel.FLA_Cache_FundInfo;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class Constants {

    public static final String url_funddata = "https://ql-magnushyttsten.firebaseio.com/funddata.json";
    public static final String url_extractdata = "https://ql-magnushyttsten.firebaseio.com/extractdata";


    public static final String ACCOUNT_TYPE_PPM = "PPM";
    public static final String ACCOUNT_TYPE_SEB = "SEB";
    public static final String ACCOUNT_TYPE_SPP = "SPP";
    public static final String ACCOUNT_TYPE_VANGUARD = "VGD";


    public static String getTextAreaStringForFundInfos(List<FLA_Cache_FundInfo> cfis) {
        StringBuffer strb = new StringBuffer();
        for (int i=0; i < cfis.size(); i++) {
            strb.append(cfis.get(i).mName + "\n");
        }
        ofy().clear();
        return strb.toString();
    }

}

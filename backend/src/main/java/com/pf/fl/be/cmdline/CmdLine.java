package com.pf.fl.be.cmdline;

import com.pf.fl.be.datamodel_raw.FL_MSExtractDetails;
import com.pf.fl.be.datamodel_raw.REFundInfo;
import com.pf.fl.be.extract.FLOps1_Ext1_HTMLGet;
import com.pf.fl.be.util.Constants;
import com.pf.fl.be.util.EE;
import com.pf.shared.IndentWriter;
import com.pf.shared.MM;
import com.pf.shared.OTuple2G;

import java.io.IOException;

public class CmdLine {

    public static void main(String[] args) {
        new CmdLine().checkURL();
    }

    public void checkURL() {
        try {
            checkURLImpl();
        } catch(Exception exc) {
            System.out.println("Exception: " + exc.getMessage());
            System.out.println(MM.getStackTraceString(exc));
        }
    }
    private void checkURLImpl() throws Exception {
        IndentWriter iw = new IndentWriter();

        String url = "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000I3LA&programid=0000000000";

        byte[] pageContent = MM.getURLContentBA(url);
        MM.fileWrite("pageContent.html", pageContent);
        String htmlString = MM.newString(pageContent, EE.ENCODING_FILE_READ);
        OTuple2G<Integer, REFundInfo> reFundInfo = FL_MSExtractDetails.extractFundDetails(
                Constants.ACCOUNT_TYPE_SEB,
                url,
                htmlString,
                iw);
        iw = new IndentWriter();
        reFundInfo._o2.addString(iw);
        System.out.println("**** RESULT IS:\n" + iw.getString());
    }
}

package com.pf.shared.analyze;

import com.pf.shared.Constants;
import com.pf.shared.datamodel.DB_FundInfo;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.D_Utils;
import com.pf.shared.utils.MM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FLAnalyze {

    private String _type;
    private int _weekCount;

    public static class Randk{
        public int _index;
        public D_FundInfo _fi;
    }

    //------------------------------------------------------------------------
    public static void main(String[] args) {
        try {
            FLAnalyze fla = new FLAnalyze(D_FundInfo.TYPE_SEB, 16);
            fla.analyze();
        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }

    //------------------------------------------------------------------------
    public FLAnalyze(String type, int weekCount) {
        _type = type;
        _weekCount = weekCount;
    }

    //------------------------------------------------------------------------
    public void analyze() throws Exception {
        String DIR = "/Users/magnushyttsten/Desktop/Vanguard";
        String DB_FILENAME = "fundinfo-db-master.bin";
        byte[] fileDBDataBA = MM.fileReadFrom(DIR + File.separator + DB_FILENAME);
        DB_FundInfo.initialize(fileDBDataBA);

        List<D_FundInfo> l = DB_FundInfo.getFundInfosByType(_type);
        String fridayOldest = D_Utils.getOldestFriday(l);
        for (D_FundInfo fi: l) {
            System.out.println(fi.getOneLiner());
            break;
        }

//        for (D_FundInfo fi: l) {
//            addNullMonths(fi, fridayOldest);
//        }
//        for (D_FundInfo fi: l) {
//            deduceMissingR1Ws(fi, fridayOldest);
//        }
    }

    //------------------------------------------------------------------------
    private void deduceMissingR1Ws(D_FundInfo fi, String fridayOldest) {
        String nowYYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        String fridayExpected = MM.tgif_getLastFridayTodayExcl(nowYYMMDD);
        List<D_FundDPDay> dpds = fi._dpDays;

        int index = 0;
        while (index < dpds.size()) {
            D_FundDPDay dpd = dpds.get(index);
            if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
                deduceUsingMonth(dpds, index);
            }
            index++;
        }
    }
    private boolean deduceUsingMonth(List<D_FundDPDay> dpds, int index) {
        boolean modified = false;

        // W = Weekly, M=Monthly, C=CurrentR1WNull

        // C, W, W, WM: We have 3 previous entries
        if (index-3 >=0
                && dpds.get(index-1)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index-2)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index-3)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index-3)._r1m != D_FundDPDay.FLOAT_NULL) {
            float w3 = dpds.get(index-1)._r1w + dpds.get(index-2)._r1w + dpds.get(index-3)._r1w;
            float r = dpds.get(index)._r1m - w3;
            dpds.get(index)._r1w = r;
            modified = true;
        }
        // W, C, W, WM: One ahead of us, and 3 previous entries
        else if (index-2 >= 0
                && index >= 1
                && dpds.get(index-1)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index+1)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index+2)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index+2)._r1m != D_FundDPDay.FLOAT_NULL) {
            float w3 = dpds.get(index + 1)._r1w + dpds.get(index + 2)._r1w + dpds.get(index + 3)._r1w;
            float r = dpds.get(index + 3)._r1m - w3;
            dpds.get(index)._r1w = r;
            modified = true;
        }
        return modified;
    }

    //------------------------------------------------------------------------
    private void addNullMonths(D_FundInfo fi, String fridayOldest) {
        String nowYYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        String fridayExpected = MM.tgif_getLastFridayTodayExcl(nowYYMMDD);
        List<D_FundDPDay> dpds = fi._dpDays;

        // Fill in the blanks
        int index = 0;
        while (index < dpds.size()) {
            D_FundDPDay dpd = dpds.get(index);
            if (dpd._dateYYMMDD.compareTo(fridayExpected) > 0) {
                throw new AssertionError("Date of DPD cannot be larger than expected friday");
            } else if (dpd._dateYYMMDD.compareTo(fridayExpected) == 0) {
                fridayExpected = MM.tgif_getLastFridayTodayExcl(fridayExpected);
                index++;
            } else {
                while (dpd._dateYYMMDD.compareTo(fridayExpected) < 0) {
                    D_FundDPDay newDPD = new D_FundDPDay();
                    newDPD._dateYYMMDD = fridayExpected;
                    newDPD._dateYYMMDD_Actual = nowYYMMDD;
                    dpds.add(index, newDPD);
                    fridayExpected = MM.tgif_getLastFridayTodayExcl(fridayExpected);
                    index++;
                }
            }
        }
        if (fi._dpDays.size() > 0) {
            D_FundDPDay dpd = fi._dpDays.get(fi._dpDays.size() - 1);
            fridayExpected = dpd._dateYYMMDD;
            if (fridayExpected.compareTo(fridayOldest) > 0) {
                fridayExpected = MM.tgif_getLastFridayTodayExcl(fridayExpected);
                while (fridayExpected.compareTo(fridayOldest) >= 0) {
                    D_FundDPDay newDPD = new D_FundDPDay();
                    newDPD._dateYYMMDD = fridayExpected;
                    newDPD._dateYYMMDD_Actual = nowYYMMDD;
                    fi._dpDays.add(newDPD);
                    fridayExpected = MM.tgif_getLastFridayTodayExcl(fridayExpected);
                }
            }
        }
    }
}

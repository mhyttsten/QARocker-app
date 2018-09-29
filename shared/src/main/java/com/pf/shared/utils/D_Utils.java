package com.pf.shared.utils;


import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;

import java.util.ArrayList;
import java.util.List;

public class D_Utils {

    //------------------------------------------------------------------------
    public static String getOldestFriday(List<D_FundInfo> l) {
        String friday = null;
        for (D_FundInfo fi: l) {
            for (D_FundDPDay dpd: fi._dpDays) {
                if (friday == null) {
                    friday = dpd._dateYYMMDD;
                }
                else if (friday.compareTo(dpd._dateYYMMDD) > 0) {
                    friday = dpd._dateYYMMDD;
                }
            }
        }
        return friday;
    }


    //------------------------------------------------------------------------
    public static String getR1WsAsCSV(String[] fridays, D_FundInfo fi) {
        for (String friday: fridays) {
            System.out.println("friday: " + friday);
        }
        for (D_FundDPDay dpd: fi._dpDays) {
            System.out.println("dpdays: " + dpd._dateYYMMDD);
        }

        StringBuffer strb = new StringBuffer();
        boolean first = true;
        for (String friday: fridays) {
            if (!first) {
                strb.append(", ");
            }
            first = false;
            boolean found = false;
            for (D_FundDPDay dpd: fi._dpDays) {
                if (friday.equals(dpd._dateYYMMDD)) {
                    found = true;
                    if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
                        strb.append("-");
                    } else {
                        strb.append(String.valueOf(dpd._r1w));
                    }
                }
            }
            if (!found) {
                strb.append("-");
            }
        }
        return strb.toString();
    }

}

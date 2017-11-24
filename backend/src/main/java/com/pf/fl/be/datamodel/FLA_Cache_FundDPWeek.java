package com.pf.fl.be.datamodel;

import com.pf.fl.be.util.EE;
import com.pf.shared.MM;

import java.util.logging.Logger;

public class FLA_Cache_FundDPWeek {

    private static final Logger log = Logger.getLogger(FLA_Cache_FundDPWeek.class.getName());
    private static final String TAG = MM.getClassName(FLA_Cache_FundDPWeek.class.getName());

    /**
     *
     */
    public static FLA_Cache_FundDPWeek instantiate(String fund, String date, String r1w) throws Exception {
        EE ee = EE.getEE();
        FLA_Cache_FundDPWeek r = null;
        if (date == null || date.trim().length() == 0) {
            if (r1w != null && r1w.trim().length() > 0) {
                ee.dsevere(log, TAG, "ERROR for: " + fund + ", date was null but it still had a r1w of: " + r1w);
            }
            return null;
        }
        else if (date.trim().length() != 6) {
            ee.dsevere(log, TAG, "ERROR for: " + fund + ", date did not have 6 characters: " + date);
            return null;
        }
        r = new FLA_Cache_FundDPWeek();
        r.mDateYYMMDD = date.trim();

        if (r1w == null || r1w.trim().length() == 0) {
            return r;
        }
        Double d = null;
        try {
            d = Double.parseDouble(r1w.trim());
            r.mR1w = d;
        } catch(Exception exc) {
            ee.dsevere(log, TAG, "ERROR for: " + fund + ", r1w could not be parsed: " + r1w + ", for date: " + r.mDateYYMMDD);

        }
        return r;
    }

    /**
     *
     */
    public static FLA_Cache_FundDPWeek instantiate(FLA_FundDPDay dpday) {
        FLA_Cache_FundDPWeek r = new FLA_Cache_FundDPWeek();
        r.mDateYYMMDD = dpday.mDateYYMMDD;
        if (dpday.mR1w != null) {
            r.mR1w = dpday.mR1w;
        }
        return r;
    }

    /**
     *
     */
    public String toString() {
        return mDateYYMMDD + ": " + mR1w;
    }

    public String mDateYYMMDD;
    public Double mR1w;
}

package com.pf.shared.analyze;

import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.D_Utils;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.util.List;

public class FLAnalyze_DataPreparation {

    //------------------------------------------------------------------------
    public static void fillVoids(IndentWriter iw, List<D_FundInfo> fis) {
        if (iw == null) {
            iw = new IndentWriter();
        }

        String fridayOldest = D_Utils.getOldestFriday(fis);

        for (D_FundInfo fi: fis) {
            insertNullMonths(fi._dpDays, D_Utils.getLastExtractedFriday(), fridayOldest);
        }

        List<D_FundDPDay> dpds = null;
        for (D_FundInfo fi: fis) {
            dpds = fi._dpDays;
            insertMissingDPs(iw, dpds);
        }
//        System.out.println("Done with: " + _countFunds + ", r1w fixed: " + _countR1WFixed + ", r1m fixed: " + _countR1MFixed);
    }

    //------------------------------------------------------------------------
    public static void insertMissingDPs(IndentWriter iw, List<D_FundDPDay> dpds) {
        boolean modified = true;
        while (modified) {
            modified = true;
            boolean r1wModified = insertMissingR1Ws(iw, dpds);

            if (!r1wModified) {
                modified = false;
                boolean r1mModified = true;
                while (r1mModified) {
                    r1mModified = insertMissingR1Ms(iw, dpds);
                    // If we get an R1M modification, we wanna go back to check R1W again
                    if (r1mModified) {
                        modified = true;
                    }
                }
            }
        }
    }

    //------------------------------------------------------------------------
    private static boolean insertMissingR1Ms(IndentWriter iw, List<D_FundDPDay> dpds) {
        boolean updated = false;
        int index = dpds.size() - 1;
        while (index >= 0) {
            D_FundDPDay dpd = dpds.get(index);
            String date = dpd._dateYYMMDD;
            if (dpd._r1m == D_FundDPDay.FLOAT_NULL) {
                if (index+3 < dpds.size()
                        && dpd._r1w != D_FundDPDay.FLOAT_NULL
                        && dpds.get(index+1)._r1w != D_FundDPDay.FLOAT_NULL
                        && dpds.get(index+2)._r1w != D_FundDPDay.FLOAT_NULL
                        && dpds.get(index+3)._r1w != D_FundDPDay.FLOAT_NULL) {
                    dpd._r1m = dpd._r1w + dpds.get(index+1)._r1w + dpds.get(index+2)._r1w + dpds.get(index+3)._r1w;
                    updated = true;
                }
            }
            index--;
        }
        return updated;
    }

    //------------------------------------------------------------------------
    private static boolean insertMissingR1Ws(IndentWriter iw, List<D_FundDPDay> dpds) {
        boolean updated = false;
        int index = dpds.size() - 1;
        while (index >= 0) {
            D_FundDPDay dpd = dpds.get(index);
            String date = dpd._dateYYMMDD;
            if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
                boolean subupdated = insertR1WUsingR1M(iw, dpds, index);
                if (subupdated) {
                    updated = true;
                }
            }
            index--;
        }
        return updated;
    }
    private static boolean insertR1WUsingR1M(IndentWriter iw, List<D_FundDPDay> dpds, int index) {
        boolean modified = false;

        // W = Weekly, M=Monthly, C=CurrentR1WNull

        // Earlier indexes are later dates

        // Index order: WM, W, W, C
        if (index-3 >=0
                && index < dpds.size()
                && dpds.get(index-1)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index-2)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index-3)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index-3)._r1m != D_FundDPDay.FLOAT_NULL) {
            float w3 = dpds.get(index-1)._r1w + dpds.get(index-2)._r1w + dpds.get(index-3)._r1w;
            float r = dpds.get(index-3)._r1m - w3;
            dpds.get(index)._r1w = r;
            modified = true;
        }
        // Index order: WM, W, C, W
        else if (index-2 >= 0
                && index+1 < dpds.size()
                && dpds.get(index-2)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index-1)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index+1)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index-2)._r1m != D_FundDPDay.FLOAT_NULL) {
            float w3 = dpds.get(index-2)._r1w + dpds.get(index-1)._r1w + dpds.get(index+1)._r1w;
            float r = dpds.get(index-2)._r1m - w3;
            dpds.get(index)._r1w = r;
            modified = true;
        }
        // Index order: WM, C, W, W
        else if (index-1 >= 0
                && index+2 < dpds.size()
                && dpds.get(index-1)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index+1)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index+2)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index-1)._r1m != D_FundDPDay.FLOAT_NULL) {
            float w3 = dpds.get(index-1)._r1w + dpds.get(index+1)._r1w + dpds.get(index+2)._r1w;
            float r = dpds.get(index-1)._r1m - w3;
            dpds.get(index)._r1w = r;
            modified = true;
        }
        // Index order: CM, W, W, W
        else if (index >= 0
                && index+3 < dpds.size()
                && dpds.get(index+1)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index+2)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index+3)._r1w != D_FundDPDay.FLOAT_NULL
                && dpds.get(index)._r1m != D_FundDPDay.FLOAT_NULL) {
            float w3 = dpds.get(index+1)._r1w + dpds.get(index+2)._r1w + dpds.get(index+3)._r1w;
            float r = dpds.get(index)._r1m - w3;
            dpds.get(index)._r1w = r;
            modified = true;
        }

        return modified;
    }

    //------------------------------------------------------------------------
    // Make sure we have continuous DPDs in range [fridayStart, fridayEnd] fill with dummy's when not
    public static void insertNullMonths(List<D_FundDPDay> dpds,
                                        String fridayStart,
                                        String fridayEnd) {

        // This loop ensure the dpds sequence is continuous
        String fridayExpected = fridayStart;  // We start by expecting latest extraction
        int index = 0;
        while (index < dpds.size()) {
            D_FundDPDay dpd = dpds.get(index);
            String fridayDPD = dpd._dateYYMMDD;

            // DPD cannot be more recent than fridayExpected, since this would either mean:
            // 1. DPD sequence is not ordered in descending order (low to high index)
            // 2. Our first friday (fridayStart) is not the latest extraction friday
            // Observe that #2 could change in future, if called with fridayStart order than last extraction friday
            if (dpd._dateYYMMDD.compareTo(fridayExpected) > 0) {
                throw new AssertionError("Date of DPD: " + dpd._dateYYMMDD + ", cannot be larger than last friday: " + fridayExpected);
            }

            // DPD matches expected, then move to one step older
            else if (dpd._dateYYMMDD.compareTo(fridayExpected) == 0) {
                fridayExpected = MM.tgif_getLastFridayTodayExcl(fridayExpected);
                index++;
            }

            // DPD sequence misses fridayExpected
            else {
                // Fill in dummy DPDs to close the sequence gaps
                while (dpd._dateYYMMDD.compareTo(fridayExpected) < 0) {
                    D_FundDPDay newDPD = new D_FundDPDay();
                    newDPD._dateYYMMDD = fridayExpected;
                    newDPD._dateYYMMDD_Actual = fridayExpected;
                    dpds.add(index, newDPD);
                    fridayExpected = MM.tgif_getLastFridayTodayExcl(fridayExpected);
                    index++;
                }
            }
        }

        // This loop ensures dpds goes continous all the way down to fridayOldest
        // (as last dpd may be younger than fridayOldest)
        if (dpds.size() > 0) {
            D_FundDPDay dpd = dpds.get(dpds.size() - 1);
            fridayExpected = dpd._dateYYMMDD;  // Set it to oldest in dpds
            if (fridayExpected.compareTo(fridayEnd) > 0) {   // And fill if that's younger than fridayEnd
                fridayExpected = MM.tgif_getLastFridayTodayExcl(fridayExpected);
                while (fridayExpected.compareTo(fridayEnd) >= 0) {
                    D_FundDPDay newDPD = new D_FundDPDay();
                    newDPD._dateYYMMDD = fridayExpected;
                    newDPD._dateYYMMDD_Actual = fridayExpected;
                    dpds.add(newDPD);
                    fridayExpected = MM.tgif_getLastFridayTodayExcl(fridayExpected);
                }
            }
        }
    }
}

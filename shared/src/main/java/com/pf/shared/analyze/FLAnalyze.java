package com.pf.shared.analyze;

import com.pf.shared.Constants;
import com.pf.shared.datamodel.DB_FundInfo;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.D_Utils;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

public class FLAnalyze {

    //------------------------------------------------------------------------
    public static void main(String[] args) {
//        try {
//            String DIR = "/Users/magnushyttsten/Desktop/Vanguard";
//            String DB_FILENAME = "fundinfo-db-master.bin";
//            byte[] fileDBDataBA = MM.fileReadFrom(DIR + File.separator + DB_FILENAME);
//            DB_FundInfo.initialize(fileDBDataBA, true);
//
//            FLAnalyze fla = new FLAnalyze(D_FundInfo.TYPE_SEB, 2);
//            IndentWriter iw = new IndentWriter();
//            fla.analyze(iw);
//        } catch(Exception exc) {
//            exc.printStackTrace();
//        }
    }

    private String _type;
    private List<String> _fridayList = new ArrayList<>();

    // These are the resulting structures from the analysis
    private Map<String, List<FundRank>> _typeAndName2FRs;
    private List<FundRank> _frSummary;  // Sorted by lowest rank (which is strongest rank :)

    //------------------------------------------------------------------------
    public static class FundRankFriday {
        public String _friday;
        public List<FundRank> _frs;
    }

    public static class FundRank {
        String _friday;
        public float _rank;
        public D_FundInfo _fi;

        public float _r1w;
        public boolean _deducedR1W;
        public float   _deducedR1WRatio; // r1wNull / (r1wNull + r1wNotNull). Set for all

        public int _tmpCounter;

        public String toString() { return _fi.getTypeAndName() + ", rank: " + _rank + ", r1w: " + _r1w; }
    }

    //------------------------------------------------------------------------
    private static Comparator<FundRank> R1WComparator = new Comparator<FundRank>() {
        @Override
        public int compare(FundRank o1, FundRank o2) {
            if (o1._r1w != D_FundDPDay.FLOAT_NULL && o2._r1w == D_FundDPDay.FLOAT_NULL) {
                return -1;
            } else if (o1._r1w == D_FundDPDay.FLOAT_NULL && o2._r1w != D_FundDPDay.FLOAT_NULL) {
                return 1;
            }

            if (o1._r1w > o2._r1w) {
                return -1;
            }
            return 1;
        }
    };

    //------------------------------------------------------------------------
    public FLAnalyze(String type, int weekCount) throws Exception {
        _type = type;
        if (weekCount <= 0) {
            throw new AssertionError("weekCount must be larger than 0");
        }
        String lastFriday = D_Utils.getLastExtractedFriday();

        _fridayList.add(lastFriday);
        weekCount--;
        while (weekCount >= 1) {
            lastFriday = MM.tgif_getLastFridayTodayExcl(lastFriday);
            _fridayList.add(lastFriday);
            weekCount--;
        }
    }

    //------------------------------------------------------------------------
    public FLAnalyze(String type, String fridayNewest, String fridayOldest) {
        _type = type;
        if(!MM.tgif_isFriday(fridayNewest) || !MM.tgif_isFriday(fridayOldest)) {
            throw new AssertionError("One parameter not a friday: " + fridayNewest + ", " + fridayOldest);
        }
        _fridayList.add(fridayNewest);
        do {
            fridayNewest = MM.tgif_getLastFridayTodayExcl(fridayNewest);
            _fridayList.add(fridayNewest);
        } while (!fridayNewest.equals(fridayOldest));
    }

    //------------------------------------------------------------------------
    public void analyze(IndentWriter iw) throws Exception {
        System.out.println("FLAnalyze.analyze");

        fillVoids(null);

        // For each friday
        // Create FundRankFriday
        // With ranked entries (rank 0 = best, ..., rank = -1 no DPDay
        Set<String> fisNullR1W = new HashSet<>();
        List<FundRankFriday> fundRankSeries = new ArrayList<>();
        List<D_FundInfo> fis = DB_FundInfo.getFundInfosByType(_type);
        for (String friday: _fridayList) {
            FundRankFriday frf = new FundRankFriday();
            frf._friday = friday;
            List<FundRank> frs = new ArrayList<>();
            frf._frs = frs;
            fundRankSeries.add(frf);
            for (D_FundInfo fi: fis) {
                FundRank fr = new FundRank();
                fr._fi = fi;
                fr._friday = friday;
                List<D_FundDPDay> dpds = fi._dpDays;
                for (D_FundDPDay dpd: dpds) {
                    if (dpd._dateYYMMDD.equals(friday)) {
                        if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
                            fisNullR1W.add(fi.getTypeAndName());
                        }
                        fr._r1w = dpd._r1w;
                    }
                }
                frs.add(fr);
            }
            Collections.sort(frs, R1WComparator);
        }

        // For those where, you don't have a rank
        // Calculate average rank over series and assign that
        // If we cannot calculate average R1W (i.e. no data to work on), then remove typeAndName from series
        for (String typeAndName: fisNullR1W) {
            assignAveragesWhereNull(fundRankSeries, typeAndName);
        }

        // Do the ranking
        // Those who did not have enough DP will have FLOAT_NULL and get last rating
        for (FundRankFriday frf: fundRankSeries) {
            Collections.sort(frf._frs, R1WComparator);
            for (int i=0; i < frf._frs.size(); i++) {
                frf._frs.get(i)._rank = i + 1;
            }
        }

        // Create the 2 summary structures
        //    TypeAndName -> List<FundRank>
        //    TypeAndName -> FundRank (this is the summary)
        _typeAndName2FRs = new HashMap<>();
        HashMap<String, FundRank> tn2frSummary = new HashMap<>();
        for (FundRankFriday frf: fundRankSeries) {
            for (FundRank fr: frf._frs) {

                // TypeAndName -> List<FundRank>
                List<FundRank> frsExisting = _typeAndName2FRs.get(fr._fi.getTypeAndName());
                if (frsExisting == null) {
                    frsExisting = new ArrayList<>();
                    _typeAndName2FRs.put(fr._fi.getTypeAndName(), frsExisting);
                }
                frsExisting.add(fr);

                // TypeAndName -> FundRank (this is the summary)
                FundRank frExisting = tn2frSummary.get(fr._fi.getTypeAndName());
                if (frExisting == null) {
                    frExisting = new FundRank();
                    frExisting._fi = fr._fi;
                    frExisting._friday = null;
                    frExisting._deducedR1WRatio = fr._deducedR1WRatio;
                    tn2frSummary.put(fr._fi.getTypeAndName(), frExisting);
                }
                if (fr._deducedR1W) {
                    frExisting._deducedR1W = true;
                }
                frExisting._r1w += fr._r1w;
                frExisting._rank += fr._rank;
                frExisting._tmpCounter++;
            }
        }
        // Create final list with summary order
        // Rank will be the average rank across all weeks
        _frSummary = new ArrayList<>(tn2frSummary.values());
        Collections.sort(_frSummary, new Comparator<FundRank>() {
            @Override
            public int compare(FundRank o1, FundRank o2) {
                if (o1._rank < o2._rank)
                    return -1;
                return 1;
            }
        });
    }
    private void assignAveragesWhereNull(List<FundRankFriday> fundRankFridayList, String typeAndName) {
        float r1wSum = 0.0F;
        float r1wValueCount = 0;
        float r1wNullCount = 0;

        for (FundRankFriday fundRankFriday: fundRankFridayList) {
            List<FundRank> fundRankFridaySeries = fundRankFriday._frs;
            for (FundRank fr: fundRankFridaySeries) {
                if (fr._fi.getTypeAndName().equals(typeAndName)) {
                    if (fr._r1w == D_FundDPDay.FLOAT_NULL) {
                        r1wNullCount++;
                    } else {
                        r1wSum += fr._r1w;
                        r1wValueCount++;
                    }
                }
            }
        }
        float r1wAvg = D_FundDPDay.FLOAT_NULL;
        if (r1wValueCount != 0) {
            r1wAvg = r1wSum / r1wValueCount;
        }

        // Assign the averages where R1W == null
        for (FundRankFriday fundRankFriday: fundRankFridayList) {
            List<FundRank> fundRankFridaySeries = fundRankFriday._frs;
            int index = 0;
            while (index < fundRankFridaySeries.size()) {
                FundRank fr = fundRankFridaySeries.get(index);
                if (fr._fi.getTypeAndName().equals(typeAndName)) {
                    // If could not get an average (because all were null)
                    // Then remove from the entire series
                    if (r1wAvg == D_FundDPDay.FLOAT_NULL) {
                        fundRankFridaySeries.remove(index);
                    }
                    else {
                        // We have an average, then use it for the entries which are null
                        if (fr._r1w == D_FundDPDay.FLOAT_NULL) {
                            fr._r1w = r1wAvg;
                            fr._deducedR1W = true;
                        }
                        fr._deducedR1WRatio = r1wNullCount / (r1wNullCount+r1wSum);
                        index++;
                    }
                }
            }
        }
    }

    //------------------------------------------------------------------------
    public void fillVoids(IndentWriter iw) throws Exception {
        if (iw == null) {
            iw = new IndentWriter();
        }

        List<D_FundInfo> l = DB_FundInfo.getFundInfosByType(_type);
        String fridayOldest = D_Utils.getOldestFriday(l);

        for (D_FundInfo fi: l) {
            addNullMonths(fi, fridayOldest);
        }

        List<D_FundDPDay> dpds = null;
        for (D_FundInfo fi: l) {
            dpds = fi._dpDays;

            boolean modified = true;
            do {
                modified = true;
                boolean emodified = deduceMissingR1Ws(iw, fi, fridayOldest);
//                System.out.println("DPDs now: ");
//                for (D_FundDPDay dpd: dpds) { System.out.println("..." + dpd); }
                if (!emodified) {
                    modified = false;
                    boolean r1m = true;
                    do {
                        r1m = deduceMissingR1Ms(iw, fi, fridayOldest);
                        if (r1m) {
                            modified = true;
                        }
                    } while (r1m);
                }
            } while(modified);
        }

//        System.out.println("Done with: " + _countFunds + ", r1w fixed: " + _countR1WFixed + ", r1m fixed: " + _countR1MFixed);
    }

    //------------------------------------------------------------------------
    private boolean deduceMissingR1Ms(IndentWriter iw, D_FundInfo fi, String fridayOldest) {
        String nowYYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        String fridayExpected = MM.tgif_getLastFridayTodayExcl(nowYYMMDD);
        List<D_FundDPDay> dpds = fi._dpDays;

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
    private boolean deduceMissingR1Ws(IndentWriter iw, D_FundInfo fi, String fridayOldest) {
        String nowYYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        String fridayExpected = MM.tgif_getLastFridayTodayExcl(nowYYMMDD);
        List<D_FundDPDay> dpds = fi._dpDays;

        boolean updated = false;
        int index = dpds.size() - 1;
        while (index >= 0) {
            D_FundDPDay dpd = dpds.get(index);
            String date = dpd._dateYYMMDD;
            if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
                boolean subupdated = deduceR1WUsingR1M(iw, dpds, index);
                if (subupdated) {
                    updated = true;
                }
            }
            index--;
        }
        return updated;
    }
    private boolean deduceR1WUsingR1M(IndentWriter iw, List<D_FundDPDay> dpds, int index) {
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

//        dpds = new ArrayList<>();
//        {
//        D_FundDPDay dpd = null;
//        dpd = new D_FundDPDay(); dpd._r1m = 1.0F; dpds.add(dpd);
//        dpd = new D_FundDPDay(); dpd._r1m = 1.0F; dpds.add(dpd);
//        dpd = new D_FundDPDay(); dpd._r1m = 1.0F; dpds.add(dpd);
//        dpd = new D_FundDPDay(); dpd._r1w = 0.25F; dpds.add(dpd);
//        dpd = new D_FundDPDay(); dpd._r1m = 1.0F; dpd._r1w = 0.25F; dpds.add(dpd);
//        dpd = new D_FundDPDay(); dpds.add(dpd);
//        dpd = new D_FundDPDay(); dpd._r1w = 0.25F; dpds.add(dpd);
//        dpd = new D_FundDPDay(); dpd._r1w = 0.25F; dpds.add(dpd);
//        dpd = new D_FundDPDay(); dpd._r1m = 1.0F; dpds.add(dpd);
//        }


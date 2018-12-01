package com.pf.shared.analyze;

import com.pf.shared.Constants;
import com.pf.shared.datamodel.DB_FundInfo;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.D_Utils;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Set;

public class FLAnalyze {

    // This is original list from DB file, but with DPDs padded so all have entries
    private List<D_FundInfo> _fundsToAnalyzeOrig;


    // Usage
    // - Call setRange, analyze repeatedly

    // These are the resulting structures from the analysis
    public Map<String, List<FundRank>> _typeAndName2FRs;
    public List<FundRank> _frSummary;  // Sorted by lowest rank (which is strongest rank :)

    // Private members used in calculation
    private List<String> _fridayList = new ArrayList<>();

    //------------------------------------------------------------------------
    public static class FundRank {
        public D_FundInfo _fi;

        String _friday;
        public float _rank;
        public float _r1w = D_FundDPDay.FLOAT_NULL;

        public int _countMissing;
        public int _countTotal;

        public String toString() { return _fi.getTypeAndName() + ", rank: " + _rank + ", r1w: " + _r1w; }
    }

    //------------------------------------------------------------------------
    public static class FundRankFriday {
        public String _friday;
        public List<FundRank> _frs;
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
    public FLAnalyze(List<D_FundInfo> fundsToAnalyze) {
        _fundsToAnalyze = fundsToAnalyze;
        FLAnalyze_DataPreparation.fillVoids(null, _fundsToAnalyze);
    }

    //------------------------------------------------------------------------
    public void setRange(int weekCount) {
        _fridayList = new ArrayList<>();
        _typeAndName2FRs = null;
        _frSummary = null;

        if(weekCount <= 0) {
            throw new AssertionError("Week count must be positive");
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
    public void setRange(String type, String fridayNewest, String fridayOldest) {
        _fridayList = new ArrayList<>();
        _typeAndName2FRs = null;
        _frSummary = null;

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
    public void analyze(IndentWriter iw) {


        here();
        // Problem is, we modify dpds list
        // Ideally we should work on a clone only valid for this range analysis
        // Solution - clone D_FundInfo and extract only DPDs needed for range
        List<FundRank> frTemplates = new ArrayList<>();
        for (D_FundInfo fi: _fundsToAnalyze) {
            FundRank fr = getFundRanks(fi, _fridayList);
            frTemplates.add(fr);
        }

        // List fridayNewest -> fridayOldest with ranked lists of the fund performances
        //   FundRankFriday: Friday -> List<Funds> (ranked)
        List<FundRankFriday> fundRankSeries = new ArrayList<>();

        // Funds that has had at least 1 r1w == FLOAT_NULL throughout analyzed fridays
        // We will rescue these in next phase by assiging average rank to null slots
        Map<String, D_FundInfo> fundsToFix = new HashMap<>();

        // For each friday, create FundRankFriday (friday->List<Funds> 0=best, ..., -1=no DPDay)
        // Store each friday in fundRankSeries
        for (int fridayIndex=0; fridayIndex < _fridayList.size(); fridayIndex++) {
            String friday = _fridayList.get(fridayIndex);
            FundRankFriday frf = new FundRankFriday();
            frf._friday = friday;
            List<FundRank> frs = new ArrayList<>();
            frf._frs = frs;
            fundRankSeries.add(frf);

            // First,add all FundInfos to a FundRank structure
            for (D_FundInfo fi: _fundsToAnalyze) {
                FundRank fr = new FundRank();
                fr._fi = fi;
                fr._friday = friday;

                D_FundDPDay dpd = fi._dpDays.get(fridayIndex);
                if (!dpd._dateYYMMDD.equals(friday)) {
                    throw new AssertionError("Friday mismatch, found: " + dpd._dateYYMMDD + ", expected: " + friday);
                }
                fr._r1w = dpd._r1w;  // It's ok if this is FLOAT_NULL we're dealing with that below
                frs.add(fr);
            }

            // Then sort based on r1w (those with FLOAT_NULL will be at end of list)
            Collections.sort(frs, R1WComparator);

            // Assign rank according to position or -1 if ranking could not be done (r1w == FLOAT_NULL)
            int index = 1;
            for (int i=0; i < frs.size(); i++) {
                FundRank fr = frs.get(i);
                if (fr._r1w == D_FundDPDay.FLOAT_NULL) {
                    fr._rank = -1;
                    fundsToFix.put(fr._fi.getTypeAndName(), fr._fi);
                } else {
                    fr._rank = index;
                    index++;
                }
            }
        }

        // Fix all ranks having -1 (== FLOAT_NULL) to their average value throughout all fridays
        // If we cannot calculate average R1W (i.e. no data to work on), then remove them from series
        Iterator<D_FundInfo> f2fixIter = fundsToFix.values().iterator();
        while (f2fixIter.hasNext()) {
            D_FundInfo fl2fix = f2fixIter.next();
            assignAveragesWhereNull(fundRankSeries, fl2fix);
        }

        here();

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

    //------------------------------------------------------------------------
    public static FundRank getFundRanks(List<D_FundDPDay> dpds, String[] fridays) {
        OTuple2G<Integer, Integer> p = D_Utils.getStartAndEndP1Indexes(dpds, fridays);
        int indexStart = p._o1;
        int indexEndP1 = p._o2;

        // First calculate sum of r1w that exist
        // And how many we used to get to that sum
        float r1wSum = 0;
        int countTotal = 0;
        int countMissing = 0;
        for (int i=indexStart; i < indexEndP1; i++) {
            D_FundDPDay dpd = dpds.get(i);
            countTotal++;
            if (dpd._r1w != D_FundDPDay.FLOAT_NULL) {
                r1wSum += dpd._r1w;
            } else {
                countMissing++;
            }
        }

        // If ratio of missing > 25% then don't use this
        float countTotalF = (float)countTotal;
        float countMissingF = (float)countMissing;
        if ((countMissingF / countTotalF) > 0.25) {
            return null;
        }

        // Calculate the average
        // Then assign it to the empty slots
        float averageR1W = r1wSum / (countTotalF - countMissingF);
        for (int i=indexStart; i < indexEndP1; i++) {
            D_FundDPDay dpd = dpds.get(i);
            if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
                dpd._r1w = averageR1W;

            }
        }

        FundRank fr = new FundRank();
        fr._countMissing  = countMissing;
        fr._countTotal = countTotal;
        return fr;
    }

    //------------------------------------------------------------------------
    private static void assignAveragesWhereNull(List<FundRankFriday> fundRankFridayList, D_FundInfo fi) {
        maybe_do_This_firwst_on_all_funds();
        // And assign r1w based on averages (not its rank)
        // And remove the ones that have a too high enough ratio (>=0.75)
        // Then we can do the ranking that can assume all r1ws are non-null
        float r1wSum = 0;
        int countTotal = 0;
        int countMissing = 0;

        // Go through all fridays
        for (FundRankFriday fundRankFriday: fundRankFridayList) {
            List<FundRank> fundRankFridaySeries = fundRankFriday._frs;
            // Go through all funds every friday
            for (FundRank fr: fundRankFridaySeries) {
                if (fr._fi.getTypeAndName().equals(fi.getTypeAndName())) {
                    countTotal++;
                    if (fr._r1w == D_FundDPDay.FLOAT_NULL) {
                        countMissing++;
                    } else {
                        r1wSum += fr._r1w;
                        r1wSum++;
                    }
                }
            }
        }
        float r1wAvg = D_FundDPDay.FLOAT_NULL;
        if (r1wValueCount != 0) {
            r1wAvg = ((float)r1wSum) / ((float)r1wValueCount);
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
                        fr._countMissing = r1wNullCount;
                        fr._countTotal = r1wSum;
                        fr._deducedR1WRatio = ((float)r1wNullCount) / ((float)(r1wNullCount+r1wSum));
                        index++;
                    }
                }
            }
        }
    }
}
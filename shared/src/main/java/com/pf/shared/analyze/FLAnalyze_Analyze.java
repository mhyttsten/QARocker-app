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

public class FLAnalyze_Analyze {

    // Usage
    // - Call setRange, analyze repeatedly

    // This is original list from DB file, but with DPDs padded so all have entries
    private List<D_FundInfo> _fiClonesOriginals;  // Mutated in that we only add null fridays & deduce r1ws, r1ms
    private List<D_FundInfo> _fiClonesToAnalyze;  // Temporary clone created in setRange for analysis

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

        public String getAverageRank_2F() { return String.format("%.2f", ((float)_rank)/((float)_countTotal)); }
        public String getAverageR1W_2F() { return String.format("%.2f", _r1w/(float)_countTotal); }

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
    public FLAnalyze_Analyze(List<D_FundInfo> fundsToAnalyze) {
        _fiClonesOriginals = D_FundInfo.cloneList(fundsToAnalyze);
//        FLAnalyze_DataPreparation.fillVoids(null, _fiClonesOriginals);
    }

    //------------------------------------------------------------------------
    public void setRange(int weekCount) {
        if(weekCount <= 0) {
            throw new AssertionError("Week count must be positive");
        }
        String[] fridays = D_Utils.getRecentDates(weekCount);
        setRange(fridays[0], fridays[fridays.length-1]);
    }
    public void setRange(String fridayNewest, String fridayOldest) {
        _fridayList = new ArrayList<>();
        _typeAndName2FRs = null;
        _frSummary = null;

        // Create the list of fridays to analyze
        if(!MM.tgif_isFriday(fridayNewest) || !MM.tgif_isFriday(fridayOldest)) {
            throw new AssertionError("One parameter not a friday: " + fridayNewest + ", " + fridayOldest);
        }
        _fridayList.add(fridayNewest);
        String fridayCurrent = fridayNewest;
        while (!fridayCurrent.equals(fridayOldest)) {
            fridayCurrent = MM.tgif_getLastFridayTodayExcl(fridayCurrent);
            _fridayList.add(fridayCurrent);
        }

        // Create clone list of funds and dpdays to analyze
        _fiClonesToAnalyze = D_FundInfo.cloneList(_fiClonesOriginals);
        for (D_FundInfo fi: _fiClonesToAnalyze) {
            List<D_FundDPDay> dpds = fi._dpDays;
            int idx = 0;
            while (idx < dpds.size()) {
                // Remove / keep dpds within our analysis range only
                D_FundDPDay dpd = dpds.get(idx);
                if (dpd._dateYYMMDD.compareTo(fridayNewest) > 0
                        || dpd._dateYYMMDD.compareTo(fridayOldest) < 0) {
                    dpds.remove(idx);
                } else {
                    idx++;
                }
            }
        }
    }

    //------------------------------------------------------------------------
    public void analyze(IndentWriter iw) {

        // Get template fundranks for funds
        // This also sorts out funds with too few valid dps in getFundRanks
        List<FundRank> frTemplates = new ArrayList<>();
        for (D_FundInfo fi: _fiClonesToAnalyze) {
            FundRank fr = getFundRanks(fi._dpDays);
            if (fr != null) {
                fr._fi = fi;
                frTemplates.add(fr);
            }
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
            // Initialize the baseline stuff
            String friday = _fridayList.get(fridayIndex);
            FundRankFriday frf = new FundRankFriday();
            frf._friday = friday;
            List<FundRank> frs = new ArrayList<>();
            frf._frs = frs;
            fundRankSeries.add(frf);

            // Create FundRank for this Friday
            for (FundRank frT: frTemplates) {
                FundRank fr = new FundRank();
                fr._fi = frT._fi;
                fr._countTotal = frT._countTotal;
                fr._countMissing = frT._countMissing;
                fr._friday = friday;

                D_FundDPDay dpd = fr._fi._dpDays.get(fridayIndex);
                if (!dpd._dateYYMMDD.equals(friday)) {
                    throw new AssertionError("Friday mismatch, found: " + dpd._dateYYMMDD + ", expected: " + friday);
                }
                if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
                    throw new AssertionError("Unexpected null for DPD, should have been fixed in getFundRanks(...)");
                }
                fr._r1w = dpd._r1w;
                frs.add(fr);
            }

            // Then sort based on r1w (none will be FLOAT_NULL because of assertion above)
            Collections.sort(frs, R1WComparator);

            // Assign rank according to position or -1 if ranking could not be done (r1w == FLOAT_NULL)
            for (int i=0; i < frs.size(); i++) {
                FundRank fr = frs.get(i);
                fr._rank = i+1;
            }
        }

        // Create the 2 summary structures
        //    _typeAndName2FRs: TypeAndName -> List<FundRank>
        //    _frSummary: List<FundRank> where 0 is the highest ranked
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
                    frExisting._countMissing = fr._countMissing;
                    frExisting._countTotal = fr._countTotal;
                    frExisting._r1w = 0.0F;
                    frExisting._rank = 0;
                    tn2frSummary.put(fr._fi.getTypeAndName(), frExisting);
                } else {
                    if (!frExisting._fi.getTypeAndName().equals(fr._fi.getTypeAndName())
                        || frExisting._countTotal != fr._countTotal
                        || frExisting._countMissing != fr._countMissing) {
                        throw new AssertionError("Mismatch between summary and increments");
                    }
                }
                frExisting._r1w += fr._r1w;
                frExisting._rank += fr._rank;
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
    public static FundRank getFundRanks(List<D_FundDPDay> dpds) {
        // First calculate sum of r1w that exist
        // And how many we used to get to that sum
        float r1wSum = 0;
        int countTotal = 0;
        int countMissing = 0;
        for (D_FundDPDay dpd: dpds) {
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
        for (D_FundDPDay dpd: dpds) {
            if (dpd._r1w == D_FundDPDay.FLOAT_NULL) {
                dpd._r1w = averageR1W;
            }
        }

        FundRank fr = new FundRank();
        fr._countMissing  = countMissing;
        fr._countTotal = countTotal;
        return fr;
    }
}
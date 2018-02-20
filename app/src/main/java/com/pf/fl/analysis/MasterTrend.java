package com.pf.fl.analysis;


import com.pf.fl.datamodel.DM_Fund;
import com.pf.shared.utils.IndentWriter;

import java.util.ArrayList;
import java.util.List;

public class MasterTrend  {
    private static final String TAG = MasterTrend.class.getSimpleName();

    public List<TrendEntity> mTrends = new ArrayList<>();

    private int mStart;
    private DataPoint[] mDPs;

    private String mDataSet;

    /**
     */
    public void initialize(DM_Fund f) {
        initialize(f.mDPs);
    }

    /**
     */
    public void initialize(DataPoint[] dps) {
        mDPs = dps;

        while (true) {
            TrendEntity te = getNextTrend();
            if (te == null) {
                break;
            }
            stripTrailingNulls(te);
            mTrends.add(te);
        }

//        System.out.println("*** Done retrieving trends, results\n" + toString());
        for (TrendEntity te : mTrends) {
            te.calculate();
        }
        mergeAdjacentMarginalTrends();
        for (TrendEntity te : mTrends) {
            te.calculate();
        }
//        System.out.println("*** Done merging trends, results\n" + toString());
    }

    /**
     */
    private TrendEntity getNextTrend() {
        // No more data to process trend from
        if (mStart >= mDPs.length) {
            return null;
        }

//        System.out.println("Here are the data points");
//        System.out.println("..." + getString(mDPs, mStart, mDPs.length));

        // Leading 0.0s does not support any trend
        int start = mStart;
        boolean startsWithNull = false;
        while (start < mDPs.length && (mDPs[start].mValue == null || mDPs[start].mValue == 0.0)) {
            startsWithNull = true;
            start++;
        }
        if (startsWithNull) {
            TrendEntity te = new TrendEntity();
            te.initialize(TrendEntity.T_TYPE_NONE, mDPs, mStart, start);
            mStart = te.mEnd;
            return te;
        }

        // Looping through values
        TrendAnalyzer ta = new TrendAnalyzer();
        ta.initialize(mDPs);
        boolean hasAssignedValueCap = false;
        for (int i=start; i < mDPs.length; i++) {
            mDataSet = getString(mDPs, i, mDPs.length);

            // Null value, continue
            if (mDPs[i].mValue == null) {
                continue;
            }

            // Let analyzer consume point, break if we've reached saturation
            if (!ta.consumeDataPoint(mDataSet, i)) {
                TrendEntity te = ta.getTrendResult();
                mStart = te.mEnd;
                return te;
            }
        }
        ta.cutTrend(mDPs.length);
        TrendEntity te = ta.getTrendResult();
        mStart = te.mEnd;
        return te;
    }

    /**
     */
    private void stripTrailingNulls(TrendEntity te) {
        // A None trend, can contain whatever and end with whatever
        if (te.mTrendType == TrendEntity.T_TYPE_NONE) {
            return;
        }

        String s = "stripTrailingNulls for: " + te;
//        System.out.println(s);

        int cend = te.mEnd-1;
        int start = te.mStart;
        int trendType = te.mTrendType;

        // Nothing to do, since start value is a valid trend (this it is not a NONE trend)
        if (cend == start) {
            return;
        }

        // Remove trailing nulls or values contrary to trend
        while (cend > start) {
            Double dD = te.mDPs[cend].mValue;
            if (dD == null) {
                cend--;
                mStart--;
                continue;
            }

            double d = dD.doubleValue();
            if (trendType == TrendEntity.T_TYPE_UP && d <= 0.0) {
                cend--;
                mStart--;
                continue;
            }
            if (trendType == TrendEntity.T_TYPE_DOWN && d >= 0.0) {
                cend--;
                mStart--;
                continue;
            }
            break;
        }
        cend++;
        te.mEnd = cend;

//        System.out.println("...done - stripTrailingNulls for: " + te);
        return;
    }

    /**
     */
    private void mergeAdjacentMarginalTrends() {
        boolean didMerge = false;
        do {
            didMerge = mergeAdjacentMarginalTrendsImpl();
//            System.out.println("--- Another merge iteration\n" + toString());
        } while (didMerge);
    }
    private boolean mergeAdjacentMarginalTrendsImpl() {
        boolean didMerge = false;

        // Nothing to merge
        if (mTrends.size() <= 1) {
            return false;
        }

        // Flip member to populate anew
        List<TrendEntity> l = mTrends;
        mTrends = new ArrayList<>();

        TrendEntity prev = l.remove(0);
        TrendEntity curr = null;
        while (l.size() > 0) {
            curr = l.remove(0);
            boolean merge = false;

            // They are both NONE
            if (prev.mTrendType == TrendEntity.T_TYPE_NONE
                    && curr.mTrendType == TrendEntity.T_TYPE_NONE) {
                merge = true;
            }
            // They are either NONE or they have less than 2 data points
            else if ((prev.mTrendType == TrendEntity.T_TYPE_NONE || prev.mCount <= 2)
                    && (curr.mTrendType == TrendEntity.T_TYPE_NONE || curr.mCount <= 2)) {
                merge = true;
            }

            // To merge or not to merge
            if (merge) {
                TrendEntity te = new TrendEntity();
                te.initialize(TrendEntity.T_TYPE_NONE, mDPs, prev.mStart, curr.mEnd);
                didMerge = true;
                if (l.size() == 0) {
                    mTrends.add(te);
                }
                prev = te;
            } else {
                /// No merge, just add prev and move forward trying to merge curr with next
                mTrends.add(prev);
                if (l.size() == 0) {
                    mTrends.add(curr);
                }
                prev = curr;
            }
        }
        return didMerge;
     }

    public static String getString(DataPoint[] dp, int start, int end) {
        StringBuffer s = new StringBuffer();
        for (int i=start; i < end; i++) {
            s.append(dp[i].mFridayYYMMDD + ":" + dp[i].mValue);
            if (i+1 < end) {
                s.append(", ");
            }
        }
        return s.toString();
    }
    public String toString() {
        IndentWriter iw = new IndentWriter();
        iw.println("MasterTrend, number of trends: " + mTrends.size());
        iw.push();
        for (TrendEntity te: mTrends) {
            iw.println("Type: " + te.getTrend() + ", start: " + te.mStart + ", end: " + te.mEnd);
            iw.push();
            iw.println(getString(te.mDPs, te.mStart, te.mEnd));
            iw.pop();
        }
        return iw.getString();
    }

    // ***********************************************************************
    // Test Code Below

    static String[] fridays = { "161028", "161104", "161111", "161118", "161125", "161202", "161209", "161216", "161223", "161230", "170106", "170113", "170120" };
    static Double[] v1 = { null, 0.0, null, null, 0.10, -0.10, -0.10, null, 0.10, -0.1, 0.0, 0.10, 0.10 };
    static Double[] v2 = { null, 0.0, null, null, 0.10, 0.10, -0.10, null, 0.10, null, 0.0, null, null };
    static Double[] v3 = { null, 0.0, null, null, 0.10, 0.10, -0.10, null, 0.10, null, 0.0, -0.10, null};

    static Double[] v_up_clean = { 0.1, 0.1, -0.1, 0.1, 0.1, -0.1, 0.5, -0.33, 0.1, 0.1, 0.1, 0.1, 0.1 };
    static Double[] v_up_broke = { 0.1, 0.1, -0.1, 0.1, 0.1, -0.1, 0.5, -0.34, 0.1, 0.1, 0.1, 0.1, 0.1 };
    static Double[] v_up_broke2 = { 0.1, 0.1, -0.1, 0.1, 0.1, -0.1, 0.5, -0.34, -0.1, -0.1, -0.1, 0.1, -0.1 };

    static Double[] v_down_clean = { -0.1, -0.1, 0.1, -0.1, -0.1, 0.1, -0.5, 0.99, -0.1, -0.1, -0.1, -0.1, -0.1 };
    static Double[] v_down_broke = { -0.1, -0.1, 0.1, -0.1, -0.1, 0.1, -0.5, 1.0001, -0.1, -0.1, -0.1, -0.1, -0.1 };
    static Double[] v_down_broke2 = { -0.1, -0.1, 0.1, -0.1, -0.1, 0.1, -0.5, 1.0001, 0.1, 0.1, 0.1, 0.1, 0.1 };

    public static void main(String[] args) {
        System.out.println("MasterTrend, starting...");
        DataPoint[] dp1 = createDPs(v1);
        DataPoint[] dp2 = createDPs(v2);
        DataPoint[] dp3 = createDPs(v3);
        DataPoint[] dp_test = createDPs(v_down_broke2);

        MasterTrend mt1 = new MasterTrend();
        mt1.initialize(dp_test);
//        System.out.println("*** mt1\n" + mt1.toString());

    }
    static DataPoint[] createDPs(Double[] a) {
        DataPoint[] dps = new DataPoint[a.length];
        for (int i=0; i < fridays.length; i++) {
            DataPoint dp = new DataPoint(null, fridays[i], a[i]);
            dps[i] = dp;
        }
        return dps;
    }
}

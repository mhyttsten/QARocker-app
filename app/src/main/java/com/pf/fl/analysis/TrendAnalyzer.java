package com.pf.fl.analysis;

import static com.pf.fl.analysis.TrendEntity.T_TYPE_DOWN;
import static com.pf.fl.analysis.TrendEntity.T_TYPE_UP;

public class TrendAnalyzer {

    // Tests
    // x - Strip trailing nulls, 0.0 or values contrary to trend
    // x - First contrary value to trend brings it below/above trend start value (+, - to <= 1.0)
    // x - Merge adjacent single value trends
    //
    // Todos
    // - Add screen - biggest trenders
    //
    // Todos2
    // - In Firebase, per day, report invalids and funds not extracted
    // - Invent better trend algorithm (two negative in row should not break trend)
    // - Merge bigger trends into one (+, -, +) - but using which criteria?
    //
    // What we want
    // - Identify funds that are currently trending
    // - Identify funds that are switching trends
    // - How strong are the trends (#dps, value change, instability points (nulls))

    // Data we use to calculate trend
    private DataPoint[] mDPs;
    private boolean mIsFirst = true;
    private int mTrendType = -1;

    // Resulting Trend
    private TrendEntity mTrend;

    // Calculation values
    private double mValue = 1;
    private int mStart;
    private int mLastIndexProcessed;
    private boolean mHasAssignedValueCap;
    private double mValueCap;
    private boolean mAmICut;

    /**
     */
    public TrendEntity getTrendResult() {
        if (!mAmICut) {
            cutTrend(mLastIndexProcessed);
        }
        return mTrend;
    }

    /**
     */
    public void initialize(DataPoint[] dps) {
        mDPs = dps;
    }

    public double getFinalValue() {
        if (mTrendType == TrendEntity.T_TYPE_NONE) {
            throw new AssertionError("Cannot be called for trend type NONE");
        }
        return mTrendType == TrendEntity.T_TYPE_UP ? mValue : -mValue;
    }

    /**
     * Input: Can assume non-null. For first call, you can assume a non-zero value
     * Returns: False if needs more data, true if saturated
     *
     * Algo: Never drop beneath previous low --> we can never have 2 consecutive negative values
     * Your Job: Come up with something better...
     */
    public boolean consumeDataPoint(String dataSetDebug, int index) {
        mLastIndexProcessed = index;

        // Calculate new value
        double value = mDPs[index].mValue / 100.0;
        double multiplier = 1.0 + value;
        double newValue = mValue * multiplier;

        // First DP, set trend
        if (mIsFirst) {
            mStart = index;
            mIsFirst = false;
            if (value > 0) {
                mTrendType = T_TYPE_UP;
            } else {
                mTrendType = T_TYPE_DOWN;
            }
            mValue = newValue;
            return true;
        }

        // Direction of new value supports existing trend, continue with next value
        if ((value >= 0.0 && mTrendType == T_TYPE_UP)
                || (value <= 0.0 && mTrendType == T_TYPE_DOWN)) {
            mValue = newValue;
            return true;
        }

        // First value contrary to trend, set caps
        if (!mHasAssignedValueCap) {
            // First contrary value to trend brings us below/above baseline (1.0)
            if (newValue <= 1.0 && mTrendType == T_TYPE_UP
                    ||  newValue >= 1.0 && mTrendType == T_TYPE_DOWN) {
                cutTrend(index);
                return false;
            }

            // Set the first cap
            mValue = newValue;
            mValueCap = mValue;
            mHasAssignedValueCap = true;
            return true;
        }

        // New value does not violate previous cap
        // Assign new cap and continue
        if ((mTrendType == T_TYPE_UP && mValueCap <= newValue)
                || (mTrendType == T_TYPE_DOWN && mValueCap >= newValue)) {
            mValue = newValue;
            mValueCap = mValue;
            return true;
        }

        // New value violates cap, trend is over
        cutTrend(index);
        return false;
    }

    /**
     */
    public void cutTrend(int end) {
        if (mAmICut) {
            return;
        }
        TrendEntity te = new TrendEntity();
        te.initialize(mTrendType, mDPs, mStart, end);
        mTrend = te;
        mAmICut = true;
    }
}

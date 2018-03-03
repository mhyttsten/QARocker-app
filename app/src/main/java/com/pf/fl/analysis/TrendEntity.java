package com.pf.fl.analysis;


public class TrendEntity {
/*
    // TrendParser constants
    public static final int T_TYPE_UP = 1;
    public static final int T_TYPE_DOWN = 2;
    public static final int T_TYPE_NONE = 3;

    public String getTrend() {
        switch(mTrendType) {
            case T_TYPE_NONE: return "None";
            case T_TYPE_DOWN: return "Down";
            case T_TYPE_UP: return "Up";
            default: return "<ERROR>";
        }
    }

    public void initialize(int trendType, DataPoint[] dps, int start, int end) {
        mDPs = dps;
        mStart = start;
        mEnd = end;
        mTrendType = trendType;
    }

    public void calculate() {
        mCount = 0;
        mValue = 1.0;
        for (int i = mStart; i < mEnd; i++) {
            DataPoint dp = mDPs[i];
            if (dp.mValue != null) {
                mCount++;
                double multiplier = 1.0 + (dp.mValue / 100.0);
                mValue *= multiplier;
            }
        }

    }

    public DataPoint[] mDPs;
    public int mStart;
    public int mEnd;
    public int mTrendType;

    public int mCount;
    public double mValue;

    public String toString() {
        return getTrend() + ", " + MasterTrend.getString(mDPs, mStart, mEnd);
    }
*/
}

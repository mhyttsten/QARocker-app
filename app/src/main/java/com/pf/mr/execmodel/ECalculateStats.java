package com.pf.mr.execmodel;

import android.util.Log;

import com.pf.mr.datamodel.StatTermForUser;

import java.util.ArrayList;
import java.util.List;

public class ECalculateStats {
    private static final String TAG = ECalculateStats.class.getSimpleName();

    private List<StatTermForUser> mStats = new ArrayList<>();

    public int mCL1;
    public int mCL2;
    public int mCL3;
    public int mCL4;
    public int mCL5;
    public int mCTotal;
    public int mPercentFinished;

    public void addAll(List<StatTermForUser> l) {
        Log.i(TAG, "Adding a total of: " + l.size() + " stats");
        mStats.addAll(l);
        mCL1 = 0;
        mCL2 = 0;
        mCL3 = 0;
        mCL4 = 0;
        mCL5 = 0;
        mCTotal = 0;
        mPercentFinished = 0;
        calculate();
    }

    private String calculate() {
        mCTotal = mStats.size();
        int pScore = 0;

        for (StatTermForUser t: mStats) {
            switch (t.leitnerBox) {
                case StatTermForUser.LB_1:
                    mCL1++;
                    pScore += 0;
                    break;
                case StatTermForUser.LB_2:
                    mCL2++;
                    pScore += 1;
                    break;
                case StatTermForUser.LB_3:
                    mCL3++;
                    pScore += 2;
                    break;
                case StatTermForUser.LB_4:
                    mCL4++;
                    pScore += 4;
                    break;
                case StatTermForUser.LB_5:
                    mCL5++;
                    pScore += 8;
                    break;
                default:
                    Log.e(TAG, "Unexpected Leitner box: " + t.leitnerBox);
            }
        }

        int maxScore = mCTotal * 8;
        double d = ((double)pScore) / ((double)maxScore);
        d *= 100;
        mPercentFinished = (int)d;
        return null;
    }

    public int getPixels(int availPixels, int count) {
        double d =  ((double)count) / ((double)mCTotal);
        double rd = ((double)availPixels) * d;
        return (int)rd;
    }
}

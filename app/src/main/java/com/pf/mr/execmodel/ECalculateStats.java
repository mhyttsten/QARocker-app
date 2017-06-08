package com.pf.mr.execmodel;

import android.util.Log;

import com.pf.mr.SingletonMR;
import com.pf.mr.datamodel.StatTermForUser;
import com.pf.mr.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ECalculateStats {
    private static final String TAG = ECalculateStats.class.getSimpleName();

    private ESet mESet;

    public ECalculateStats(ESet eset) {
        mESet = eset;
        mTerms.addAll(mESet.mETermsAll);
    }

    private List<ETerm> mTerms = new ArrayList<>();

    public int mCL0;
    public int mCL1;
    public int mCL2;
    public int mCL3;
    public int mCL4;
    public int mCL5;
    public int mCTotal;
    public int mDue;
    public int mPercentFinished;

    public void calculate() {
        long timeNow = System.currentTimeMillis();

        mCTotal = mTerms.size();
        int pScore = 0;

        for (ETerm t : mTerms) {
            if (timeNow > t.getStat().nextRehearsalTime) {
                mDue++;
            } else if (t.getStat().leitnerBox == StatTermForUser.LB_0) {
                mDue++;
            }

            switch (t.getStat().leitnerBox) {
                case StatTermForUser.LB_0:
                    mCL0++;
                    pScore += 0;
                    break;
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
                    Log.e(TAG, "Unexpected Leitner box: " + t.getStat().leitnerBox);
            }
        }

        int maxScore = mCTotal * 8;
        double d = ((double) pScore) / ((double) maxScore);
        d *= 100;
        mPercentFinished = (int) d;
    }

    public String toString() {
        return "Calculated all for set: " + mESet.getSetTitle()
                + "\n...mStats.size: " + mTerms.size()
                + "\n...mPercentFinished: " + mPercentFinished
                + "\n...mTotal: " + mCTotal
                + "\n...mDue: " + mDue
                + "\n...mCL0: " + mCL0
                + "\n...mCL1: " + mCL1
                + "\n...mCL2: " + mCL2
                + "\n...mCL3: " + mCL3
                + "\n...mCL4: " + mCL4
                + "\n...mCL5: " + mCL5;
    }
}

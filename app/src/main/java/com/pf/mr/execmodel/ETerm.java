package com.pf.mr.execmodel;

import android.net.NetworkInfo;
import android.util.Log;

import com.firebase.client.Firebase;
import com.pf.mr.datamodel.QLTerm;
import com.pf.mr.datamodel.StatTermForRaw;
import com.pf.mr.datamodel.StatTermForUser;
import com.pf.mr.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by magnushyttsten on 3/25/16.
 */
public class ETerm {
    public static final String TAG = ETerm.class.getSimpleName();

    public static final int AS_UNDEFINED = 0;
    public static final int AS_NO_CLUE = 1;
    public static final int AS_KNEW_IT = 2;
    public static final int AS_NAILED_IT = 3;

    private QLTerm mQA;
    private String mSetTitle;
    private StatTermForUser mStat;
    private boolean mIsDoneForToday;
    private boolean mHasLeitnerBeenAdjusted;

    private long mStartTimer;

    public ETerm(String setTitle, QLTerm qa, StatTermForUser stat) {
        mQA = qa;
        mStat = stat;
        mSetTitle = setTitle;
    }

    public String getQ() { return mQA.term; }
    public String getA() { return mQA.definition; }

    public static List<ETerm> getQAs(long pSetId,
                                     String pSetTitle,
                                     String pEmail,
                                     List<QLTerm> eQAs,
                                     List<StatTermForUser> stats) {

        List<ETerm> al = new ArrayList<>();
        for (QLTerm eQA: eQAs) {
            StatTermForUser stat = StatTermForUser.getInitialValues(pSetId, eQA.id, pEmail);
            if (stats != null) {
                for (StatTermForUser aStat : stats) {
                    if (eQA.id == aStat.termId) {
                        stat = aStat;
                    }
                }
            }
            al.add(new ETerm(pSetTitle, eQA, stat));
        }
        return al;
    }

    public StatTermForUser getStat() { return mStat; }
    public boolean isDoneForToday() { return mIsDoneForToday; }
    public void setQuestionDisplayedTimer() { mStartTimer = System.currentTimeMillis(); }

    public void setAnswerGiven(int answer) {
        long endTimer = System.currentTimeMillis();
        long duration = endTimer - mStartTimer;

        int leitnerBoxBefore = mStat.leitnerBox;
        int leitnerBoxAfter = leitnerBoxBefore;

        long newNextRehearsalTime = mStat.nextRehearsalTime;

        // Calculate new average time
        long newTotalTime = mStat.answerTimeTotal + duration;
        long totalQs = mStat.acountNoClue + mStat.acountKnewIt + mStat.acountNailedIt + 1;
        long newAverageTime = newTotalTime / totalQs;
        Log.i(TAG, "totalTime: " + newTotalTime + ", averageTime: " + newAverageTime + ", qs: " + totalQs);

        long newNoClueCount = mStat.acountNoClue;
        long newKnewItCount = mStat.acountKnewIt;
        long newNailedItCount = mStat.acountNailedIt;

        switch (answer) {
            case AS_NO_CLUE:
                newNoClueCount++;
                if (!mHasLeitnerBeenAdjusted) {
                    if (leitnerBoxBefore > StatTermForUser.LB_1) {
                        leitnerBoxAfter--;
                    } else if (leitnerBoxBefore == StatTermForUser.LB_0) {
                        leitnerBoxAfter = StatTermForUser.LB_1;
                    }
                    mHasLeitnerBeenAdjusted = true;
                }
                // If we answered wrong, we rehearse as quickly as possible regardless of box
                newNextRehearsalTime = Constants.NEXT_REHEARSAL_TIME_LB1();
                break;
            case AS_KNEW_IT:
                newKnewItCount++;
                mIsDoneForToday = true;
                if (!mHasLeitnerBeenAdjusted) {
                    if (leitnerBoxBefore == StatTermForUser.LB_0
                            || leitnerBoxAfter == StatTermForUser.LB_1) {
                        leitnerBoxAfter = StatTermForUser.LB_2;
                    } else {
                        leitnerBoxAfter++;
                    }
                    newNextRehearsalTime = getNewRehearsalTime(leitnerBoxAfter);
                    mHasLeitnerBeenAdjusted = true;
                }
                break;
            case AS_NAILED_IT:
                newNailedItCount++;
                mIsDoneForToday = true;
                if (!mHasLeitnerBeenAdjusted) {
                    if (leitnerBoxBefore == StatTermForUser.LB_0
                            || leitnerBoxBefore == StatTermForUser.LB_1) {
                        leitnerBoxAfter = StatTermForUser.LB_4;
                    } else {
                        if (leitnerBoxBefore < StatTermForUser.LB_5) {
                            leitnerBoxAfter++;
                        }
                    }
                    mHasLeitnerBeenAdjusted = true;
                    newNextRehearsalTime = getNewRehearsalTime(leitnerBoxAfter);
                } else {
                    // Nailed it after an error, push to L2
                    newNextRehearsalTime = getNewRehearsalTime(StatTermForUser.LB_2);
                }
                break;
            default:
                Log.e(TAG, "Unexpected answer type: " + answer);
                break;
        }

        mStat.updateData(
                leitnerBoxAfter,
                newNextRehearsalTime,
                newNoClueCount, newKnewItCount, newNailedItCount,
                newTotalTime, newAverageTime);

        // Store in StatForUser table
        Firebase forUser = new Firebase(Constants.FPATH_STATFORUSER())
                .child(String.valueOf(mStat.setId))
                .child(Constants.EMAIL_TO_FIREBASEPATH(mStat.email))
                .child(String.valueOf(mStat.termId));
        forUser.setValue(mStat);

        // Store in StatForRaw table
        StatTermForRaw statRaw = new StatTermForRaw(
                mStat.setId, mStat.termId, mStat.email,
                leitnerBoxBefore, leitnerBoxAfter,
                answer, duration);
        Firebase forRaw = new Firebase(Constants.FPATH_STATFORRAW())
                .child(String.valueOf(mStat.setId))
                .child(String.valueOf(mStat.termId))
                .child(String.valueOf(Constants.EMAIL_TO_FIREBASEPATH(mStat.email))
                        + "_" + String.valueOf(System.currentTimeMillis()));
        forRaw.setValue(statRaw);
    }

    private static long getNewRehearsalTime(long leitnerBox) {
        if (leitnerBox == StatTermForUser.LB_1) {
            return Constants.NEXT_REHEARSAL_TIME_LB1();
        }
        if (leitnerBox == StatTermForUser.LB_2) {
            return Constants.NEXT_REHEARSAL_TIME_LB2();
        }
        if (leitnerBox == StatTermForUser.LB_3) {
            return Constants.NEXT_REHEARSAL_TIME_LB3();
        }
        if (leitnerBox == StatTermForUser.LB_4) {
            return Constants.NEXT_REHEARSAL_TIME_LB4();
        }
        if (leitnerBox == StatTermForUser.LB_5) {
            return Constants.NEXT_REHEARSAL_TIME_LB5();
        }
        return -1;
    }
}

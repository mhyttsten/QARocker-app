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
    private StatTermForUser mStat;
    private int mLastAnswer;

    private boolean hasLeitnerBeenAdjusted = false;

    private long mStartTimer;

    public ETerm(QLTerm qa, StatTermForUser stat) {
        mQA = qa;
        mStat = stat;
    }

    public String getQ() { return mQA.term; }
    public String getA() { return mQA.definition; }

    public static List<ETerm> getQAs(long pSetId,
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
            al.add(new ETerm(eQA, stat));
        }
        return al;
    }

    public StatTermForUser getStat() { return mStat; }

    public void setQuestionDisplayedTimer() {
        mStartTimer = System.currentTimeMillis();
    }

    public void setAnswerGiven(int answer) {
        long endTimer = System.currentTimeMillis();
        long duration = endTimer - mStartTimer;

        mLastAnswer = answer;
        int leitnerBoxBefore = mStat.leitnerBox;
        int leitnerBoxAfter = leitnerBoxBefore;

        long newNextRehearsalTime = mStat.nextRehearsalTime;

        // Calculate new average time
        long newTotalTime = mStat.answerTimeTotal + duration;
        long newAverageTime =
                newTotalTime /
                ((mStat.acountNoClue + mStat.acountKnewIt + mStat.acountNailedIt) + 1);

        long newNoClueCount = mStat.acountNoClue;
        long newKnewItCount = mStat.acountKnewIt;
        long newNailedItCount = mStat.acountNailedIt;

        switch (answer) {
            case AS_NO_CLUE:
                newNoClueCount++;
                if (!hasLeitnerBeenAdjusted) {
                    if (leitnerBoxBefore > StatTermForUser.LB_1) {
                        leitnerBoxAfter--;
                    } else if (leitnerBoxBefore == StatTermForUser.LB_0) {
                        leitnerBoxAfter++;
                    }
                }
                newNextRehearsalTime = -1;
                break;
            case AS_KNEW_IT:
                if (!hasLeitnerBeenAdjusted) {
                    if (leitnerBoxBefore == StatTermForUser.LB_0) {
                        leitnerBoxAfter = StatTermForUser.LB_2;
                    } else {
                        leitnerBoxAfter++;
                    }
                } else {
                    // No matter how bad it's been going, a KNEW_IT always brings us to L2
                    if (leitnerBoxAfter == StatTermForUser.LB_0
                            || leitnerBoxAfter == StatTermForUser.LB_1) {
                        leitnerBoxAfter = StatTermForUser.LB_2;
                    }
                }
                newKnewItCount++;
                newNextRehearsalTime = getNewRehearsalTime(leitnerBoxAfter);
                break;
            case AS_NAILED_IT:
                if (!hasLeitnerBeenAdjusted) {
                    if (leitnerBoxBefore == StatTermForUser.LB_0) {
                        leitnerBoxAfter = StatTermForUser.LB_4;
                    } else {
                        leitnerBoxAfter++;
                    }
                }
                newNailedItCount++;
                newNextRehearsalTime = getNewRehearsalTime(leitnerBoxAfter);
                break;
            default:
                Log.e(TAG, "Unexpected answer type: " + answer);
                break;
        }
        hasLeitnerBeenAdjusted = true;

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

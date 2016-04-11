package com.pf.mr.datamodel;

import com.pf.mr.utils.Constants;

/**
 * Created by magnushyttsten on 3/29/16.
 */
public class StatTermForUser {

    // Leitner boxes
    public static final int LB_0 = 0; // Never tested
    public static final int LB_1 = 1; // Always needs rehearsal
    public static final int LB_2 = 2; // Wait 12 hours
    public static final int LB_3 = 3; // Wait 7 days
    public static final int LB_4 = 4; // Wait 28 days
    public static final int LB_5 = 5; // Wait 365 days
    // NC (no clue), KI (knew it), NI (nailed it)

    public long setId;
    public long termId;
    public String email;

    public int leitnerBox;
    public long nextRehearsalTime;
    public String getNextRehearsalTimeReadable() { return new java.util.Date(nextRehearsalTime).toString(); }
    public void setNextRehearsalTimeReadable(String s) { }

    public long acountNoClue;
    public long acountKnewIt;
    public long acountNailedIt;

    public long answerTimeTotal;
    public long answerTimeAverage;

    public void updateData(int pLeitnerBox,
                           long pNextRehearsalTime,
                           long pNoClueCount,
                           long pKnewItCount,
                           long pNailedItCount,
                           long pAnswerTimeTotal,
                           long pAnswerTimeAverage) {
        leitnerBox = pLeitnerBox;
        nextRehearsalTime = pNextRehearsalTime;
        acountNoClue = pNoClueCount;
        acountKnewIt = pKnewItCount;
        acountNailedIt = pNailedItCount;
        answerTimeTotal = pAnswerTimeTotal;
        answerTimeAverage = pAnswerTimeAverage;
    }

    public StatTermForUser() { }

    public StatTermForUser(
            long pSetId,
            long pTermId,
            String pEmail,
            int pLeitnerBox,
            long pNextRehearsalTime,
            long pNoClueCount,
            long pKnewItCount,
            long pNailedItCount,
            long pAnswerTimeTotal,
            long pAnswerTimeAverage) {
        setId = pSetId;
        termId = pTermId;
        email = pEmail;
        leitnerBox = pLeitnerBox;
        nextRehearsalTime = pNextRehearsalTime;
        acountNoClue = pNoClueCount;
        acountKnewIt = pKnewItCount;
        acountNailedIt = pNailedItCount;
        answerTimeTotal = pAnswerTimeTotal;
        answerTimeAverage = pAnswerTimeAverage;
    }

    public static StatTermForUser getInitialValues(long pSetId, long pTermId, String pEmail) {
        return new StatTermForUser(
                pSetId, pTermId, pEmail,
                LB_0,
                -1,       // Next time is now
                0, 0, 0,  // No clue, knew it, nailed it
                0, 0);    // Answer times are all 0
    }

}

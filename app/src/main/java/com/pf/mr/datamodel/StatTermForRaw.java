package com.pf.mr.datamodel;

/**
 * Created by magnushyttsten on 3/29/16.
 */
public class StatTermForRaw {
    public long setId;
    public long termId;
    public String email;

    public long leitnerBoxBefore;
    public long leitnerBoxAfter;
    public long asCorrectCode;
    public long answerDuration;

    public StatTermForRaw() { }

    public StatTermForRaw(
            long pSetId,
            long pTermId,
            String pEmail,
            long pLeitnerBoxBefore,
            long pLeitnerBoxAfter,
            long pASCorrectCode,
            long pAnswerDuration) {
        setId = pSetId;
        termId = pTermId;
        email = pEmail;
        leitnerBoxBefore = pLeitnerBoxBefore;
        leitnerBoxAfter = pLeitnerBoxAfter;
        asCorrectCode = pASCorrectCode;
        answerDuration = pAnswerDuration;
    }

}
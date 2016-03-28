package com.pf.mr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by magnushyttsten on 3/25/16.
 */
public class E_Term {

    public static final int AS_UNDEFINED = 0;
    public static final int AS_NO_CLUE = 1;
    public static final int AS_KNEW_IT = 2;
    public static final int AS_NAILED_IT = 3;

    public QL_Term mQA;
    public int mLastAnswer;

    public E_Term(QL_Term qa) {
        mQA = qa;
    }

    public String getQ() { return mQA.term; }
    public String getA() { return mQA.definition; }
    public void setLastAnswer(int answer) { mLastAnswer = answer; }

    public static List<E_Term> getQAs(List<QL_Term> eQAs) {
        List<E_Term> al = new ArrayList<>();
        for (QL_Term eQA: eQAs) {
            al.add(new E_Term(eQA));
        }
        return al;
    }
}

package com.example.android.animationsdemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by magnushyttsten on 3/25/16.
 */
public class QA {

    public static final int AS_UNDEFINED = 0;
    public static final int AS_NO_CLUE = 1;
    public static final int AS_KNEW_IT = 2;
    public static final int AS_NAILED_IT = 3;

    public E_QA mQA;
    public int mLastAnswer;

    public QA(E_QA qa) {
        mQA = qa;
    }

    public String getQ() { return mQA.getQ(); }
    public String getA() { return mQA.getA(); }
    public void setLastAnswer(int answer) { mLastAnswer = answer; }

    public static List<QA> getQAs(List<E_QA> eQAs) {
        List<QA> al = new ArrayList<>();
        for (E_QA eQA: eQAs) {
            al.add(new QA(eQA));
        }
        return al;
    }

    public static List<QA> getSampleDeck(String qualifier) {
        QA[] qas = new QA[] {
                new QA(new E_QA(null, qualifier + "_Q1", qualifier + "_A1")),
                new QA(new E_QA(null, qualifier + "_Q2", qualifier + "_A2")),
                new QA(new E_QA(null, qualifier + "_Q3", qualifier + "_A3"))
        };
        List<QA> r = new ArrayList<>();
        r.addAll(Arrays.asList(qas));

        return r;
    }
}

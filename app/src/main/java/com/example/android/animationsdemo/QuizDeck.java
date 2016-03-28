package com.example.android.animationsdemo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by magnushyttsten on 3/25/16.
 */
public class QuizDeck {

    public E_Quiz mQ;

    public QuizDeck(E_Quiz q) {
        mQ = q;
        mQAs = QA.getQAs(q.mQAs);
    }

    public boolean hasNext() {
        return mQAs.size() > 0;
    }

    public QA next() {
        return mQAs.remove(0);
    }

    public void reportAnswer(QA qa) {
        // For now, lets just saturate the cards
        // mQAs.add(qa);
    }

    /*
    public static QuizDeck getSample(String name) {
        QuizDeck rd = new QuizDeck(name);
        rd.mQAs = QA.getSampleDeck(name);
        return rd;
    }
*/
    private List<QA> mQAs = new ArrayList<>();
}

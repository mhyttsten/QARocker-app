package com.pf.mr;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by magnushyttsten on 3/25/16.
 */
public class E_Set {

    public QL_Set mQ;
    private List<E_Term> mETerms = new ArrayList<>();

    public E_Set(QL_Set q) {
        mQ = q;
        mETerms = E_Term.getQAs(mQ.terms);
    }

    public boolean hasNext() {
        return mETerms.size() > 0;
    }

    public E_Term next() {
        return mETerms.remove(0);
    }

    public void reportAnswer(E_Term ETerm) {
        // For now, lets just saturate the cards
        // mETerms.add(ETerm);
    }
}

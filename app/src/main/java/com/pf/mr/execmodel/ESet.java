package com.pf.mr.execmodel;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.pf.mr.datamodel.QLSet;
import com.pf.mr.datamodel.StatTermForUser;
import com.pf.mr.utils.Constants;
import com.pf.mr.utils.IndentWriter;
import com.pf.mr.utils.MPair;
import com.pf.mr.utils.Misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ESet {
    private static final String TAG = ESet.class.getSimpleName();

    private QLSet mSet;
    private String mSetTitle;

    public List<ETerm> mETermsAll = new ArrayList<>();
    private List<ETerm> mETermsDue = new ArrayList<>();
    private List<ETerm> mETermsNotDue = new ArrayList<>();
    private List<ETerm> mETermsNew = new ArrayList<>();

    private List<ETerm> mETermsCRound = new ArrayList<>();
    private List<ETerm> mETermsCRound_PushedBack = new ArrayList<>();

    public int getTodoCount() {
        return mETermsDue.size()
                + mETermsNew.size() + mETermsCRound.size() + mETermsCRound_PushedBack.size();
    }

    /**
     * Called when creating a set that contains sets (e.g. "All")
     */
    public ESet(String title, List<ESet> esets) {
        mSetTitle = title;
        // This method will be destructive to esets elements

        // Get all due and sort in next rehearsal time order
        for (ESet set: esets) {
            mETermsDue.addAll(set.mETermsDue);
            mETermsNotDue.addAll(set.mETermsNotDue);
        }
        // sortETermList(title, mETermsDue);

        // Get all new, 1 from each set
        List<List<ETerm>> esetsNew = new ArrayList<>();
        for (ESet set: esets) {
            List<ETerm> nterm = new ArrayList<ETerm>();
            nterm.addAll(set.mETermsNew);
            esetsNew.add(nterm);
        }
        boolean found = true;
        while (found) {
            found = false;
            for (List<ETerm> terms: esetsNew) {
                if (terms.size() > 0) {
                    mETermsNew.add(terms.remove(0));
                    found = true;
                }
            }
        }
        populateAll();
    }

    /**
     * Called to instantiate an actual set with terms
     */
    public ESet(QLSet set, String email, List<StatTermForUser> statTerms) {
        Log.i(TAG, "Creating primitive set: " + set.title + ", stats.size: " + statTerms.size());
        mSet = set;

        List<ETerm> qAs = ETerm.getQAs(set.id, set.title, email, set.terms, statTerms);
        Log.i(TAG, "...all stats.size: " + qAs.size());
        sortETermList(set.title, qAs);

        List<ETerm> lb0 = new ArrayList<>();
        List<ETerm> lb1 = new ArrayList<>();
        long timeNow = System.currentTimeMillis();
        boolean first = true;
        for(ETerm e: qAs) {
            boolean isDue = timeNow > e.getStat().nextRehearsalTime;
            if (e.getStat().leitnerBox == StatTermForUser.LB_0) {
                lb0.add(e);
                if (first) {
                    first = false;
                    // System.out.println("###-10 Q: " + e.getQ());
                }
            } else {
                if (isDue) {
                    if (e.getStat().leitnerBox == StatTermForUser.LB_1) {
                        mETermsNew.add(e);
                    } else {
                        mETermsDue.add(e);
                    }
                } else {
                    mETermsNotDue.add(e);
                }
            }
        }

        // Box 1 should come before 0
        mETermsNew.addAll(lb0);

        populateAll();

        Log.i(TAG, "...mETermsNew.size: " + mETermsNew.size());
        Log.i(TAG, "...mETermsDue.size: " + mETermsDue.size());
        Log.i(TAG, "...mETermsNotDue.size: " + mETermsNotDue.size());

        // String s1 = printList("*** New list", mETermsNew);
        // String s2 = printList("*** Due list", mETermsDue);
    }

    private void populateAll() {
//        ETerm.printSome("NEW", mETermsNew);
//        ETerm.printSome("DUE", mETermsDue);
        mETermsAll.addAll(mETermsNew);
        mETermsAll.addAll(mETermsDue);
        mETermsAll.addAll(mETermsNotDue);
    }

    public void rescaleImages() {
        for( ETerm e: mETermsAll) {
            e.rescaleImages();
        }
    }

    private void refillCurrentRound() {
        assert(mETermsCRound.size() == 0);

        // Add all spilling over from old round
        mETermsCRound.addAll(mETermsCRound_PushedBack);
        mETermsCRound_PushedBack.clear();
        if (mETermsCRound.size() >= Constants.TERMS_PER_ROUND) { return; }

        // Make sure to include at least a new one...
        if (!containsNew(mETermsCRound) && mETermsNew.size() > 0) {
            mETermsCRound.add(mETermsNew.remove(0));
        }
        if (mETermsCRound.size() >= Constants.TERMS_PER_ROUND) { return; }

        // Fill remaining by any means possible
        boolean added = true;
        while (added
                && mETermsCRound.size() < Constants.TERMS_PER_ROUND) {
            added = false;
            if (mETermsDue.size() > 0) {
                mETermsCRound.add(mETermsDue.remove(0));
                added = true;
            } else if (mETermsNew.size() > 0) {
                mETermsCRound.add(mETermsNew.remove(0));
                added = true;
            }
        }
    }

    /**
     */
    private static boolean containsNew(List<ETerm> al) {
        for (ETerm e: al) {
            if (e.getStat().leitnerBox <= 1) { return true; }
        }
        return false;
    }

    /*
     */
    public boolean hasNext() {
        if (mETermsCRound.size() > 0) {
            return true;
        } else {
            refillCurrentRound();
        }
        if (mETermsCRound.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     */
    public ETerm next() {
        return mETermsCRound.remove(0);

    }

    /**
     */
    public void reportAnswer(ETerm eterm) {
        long timeNow = System.currentTimeMillis();
        if (!eterm.isDoneForToday()) {
            mETermsCRound_PushedBack.add(eterm);
        }
    }

    /**
     */
    private static List<MPair<ETerm, ETerm>> mHistory = new ArrayList<>();
    public static void sortETermList(final String setName, final List<ETerm> eterms) {
        long timeNow = System.currentTimeMillis();
        int countNeverTested = 0;
        for (ETerm e: eterms) {
            if (e.getStat().nextRehearsalTime == -1) {
                countNeverTested++;
            }
        }
        countNeverTested = (countNeverTested * 1000) + 5000; // 100ms spacing, with 5s margin to now
        long timeStart = timeNow - countNeverTested;
        for (ETerm e: eterms) {
            if (e.getStat().nextRehearsalTime == -1) {
                e.getStat().nextRehearsalTime = timeStart;
                timeStart += 1000; // just add 10ms
            }
        }

        final IndentWriter iwError = new IndentWriter();
        iwError.setIndentChar('.');
        iwError.println("*** Sorting set: " + setName);
        iwError.push();
        try {
            mHistory = new ArrayList<>();
            Collections.sort(eterms, new Comparator<ETerm>() {
                @Override
                public int compare(ETerm lhs, ETerm rhs) {
                    int r = -1;
                    mHistory.add(new MPair<>(lhs, rhs));
                    r = compareImpl(lhs, rhs);
                    iwError.println(
                            "r: " + r + ". "
                            + String.format("%,d", lhs.getStat().nextRehearsalTime)
                            + " (" + new java.util.Date(lhs.getStat().nextRehearsalTime).toString() + ")"
                            + ", \"" + Misc.getPartStr(lhs.mQA.term)+ "\", "
                            + String.format("%,d", rhs.getStat().nextRehearsalTime)
                            + " (" + new java.util.Date(rhs.getStat().nextRehearsalTime).toString() + ")"
                            + ", \"" + Misc.getPartStr(rhs.mQA.term) + "\"");
                    return r;
                }
                private int compareImpl(ETerm lhs, ETerm rhs) {
                    if (lhs.getStat() == null || rhs.getStat() == null) {
                        throw new AssertionError("Unexpected: Neither LHS/RHS stat object should be null");
                    }
                    long lhsTime = lhs.getStat().nextRehearsalTime;
                    long rhsTime = rhs.getStat().nextRehearsalTime;
                    long result =  lhsTime-rhsTime;
                    return (int)(result/1000);
                }
            });
        } catch (IllegalArgumentException exc) {
            IndentWriter iw = new IndentWriter();
            iw.setFlowChar('.');
            iw.println("IllegalArgument exception when sorting terms");
            iw.push();
            int start = 0;
            int end = mHistory.size();
            if (end - start > 10) {
                start = end - 10;
            }
            for (int i=start; i < end; i++) {
                MPair<ETerm, ETerm> p = mHistory.get(i);
                iw.println(p.mO1.getStat().nextRehearsalTime + ", " + Misc.getPartStr(p.mO1.mQA.term) +
                        ", " + p.mO2.getStat().nextRehearsalTime + ", " + Misc.getPartStr(p.mO2.mQA.term));
            }
            MPair<ETerm, ETerm> p = mHistory.get(mHistory.size()-1);
            iw.println("Last two\n"
                    + p.mO1.getStat().nextRehearsalTime + ", " + Misc.getPartStr(p.mO1.mQA.term) +
                    ", " + p.mO2.getStat().nextRehearsalTime + ", " + Misc.getPartStr(p.mO2.mQA.term));
            iw.println();
            iw.pop();
            iw.println("Done");
            FirebaseCrash.log(iw.getString());
            Log.e(TAG, iw.getString());
            Log.e(TAG, iwError.getString());
            Log.e(TAG, "That was the end of everything");
            throw exc;
        }
    }

    /**
     */
    public String getSetTitle() {
        if (mSet == null && mSetTitle == null) {
            Log.wtf(TAG, "Calling getSetTitle on a composite ESet",
                    new Exception("Calling getSetTitle on a composite ESet"));
        }
        return mSet != null ? mSet.title : mSetTitle;
    }


    /**
     */
    public long getSetId() {
        if (mSet == null) {
            Log.wtf(TAG, "Calling getSetId on a composite ESet",
                    new Exception("Calling getSetId on a composite ESet"));
        }
        return mSet.id;
    }
}

package com.pf.mr.execmodel;

import android.util.Log;

import com.pf.mr.datamodel.QLSet;
import com.pf.mr.datamodel.StatTermForUser;
import com.pf.mr.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ESet {

    private QLSet mSet;

    public List<StatTermForUser> mStatsAll = new ArrayList<>();

    private List<ETerm> mETermsDue = new ArrayList<>();
    private List<ETerm> mETermsNew = new ArrayList<>();

    private List<ETerm> mETermsCRound = new ArrayList<>();
    private List<ETerm> mETermsCRound_PushedBack = new ArrayList<>();

    private static final String TAG = ESet.class.getSimpleName();

    public ESet(List<ESet> esets) {
        // This method will be destructive to esets elements

        // Get all due and sort in next rehearsal time order
        for (ESet set: esets) {
            mETermsDue.addAll(set.mETermsDue);
        }
        sortETermList(mETermsDue);

        // Get all new, 1 from each set
        boolean found = true;
        while (found) {
            found = false;
            for (ESet set: esets) {
                if (set.mETermsNew.size() > 0) {
                    mETermsNew.add(set.mETermsNew.remove(0));
                    found = true;
                }
            }
        }
    }

    public ESet(QLSet set, String email, List<StatTermForUser> statTerms) {
        mSet = set;
        mStatsAll.addAll(statTerms);

        List<ETerm> qAs = ETerm.getQAs(set.id, set.title, email, set.terms, statTerms);
        sortETermList(qAs);

        List<ETerm> lb0 = new ArrayList<>();
        List<ETerm> lb1 = new ArrayList<>();
        for(ETerm e: qAs) {
            if (e.getStat().leitnerBox == StatTermForUser.LB_0) {
                lb0.add(e);
            } else if (e.getStat().leitnerBox == StatTermForUser.LB_1) {
                lb1.add(e);
            } else {
                mETermsDue.add(e);
            }
        }
        // Box 1 should come before 0
        mETermsNew.addAll(lb1);
        mETermsNew.addAll(lb0);

        // Remove any entries that are not due
        long timeNow = System.currentTimeMillis();
        while (mETermsDue.size() > 0) {
            ETerm eterm = mETermsDue.get(0);
            if (eterm.getStat().nextRehearsalTime > timeNow) {
                mETermsDue.remove(0);
            } else {
                break;
            }
        }
    }

    /**
     *
     */
    private void refillCurrentRound() {
        assert(mETermsCRound.size() == 0);

        // Add all spilling over from old round
        mETermsCRound.addAll(mETermsCRound_PushedBack);
        mETermsCRound_PushedBack = new ArrayList<ETerm>();
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
    public static void sortETermList(List<ETerm> eterms) {
        Collections.sort(eterms, new Comparator<ETerm>() {
            @Override
            public int compare(ETerm lhs, ETerm rhs) {
                if (lhs.getStat() == null) {
                    return -1;
                }
                if (rhs.getStat() == null) {
                    return -1;
                }
                return (int) (lhs.getStat().nextRehearsalTime - rhs.getStat().nextRehearsalTime);
            }
        });
    }


    /**
     */
    public String getSetTitle() {
        if (mSet == null) {
            Log.wtf(TAG, "Calling getSetTitle on a composite ESet",
                    new Exception("Calling getSetTitle on a composite ESet"));
        }
        return mSet.title;
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

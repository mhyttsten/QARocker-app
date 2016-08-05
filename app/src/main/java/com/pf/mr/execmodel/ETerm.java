package com.pf.mr.execmodel;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pf.mr.R;
import com.pf.mr.datamodel.QLTerm;
import com.pf.mr.datamodel.StatTermForRaw;
import com.pf.mr.datamodel.StatTermForUser;
import com.pf.mr.utils.Constants;
import com.pf.mr.utils.Misc;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class ETerm {
    public static final String TAG = ETerm.class.getSimpleName();

    public static final int AS_UNDEFINED = 0;
    public static final int AS_NO_CLUE = 1;
    public static final int AS_KNEW_IT = 2;
    public static final int AS_NAILED_IT = 3;

    private QLTerm mQA;
    private List<QAElem> mQElems;
    private List<QAElem> mAElems;
    public String mSetTitle;
    public long mRank;
    private StatTermForUser mStat;
    private boolean mIsDoneForToday;
    private boolean mHasLeitnerBeenAdjusted;

    private long mStartTimer;

    public void rescaleImages() {
        if (mQElems != null) {
            for (QAElem e: mQElems) {
                if (e.mBitmapScaled != null) {
                    e.mBitmapScaled = null;
                }
            }
        }
        if (mAElems != null) {
            for (QAElem e: mAElems) {
                if (e.mBitmapScaled != null) {
                    e.mBitmapScaled = null;
                }
            }
        }
    }

    public static final Comparator ETerm_Comparator = new Comparator<ETerm>() {
        public int compare(ETerm t1, ETerm t2) {
            return (int)(t1.mRank  - t2.mRank);
        }
    };

    public void renderQ(Activity a, TextView tw) {
        mQElems = initializeAndDisplay(a, tw, mQElems, mQA.term);
    }
    public void renderA(Activity a, TextView tw) {
        mAElems = initializeAndDisplay(a, tw, mAElems, mQA.definition);
    }

    // How to really do this
    //    https://gist.github.com/samtstern/2e3870ed7896eb73d21c95a4c2e7fa25
    private List<QAElem> initializeAndDisplay(Activity a,
                                              TextView tv,
                                              List<QAElem> qaElems,
                                              String t) {
        if (qaElems != null) {
            displayQAElems(a, tv, qaElems);
            return qaElems;
        }

        qaElems = getQAElems(t);

        if (qaElems == null || qaElems.size() == 0) {
            Log.e(TAG, "QAElems are NULL for string: " + t);
        }

        final List<StorageReference> downloadSRs = getDownloadSRs(qaElems);

        // Nothing to download, then do it the easy way
        if (downloadSRs.size() == 0) {
            displayQAElems(a, tv, qaElems);
            return qaElems;
        }

        final List<Class> finishedProgress = Collections.synchronizedList(new ArrayList<Class>());
        final StringBuffer excStrb = new StringBuffer();
        for (StorageReference srElem: downloadSRs) {
            taskDownloadIterator(a, tv, qaElems, srElem, excStrb, downloadSRs.size(), finishedProgress);
        }
        return qaElems;
    }

    private void taskDownloadIterator(
            final Activity a,
            final TextView tv,
            final List<QAElem> qaElems,
            final StorageReference srElem,
            final StringBuffer excStrb,
            final int finishedCount,
            final List<Class> finishedProgress) {
        Task<byte[]> srTask = srElem.getBytes(2 << 23);

        srTask.addOnCompleteListener(a, new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if (task.isSuccessful()) {
                    QAElem elem = getQAElem(qaElems, srElem);
                    byte[] bytes = task.getResult();
                    if (bytes == null || bytes.length == 0) {
                        throw new Error("Bytes is null or len=0 for image: \" + srElem.getPath()");
                    }
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap bmScaled = adjustBitmapSize(bm, tv);
                    elem.mBitmapOriginal = bm;
                    elem.mBitmapScaled = bmScaled;
                } else {
                    excStrb.append(task.getException() + "\n");
                }

                // All downloads finished?
                finishedProgress.add(Void.class);
                if (finishedProgress.size() >= finishedCount) {
                    if (excStrb.length() > 0) {
                        throw new Error(excStrb.toString());
                    } else {
                        displayQAElems(a, tv, qaElems);
                    }
                }
            }
        });
    }

    private List<StorageReference> getDownloadSRs(List<QAElem> qaElems) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        List<StorageReference> r = new ArrayList<>();
        for (QAElem e: qaElems) {
            if (e.mBitmapURL != null) {
                StorageReference sr = storage.getReferenceFromUrl(e.mBitmapURL);
                r.add(sr);
                e.mStorageReference = sr;
            }
        }
        return r;
    }

    private QAElem getQAElem(List<QAElem> qaElems, StorageReference sr) {
        String url = sr.getPath();
        for (QAElem e: qaElems) {
            if (e.mStorageReference != null
                    && e.mStorageReference.equals(sr)
                    && e.mBitmapOriginal == null) {
                return e;
            }
        }
        return null;
    }

    private void displayQAElems(Activity a, TextView tv, List<QAElem> qaElems) {
        // Make sure we have correct bitmap sizes
        for (QAElem e: qaElems) {
            if (e.mBitmapOriginal != null && e.mBitmapScaled == null) {
                e.mBitmapScaled = adjustBitmapSize(e.mBitmapOriginal, tv);
            }
        }

        // Create the textview
        SpannableStringBuilder b = new SpannableStringBuilder();
        for (QAElem qae: qaElems) {
            if (qae.mText != null) {
                b.append(qae.mText);
            } else {
                b.append("."); // You need this otherwise image will not show
                int bstart = 0;
                int bend = 0;
                if (b.length() > 0) {
                    bstart = b.length()-1;
                    bend = bstart+1;
                } else {
                    bstart = 0;
                    bend = 0;
                }
                b.setSpan(new ImageSpan(a, qae.mBitmapScaled), bstart, bend, 0);
            }
        }
        tv.setText(b);
    }

    private static class QAElem {
        public String mText;
        public Bitmap mBitmapScaled;

        public Bitmap mBitmapOriginal;
        public String mBitmapURL;
        public StorageReference mStorageReference;

        public String toString() {
            StringBuilder strb = new StringBuilder();
            if (mText != null) {
                strb.append("t:" + mText);
            } else {
                strb.append("bmurl: " + mBitmapURL);
            }
            if (mBitmapOriginal != null) {
                strb.append(", bm: set");
            } else {
                strb.append(", bm: null");
            }
            if (mStorageReference != null) {
                strb.append(", sr: set");
            } else {
                strb.append(", sr: null");
            }
            return strb.toString();
        }
    };

    public static List<QAElem> getQAElems(String t) {
        List<QAElem> r = new ArrayList<>();
        int io = -1;
        String tagStart = "[[image:";
        String tagEnd = "]]";
        while(true) {
            t = t.trim();
            io = t.indexOf(tagStart);
            if (t.length() == 0) {
                break;
            }

            QAElem e = new QAElem();
            if (io == -1 || io > 0) {
                if (io == -1) {
                    e.mText = t;
                    t = "";
                } else {
                    e.mText = t.substring(0, io);
                    if (io < t.length()) {
                        t = t.substring(io);
                    } else {
                        t = "";
                    }
                }
                r.add(e);
            } else {  // io == 0
                int ioTEnd = t.indexOf(tagEnd);
                if (ioTEnd == -1) {
                    e.mText = "*** ERROR, no end tag for image URL: " + t;
                }
                String url = t.substring(tagStart.length(), ioTEnd);
                e.mBitmapURL = url;
                r.add(e);
                if (ioTEnd + tagEnd.length() >= t.length()) {
                    break;
                }
                t = t.substring(ioTEnd + tagEnd.length());
            }
        }
        return r;
    }

    public static List<ETerm> getQAs(long pSetId,
                                     String pSetTitle,
                                     String pEmail,
                                     List<QLTerm> eQAs,
                                     List<StatTermForUser> stats) {
        List<ETerm> al = new ArrayList<>();
        for (QLTerm eQA: eQAs) {
            StatTermForUser stat = null;
            // Locate stat for the QA
            if (stats != null) {
                for (StatTermForUser aStat : stats) {
                    if (eQA.id == aStat.termId) {
                        stat = aStat;
                    }
                }
            }
            // If no previous stat found, then give default initial
            if (stat == null) {
                stat = StatTermForUser.getInitialValues(pSetId, eQA.id, pEmail);
            }
            al.add(new ETerm(pSetTitle, eQA, stat));
            Collections.sort(al, new Comparator<ETerm>() {
                public int compare(ETerm e1, ETerm e2) {
                    if (e1.mRank < e2.mRank) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });
        }
        return al;
    }

    private ETerm(String setTitle, QLTerm qa, StatTermForUser stat) {
        mQA = qa;
        mRank = qa.rank;
        mStat = stat;
        mSetTitle = setTitle;
    }

    public StatTermForUser getStat() { return mStat; }
    public boolean isDoneForToday() { return mIsDoneForToday; }

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
        DatabaseReference forUser = Misc.getDatabaseReference(Constants.FPATH_STATFORUSER())
                .child(String.valueOf(mStat.setId))
                .child(mStat.userToken)
                .child(String.valueOf(mStat.termId));
        forUser.setValue(mStat);

        // Store in StatForRaw table
        StatTermForRaw statRaw = new StatTermForRaw(
                mStat.setId, mStat.termId, mStat.userToken,
                leitnerBoxBefore, leitnerBoxAfter,
                answer, duration);
        DatabaseReference forRaw = Misc.getDatabaseReference(Constants.FPATH_STATFORRAW())
                .child(String.valueOf(mStat.setId))
                .child(String.valueOf(mStat.termId))
                .child(String.valueOf(mStat.userToken)
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

    private Bitmap adjustBitmapSize(Bitmap bm, TextView tv) {
        int tvH = tv.getHeight() - 10;
        int tvW = tv.getWidth() - 10;
        int bmH = bm.getHeight();
        int bmW = bm.getWidth();

        // Lets try to fit the image on the screen
        int bmNewH = 100; // use dummy defaults
        int bmNewW = 100;

        if (tvH > tvW) {
            // Scale out horizontally
            float ar = ((float)bmH) / ((float)bmW);
            bmNewW = tvW;
            bmNewH = (int)(ar * bmNewW);
        } else { // tvW > tvH
            // Use maximum horizontal space, let user scroll vertically to keep aspect ratio
            float ar = ((float)bmH) / ((float)bmW);
            bmNewW = tvW;
            bmNewH = (int)(ar* bmNewW);
        }

        return Bitmap.createScaledBitmap(bm, bmNewW, bmNewH, false);
    }
}

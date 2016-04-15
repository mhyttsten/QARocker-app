/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pf.mr.screens;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.pf.mr.screens.display_set_stats.RehearsalFinishedActivity;
import com.pf.mr.datamodel.StatTermForUser;
import com.pf.mr.utils.Constants;
import com.pf.mr.R;
import com.pf.mr.datamodel.QLSet;
import com.pf.mr.execmodel.ESet;
import com.pf.mr.execmodel.ETerm;
import com.pf.mr.utils.Misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Demonstrates a "card-flip" animation using custom fragment transactions ({@link
 * android.app.FragmentTransaction#setCustomAnimations(int, int)}).
 *
 * <p>This sample shows an "info" action bar button that shows the back of a "card", rotating the
 * front of the card out and the back of the card in. The reverse animation is played when the user
 * presses the system Back button or the "photo" action bar button.</p>
 */
public class CardFlipActivity extends Activity
        implements FragmentManager.OnBackStackChangedListener,
        GestureDetector.OnDoubleTapListener,
        GestureDetector.OnGestureListener {
    public static final String TAG = CardFlipActivity.class.getSimpleName();

    public boolean onDown(MotionEvent me) { return true; }
    public boolean onFling(MotionEvent me1, MotionEvent me2, float vx, float vy) { return false; }
    public void onLongPress(MotionEvent me) { }
    public boolean onScroll(MotionEvent me1, MotionEvent me2, float dx, float dy) { return false; }
    public void onShowPress(MotionEvent me) { }
    public boolean onSingleTapUp(MotionEvent me) {
        Log.i(TAG, "onSingleTapUp");
        Toast.makeText(this, "onSingleTapUp", Toast.LENGTH_SHORT).show();
        return true;
    }

    public boolean onTouchEvent(MotionEvent me) {
        mDetector.onTouchEvent(me);
        Log.i(TAG, "HERE");
        return super.onTouchEvent(me);
    }

    public boolean onDoubleTap(MotionEvent me) { return false; }
    public boolean onDoubleTapEvent(MotionEvent me) { return false; }
    public boolean onSingleTapConfirmed(MotionEvent me) {
        Log.i(TAG, "onSingleTapConfirmed");
        Toast.makeText(this, "onSingleTapConfirmed", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * A handler object, used for deferring UI operations.
     */
    private Handler mHandler = new Handler();

    /**
     * Whether or not we're showing the back of the card (otherwise showing the front).
     */
    private boolean mShowingBack = false;

    private String mUserEmail;
    private String mSetName;
    private ESet mESet;
    private ETerm mCurrentETerm;
    private GestureDetectorCompat mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_flip);

        Firebase.setAndroidContext(this);

        Log.i(TAG, "Now setting GestureDetector");
        mDetector = new GestureDetectorCompat(this, this);
        mDetector.setOnDoubleTapListener(this);

        mUserEmail = getIntent().getStringExtra(Constants.USER_EMAIL);
        Log.i(TAG, "User email: " + mUserEmail);
        mSetName = getIntent().getStringExtra(Constants.SETNAME);

        getESetAndStartCircus();
    }


    public void getESetAndStartCircus() {

        final List<ESet> esets = new ArrayList<>();
        Misc.getESets(mUserEmail, mSetName, esets, new Runnable() {
            @Override
            public void run() {
                if (esets.size() == 0) {
                    Log.e(TAG, "*** Could not find a set named: " + mSetName);
                } else if (esets.size() > 1) {
                    Log.e(TAG, "*** Found multiple results from name: " + mSetName);
                } else {
                    ESet eset = esets.get(0);
                    if (!eset.mSet.title.equals(mSetName)) {
                        Log.e(TAG, "*** Retrieved set: " + eset.mSet.title
                                + ", expected: " + mSetName);
                    } else {
                        mESet = eset;
                        startNextRound();
                    }
                }
            }
        });

/*
        Firebase ref = new Firebase(Constants.FPATH_SETS());
        Query qref = ref.orderByKey();
        qref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot qs) {
                // Data is ordered by increasing height, so we want the first entry
                Log.e(TAG, "Result count: " + qs.getChildrenCount());
                Iterator<DataSnapshot> iter = qs.getChildren().iterator();
                while (iter.hasNext()) {
                    QLSet s = (QLSet) iter.next().getValue(QLSet.class);
                    if (s.title.equals(mSetName)) {
                        mQLSet = s;
                    }
                }
                getTermStats();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public void getTermStats() {
        Firebase ref = new Firebase(Constants.FPATH_STATFORUSER())
                .child(String.valueOf(mQLSet.id))
                .child(Constants.EMAIL_TO_FIREBASEPATH(mUserEmail));
        Query qref = ref.orderByKey();
        qref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot qs) {
                List<StatTermForUser> terms = null;
                // Data is ordered by increasing height, so we want the first entry
                Iterator<DataSnapshot> iter = qs.getChildren().iterator();
                while (iter.hasNext()) {
                    StatTermForUser stfu = iter.next().getValue(StatTermForUser.class);
                    if (terms == null) {
                        terms = new ArrayList<StatTermForUser>();
                    }
                    terms.add(stfu);
                }
                mESet = new ESet(mQLSet, mUserEmail, terms);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
        */
    }


    //----
    // After all above retrievals have been completed

    public void startNextRound() {
        // If there is no saved instance state, add a fragment representing the
        // front of the card to this activity. If there is saved instance state,
        // this fragment will have already been added to the activity.
        boolean hasNext = mESet.hasNext();
        if (!hasNext) {
            Intent i = Misc.getIntentWithUserId(this, RehearsalFinishedActivity.class, mUserEmail);
            i.putExtra(Constants.SETNAME, String.valueOf(mESet.mSet.title));
            startActivity(i);
            finish();
            return;
        }
        mCurrentETerm = mESet.next();
        CardFrontFragment cardFront = new CardFrontFragment();
        mCurrentETerm.setQuestionDisplayedTimer();
        cardFront.setQ(mCurrentETerm.getQ());

        // Force stack to be empty
        while (getFragmentManager().popBackStackImmediate()) {
        }

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, cardFront)
                .commit();

        // Monitor back stack changes to ensure the action bar shows the appropriate
        // button (either "photo" or "info").
        getFragmentManager().addOnBackStackChangedListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Add either a "photo" or "finish" button to the action bar, depending on which page
        // is currently selected.
        MenuItem item = menu.add(Menu.NONE, R.id.action_flip, Menu.NONE,
                mShowingBack
                        ? R.string.action_photo
                        : R.string.action_info);
        item.setIcon(mShowingBack
                ? R.drawable.ic_action_photo
                : R.drawable.ic_action_info);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_flip:
                flipCard();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void flipCard() {
        if (mShowingBack) {
            getFragmentManager().popBackStack();
            return;
        }

        // Flip to the back.
        mShowingBack = true;

        // Create and commit a new fragment transaction that adds the fragment for the back of
        // the card, uses custom animations, and is part of the fragment manager's back stack.

        CardBackFragment cardBack = new CardBackFragment();
        cardBack.setA(mCurrentETerm.getA());

        getFragmentManager()
                .beginTransaction()

                // Replace the default fragment animations with animator resources representing
                // rotations when switching to the back of the card, as well as animator
                // resources representing rotations when flipping back to the front (e.g. when
                // the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)

                // Replace any fragments currently in the container view with a fragment
                // representing the next page (indicated by the just-incremented currentPage
                // variable).
                .replace(R.id.container, cardBack)

                // Add this transaction to the back stack, allowing users to press Back
                // to get to the front of the card.
                .addToBackStack(null)

                // Commit the transaction.
                .commit();

        // Defer an invalidation of the options menu (on modern devices, the action bar). This
        // can't be done immediately because the transaction may not yet be committed. Commits
        // are asynchronous in that they are posted to the main thread's message loop.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onBackStackChanged() {
        mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);

        // When the back stack changes, invalidate the options menu (action bar).
        invalidateOptionsMenu();
    }

    public void clickNoClue(View v) {
        Log.i(TAG, "clickNoClue");
        mCurrentETerm.setAnswerGiven(ETerm.AS_NO_CLUE);
        mESet.reportAnswer(mCurrentETerm);
        startNextRound();
    }

    public void clickKnewIt(View v) {
        Log.i(TAG, "clickKnewIt");
        mCurrentETerm.setAnswerGiven(ETerm.AS_KNEW_IT);
        mESet.reportAnswer(mCurrentETerm);
        startNextRound();
    }

    public void clickNailedIt(View v) {
        Log.i(TAG, "clickNailedIt");
        mCurrentETerm.setAnswerGiven(ETerm.AS_NAILED_IT);
        mESet.reportAnswer(mCurrentETerm);
        startNextRound();
    }

    /**
     * A fragment representing the front of the card.
     */
    public static class CardFrontFragment extends Fragment {
        private String mQ;

        public CardFrontFragment() {}

        public void setQ(String q) { mQ = q; }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_card_front, container, false);
            TextView tw = (TextView)v.findViewById(android.R.id.text1);
            tw.setText(mQ);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((CardFlipActivity) getActivity()).flipCard();
                }
            });
            return v;
        }
    }

    /**
     * A fragment representing the back of the card.
     */
    public static class CardBackFragment extends Fragment {
        private String mA;

        public CardBackFragment() { }

        public void setA(String a) { mA = a; }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_card_back, container, false);

            TextView tw = (TextView)v.findViewById(android.R.id.text1);
            tw.setScroller(new Scroller(getActivity()));
            //tw.setMaxLines(1);
            tw.setVerticalScrollBarEnabled(true);
            tw.setMovementMethod(new ScrollingMovementMethod());
            tw.setText(mA);

            //ScrollView sv = (ScrollView)v.findViewById(R.id.scroll_view_id);
            //sv.requestDisallowInterceptTouchEvent(true);

            tw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Toast.makeText(getActivity(), "ehll", Toast.LENGTH_SHORT).show();
                    ((CardFlipActivity) getActivity()).flipCard();
                }
            });
            return v;
        }


    }
}
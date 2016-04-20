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
import android.net.Uri;
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
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
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

public class CardFlipActivity extends Activity
        implements FragmentManager.OnBackStackChangedListener {
    private static final String TAG = CardFlipActivity.class.getSimpleName();

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

    private TextView mTitle;
    private TextView mStats;

    private int mDoneCount;
    private int mTodoCount;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_flip);

        Firebase.setAndroidContext(this);

        Log.i(TAG, "Now setting GestureDetector");

        mTitle = (TextView)findViewById(R.id.test_title_id);
        mStats = (TextView)findViewById(R.id.test_data_id);

        mUserEmail = getIntent().getStringExtra(Constants.USER_EMAIL);
        Log.i(TAG, "User email: " + mUserEmail);
        mSetName = getIntent().getStringExtra(Constants.SETNAME);

        getESetAndStartCircus();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    public void getESetAndStartCircus() {

        final List<ESet> esets = new ArrayList<>();
        Log.e(TAG, "Now starting circus with set: " + mSetName);
        Misc.getESets(mUserEmail, mSetName, esets, new Runnable() {
            @Override
            public void run() {
                if (esets.size() == 0) {
                    Log.e(TAG, "*** Could not find a set named: " + mSetName);
                } else {
                    if (esets.size() == 1) {
                        mESet = esets.get(0);
                    } else {
                        mESet = new ESet(Constants.SETNAME_ALL, esets);
                    }
                    mTodoCount = mESet.getTodoCount();
                    startNextRound();
                }
            }
        });
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
            i.putExtra(Constants.SETNAME, String.valueOf(mESet.getSetTitle()));
            startActivity(i);
            finish();
            return;
        }
        mCurrentETerm = mESet.next();
        mTitle.setText(mCurrentETerm.mSetTitle);
        mStats.setText("[" + mDoneCount + " / " + mTodoCount  + "]");

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
        mDoneCount++;
        mTodoCount--;
        startNextRound();
    }

    public void clickNailedIt(View v) {
        Log.i(TAG, "clickNailedIt");
        mCurrentETerm.setAnswerGiven(ETerm.AS_NAILED_IT);
        mESet.reportAnswer(mCurrentETerm);
        mDoneCount++;
        mTodoCount--;
        startNextRound();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * A fragment representing the front of the card.
     */
    public static class CardFrontFragment extends Fragment {
        private String mQ;

        public CardFrontFragment() {
        }

        public void setQ(String q) {
            mQ = q;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_card_front, container, false);
            TextView tw = (TextView) v.findViewById(android.R.id.text1);
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

        public CardBackFragment() {
        }

        public void setA(String a) {
            mA = a;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_card_back, container, false);

            TextView tw = (TextView) v.findViewById(android.R.id.text1);
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

package com.pf.mr.screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.pf.mr.SingletonMR;
import com.pf.mr.screens.display_set_stats.RehearsalFinishedActivity;
import com.pf.mr.utils.Constants;
import com.pf.mr.R;
import com.pf.mr.execmodel.ESet;
import com.pf.mr.execmodel.ETerm;
import com.pf.mr.utils.Misc;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.name;

public class CardFlipActivity extends Activity {
    private static final String TAG = CardFlipActivity.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;

    private static class Data {
        boolean mShowingBack;
        ETerm mCurrentETerm;
        int mDoneCount;
        int mTodoCount;
    }

    private static Data mData;
    private TextView mTitle;
    private TextView mStats;

    public static void reset() {
        mData = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate Entered");

        setContentView(R.layout.activity_card_flip);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        if (mData == null) {
            mData = new Data();
            mData.mTodoCount = SingletonMR.mCurrentESet.getTodoCount();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume Entered");

        FirebaseCrash.log("CardFlipActivity.onResume: Entered");
        mTitle = (TextView)findViewById(R.id.test_title_id);
        SingletonMR.mCurrentESet.rescaleImages();
        startNextRound(); // newCard);
        FirebaseCrash.log("CardFlipActivity.onResume: Exit");
    }

    /**
     */
    public void startNextRound() {
        Log.i(TAG, "startNextRound");
        if (mData.mCurrentETerm == null) {
            boolean hasNext = SingletonMR.mCurrentESet.hasNext();
            if (!hasNext) {
                Intent i = new Intent(this, RehearsalFinishedActivity.class);
                mData = null;
                startActivity(i);
                finish();
                return;
            }
            ESet eset = SingletonMR.mCurrentESet;
            mData.mCurrentETerm = SingletonMR.mCurrentESet.next();
            mData.mShowingBack = false;
        }
        mTitle.setText(mData.mCurrentETerm.mSetTitle +
                " [" + mData.mDoneCount + " / " + mData.mTodoCount  + "]");
        showCard();

        if (mData.mCurrentETerm == null) {
            Log.i(TAG, "...at exit, mCurrentETerm == null");
        } else {
            Log.i(TAG, "...at exit, mCurrentETerm != null");
        }
    }

    /**
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.add(Menu.NONE, R.id.action_flip, Menu.NONE, R.string.action_info);
        item.setIcon(R.drawable.ic_action_info);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    /**
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mData = null;
                SingletonMR.mCurrentESet.reset();
                finish();
                return true;

            case R.id.action_flip:
                mData.mShowingBack = !mData.mShowingBack;
                showCard();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showCard() {
        Fragment card = null;
        if (mData.mShowingBack) {
            card = new CardBackFragment();
        } else {
            card = new CardFrontFragment();
        }

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                .replace(R.id.container, card)
                .commit();
    }

    private void showDialog(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(s)
                .setTitle("Stats History")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static final String E_NO_CLUE = "answer_noclue";
    private static final String E_KNEW_IT = "answer_knewit";
    private static final String E_NAILED_IT = "answer_nailedit";

    private static final String E1_ANSWER = "answer";
    private static final String EP1_NO_CLUE = "no_clue";
    private static final String EP1_KNEW_IT = "knew_it";
    private static final String EP1_NAILED_IT = "nailed_it";

    public void clickNoClue(View v) {
        Log.i(TAG, "clickNoClue");
        mData.mCurrentETerm.setAnswerGiven(ETerm.AS_NO_CLUE);
        SingletonMR.mCurrentESet.reportAnswer(mData.mCurrentETerm);
        String str = "Come on... memorize it!";

        Bundle p = null;
        p = new Bundle();
        p.putInt(FirebaseAnalytics.Param.VALUE, 1);
        mFirebaseAnalytics.logEvent(E1_ANSWER, p);
        mFirebaseAnalytics.logEvent(E_NO_CLUE, null);

        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        getFragmentManager().popBackStack();
        mData.mCurrentETerm = null;
        startNextRound(); //true);
    }

    public void clickKnewIt(View v) {
        Log.i(TAG, "clickKnewIt");
        mData.mCurrentETerm.setAnswerGiven(ETerm.AS_KNEW_IT);
        showDialog(mData.mCurrentETerm.mstr.toString());
        SingletonMR.mCurrentESet.reportAnswer(mData.mCurrentETerm);
        mData.mDoneCount++;
        mData.mTodoCount--;

        Bundle p = null;
        p = new Bundle();
        p.putInt(FirebaseAnalytics.Param.VALUE, 1);
        mFirebaseAnalytics.logEvent(E1_ANSWER, p);
        mFirebaseAnalytics.logEvent(E_KNEW_IT, null);

        Toast.makeText(this, mData.mCurrentETerm.mRehearsalNextString, Toast.LENGTH_SHORT).show();
        getFragmentManager().popBackStack();
        mData.mCurrentETerm = null;
        startNextRound(); // true);
    }

    public void clickNailedIt(View v) {
        Log.i(TAG, "clickNailedIt");
        mData.mCurrentETerm.setAnswerGiven(ETerm.AS_NAILED_IT);
        showDialog(mData.mCurrentETerm.mstr.toString());
        SingletonMR.mCurrentESet.reportAnswer(mData.mCurrentETerm);
        mData.mDoneCount++;
        mData.mTodoCount--;

        Bundle p = null;
        p = new Bundle();
        p.putInt(FirebaseAnalytics.Param.VALUE, 1);
        mFirebaseAnalytics.logEvent(E1_ANSWER, p);
        mFirebaseAnalytics.logEvent(E_NAILED_IT, null);

        Toast.makeText(this, mData.mCurrentETerm.mRehearsalNextString, Toast.LENGTH_SHORT).show();
        getFragmentManager().popBackStack();
        mData.mCurrentETerm = null;
        startNextRound(); //true);
    }

    /**
     */
    public static class CardFrontFragment extends Fragment {
        private View mView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mView = inflater.inflate(R.layout.fragment_card_front, container, false);
            return mView;
        }

        @Override
        public void onResume() {
            super.onResume();

            final TextView tv = (TextView) mView.findViewById(android.R.id.text1);
            tv.setMovementMethod(new ScrollingMovementMethod());

            tv.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (getActivity() != null
                                    && CardFlipActivity.mData.mCurrentETerm != null) {
                                CardFlipActivity.mData.mCurrentETerm.renderQ(getActivity(), tv);
                            }
                        }
                    });
        }

    }

    /**
     */
    public static class CardBackFragment extends Fragment {
        View mView;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            mView = inflater.inflate(R.layout.fragment_card_back, container, false);
            return mView;
        }

        @Override
        public void onResume() {
            super.onResume();

            final TextView tv = (TextView) mView.findViewById(android.R.id.text1);
            tv.setMovementMethod(new ScrollingMovementMethod());

            tv.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (getActivity() != null
                                    && CardFlipActivity.mData.mCurrentETerm != null) {
                                FirebaseCrash.log("CardBackFragment.onResume: About to do mData.mCurrentETerm.rednerA");
                                CardFlipActivity.mData.mCurrentETerm.renderA(getActivity(), tv);
                            }
                        }
                    });
        }
    }
}

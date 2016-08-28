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
import com.pf.mr.screens.display_set_stats.RehearsalFinishedActivity;
import com.pf.mr.utils.Constants;
import com.pf.mr.R;
import com.pf.mr.execmodel.ESet;
import com.pf.mr.execmodel.ETerm;
import com.pf.mr.utils.Misc;

import java.util.ArrayList;
import java.util.List;

public class CardFlipActivity extends Activity {
    private static final String TAG = CardFlipActivity.class.getSimpleName();

    private static class Data {
        boolean mShowingBack;
        String mUserToken;
        String mSetName;
        ESet mESet;
        ETerm mCurrentETerm;
        int mDoneCount;
        int mTodoCount;
    };

    public static Data mData;
    private TextView mTitle;
    private TextView mStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_flip);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTitle = (TextView)findViewById(R.id.test_title_id);
        String userToken = getIntent().getStringExtra(Constants.USER_TOKEN);
        String setName = getIntent().getStringExtra(Constants.SETNAME);
        Log.i(TAG, "Incoming userToken: " + userToken + ", setName: " + setName);

        if (mData == null
                || !mData.mSetName.equals(setName)
                || !mData.mUserToken.equals(userToken)) {
            Log.i(TAG, "No previous data found, or setName/userToken mismatch, initializing");
            mData = new Data();
            mData.mUserToken = userToken;
            mData.mSetName = setName;
            getESetAndStartCircus();
        } else {
            Log.i(TAG, "Previous data found, let's continue that session");
            mData.mESet.rescaleImages();
            startNextRound(false);
        }
    }

    /**
     */
    private void getESetAndStartCircus() {
        final List<ESet> esets = new ArrayList<>();
        Log.e(TAG, "Retrieving data for set: " + mData.mSetName);
        Misc.getESets(mData.mUserToken, mData.mSetName, esets, new Runnable() {
            @Override
            public void run() {
                if (esets.size() == 0) {
                    Log.e(TAG, "Could not find a set named: " + mData.mSetName);
                } else {
                    if (esets.size() == 1) {
                        mData.mESet = esets.get(0);
                    } else {
                        mData.mESet = new ESet(Constants.SETNAME_ALL, esets);
                    }
                }
                mData.mTodoCount = mData.mESet.getTodoCount();
                startNextRound(true);
            }
        });
    }

    /**
     */
    public void startNextRound(boolean newCard) {
        if (newCard) {
            boolean hasNext = mData.mESet.hasNext();
            if (!hasNext) {
                Intent i = Misc.getIntentWithUserId(this,
                        RehearsalFinishedActivity.class,
                        mData.mUserToken);
                i.putExtra(Constants.SETNAME, String.valueOf(mData.mESet.getSetTitle()));
                mData = null;
                startActivity(i);
                finish();
                return;
            }
            mData.mCurrentETerm = mData.mESet.next();
            mData.mShowingBack = false;
        }
        mTitle.setText(mData.mCurrentETerm.mSetTitle +
                " [" + mData.mDoneCount + " / " + mData.mTodoCount  + "]");
        showCard();
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

    public void clickNoClue(View v) {
        Log.i(TAG, "clickNoClue");
        mData.mCurrentETerm.setAnswerGiven(ETerm.AS_NO_CLUE);
        mData.mESet.reportAnswer(mData.mCurrentETerm);
        String str = "Come on... memorize it!";
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        getFragmentManager().popBackStack();
        startNextRound(true);
    }

    public void clickKnewIt(View v) {
        Log.i(TAG, "clickKnewIt");
        mData.mCurrentETerm.setAnswerGiven(ETerm.AS_KNEW_IT);
        showDialog(mData.mCurrentETerm.mstr.toString());
        mData.mESet.reportAnswer(mData.mCurrentETerm);
        mData.mDoneCount++;
        mData.mTodoCount--;
        Toast.makeText(this, mData.mCurrentETerm.mRehearsalNextString, Toast.LENGTH_SHORT).show();
        getFragmentManager().popBackStack();
        startNextRound(true);
    }

    public void clickNailedIt(View v) {
        Log.i(TAG, "clickNailedIt");
        mData.mCurrentETerm.setAnswerGiven(ETerm.AS_NAILED_IT);
        showDialog(mData.mCurrentETerm.mstr.toString());
        mData.mESet.reportAnswer(mData.mCurrentETerm);
        mData.mDoneCount++;
        mData.mTodoCount--;
        Toast.makeText(this, mData.mCurrentETerm.mRehearsalNextString, Toast.LENGTH_SHORT).show();
        getFragmentManager().popBackStack();
        startNextRound(true);
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
                            if (getActivity() != null) {
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
                            if (getActivity() != null) {
                                CardFlipActivity.mData.mCurrentETerm.renderA(getActivity(), tv);
                            }
                        }
                    });
        }
    }
}

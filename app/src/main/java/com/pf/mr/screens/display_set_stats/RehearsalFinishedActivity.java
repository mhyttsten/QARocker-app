package com.pf.mr.screens.display_set_stats;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;

import com.firebase.client.Firebase;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pf.mr.R;
import com.pf.mr.screens.five_vertical_bars.FiveVerticalBarsFragment;
import com.pf.mr.utils.Constants;

public class RehearsalFinishedActivity extends AppCompatActivity {
    private static final String TAG = RehearsalFinishedActivity.class.getSimpleName();

    private FiveVerticalBarsFragment fvbFragment;
    private String mUserEmail;
    private String mSetId;

    private View mMainView;
    private View mFragmentView;
    private FiveVerticalBarsFragment mFVMFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rehearsal_finished);

        mUserEmail = getIntent().getStringExtra(Constants.USER_EMAIL);
        mSetId = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        mFVMFragment = (FiveVerticalBarsFragment)getSupportFragmentManager().findFragmentById(R.id.fvb_fragment);

        Log.i(TAG, "onCreate");





        mFragmentView = (View)findViewById(R.id.fvb_fragment);
        Log.i(TAG, "Fragment view class: " + mFragmentView.getClass().getName());
        ViewTreeObserver vtObserver2 = mFragmentView.getViewTreeObserver();
        vtObserver2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFVMFragment.adjustBarHeights(mFragmentView);
                Log.i(TAG, "onGlobalLayout.fragmentView, width: " + mFragmentView.getWidth() + ", height: " + mFragmentView.getHeight());
            }
        });



    }

    public void clickToMain(View v) {
        CheckBox cb = (CheckBox) findViewById(R.id.checkBox);

        if (cb.isChecked()) {
            String setId = getIntent().getStringExtra(Constants.SETID);
            String userEmail = getIntent().getStringExtra(Constants.USER_EMAIL);
            Firebase forUser = new Firebase(Constants.FPATH_STATFORUSER())
                    .child(setId)
                    .child(Constants.EMAIL_TO_FIREBASEPATH(userEmail));
            forUser.setValue(null);
        }
        finish();
    }

}

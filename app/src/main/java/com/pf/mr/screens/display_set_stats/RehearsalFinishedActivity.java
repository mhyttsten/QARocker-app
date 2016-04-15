package com.pf.mr.screens.display_set_stats;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.pf.mr.execmodel.ECalculateStats;
import com.pf.mr.execmodel.ESet;
import com.pf.mr.screens.five_vertical_bars.FiveVerticalBarsFragment;
import com.pf.mr.utils.Constants;
import com.pf.mr.utils.Misc;

import java.util.ArrayList;
import java.util.List;

public class RehearsalFinishedActivity extends AppCompatActivity {
    private static final String TAG = RehearsalFinishedActivity.class.getSimpleName();

    private String mUserEmail;
    private String mSetName;

    private FiveVerticalBarsFragment mFVMFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rehearsal_finished);

        mUserEmail = getIntent().getStringExtra(Constants.USER_EMAIL);
        mSetName = getIntent().getStringExtra(Constants.SETNAME);
        Log.i(TAG, "...email:   " + mUserEmail);
        Log.i(TAG, "...setName: " + mSetName);

        mFVMFragment = new FiveVerticalBarsFragment();
        mFVMFragment.setData(mUserEmail, mSetName);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fvb_fragment_layout, mFVMFragment)
                .commit();
    }

    public void clickToMain(View v) {
        CheckBox cb = (CheckBox) findViewById(R.id.checkBox);
        if (cb.isChecked()) {
            String setId = getIntent().getStringExtra(Constants.SETID);
            String userEmail = getIntent().getStringExtra(Constants.USER_EMAIL);
            Firebase forUser = new Firebase(Constants.FPATH_STATFORUSER())
                    .child(String.valueOf(mFVMFragment.mESet.mSet.id))
                    .child(Constants.EMAIL_TO_FIREBASEPATH(mUserEmail));
            forUser.setValue(null);
        }
        finish();
    }

}

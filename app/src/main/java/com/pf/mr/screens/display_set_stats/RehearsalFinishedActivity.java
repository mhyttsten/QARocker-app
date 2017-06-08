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
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.pf.mr.R;
import com.pf.mr.SingletonMR;
import com.pf.mr.execmodel.ECalculateStats;
import com.pf.mr.execmodel.ESet;
import com.pf.mr.screens.five_vertical_bars.FiveVerticalBarsFragment;
import com.pf.mr.utils.Constants;
import com.pf.mr.utils.Misc;

public class RehearsalFinishedActivity extends AppCompatActivity {
    private static final String TAG = RehearsalFinishedActivity.class.getSimpleName();

    private FiveVerticalBarsFragment mFVMFragment;
    private static String FVM_FRAGMENT_TAG = "FVMFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rehearsal_finished);

        mFVMFragment = (FiveVerticalBarsFragment)getSupportFragmentManager().findFragmentByTag(FVM_FRAGMENT_TAG);
        if (mFVMFragment == null) {
            mFVMFragment = new FiveVerticalBarsFragment();
        }

        mFVMFragment.setParentActivity(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fvb_fragment_layout, mFVMFragment, FVM_FRAGMENT_TAG)
                .commit();
    }

    public void clickToMain(View v) {
        CheckBox cb = (CheckBox) findViewById(R.id.checkBox);
        if (cb.isChecked()) {
            DatabaseReference forUser = Misc.getDatabaseReference(Constants.FPATH_STATFORUSER())
                    .child(String.valueOf(SingletonMR.mCurrentESet.getSetId()))
                    .child(SingletonMR.mUserToken);
            forUser.setValue(null);
            SingletonMR.reset();
        }
        finish();
    }

    public void updateUI(ECalculateStats ecs) {
        TextView tv1 = (TextView)findViewById(R.id.stats_completion);
        tv1.setText(String.valueOf(ecs.mPercentFinished) + "%");

        TextView tv2 = (TextView)findViewById(R.id.stats_due);
        tv2.setText(String.valueOf(ecs.mDue));

        TextView tv3 = (TextView)findViewById(R.id.stats_new);
        tv3.setText(String.valueOf(ecs.mCL0));

        TextView tv4 = (TextView)findViewById(R.id.stats_total);
        tv4.setText(String.valueOf(ecs.mCTotal));
    }

}

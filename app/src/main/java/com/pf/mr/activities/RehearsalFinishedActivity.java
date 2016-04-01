package com.pf.mr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.firebase.client.Firebase;
import com.pf.mr.R;
import com.pf.mr.utils.Constants;
import com.pf.mr.utils.Misc;

public class RehearsalFinishedActivity extends AppCompatActivity {

    private String mUserEmail;
    private String mSetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rehearsal_finished);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        mUserEmail = getIntent().getStringExtra(Constants.USER_EMAIL);
        mSetId = getIntent().getStringExtra(Intent.EXTRA_TITLE);
    }

    public void clickToMain(View v) {
        CheckBox cb = (CheckBox)findViewById(R.id.checkBox);
        if (cb.isChecked()) {
            String setId = getIntent().getStringExtra(Constants.SETID);
            String userEmail = getIntent().getStringExtra(Constants.USER_EMAIL);
            Firebase forUser = new Firebase(Constants.FPATH_STATFORUSER)
                    .child(setId)
                    .child(Constants.EMAIL_TO_FIREBASEPATH(userEmail));
            forUser.setValue(null);
        }
//        Intent i = Misc.getIntentWithUserId(
//                this,
//                DisplaySetListActivity.class,
//                getIntent().getStringExtra(Constants.USER_EMAIL));
//        startActivity(i);
        finish();
    }
}

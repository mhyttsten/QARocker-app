package com.pf.mr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.pf.mr.R;
import com.pf.mr.utils.Constants;
import com.pf.mr.utils.Misc;

public class RehearsalFinishedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rehearsal_finished);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void clickToMain(View v) {
//        Intent i = Misc.getIntentWithUserId(
//                this,
//                DisplaySetListActivity.class,
//                getIntent().getStringExtra(Constants.USER_EMAIL));
//        startActivity(i);
        finish();
    }
}

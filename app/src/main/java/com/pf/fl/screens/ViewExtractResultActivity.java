package com.pf.fl.screens;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.widget.TextView;

import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.mr.R;

public class ViewExtractResultActivity extends AppCompatActivity {
    private static final String TAG = ViewExtractResultActivity.class.getSimpleName();

    @Override
    protected void onResume() {
        super.onResume();

        setContentView(R.layout.extract_result_viewer_fl);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_fl);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        String s = "No extract info found";
        if (DB_FundInfo_UI._extractStatistics != null && DB_FundInfo_UI._extractStatistics.length() > 0) {
            s = DB_FundInfo_UI._extractStatistics;
        }
        TextView tv = (TextView) findViewById(R.id.extract_result_textview_fl);
        tv.setClickable(true);
        tv.setText(s); // Html.fromHtml(s));
        Linkify.addLinks(tv, Linkify.ALL);
    }
}

package com.pf.fl.screens;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import com.pf.mr.R;
import com.pf.fl.datamodel.FL_DB;

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
        if (FL_DB._extractStatistics != null && FL_DB._extractStatistics.length() > 0) {
            s = FL_DB._extractStatistics;
        }
        TextView tv = (TextView) findViewById(R.id.extract_result_textview_fl);
        tv.setClickable(true);
        tv.setText(s); // Html.fromHtml(s));
        Linkify.addLinks(tv, Linkify.ALL);
    }
}

package com.pf.fl.screens.extract;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.widget.TextView;

import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.mr.R;

public class DisplayStringActivity extends AppCompatActivity {
    private static final String TAG = DisplayStringActivity.class.getSimpleName();

    public static String string2display = "No extract info found";

    @Override
    protected void onResume() {
        super.onResume();

        setContentView(R.layout.extract_result_viewer_fl);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_fl);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        TextView tv = (TextView) findViewById(R.id.extract_result_textview_fl);
        tv.setClickable(true);
        tv.setText(string2display); // Html.fromHtml(s));
        Linkify.addLinks(tv, Linkify.ALL);
    }
}

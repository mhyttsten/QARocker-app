package com.pf.fl.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pf.fl.datamodel.DMA_ExtractInfo;
import com.pf.mr.R;
import com.pf.fl.datamodel.DMA_Portfolio;
import com.pf.fl.datamodel.DM_Transform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        if (DM_Transform.extractInfos != null && DM_Transform.extractInfos.size() > 0) {
            Log.i(TAG, "Number of extract info entries: " + DM_Transform.extractInfos.size());
            DMA_ExtractInfo ei = DM_Transform.extractInfos.get(0);
            s = ei.toTextViewString() + "\n";
//            s += "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000KQTP&programid=0000000000\n" +
//                    "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00000KVF\n";
        }
        TextView tv = (TextView) findViewById(R.id.extract_result_textview_fl);
        tv.setClickable(true);
        tv.setText(s); // Html.fromHtml(s));
        Linkify.addLinks(tv, Linkify.ALL);

    }
}

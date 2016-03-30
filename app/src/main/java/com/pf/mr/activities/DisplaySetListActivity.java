/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pf.mr.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.pf.mr.utils.Constants;
import com.pf.mr.R;
import com.pf.mr.datamodel.QLSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The launchpad activity for this sample project. This activity launches other activities that
 * demonstrate implementations of common animations.
 */
public class DisplaySetListActivity extends ListActivity {
    public static String LOG_TAG = DisplaySetListActivity.class.getSimpleName();

    private List<String> mQuizList = new ArrayList<>();
    private String mUserEmail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_display_set_list);

        mUserEmail = getIntent().getStringExtra(Constants.USER_EMAIL);

        Firebase ref = new Firebase(Constants.FPATH_SETS);
        Query qref = ref.orderByKey();

        qref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot qs) {
                    // Data is ordered by increasing height, so we want the first entry
                Log.e(LOG_TAG, "Result count: " + qs.getChildrenCount());
                Iterator<DataSnapshot> iter = qs.getChildren().iterator();
                while (iter.hasNext()) {
                    QLSet s = (QLSet)iter.next().getValue(QLSet.class);
                    Log.i(LOG_TAG, s.toString());
                    if (!mQuizList.contains(s.title)) {
                        mQuizList.add(s.title);
                    }
                }
                DisplaySetListActivity.this.setListAdapter(new ArrayAdapter<String>(
                        DisplaySetListActivity.this,
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        mQuizList));
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        String name = mQuizList.get(position);

        // Launch the sample associated with this list position.
        Intent i = new Intent(DisplaySetListActivity.this, CardFlipActivity.class);
        i.putExtra(Intent.EXTRA_TITLE, name);
        i.putExtra(Constants.USER_EMAIL, mUserEmail);
        startActivity(i);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}

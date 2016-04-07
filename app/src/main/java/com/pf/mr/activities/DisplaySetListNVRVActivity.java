package com.pf.mr.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.pf.mr.R;
import com.pf.mr.activities.settings.SettingsActivity;
import com.pf.mr.datamodel.QLSet;
import com.pf.mr.utils.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DisplaySetListNVRVActivity extends AppCompatActivity {
    private static final String TAG = DisplaySetListNVRVActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<String> mQuizList = new ArrayList<>();
    private String mUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "DisplaySetListNVRVActivity.onCreate");

        setContentView(R.layout.activity_display_set_list_nvrv);
        Firebase.setAndroidContext(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        setupRecyclerView();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        Log.e(TAG, "setupDrawerContent");
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.nv_settings:
                                Intent i = new Intent(DisplaySetListNVRVActivity.this, SettingsActivity.class);
                                // i.putExtra(Constants.USER_EMAIL, DisplaySetListNVRVActivity.this.mUserEmail);
                                startActivity(i);
                                return true;
                            case R.id.nv_logout:
                                return true;

                        }

                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mUserEmail = getIntent().getStringExtra(Constants.USER_EMAIL);

        Firebase ref = new Firebase(Constants.FPATH_SETS);
        Query qref = ref.orderByKey();
        qref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot qs) {
                // Data is ordered by increasing height, so we want the first entry
                Log.e(TAG, "Result count: " + qs.getChildrenCount());
                Iterator<DataSnapshot> iter = qs.getChildren().iterator();
                while (iter.hasNext()) {
                    QLSet s = (QLSet) iter.next().getValue(QLSet.class);
                    if (!mQuizList.contains(s.title)) {
                        mQuizList.add(s.title);
                    }
                }
                mQuizList.add(0, "All");
                String[] sa = mQuizList.toArray(new String[mQuizList.size()]);
                mAdapter = new MyAdapter(DisplaySetListNVRVActivity.this, mUserEmail, sa);
                Log.i(TAG, "Adapter now ready to set: " + sa.length);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }


    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private Activity mParent;
        private String mEmail;
        private String[] mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mTextView;
            public ViewHolder(TextView v) {
                super(v);
                mTextView = v;
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(Activity parent, String email, String[] myDataset) {
            mParent = parent;
            mEmail = email;
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_viewitem, parent, false);
            // set the view's size, margins, paddings and layout parameters
            TextView tv = (TextView)v.findViewById(R.id.rv_viewitem_text);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tw = (TextView)v;
                    String name = tw.getText().toString();
                    Log.i(TAG, "I was clicked, with text: " + name);

                    // Launch the sample, associated with this list position.
                    if (!name.equals("All")) {
                        Intent i = new Intent(mParent, CardFlipActivity.class);
                        i.putExtra(Constants.SETNAME, name);
                        i.putExtra(Constants.USER_EMAIL, mEmail);
                        mParent.startActivity(i);
                    }
                }
            });
            ViewHolder vh = new ViewHolder(tv);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mTextView.setText(mDataset[position]);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }
}

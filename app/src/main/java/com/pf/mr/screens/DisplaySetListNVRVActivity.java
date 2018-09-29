package com.pf.mr.screens;

import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.crash.FirebaseCrash;
import com.pf.mr.R;
import com.pf.mr.SingletonMR;
import com.pf.mr.execmodel.ECalculateStats;
import com.pf.mr.execmodel.ESet;
import com.pf.mr.screens.display_set_stats.RehearsalFinishedActivity;
import com.pf.mr.screens.settings.SettingsActivity;
import com.pf.mr.datamodel.QLSet;
import com.pf.mr.utils.Constants;
import com.pf.mr.utils.Misc;
import com.pf.mr.utils.TestYourBatchStuff;
import com.pf.shared.extract.ExtractFromHTML_Helper;
import com.pf.shared.utils.MM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DisplaySetListNVRVActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = DisplaySetListNVRVActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private DrawerLayout mDrawerLayout;

    private String mDBName;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<ESet> mQuizList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        FirebaseCrash.log(this.getClass().getSimpleName() + ".onCreate");
        Log.e(TAG, "DisplaySetListNVRVActivity.onCreate");
        Misc.executedAtAppStart();

        setContentView(R.layout.activity_display_set_list_nvrv);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.nv_menu);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        setupRecyclerView();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        Log.e(TAG, "setupDrawerContent");
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(false);
                        // menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.nv_goto_fl:
                                Intent i1 = new Intent(DisplaySetListNVRVActivity.this, com.pf.fl.screens.MainActivity.class);
                                startActivity(i1);
                                return true;
                            case R.id.nv_settings:
                                Intent i = new Intent(DisplaySetListNVRVActivity.this, SettingsActivity.class);
                                startActivity(i);
                                return true;
                            case R.id.nv_logout:
                                Log.i(TAG, "R.id.nv_logout entered");
                                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestEmail()
                                        .build();
                                mGoogleApiClient = new GoogleApiClient.Builder(
                                        DisplaySetListNVRVActivity.this,
                                        DisplaySetListNVRVActivity.this,
                                        DisplaySetListNVRVActivity.this)
                                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                        .build();
                                mGoogleApiClient.connect();
                                Log.i(TAG, "R.id.nv_logout exit");
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
        boolean didDBNameChange = false;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String dbName = sp.getString(getString(R.string.pref_key_database), null);
        String dbOld = mDBName;
        String dbNew = dbName;
        if (mDBName == null) {
            didDBNameChange = true;
            mDBName = dbName;
        } else if (dbName != null && dbName.trim().length() > 0 && !dbName.equals(mDBName)) {
            didDBNameChange = true;
            mDBName = dbName;
        }

        if (!didDBNameChange) {
            Log.i(TAG, "No name change, no need to recreate recyclerview");
            return;
        }

        Log.i(TAG, "DB name has changed, old: " + dbOld + ", new: " + dbNew);
        Constants.set_FPATH_BASE(mDBName);
        Log.i(TAG, "Settings reporting database: " + mDBName);
        Log.i(TAG, "Using database: " + Constants.FPATH_BASE);


        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(null);
        mQuizList.clear();

        final List<ESet> esets = new ArrayList<>();
        mAdapter = new MyAdapter(DisplaySetListNVRVActivity.this);
        if (SingletonMR.mQuizList == null) {
            Log.i(TAG, "Nothing in singleton cache, I have to retrieve the ESet list");
            Log.i(TAG, "FPathBase is: " + Constants.FPATH_BASE);
            Log.i(TAG, "Will now retrieve sets from URL: " + Constants.FPATH_SETS());
            Misc.getESets(SingletonMR.mUserToken, null, esets, new Runnable() {
                @Override
                public void run() {
                    List<ESet> list = new ArrayList<>();
                    ESet esetAll = new ESet(Constants.SETNAME_ALL, esets);
                    list.add(esetAll);
                    for (ESet e : esets) {
//                        Log.i(TAG, "Now adding: " + e.getSetTitle() + " with stats.size: " + e.mETermsAll.size());
                        list.add(e);
                    }
                    ESet[] sa = list.toArray(new ESet[mQuizList.size()]);
                    SingletonMR.mQuizList = sa;
                    mRecyclerView.setAdapter(mAdapter);
                }
            });
        } else {
            Log.i(TAG, "Using ESet cache");
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    public static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private AppCompatActivity mParent;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public static class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public View mView;
            public TextView mTextView;
            public Button mButton;

            public ViewHolder(View v, TextView tv, Button b) {
                super(v);
                mView = v;
                mTextView = tv;
                mButton = b;
            }

            public void setText(String t) {
                mTextView.setText(t);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(AppCompatActivity parent) {
            mParent = parent;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            Log.i(TAG, "onCreateViewHolder");
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_viewitem, parent, false);
            // set the view's size, margins, paddings and layout parameters
            View lv = v.findViewById(R.id.rv_viewitem);
            final TextView tv = (TextView)lv.findViewById(R.id.rv_viewitem_tv);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TextView tv = (TextView) v.findViewById(R.id.rv_viewitem_tv);
                    TextView mtv = (TextView)v;
                    String name = mtv.getText().toString();
                    Log.i(TAG, "TextView, I was clicked, with text: " + name);
                    SingletonMR.setCurrentSet(name);

                    // Launch the sample, associated with this list position.
                    CardFlipActivity.reset();
                    Intent i = new Intent(mParent, CardFlipActivity.class);
                    mParent.startActivity(i);
                }
            });
            Button btn = (Button)lv.findViewById(R.id.rv_viewitem_btn);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = tv.getText().toString();
                    Log.i(TAG, "Button, I was clicked, with text: " + name);
                    SingletonMR.setCurrentSet(name);

                    // Launch the sample, associated with this list position.
                    Intent i = new Intent(mParent, RehearsalFinishedActivity.class);
                    mParent.startActivity(i);
                }
            });


            ViewHolder vh = new ViewHolder(lv, tv, btn);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
//            Log.i(TAG, "onBindViewHolder, position: " + position);
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            ESet eset = SingletonMR.mQuizList[position];
            holder.mTextView.setText(eset.getSetTitle());

            ECalculateStats ecs = new ECalculateStats(eset);
//            Log.i(TAG, "For " + eset.getSetTitle() + ", creating ECS with stats size: " + eset.mETermsAll.size());
            ecs.calculate();
//            if (ecs.mDue > 0) {
//                holder.mButton.setTextColor(Constants.COLOR_RED);
//            } else {
//                holder.mButton.setTextColor(Constants.COLOR_GREEN);
//            }
            holder.mButton.setText(String.valueOf(ecs.mDue) + " DUE / @" + String.valueOf(ecs.mPercentFinished)+"%");
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
//            Log.i(TAG, "getItemCount, returning: " + SingletonMR.mQuizList.length);
            return SingletonMR.mQuizList.length;
        }
    }

    @Override
    public void onConnected(Bundle r) {
        Log.e(TAG, "onConnected");
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent i = new Intent(DisplaySetListNVRVActivity.this, SignInActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                        finish();
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int r) {
        Log.e(TAG, "ERROR, onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "ERROR, onConnectionFailed");
    }
}

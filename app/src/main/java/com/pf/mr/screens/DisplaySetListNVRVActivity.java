package com.pf.mr.screens;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.pf.mr.R;
import com.pf.mr.execmodel.ESet;
import com.pf.mr.screens.settings.SettingsActivity;
import com.pf.mr.datamodel.QLSet;
import com.pf.mr.utils.Constants;
import com.pf.mr.utils.Misc;

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

    private List<String> mQuizList = new ArrayList<>();
    private String mUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "DisplaySetListNVRVActivity.onCreate");

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
                            case R.id.nv_settings:
                                Intent i = new Intent(DisplaySetListNVRVActivity.this, SettingsActivity.class);
                                // i.putExtra(Constants.USER_EMAIL, DisplaySetListNVRVActivity.this.mUserEmail);
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
        mUserEmail = getIntent().getStringExtra(Constants.USER_EMAIL);
        mQuizList.clear();

        Log.i(TAG, "FPathBase is: " + Constants.FPATH_BASE);
        Log.i(TAG, "Will now retrieve sets from URL: " + Constants.FPATH_SETS());
        final List<ESet> esets = new ArrayList<>();
        Misc.getESets(mUserEmail, null, esets, new Runnable() {
            @Override
            public void run() {
                mQuizList.clear();
                mQuizList.add(Constants.SETNAME_ALL);
                for (ESet e: esets) {
                    mQuizList.add(e.getSetTitle());
                }
                String[] sa = mQuizList.toArray(new String[mQuizList.size()]);
                mAdapter = new MyAdapter(DisplaySetListNVRVActivity.this, mUserEmail, sa);
                Log.i(TAG, "Adapter now ready to set: " + sa.length);
                mRecyclerView.setAdapter(mAdapter);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
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
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_viewitem, parent, false);
            // set the view's size, margins, paddings and layout parameters
            TextView tv = (TextView) v.findViewById(R.id.rv_viewitem_text);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tw = (TextView) v;
                    String name = tw.getText().toString();
                    Log.i(TAG, "I was clicked, with text: " + name);

                    // Launch the sample, associated with this list position.
                    Intent i = new Intent(mParent, CardFlipActivity.class);
                    i.putExtra(Constants.SETNAME, name);
                    i.putExtra(Constants.USER_EMAIL, mEmail);
                    mParent.startActivity(i);
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

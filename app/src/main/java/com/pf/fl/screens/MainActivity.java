package com.pf.fl.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pf.fl.analysis.DPSequenceAnalyzer;
import com.pf.fl.datamodel.DMA_ExtractInfo;
import com.pf.mr.R;
import com.pf.fl.datamodel.DMA_Portfolio;
import com.pf.fl.datamodel.DM_Transform;
import com.pf.shared.Compresser;
import com.pf.shared.base64.Base64;
import com.pf.mr.datamodel.QLSet;
import com.pf.mr.screens.DisplaySetListNVRVActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Main page
// - Top Bar with left menu
// - Overall - all portfolios
// - Portfolios
// - Invalidity report

// Portfolios Master/Detail
// - Name
// - Type (SEB, PPM, SPP, Vanguard)
// - Date created
// - Funds (each has a date created)
// - % return (ideally you should be able to specify start/stop date)


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;

    private FirebaseDatabase mDB;
    private DatabaseReference mDBRef;
    private List<DMA_Portfolio> mPortfolios = new ArrayList<>();

    private boolean mHasDB = false;
    private boolean mHasPortfolios = false;
    private boolean mHasExtractInfo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "MainActivity.onCreate");
        setContentView(R.layout.activity_main_fl);

        Toolbar t = (Toolbar)findViewById(R.id.toolbar_fl);
        setSupportActionBar(t);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_fl);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_fl);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, PortfolioActivity.class);
                startActivity(i);
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_fl);
        mDrawerLayout.closeDrawers();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_fl);
//        navigationView.getMenu().clear();
        if (navigationView != null) {
            Log.i(TAG, "NavigationView != null settting drawer content");
            setupDrawerContent(navigationView);
        } else {
            Log.i(TAG, "NavigationView == null cannot set drawer content");
        }
//        navigationView.inflateMenu(R.menu.nv_menu_fl);

        mDB = FirebaseDatabase.getInstance();

        // Initialize all fund data from raw database
        mDBRef = mDB.getReference("funddata/data");
        mDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                byte[] ba = Base64.decodeBase64(value);
                ba = Compresser.dataUncompress(ba);
                try {
                    value = new String(ba, "UTF-8");
                } catch(Exception exc) {
                    throw new AssertionError("Character encoding error for UTF-8");
                }
                Log.d(TAG, "BA length: " + ba.length + ", orig string length: " + value.length());
                value = new String(ba);
                Log.d(TAG, "Value length: " + value.length() + ", content: " + (value.length() <= 500 ? value : value.substring(0, 500)));

                Log.d(TAG, "Now initializing data structures");
                DM_Transform.initializeFromRawDB(value);
                mHasDB = true;
                setupRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        mDBRef = mDB.getReference("extractdata");
        mDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iter = dataSnapshot.getChildren().iterator();
                List<DMA_ExtractInfo> l = new ArrayList<>();
                while (iter.hasNext()) {
                    DataSnapshot dsh = iter.next();
                    Object o = dsh.getValue();
                    DMA_ExtractInfo s = null;
                    try {
                        s = (DMA_ExtractInfo) dsh.getValue(DMA_ExtractInfo.class);
                        l.add(s);
                    } catch (Exception exc) {
                        System.out.println("Error");
                        exc.printStackTrace();
                        throw exc;
                    }
                }
                DM_Transform.initializeFromExtractList(l);
                mHasExtractInfo = true;
                setupRecyclerView();
            }



            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "MainActivity.onResume, hasDB: " + mHasDB + ", hasP: " + mHasPortfolios);
//        if (mRVAdapter != null) {
//            mRVAdapter.notifyDataSetChanged();
//        }
        // Get the portfolios
        mDBRef = mDB.getReference("portfolios");
        mDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot qs) {
                mPortfolios.clear();
                Iterator<DataSnapshot> iter = qs.getChildren().iterator();
                while (iter.hasNext()) {
                    DataSnapshot dsh = iter.next();
                    Object o = dsh.getValue();
                    DMA_Portfolio p = null;
                    try {
                        p = (DMA_Portfolio) dsh.getValue(DMA_Portfolio.class);
                    } catch (Exception exc) {
                        System.out.println("Error");
                        exc.printStackTrace();
                        throw exc;
                    }
                    if (p != null) {
                        mPortfolios.add(p);
                    }
                }
                mHasPortfolios = true;
                System.out.println("Initializing portfolios");
                DM_Transform.initializePortfolios(mPortfolios);
                setupRecyclerView();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        setupRecyclerView();
    }


    // **********

    private RecyclerView mRV;
    private RecyclerView.LayoutManager mRVLayout;
    private MyRVAdapter mRVAdapter;
    private void setupRecyclerView() {
        System.out.println("Now doing recycler view setup");
        if (!mHasDB || !mHasPortfolios || !mHasExtractInfo) {
            return;
        }
        System.out.println("...for real");
        mRV = (RecyclerView)findViewById(R.id.recycler_view_fl);
        mRVLayout = new LinearLayoutManager(this);
        mRV.setLayoutManager(mRVLayout);
        mRVAdapter = new MyRVAdapter(this);
        mRVAdapter.mPortfolios = mPortfolios;
        mRV.setAdapter(mRVAdapter);
        mRVAdapter.notifyDataSetChanged();
    }
    private static class MyRVAdapter extends RecyclerView.Adapter<MyRVAdapter.MyRVViewHolder> {
        private AppCompatActivity mParent;
        List<DMA_Portfolio> mPortfolios = new ArrayList<>();

        private static class MyRVViewHolder extends RecyclerView.ViewHolder {
            private TextView mTextView;

            public MyRVViewHolder(View v, TextView tv) {
                super(v);
                mTextView = tv;
            }
        }

        public MyRVAdapter(AppCompatActivity parent) {
            mParent = parent;
        }

        @Override
        public int getItemCount() {
            return mPortfolios.size();
        }

        @Override
        public MyRVViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_viewitem_fl, parent, false);
            // set the view's size, margins, paddings and layout parameters
            final View lv = v.findViewById(R.id.rv_viewitem_fl);
            final TextView tv = (TextView) lv.findViewById(R.id.rv_viewitem_tv_fl);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = tv.getText().toString();
                    DM_Transform.listPopulatePortfolioView(name);
                    Intent i = new Intent(mParent, ListActivity.class);
                    mParent.startActivity(i);
                }
            });
            return new MyRVViewHolder(v, tv);
        }

        @Override
        public void onBindViewHolder(MyRVViewHolder holder, int position) {
            Log.i(TAG, "Now setting name for: " + position + ", " + mPortfolios.get(position).name);
            holder.mTextView.setText(mPortfolios.get(position).name);
        }
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

    private void setupDrawerContent(NavigationView navigationView) {
        Log.e(TAG, "setupDrawerContent");
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        Log.i(TAG, "Performing action of menu item: " + menuItem.getItemId());
                        switch (menuItem.getItemId()) {
                            case R.id.nv_goto_mr:
                                Log.i(TAG, "Switching to MR");
                                finish(); // Just pop yourself off stack, you were added by MR
                                return true;
                            case R.id.nv_extract_info_fl:
                                Intent i1 = new Intent(MainActivity.this, ViewExtractResultActivity.class);
                                startActivity(i1);
                                return true;
                            case R.id.nv_show_change_fl:
                                DPSequenceAnalyzer.createList();
                                Intent i2 = new Intent(MainActivity.this, ListActivity.class);
                                startActivity(i2);
                                return true;
                            case R.id.nv_trend_up_len_fl:
                                return true;
                            case R.id.nv_trend_down_len_fl:
                                return true;
                            case R.id.nv_trend_up_value_fl:
                                return true;
                            case R.id.nv_trend_down_value_fl:
                                return true;
                        }

                        return true;
                    }
                });
    }

}

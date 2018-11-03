package com.pf.fl.screens;

import android.content.Intent;
import android.os.AsyncTask;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

//import com.pf.fl.datamodel.DMA_ExtractInfo;
import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.fl.datamodel.DB_FundInfo_UI_Callback;
import com.pf.fl.screens.recyclerview.MarketReturnAdapter;
import com.pf.fl.screens.recyclerview.MarketReturnEntity;
import com.pf.mr.R;
import com.pf.mr.utils.TestYourBatchStuff;
import com.pf.shared.Constants;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.datamodel.D_Portfolio;

import java.util.ArrayList;
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

    // *************************************************************
    // Activity Lifecycle Setup

    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "MainActivity.onCreate");
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

        if (DB_FundInfo_UI._initialized) {
            processInitSequence(true);
            return;
        }

        DB_FundInfo_UI.initializeDB(Constants.FUNDINFO_DB_MASTER_BIN, new DB_FundInfo_UI_Callback() {
            public void callback(boolean isError, String errorMessage, Object result) {
                if (isError) {
                    Toast.makeText(MainActivity.this, "Error reading fundinfo DB: " + errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                List<D_FundInfo> fis = (List<D_FundInfo>)result;
                DB_FundInfo_UI.initialize_Funds(fis);
                processInitSequence(false);
            }
        });

        DB_FundInfo_UI.initializeDB(Constants.PORTFOLIO_DB_MASTER_BIN, new DB_FundInfo_UI_Callback() {
            public void callback(boolean isError, String errorMessage, Object result) {
                if (isError) {
                    Toast.makeText(MainActivity.this, "Error reading portfolio DB: " + errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                List<D_Portfolio> pos = (List<D_Portfolio>)result;
                DB_FundInfo_UI.initialize2_Portfolios(pos);
                processInitSequence(false);
            }
        });

        DB_FundInfo_UI.initializeDB(Constants.FUNDINFO_LOGS_EXTRACT_MASTER_TXT, new DB_FundInfo_UI_Callback() {
            public void callback(boolean isError, String errorMessage, Object result) {
                if (isError) {
                    Toast.makeText(MainActivity.this, "Error reading extract log: " + errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                String s = (String)result;
                DB_FundInfo_UI.initialize3_ExtractStatistics(s);
                processInitSequence(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "MainActivity.onResume");
        if (_rvAdapter != null) {
            _rvAdapter.notifyDataSetChanged();
        }
    }

    private int _initSequenceCount = 3;
    private void processInitSequence(boolean done) {
        if (done) {
            _initSequenceCount = 0;
        } else {
            _initSequenceCount--;
        }
        Log.e(TAG, "*** initSequenceCount down to: " + _initSequenceCount);
        if (_initSequenceCount > 0) {
            return;
        }

        AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                TestYourBatchStuff.testYourBatchStuff();
                return null;
            }
        };


        Log.e(TAG, "*** initSequenceCount final, calling setupRecyclerView()");
        DB_FundInfo_UI._initialized = true;
        setupRecyclerView();
    }

    // *************************************************************
    // Drawer Content Setup

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
//                                DPSequenceAnalyzer.createList();
//                                Intent i2 = new Intent(MainActivity.this, ListActivity.class);
//                                startActivity(i2);
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

    // *************************************************************
    // RecyclerView setup

    private MarketReturnAdapter _rvAdapter;

    private void setupRecyclerView() {
        List<MarketReturnEntity> l = new ArrayList<>();
        l.add(new MarketReturnEntity("Ronaldo 0", "Portugal 0", "Real Madrid 0", 0, 20));
        l.add(new MarketReturnEntity("Ronaldo 1", "Portugal 1", "Real Madrid 1", 1, 21));
        l.add(new MarketReturnEntity("Ronaldo 2", "Portugal 2", "Real Madrid 2", 2, 22));
        l.add(new MarketReturnEntity("Ronaldo 3", "Portugal 3", "Real Madrid 3", 3, 23));
        l.add(new MarketReturnEntity("Ronaldo 4", "Portugal 4", "Real Madrid 4", 4, 24));
        l.add(new MarketReturnEntity("Ronaldo 5", "Portugal 5", "Real Madrid 5", 5, 25));
        l.add(new MarketReturnEntity("Ronaldo 6", "Portugal 6", "Real Madrid 6", 6, 26));
        l.add(new MarketReturnEntity("Ronaldo 7", "Portugal 7", "Real Madrid 7", 7, 27));
        l.add(new MarketReturnEntity("Ronaldo 8", "Portugal 8", "Real Madrid 8", 8, 28));
        l.add(new MarketReturnEntity("Ronaldo 9", "Portugal 9", "Real Madrid 9", 9, 29));
        l.add(new MarketReturnEntity("Ronaldo 10", "Portugal 10", "Real Madrid 10", 10, 30));
        l.add(new MarketReturnEntity("Ronaldo 11", "Portugal 11", "Real Madrid 11", 11, 31));
        l.add(new MarketReturnEntity("Ronaldo 12", "Portugal 12", "Real Madrid 12", 12, 32));
        l.add(new MarketReturnEntity("Ronaldo 13", "Portugal 13", "Real Madrid 13", 13, 33));
        l.add(new MarketReturnEntity("Ronaldo 14", "Portugal 14", "Real Madrid 14", 14, 34));
        l.add(new MarketReturnEntity("Ronaldo 15", "Portugal 15", "Real Madrid 15", 15, 35));
        l.add(new MarketReturnEntity("Ronaldo 16", "Portugal 16", "Real Madrid 16", 16, 36));
        l.add(new MarketReturnEntity("Ronaldo 17", "Portugal 17", "Real Madrid 17", 17, 37));
        l.add(new MarketReturnEntity("Ronaldo 18", "Portugal 18", "Real Madrid 18", 18, 38));
        l.add(new MarketReturnEntity("Ronaldo 19", "Portugal 19", "Real Madrid 19", 19, 39));
        _rvAdapter = new MarketReturnAdapter(l);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view_fl);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(_rvAdapter);
    }

//    private RecyclerView mRV;
//    private RecyclerView.LayoutManager mRVLayout;
//    private MyRVAdapter mRVAdapter;
//    private void setupRecyclerView() {
//        mRV = (RecyclerView)findViewById(R.id.recycler_view_fl);
//        mRVLayout = new LinearLayoutManager(this);
//        mRV.setLayoutManager(mRVLayout);
//        mRVAdapter = new MyRVAdapter(this);
//        mRVAdapter.mPortfolios = DB_FundInfo_UI.getPortfolios();
//        mRV.setAdapter(mRVAdapter);
//        mRVAdapter.notifyDataSetChanged();
//    }
//    private static class MyRVAdapter extends RecyclerView.Adapter<MyRVAdapter.MyRVViewHolder> {
//        private AppCompatActivity mParent;
//        List<D_Portfolio> mPortfolios = new ArrayList<>();
//
//        private static class MyRVViewHolder extends RecyclerView.ViewHolder {
//            private TextView mTextView;
//
//            public MyRVViewHolder(View v, TextView tv) {
//                super(v);
//                mTextView = tv;
//            }
//        }
//
//        public MyRVAdapter(AppCompatActivity parent) {
//            mParent = parent;
//        }
//
//        @Override
//        public int getItemCount() {
//            return mPortfolios.size();
//        }
//
//        @Override
//        public MyRVViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.rv_viewitem_fl, parent, false);
//            // set the view's size, margins, paddings and layout parameters
//            final View lv = v.findViewById(R.id.rv_viewitem_fl);
//            final TextView tv = (TextView) lv.findViewById(R.id.rv_viewitem_tv_fl);
//            tv.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String name = tv.getText().toString();
//                    DB_FundInfo_UI.listPopulatePortfolioView(name);
//                    Intent i = new Intent(mParent, ListActivity.class);
//                    mParent.startActivity(i);
//                }
//            });
//            return new MyRVViewHolder(v, tv);
//        }
//
//        @Override
//        public void onBindViewHolder(MyRVViewHolder holder, int position) {
//            Log.i(TAG, "Now setting name for: " + position + ", " + mPortfolios.get(position)._name);
//            holder.mTextView.setText(mPortfolios.get(position)._name);
//        }
//    }
}

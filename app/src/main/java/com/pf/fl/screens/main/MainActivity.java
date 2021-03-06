package com.pf.fl.screens.main;

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
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

//import com.pf.fl.datamodel.DMA_ExtractInfo;
import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.fl.datamodel.DB_FundInfo_UI_Callback;
import com.pf.fl.screens.extract.DisplayStringActivity;
import com.pf.fl.screens.portfolio.PortfolioU_Activity;
import com.pf.mr.R;
import com.pf.mr.screens.DisplaySetListNVRVActivity;
import com.pf.shared.BackgroundWorker;
import com.pf.shared.Constants;
import com.pf.shared.datamodel.D_Portfolio;
import com.pf.shared.utils.MM;

import java.io.File;
import java.io.IOException;
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
    private long _timeStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _timeStart = System.currentTimeMillis();
        Log.w(TAG, "MainActivity.onCreate");
        setContentView(R.layout.main_activity);

        Toolbar t = (Toolbar)findViewById(R.id.toolbar_fl);
        setSupportActionBar(t);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.mipmap.ic_menu_fl);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_fl);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, PortfolioU_Activity.class);
                startActivity(i);
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_fl);
        mDrawerLayout.closeDrawers();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_fl);
        if (navigationView != null) {
            Log.i(TAG, "NavigationView != null settting drawer content");
            setupDrawerContent(navigationView);
        } else {
            Log.i(TAG, "NavigationView == null cannot set drawer content");
        }

        if (DB_FundInfo_UI._initialized) {
            processInitSequence(true);
            return;
        }

        DB_FundInfo_UI.initializeDB_Master(this);
        processInitSequence(false);

//        DB_FundInfo_UI.initializeDB(Constants.FUNDINFO_DB_MASTER_BIN, new DB_FundInfo_UI_Callback() {
//            public void callback(boolean isError, String errorMessage, Object result) {
//                if (isError) {
//                    Toast.makeText(MainActivity.this, "Error reading fundinfo DB: " + errorMessage, Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                long diff = System.currentTimeMillis() - _timeStart;
//                Log.i(TAG, "...Time to read and initialize masterDB: " + diff + "ms");
//                List<D_FundInfo> fis = (List<D_FundInfo>)result;
//                DB_FundInfo_UI.initialize_Funds(fis);
//                Log.i(TAG, "...Funds are now initialized");
//                processInitSequence(false);
//            }
//        });

        DB_FundInfo_UI.initializeDB(Constants.PORTFOLIO_DB_MASTER_BIN, new DB_FundInfo_UI_Callback() {
            public void callback(boolean isError, String errorMessage, Object result) {
                if (isError) {
                    Toast.makeText(MainActivity.this, "Error reading portfolio DB: " + errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e(TAG, "*** Initialize MasterDB\n" + DB_FundInfo_UI._timeInit.toString());
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

    //------------------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "MainActivity.onResume");
        if (_rvAdapter != null) {
            _rvAdapter.notifyDataSetChanged();
        }
    }

    //------------------------------------------------------------------------
    private int _initSequenceCount = 3;
    private void processInitSequence(boolean done) {
        if (done) {
            _initSequenceCount = 0;
        } else {
            _initSequenceCount--;
        }
        Log.e(TAG, "*** initSequenceCount down to: " + _initSequenceCount);
        // We are done when the sequence counter has reached zero
        if (_initSequenceCount > 0) {
            return;
        }

//        AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... voids) {
//                TestYourBatchStuff.testYourBatchStuff();
//                return null;
//            }
//        };

//        Log.e(TAG, "*** NOW TESTING THE DATA PREPROCESS OF FUNDS");
//        OTuple2G<String, List<D_FundInfo>> r = FLAnalyze_DataPreparation.prepareDataSlice(
//                DB_FundInfo_UI._funds,
//                12);
//        if (r._o1 != null) {
//            Log.e(TAG, "...ERROR: " + r._o1);
//        } else {
//            byte[] rdata = D_FundInfo_Serializer.crunchFundList(DB_FundInfo_UI._funds);
//            Log.e(TAG, "...uncompressed size: " + rdata.length);
//            try { rdata = Compresser.dataCompress("hello", rdata); } catch(Exception exc) {}
//            Log.e(TAG, "...compressed size:   " + rdata.length);
//        }

        FLSingleton.initialize();
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
                                // finish(); // Just pop yourself off stack, you were added by MR
                                Intent i =  new Intent(MainActivity.this, DisplaySetListNVRVActivity.class);
                                startActivity(i);
                                return true;
                            case R.id.nv_extract_info_fl:
                                DisplayStringActivity.string2display = DB_FundInfo_UI._extractStatistics;
                                Intent i1 = new Intent(MainActivity.this, DisplayStringActivity.class);
                                startActivity(i1);
                                return true;
                            case R.id.nv_display_debug:
                                File file = new File(MainActivity.this.getApplicationContext().getFilesDir(), BackgroundWorker.FILE_NAME);
                                DisplayStringActivity.string2display = "File not found";
                                if (file.exists()) {
                                    try {
                                        DisplayStringActivity.string2display = new String(MM.fileReadFrom(file));
                                    } catch(Exception exc) {
                                        DisplayStringActivity.string2display = "Exception: " + exc.getMessage();
                                    }
                                }
                                Intent idd = new Intent(MainActivity.this, DisplayStringActivity.class);
                                startActivity(idd);
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

    private MainActivity_RV_Adapter _rvAdapter;

    private void setupRecyclerView() {
        _rvAdapter = new MainActivity_RV_Adapter(this);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view_fl);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(_rvAdapter);
//        _rvAdapter.notifyDataSetChanged();
    }
}

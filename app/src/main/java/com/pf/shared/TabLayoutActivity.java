package com.pf.shared;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.pf.mr.R;
import com.pf.mr.screens.DisplaySetListNVRVActivity;
import com.pf.mr.screens.SignInActivity;
import com.pf.mr.screens.settings.SettingsActivity;
import com.pf.mr.utils.Misc;


public class TabLayoutActivity
        extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = TabLayoutActivity.class.getSimpleName();

    private DrawerLayout _drawerLayout;
    private GoogleApiClient _googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "DisplaySetListNVRVActivity.onCreate");
        System.out.println("Before executedAtAppStart: " + new java.util.Date().toString());
//        Misc.executedAtAppStart(this);
        System.out.println("After executedAtAppStart: " + new java.util.Date().toString());

        setContentView(R.layout.tab_activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.mipmap.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        _drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout2);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view2);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.nv_menu2);
        setupDrawerContent(navigationView);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        TabPageAdapter adapter = new TabPageAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
    }


    //------------------------------------------------------------------------
    private void setupDrawerContent(NavigationView navigationView) {
        Log.e(TAG, "setupDrawerContent");
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(false);
                        // menuItem.setChecked(true);
                        _drawerLayout.closeDrawers();
                        switch (menuItem.getItemId()) {
                            case R.id.nv_goto_fl:
                                Intent i1 = new Intent(TabLayoutActivity.this, com.pf.fl.screens.main.MainActivity.class);
//                                Intent i1 = new Intent(DisplaySetListNVRVActivity.this, com.pf.fl.screens.buttonnavigation.BNMainActivity.class);
                                startActivity(i1);
                                return true;
                            case R.id.nv_settings:
                                Intent i = new Intent(TabLayoutActivity.this, SettingsActivity.class);
                                startActivity(i);
                                return true;
                            case R.id.nv_logout:
                                Log.i(TAG, "R.id.nv_logout entered");
                                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestEmail()
                                        .build();
                                _googleApiClient = new GoogleApiClient.Builder(
                                        TabLayoutActivity.this,
                                        TabLayoutActivity.this,
                                        TabLayoutActivity.this)
                                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                        .build();
                                _googleApiClient.connect();
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
                _drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle r) {
        Log.e(TAG, "onConnected");
        Auth.GoogleSignInApi.signOut(_googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent i = new Intent(TabLayoutActivity.this, SignInActivity.class);
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

package com.pf.fl.screens.portfolio;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.mr.R;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.datamodel.D_Portfolio;

public class PortfolioU_Activity extends AppCompatActivity {
    private static final String TAG = PortfolioU_Activity.class.getSimpleName();

    private String _type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.portfolio_u_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_fl);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab == null) {
            Log.i(TAG, "ActionBar object is null!");
        }
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupRecyclerView();

        RadioButton rbSEB = (RadioButton) findViewById(R.id.rb_seb_fl);
        rbSEB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _type = D_FundInfo.TYPE_SEB;
                _rvAdapter.initializeList(_type);
            }
        });
        RadioButton rbVGD= (RadioButton) findViewById(R.id.rb_vanguard_fl);
        rbVGD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _type = D_FundInfo.TYPE_VANGUARD;
                _rvAdapter.initializeList(_type);
            }
        });
        RadioButton rbPPM = (RadioButton) findViewById(R.id.rb_ppm_fl);
        rbPPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _type = D_FundInfo.TYPE_PPM;
                _rvAdapter.initializeList(_type);
            }
        });
        RadioButton rbSPP = (RadioButton) findViewById(R.id.rb_spp_fl);
        rbSPP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _type = D_FundInfo.TYPE_SPP;
                _rvAdapter.initializeList(_type);
            }
        });

        Button b_save = (Button) findViewById(R.id.b_save_fl);
        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                D_Portfolio f = new D_Portfolio();
                f._name = _type;
                for (PortfolioU_Activity_RV_Adapter.CheckBoxTextView cbtv: _rvAdapter._funds) {
                    if (cbtv._check) {
                        f._urls.add(cbtv._url);
                    }
                }
                DB_FundInfo_UI._portfoliosHM.put(f._name, f);
                DB_FundInfo_UI.savePortfolios(PortfolioU_Activity.this);
                finish();
            }
        });
    }

    // **********

    private PortfolioU_Activity_RV_Adapter _rvAdapter;

    private void setupRecyclerView() {
        _rvAdapter = new PortfolioU_Activity_RV_Adapter(this);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view_fl);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(_rvAdapter);
//        _rvAdapter.notifyDataSetChanged();
    }
}

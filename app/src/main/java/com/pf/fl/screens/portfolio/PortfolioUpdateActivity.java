package com.pf.fl.screens.portfolio;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.mr.R;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.datamodel.D_Portfolio;

import java.util.ArrayList;
import java.util.List;

public class PortfolioUpdateActivity extends AppCompatActivity {
    private static final String TAG = PortfolioUpdateActivity.class.getSimpleName();

    private String mType = D_FundInfo.TYPE_SEB;
    private D_Portfolio mPortfolio = new D_Portfolio();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio_fl);
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

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        setupRecyclerView();

        RadioButton rbSEB = (RadioButton) findViewById(R.id.rb_seb_fl);
        rbSEB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = D_FundInfo.TYPE_SEB;
                mPortfolio = DB_FundInfo_UI.portfoliosHM.get(mType);
                mRVAdapter.initializeList(mRV, mType, mPortfolio);
            }
        });
        RadioButton rbVGD= (RadioButton) findViewById(R.id.rb_vanguard_fl);
        rbVGD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = D_FundInfo.TYPE_VANGUARD;
                mPortfolio = DB_FundInfo_UI.portfoliosHM.get(mType);
                mRVAdapter.initializeList(mRV, mType, mPortfolio);
            }
        });
        RadioButton rbPPM = (RadioButton) findViewById(R.id.rb_ppm_fl);
        rbPPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = D_FundInfo.TYPE_PPM;
                mPortfolio = DB_FundInfo_UI.portfoliosHM.get(mType);
                mRVAdapter.initializeList(mRV, mType, mPortfolio);
            }
        });
        RadioButton rbSPP = (RadioButton) findViewById(R.id.rb_spp_fl);
        rbSPP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = D_FundInfo.TYPE_SPP;
                mPortfolio = DB_FundInfo_UI.portfoliosHM.get(mType);
                mRVAdapter.initializeList(mRV, mType, mPortfolio);
            }
        });
        if (rbSEB.isChecked()) {
            mType = D_FundInfo.TYPE_SEB;
            mPortfolio = DB_FundInfo_UI.portfoliosHM.get(mType);
            mRVAdapter.initializeList(mRV, mType, mPortfolio);
        }
        if (rbSPP.isChecked()) {
            mType = D_FundInfo.TYPE_SPP;
            mPortfolio = DB_FundInfo_UI.portfoliosHM.get(mType);
            mRVAdapter.initializeList(mRV, mType, mPortfolio);
        }
        if (rbVGD.isChecked()) {
            mType = D_FundInfo.TYPE_VANGUARD;
            mPortfolio = DB_FundInfo_UI.portfoliosHM.get(mType);
            mRVAdapter.initializeList(mRV, mType, mPortfolio);
        }
        if (rbPPM.isChecked()) {
            mType = D_FundInfo.TYPE_PPM;
            mPortfolio = DB_FundInfo_UI.portfoliosHM.get(mType);
            mRVAdapter.initializeList(mRV, mType, mPortfolio);
        }

        Button b_save = (Button) findViewById(R.id.b_save_fl);
        final SaveHolder h = getHolder();
        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                D_Portfolio f = new D_Portfolio();
                f._name = mType;
                for (DB_FundInfo_UI.CheckableFund cf: h.rvAdapter.mFunds) {
                    if (cf.isChecked) {
                        f._urls.add(cf.fund._url);
                    }
                }
                DB_FundInfo_UI.portfoliosHM.put(f._name, f);
                DB_FundInfo_UI.savePortfolios(PortfolioUpdateActivity.this);
                finish();
            }
        });

    }

    // **********

    private static class SaveHolder {
        RadioButton rbSEB;
        RadioButton rbVanguard;
        RadioButton rbPPM;
        RadioButton rbSPP;
        MyRVAdapter rvAdapter;
    };
    public SaveHolder getHolder() {
        SaveHolder sh = new SaveHolder();
        sh.rbSEB = (RadioButton) findViewById(R.id.rb_seb_fl);
        sh.rbVanguard = (RadioButton) findViewById(R.id.rb_vanguard_fl);
        sh.rbPPM = (RadioButton) findViewById(R.id.rb_ppm_fl);
        sh.rbSPP = (RadioButton) findViewById(R.id.rb_spp_fl);
        sh.rvAdapter = mRVAdapter;
        return sh;
    }

    // **********

    private RecyclerView mRV;
    private RecyclerView.LayoutManager mRVLayout;
    private MyRVAdapter mRVAdapter;

    private void setupRecyclerView() {
        mRV = (RecyclerView) findViewById(R.id.recycler_view_fl);
        mRVLayout = new LinearLayoutManager(this);
        mRV.setLayoutManager(mRVLayout);
        mRVAdapter = new MyRVAdapter(this);
        mRV.setAdapter(mRVAdapter);
//        mRVAdapter.initializeList(mRV, mType, mPortfolio);
    }

    private static class MyRVAdapter extends RecyclerView.Adapter<MyRVAdapter.MyRVViewHolder> {
        private AppCompatActivity mParent;
        public List<DB_FundInfo_UI.CheckableFund> mFunds = new ArrayList<>();
        private D_Portfolio mPortfolio;

        private static class MyRVViewHolder extends RecyclerView.ViewHolder {
            private View mView;
            private TextView mTextView;
            private CheckBox mCheckBox;

            public MyRVViewHolder(View v, CheckBox cb, TextView tv) {
                super(v);
                mView = v;
                mCheckBox = cb;
                mTextView = tv;
            }
        }

        public void initializeList(RecyclerView rv, String kind, D_Portfolio p) {
            Log.i(TAG, "initializeList with type: " + kind);

            int oldCount = mFunds.size();
            mPortfolio = p;
            mFunds = DB_FundInfo_UI.cfundsByTypeHM.get(kind);
            for (DB_FundInfo_UI.CheckableFund f : mFunds) {
                for (String url: mPortfolio._urls) {
                    if (url.equals(f.fund._url)) {
                        Log.i(TAG, "*** Setting to checked: " + f.fund._nameMS);
                        f.isChecked = true;
                    }
                }
            }
            int newCount = mFunds.size();
            Log.i(TAG, "Refresh with: " + kind + ", new count: " + newCount + ", old: " + oldCount);
            notifyDataSetChanged();
        }

        public MyRVAdapter(AppCompatActivity parent) {
            mParent = parent;
        }

        @Override
        public int getItemCount() {
            Log.i(TAG, "getItemCount, number of items: " + mFunds.size());
            return mFunds.size();
        }

        @Override
        public MyRVAdapter.MyRVViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.i(TAG, "onCreateViewHolder");
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_cbandtv_fl, parent, false);
            View lv = v.findViewById(R.id.rv_cbAndTV_fl);
            final TextView tv = (TextView) lv.findViewById(R.id.rv_cbAndTV_tv_fl);
            final CheckBox cb = (CheckBox) lv.findViewById(R.id.rv_cbAndTV_cb_fl);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView mtv = (TextView) v;
                    String name = mtv.getText().toString();
                    Log.i(TAG, "TextView, I was clicked, with text: " + name);
                }
            });
            return new MyRVAdapter.MyRVViewHolder(lv, cb, tv);
        }

        @Override
        public void onBindViewHolder(MyRVAdapter.MyRVViewHolder holder, final int position) {
            Log.i(TAG, "onBindViewHolder, pos: " + position);
            String name = mFunds.get(position).fund._nameMS;
            if (name.length() < 30) {
                holder.mTextView.setTextAppearance(android.R.style.TextAppearance_Large);
            } else {
                holder.mTextView.setTextAppearance(android.R.style.TextAppearance_Small);
            }
            holder.mTextView.setText(name);
            holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.i(TAG, "...onCheckedChanged to: " + isChecked + " for position: " + position);
                    DB_FundInfo_UI.CheckableFund cf = mFunds.get(position);
                    cf.isChecked = isChecked;
                }
            });
            holder.mCheckBox.setChecked(mFunds.get(position).isChecked);

        };
    }
}

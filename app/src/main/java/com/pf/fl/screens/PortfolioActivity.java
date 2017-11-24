package com.pf.fl.screens;

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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pf.mr.R;
import com.pf.fl.datamodel.DMA_Portfolio;
import com.pf.fl.datamodel.DM_Transform;

import java.util.ArrayList;
import java.util.List;

public class PortfolioActivity extends AppCompatActivity {
    private static final String TAG = PortfolioActivity.class.getSimpleName();

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

        RadioButton rbSEB = (RadioButton) findViewById(R.id.rb_seb_fl);
        rbSEB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = DM_Transform.T_SEB;
                mRVAdapter.initializeList(mRV, mType);
            }
        });
        RadioButton rbVGD= (RadioButton) findViewById(R.id.rb_vanguard_fl);
        rbVGD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = DM_Transform.T_VANGUARD;
                mRVAdapter.initializeList(mRV, mType);
            }
        });
        RadioButton rbPPM = (RadioButton) findViewById(R.id.rb_ppm_fl);
        rbPPM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = DM_Transform.T_PPM;
                mRVAdapter.initializeList(mRV, mType);
            }
        });
        RadioButton rbSPP = (RadioButton) findViewById(R.id.rb_spp_fl);
        rbSPP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = DM_Transform.T_SPP;
                mRVAdapter.initializeList(mRV, mType);
            }
        });
        if (rbSEB.isChecked()) {
            mType = DM_Transform.T_SEB;
        }
        if (rbSPP.isChecked()) {
            mType = DM_Transform.T_SPP;
        }
        if (rbVGD.isChecked()) {
            mType = DM_Transform.T_VANGUARD;
        }
        if (rbPPM.isChecked()) {
            mType = DM_Transform.T_PPM;
        }

        setupRecyclerView();

        Button b = (Button) findViewById(R.id.b_save_fl);
        final SaveHolder h = getHolder();
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DMA_Portfolio f = new DMA_Portfolio();
                f.setName(mType);
                for (DM_Transform.CheckableFund cf: h.rvAdapter.funds) {
                    if (cf.isChecked) {
                        f.fund_ids.add(cf.fund.id);
                    }
                }
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference dbRef = db.getReference("portfolios");
                dbRef = dbRef.child(f.name);
                Log.i(TAG, "Saving portfolio: " + f.name);
                DM_Transform.portfolioAdd(f);
                dbRef.setValue(f);
                DM_Transform.portfoliosHM.put(f.name, f);
                finish();
            }
        });

    }

    private String mType = DM_Transform.T_SEB;


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
        mRVAdapter.initializeList(mRV, mType);
    }

    private static class MyRVAdapter extends RecyclerView.Adapter<MyRVAdapter.MyRVViewHolder> {
        private AppCompatActivity mParent;
        public List<DM_Transform.CheckableFund> funds = new ArrayList<>();

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

        public void initializeList(RecyclerView rv, String kind) {
            int oldCount = funds.size();
            funds = DM_Transform.getFunds(kind);
            int newCount = funds.size();
            Log.i(TAG, "Refresh with: " + kind + ", new count: " + newCount + ", old: " + oldCount);
            notifyDataSetChanged();
        }

        public MyRVAdapter(AppCompatActivity parent) {
            mParent = parent;
        }

        @Override
        public int getItemCount() {
            Log.i(TAG, "getItemCount, number of items: " + funds.size());
            return funds.size();
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
            String name = funds.get(position).fund.name;
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
                    DM_Transform.CheckableFund cf = funds.get(position);
                    cf.isChecked = isChecked;
                }
            });
            holder.mCheckBox.setChecked(funds.get(position).isChecked);
        };
    }
}

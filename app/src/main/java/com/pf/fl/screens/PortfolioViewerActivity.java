package com.pf.fl.screens;

import android.content.Intent;
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
import android.widget.TextView;

import com.pf.mr.R;
import com.pf.fl.datamodel.DMA_Portfolio;
import com.pf.fl.datamodel.DM_Fund;
import com.pf.fl.datamodel.DM_Transform;

public class PortfolioViewerActivity extends AppCompatActivity {
    private static final String TAG = PortfolioViewerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio_viewer_fl);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        String pname = i.getStringExtra(DM_Transform.EXTRA_PORTFOLIO_NAME);

        TextView tv = (TextView) findViewById(R.id.textView_fl);
        tv.setText(pname);

        mPortfolio = DM_Transform.portfoliosHM.get(pname);
        setupRecyclerView();
    }

    // **********

    private DMA_Portfolio mPortfolio;

    private RecyclerView mRV;
    private RecyclerView.LayoutManager mRVLayout;
    private PortfolioViewerActivity.MyRVAdapter mRVAdapter;
    private void setupRecyclerView() {
        mRV = (RecyclerView)findViewById(R.id.recycler_view_fl);
        mRVLayout = new LinearLayoutManager(this);
        mRV.setLayoutManager(mRVLayout);
        mRVAdapter = new PortfolioViewerActivity.MyRVAdapter(this);
        mRVAdapter.mPortfolio = mPortfolio;
        mRV.setAdapter(mRVAdapter);
        mRVAdapter.notifyDataSetChanged();
    }
    private static class MyRVAdapter extends RecyclerView.Adapter<MyRVAdapter.MyRVViewHolder> {
        private AppCompatActivity mParent;
        DMA_Portfolio mPortfolio;

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
            return mPortfolio.fund_ids.size();
        }

        @Override
        public MyRVViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_viewitem_fl, parent, false);
            // set the view's size, margins, paddings and layout parameters
            final View lv = v.findViewById(R.id.rv_viewitem_fl);
            final TextView tv = (TextView) lv.findViewById(R.id.rv_viewitem_tv_fl);
            return new MyRVViewHolder(v, tv);
        }

        @Override
        public void onBindViewHolder(MyRVViewHolder holder, int position) {
            Long id = mPortfolio.fund_ids.get(position);
            DM_Fund fund = DM_Transform.fundsByIdHM.get(id);
            Log.i(TAG, "Now setting name for: " + position + ", " + fund.name);
            holder.mTextView.setText(fund.name);
        }
    }

}

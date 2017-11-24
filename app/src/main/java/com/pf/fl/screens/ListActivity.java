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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pf.mr.R;
import com.pf.fl.datamodel.DM_Transform;

public class ListActivity extends AppCompatActivity {
    private static final String TAG = ListActivity.class.getSimpleName();

    ListImpl mLI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_fl);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_fl);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);


        // Get the content to display
        mLI = DM_Transform.listContent;
        setTitle(mLI.mTitle);

        setupRecyclerView();
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
        mRVAdapter.initializeList(mRV, mLI);
    }

    private static class MyRVAdapter extends RecyclerView.Adapter<MyRVAdapter.MyRVViewHolder> {
        private AppCompatActivity mParent;
        private ListImpl mLI;

        private static class MyRVViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout mLL;
            private TextView mTVHeader;
            private TextView mTVBody;

            public MyRVViewHolder(LinearLayout ll, TextView tvHeader, TextView tvBody) {
                super(ll);
                mLL = ll;
                mTVHeader = tvHeader;
                mTVBody = tvBody;
            }
        }

        public void initializeList(RecyclerView rv, ListImpl li) {
            mLI = li;
            notifyDataSetChanged();
        }

        public MyRVAdapter(AppCompatActivity parent) {
            mParent = parent;
        }

        @Override
        public int getItemCount() {
            return mLI.mHeaderAndBody.size();
        }

        @Override
        public MyRVAdapter.MyRVViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_tvandtv_fl, parent, false);
            final LinearLayout lv = (LinearLayout)v.findViewById(R.id.rv_tvAndTV_fl);
            final TextView tv1 = (TextView) lv.findViewById(R.id.rv_tvAndTV_tv1_fl);
            final TextView tv2 = (TextView) lv.findViewById(R.id.rv_tvAndTV_tv2_fl);
            lv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tvHeader = (TextView) v.findViewById(R.id.rv_tvAndTV_tv1_fl);
                    String header = tvHeader.getText().toString();
                    if (mLI.mTargetClass != null) {
                        Intent i = new Intent(mParent, mLI.mTargetClass);
                        mParent.startActivity(i);
                    }
                }
            });
            return new MyRVAdapter.MyRVViewHolder(lv, tv1, tv2);
        }

        @Override
        public void onBindViewHolder(MyRVAdapter.MyRVViewHolder holder, final int position) {
            Log.i(TAG, "onBindViewHolder, position: " + position);
            holder.mTVHeader.setText(mLI.mHeaderAndBody.get(position).mHeader);
            holder.mTVBody.setText(mLI.mHeaderAndBody.get(position).mBody);
        };
    }
}

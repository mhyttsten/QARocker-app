package com.pf.fl.screens.main;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.fl.screens.portfolio.PortfolioDisplayActivity;
import com.pf.mr.R;
import com.pf.shared.datamodel.D_Portfolio;

import java.util.List;

public class RV_MainAdapter extends RecyclerView.Adapter<RV_MainAdapter.MainAdapterViewHolder> {

    public class MainAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView _name, _nationality, _club, _rating, _age;

        public MainAdapterViewHolder(View view) {
            super(view);
            _name = (TextView) view.findViewById(R.id.name);
            _nationality = (TextView) view.findViewById(R.id.nationality);
            _club = (TextView) view.findViewById(R.id.club);
            _rating = (TextView) view.findViewById(R.id.rating);
            _age = (TextView) view.findViewById(R.id.age);
        }
    }

    private List<RV_PortfolioEntity> _portfolios = new ArrayList<>();
    private Activity _parentActivity;

    public RV_MainAdapter(Activity parentActivity) {
        _parentActivity = parentActivity;
        List<D_Portfolio> ps = DB_FundInfo_UI.getPortfolios();
        for (D_Portfolio p: ps) {
            RV_PortfolioEntity pe = new RV_PortfolioEntity();
            pe._name = p._name;
            _portfolios.add(pe);
        }
    }

    @Override
    public MainAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.main_rv_portfolio_row, parent, false);

        View v = itemView;
//            final View lv = v.findViewById(R.id.rv_viewitem_fl);
//            final TextView tv = (TextView) lv.findViewById(R.id.rv_viewitem_tv_fl);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = v.findViewById(R.id.name);
                    String name = tv.getText().toString();
                    DB_FundInfo_UI.listPopulatePortfolioView(name);
                    Intent i = new Intent(_parentActivity, PortfolioDisplayActivity.class);
                    _parentActivity.startActivity(i);
                }
            });

        return new MainAdapterViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MainAdapterViewHolder holder, int position) {
        RV_PortfolioEntity e = _portfolios.get(position);
        holder._name.setText(e._name);
        holder._nationality.setText(e._nationality);
        holder._club.setText(e._club);
        holder._rating.setText(e._rating.toString());
        holder._age.setText(e._age.toString());
    }

    @Override
    public int getItemCount() {
        return _portfolios.size();
    }
}

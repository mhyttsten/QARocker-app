package com.pf.fl.screens.portfolio;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pf.mr.R;

import java.util.List;

public class RV_MarketReturnAdapter extends RecyclerView.Adapter<RV_MarketReturnAdapter.MarketReturnViewHolder> {

    private List<RV_MarketReturnEntity> _entities;

    public class MarketReturnViewHolder extends RecyclerView.ViewHolder {
        private TextView _name, _nationality, _club, _rating, _age;

        public MarketReturnViewHolder(View view) {
            super(view);
            _name = (TextView) view.findViewById(R.id.name);
            _nationality = (TextView) view.findViewById(R.id.nationality);
            _club = (TextView) view.findViewById(R.id.club);
            _rating = (TextView) view.findViewById(R.id.rating);
            _age = (TextView) view.findViewById(R.id.age);
        }
    }

    public RV_MarketReturnAdapter(List<RV_MarketReturnEntity> entities) {
        _entities = entities;
    }

    @Override
    public MarketReturnViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.portfolio_r_rv_market_return_row, parent, false);
        return new MarketReturnViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MarketReturnViewHolder holder, int position) {
        RV_MarketReturnEntity e = _entities.get(position);
        holder._name.setText(e._name);
        holder._nationality.setText(e._nationality);
        holder._club.setText(e._club);
        holder._rating.setText(e._rating.toString());
        holder._age.setText(e._age.toString());
    }

    @Override
    public int getItemCount() {
        return _entities.size();
    }
}

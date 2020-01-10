package com.pf.fl.screens.portfolio;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pf.fl.screens.main.FLSingleton;
import com.pf.fl.screens.utils.MM_UIUtils;
import com.pf.mr.R;
import com.pf.shared.analyze.FLAnalyze_Analyze;
import com.pf.shared.datamodel.D_Analyze_FundRank;
import com.pf.shared.datamodel.D_FundInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PortfolioR_Leaders_RV_Adapter extends RecyclerView.Adapter<PortfolioR_Leaders_RV_Holder> {
    private String _type;
    private D_Analyze_FundRank _leaders;
    private PortfolioR_Activity _parentActivity;
    private PortfolioR_Leaders _fragment;

    public PortfolioR_Leaders_RV_Adapter(PortfolioR_Activity parentActivity,
                                         PortfolioR_Leaders fragment,
                                         String type) {
        _parentActivity = parentActivity;
        _fragment = fragment;
        _type = type;
    }

    public void setRange(int weekCount) {
        Map<String, D_Analyze_FundRank.D_Analyze_FundRankElement[]> m =
                FLSingleton._type2Matrix.get(_type);
        _leaders = FLAnalyze_Analyze.analyze(_type, weekCount, m);
    }

    @Override
    public PortfolioR_Leaders_RV_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.portfolio_r_leaders_rv_row, parent, false);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Here we could do detail fund data
            }
        });

        return new PortfolioR_Leaders_RV_Holder(itemView);
    }

    @Override
    public void onBindViewHolder(PortfolioR_Leaders_RV_Holder holder, int position) {

        D_Analyze_FundRank.D_Analyze_FundRankElement fre = _leaders._frSummaryForAllFridays.get(position);
        holder._name.setText(fre._fi.getNameMS());
        MM_UIUtils.setTextViewInformation(
                _parentActivity,
                holder._return_acc, fre._r1w, fre._countMissing);
        holder._position.setText(fre.getAverageRank_2F());
        holder._misc1.setText("m1");
        holder._misc2.setText("m2");
        holder._misc3.setText("m3");
    }

    @Override
    public int getItemCount() { return _leaders._frSummaryForAllFridays.size(); }
}

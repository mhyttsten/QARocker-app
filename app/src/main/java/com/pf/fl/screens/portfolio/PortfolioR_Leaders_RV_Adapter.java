package com.pf.fl.screens.portfolio;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.fl.screens.main.FLSingleton;
import com.pf.fl.screens.utils.MM_UIUtils;
import com.pf.mr.R;
import com.pf.shared.analyze.FLAnalyze_Analyze;
import com.pf.shared.datamodel.D_FundInfo;

import java.util.ArrayList;
import java.util.List;

public class PortfolioR_Leaders_RV_Adapter extends RecyclerView.Adapter<PortfolioR_Leaders_RV_Holder> {
    private List<FLAnalyze_Analyze.FundRank> _leaders = new ArrayList<>();
    private PortfolioR_Activity _parentActivity;
    private PortfolioR_Leaders _fragment;
    private FLAnalyze_Analyze _flAnalyze;

    public PortfolioR_Leaders_RV_Adapter(PortfolioR_Activity parentActivity,
                                         PortfolioR_Leaders fragment,
                                         List<D_FundInfo> fis) {
        _parentActivity = parentActivity;
        _fragment = fragment;
        _flAnalyze = new FLAnalyze_Analyze(fis);
    }

    public void setRange(int weekCount) {
        _flAnalyze.setRange(weekCount);  // 4 is also the default of the textfield
        _flAnalyze.analyze(null);
        _leaders = _flAnalyze._frSummary;
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
        FLAnalyze_Analyze.FundRank fr = _leaders.get(position);
        holder._name.setText(fr._fi._nameMS);
        MM_UIUtils.setTextViewInformation(_parentActivity, holder._return_acc, fr._r1w, fr._countMissing);
        holder._position.setText(fr.getAverageRank_2F());
        holder._misc1.setText("m1");
        holder._misc2.setText("m2");
        holder._misc3.setText("m3");
    }

    @Override
    public int getItemCount() { return _leaders.size(); }
}

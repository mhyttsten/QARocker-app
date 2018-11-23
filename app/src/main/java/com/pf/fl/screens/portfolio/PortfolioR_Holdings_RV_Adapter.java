package com.pf.fl.screens.portfolio;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.fl.screens.main.FLSingleton;
import com.pf.fl.screens.utils.MM_UIUtils;
import com.pf.fl.screens.utils.RVRow4WSummaryHolder;
import com.pf.mr.R;
import com.pf.shared.analyze.DPSeries;
import com.pf.shared.datamodel.DB_FundInfo;
import com.pf.shared.datamodel.D_Portfolio;

import java.util.ArrayList;
import java.util.List;

public class PortfolioR_Holdings_RV_Adapter extends RecyclerView.Adapter<RVRow4WSummaryHolder> {
    private List<DPSeries> _funds = new ArrayList<>();
    private Activity _parentActivity;

    public PortfolioR_Holdings_RV_Adapter(Activity parentActivity) {
        _parentActivity = parentActivity;
        D_Portfolio portfolio = DB_FundInfo_UI._portfoliosHM.get(FLSingleton._portfolioName);
        _funds = DPSeries.getDPSeriesForFunds(DB_FundInfo_UI.getFundsForPortfolio(portfolio._name), 4);
    }

    @Override
    public RVRow4WSummaryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.rv_row_4w_summary, parent, false);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Here we could do detail fund data
            }
        });

        return new RVRow4WSummaryHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RVRow4WSummaryHolder holder, int position) {
        DPSeries p = _funds.get(position);
        holder._name.setText(p._name);
        MM_UIUtils.setTextViewInformation(_parentActivity, holder._return_acc, p.getReturnAcc(), p.getCountMissing());
        MM_UIUtils.setTextViewInformation(_parentActivity, holder._return_0w, p._dps.get(0)._r1w, p._dps.get(0).countMissing);
        MM_UIUtils.setTextViewInformation(_parentActivity, holder._return_1w, p._dps.get(1)._r1w, p._dps.get(1).countMissing);
        MM_UIUtils.setTextViewInformation(_parentActivity, holder._return_2w, p._dps.get(2)._r1w, p._dps.get(2).countMissing);
        MM_UIUtils.setTextViewInformation(_parentActivity, holder._return_3w, p._dps.get(3)._r1w, p._dps.get(3).countMissing);
    }

    @Override
    public int getItemCount() { return _funds.size(); }
}

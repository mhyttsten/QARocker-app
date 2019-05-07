package com.pf.fl.screens.main;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.fl.screens.portfolio.PortfolioR_Activity;
import com.pf.fl.screens.utils.MM_UIUtils;
import com.pf.mr.R;
import com.pf.fl.screens.utils.RVRow4WSummaryHolder;
import com.pf.shared.analyze.DPSeries;
import com.pf.shared.datamodel.D_Portfolio;

public class MainActivity_RV_Adapter extends RecyclerView.Adapter<RVRow4WSummaryHolder> {

    private List<DPSeries> _portfolios = new ArrayList<>();
    private Activity _parentActivity;

    public MainActivity_RV_Adapter(Activity parentActivity) {
        _parentActivity = parentActivity;
        List<D_Portfolio> ps = DB_FundInfo_UI._portfolios;
        for (D_Portfolio p: ps) {
            System.out.println("About to calculate portfolio summary for: " + p._name);
            DPSeries dps = DB_FundInfo_UI.getPortfolioSummaryStats(p._name);
            System.out.println("...done calculating portfolio summary");
            _portfolios.add(dps);
        }
    }

    @Override
    public RVRow4WSummaryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.rv_row_4w_summary, parent, false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = v.findViewById(R.id.name);
                String name = tv.getText().toString();
                FLSingleton._portfolioName = name;
                Intent i = new Intent(_parentActivity, PortfolioR_Activity.class);
                _parentActivity.startActivity(i);
            }
        });

        return new RVRow4WSummaryHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RVRow4WSummaryHolder holder, int position) {
        DPSeries p = _portfolios.get(position);
        holder._name.setText(p._name);
        MM_UIUtils.setTextViewInformation(_parentActivity, holder._return_acc, p.getReturnAcc(), p.getCountMissing());
        if (p._dps.size() >= 1) {
            MM_UIUtils.setTextViewInformation(_parentActivity, holder._return_0w, p._dps.get(0)._r1w, p._dps.get(0).countMissing);
        }
        if (p._dps.size() >= 2) {
            MM_UIUtils.setTextViewInformation(_parentActivity, holder._return_1w, p._dps.get(1)._r1w, p._dps.get(1).countMissing);
        }
        if (p._dps.size() >= 3) {
            MM_UIUtils.setTextViewInformation(_parentActivity, holder._return_2w, p._dps.get(2)._r1w, p._dps.get(2).countMissing);
        }
        if (p._dps.size() >= 4) {
            MM_UIUtils.setTextViewInformation(_parentActivity, holder._return_3w, p._dps.get(3)._r1w, p._dps.get(3).countMissing);
        }
    }

    @Override
    public int getItemCount() {
        return _portfolios.size();
    }
}

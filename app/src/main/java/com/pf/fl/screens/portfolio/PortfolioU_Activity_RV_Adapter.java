package com.pf.fl.screens.portfolio;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pf.fl.datamodel.DB_FundInfo_UI;
import com.pf.mr.R;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.datamodel.D_Portfolio;

import java.util.ArrayList;
import java.util.List;

public class PortfolioU_Activity_RV_Adapter extends RecyclerView.Adapter<PortfolioU_Activity_RV_Holder> {

    public List<CheckBoxTextView> _funds = new ArrayList<>();
    private Activity _parentActivity;

    public PortfolioU_Activity_RV_Adapter(Activity parentActivity) {
        _parentActivity = parentActivity;
    }

    @Override
    public PortfolioU_Activity_RV_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.portfolio_u_rv_checkbox_textview, parent, false);
        return new PortfolioU_Activity_RV_Holder(itemView);
    }

    @Override
    public void onBindViewHolder(PortfolioU_Activity_RV_Holder holder, int position) {
        CheckBoxTextView cbtv = _funds.get(position);
        holder._textView.setText(cbtv._text);
        holder._checkBox.setChecked(cbtv._check);
        holder._funds = _funds;
    }

    @Override
    public int getItemCount() { return _funds.size(); }

    //------------------------------------------------------------------------
    public static class CheckBoxTextView {
        public CheckBoxTextView(boolean check, String text, String url) {
            _check = check;
            _text = text;
            _url = url;
        }
        public boolean _check;
        public String  _text = "";
        public String _url = "";
    }

    //------------------------------------------------------------------------
    public void initializeList(String type) {
        List<D_FundInfo> fundsAll = DB_FundInfo_UI._fundsByType.get(type);
        D_Portfolio p = DB_FundInfo_UI._portfoliosHM.get(type);
        _funds.clear();
        for (D_FundInfo fi: fundsAll) {
            boolean found = false;
            CheckBoxTextView cbtv = new CheckBoxTextView(false, fi._nameMS, fi._url);
            if (p != null && p._urls != null) {
                for (String url : p._urls) {
                    if (url.equals(fi._url)) {
                        cbtv._check = true;
                        found = true;
                        _funds.add(0, cbtv);
                    }
                }
            }
            if (!found) {
                _funds.add(cbtv);
            }
        }

        notifyDataSetChanged();
    }
}

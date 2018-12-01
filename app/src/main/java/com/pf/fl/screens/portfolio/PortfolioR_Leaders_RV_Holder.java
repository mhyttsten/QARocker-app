package com.pf.fl.screens.portfolio;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.pf.mr.R;

public class PortfolioR_Leaders_RV_Holder extends RecyclerView.ViewHolder {
    public TextView _name;
    public TextView _return_acc;
    public TextView _position;
    public TextView _misc1;
    public TextView _misc2;
    public TextView _misc3;

    public PortfolioR_Leaders_RV_Holder(View view) {
        super(view);
        _name = (TextView) view.findViewById(R.id.name);
        _return_acc = (TextView) view.findViewById(R.id.return_acc);
        _position = (TextView) view.findViewById(R.id.position);
        _misc1 = (TextView) view.findViewById(R.id.misc1);
        _misc2 = (TextView) view.findViewById(R.id.misc2);
        _misc3 = (TextView) view.findViewById(R.id.misc3);
    }
}

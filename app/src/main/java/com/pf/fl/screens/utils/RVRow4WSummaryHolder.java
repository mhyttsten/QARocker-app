package com.pf.fl.screens.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.pf.mr.R;

public class RVRow4WSummaryHolder extends RecyclerView.ViewHolder {
    public TextView _name;
    public TextView _return_acc;
    public TextView _return_0w;
    public TextView _return_1w;
    public TextView _return_2w;
    public TextView _return_3w;

    public RVRow4WSummaryHolder(View view) {
        super(view);
        _name = (TextView) view.findViewById(R.id.name);
        _return_acc = (TextView) view.findViewById(R.id.return_acc);
        _return_0w = (TextView) view.findViewById(R.id.return_0w);
        _return_1w = (TextView) view.findViewById(R.id.return_1w);
        _return_2w = (TextView) view.findViewById(R.id.return_2w);
        _return_3w = (TextView) view.findViewById(R.id.return_3w);
    }
}

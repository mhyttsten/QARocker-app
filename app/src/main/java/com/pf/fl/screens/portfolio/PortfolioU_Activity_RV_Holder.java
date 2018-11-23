package com.pf.fl.screens.portfolio;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.pf.mr.R;

import java.util.ArrayList;
import java.util.List;

public class PortfolioU_Activity_RV_Holder extends RecyclerView.ViewHolder {

    public CheckBox _checkBox;
    public TextView _textView;
    public List<PortfolioU_Activity_RV_Adapter.CheckBoxTextView> _funds = new ArrayList<>();

    public PortfolioU_Activity_RV_Holder(View view) {
        super(view);
        _textView = (TextView) view.findViewById(R.id.text);
        _checkBox = (CheckBox) view.findViewById(R.id.checkbox);

        _checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox)v;
                PortfolioU_Activity_RV_Adapter.CheckBoxTextView cbtv = _funds.get(getAdapterPosition());
                cbtv._check = cb.isChecked();
            }
        });


    }
}

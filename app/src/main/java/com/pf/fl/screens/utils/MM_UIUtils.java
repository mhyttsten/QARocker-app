package com.pf.fl.screens.utils;

import android.app.Activity;
import android.widget.TextView;

import com.pf.mr.R;
import com.pf.shared.datamodel.D_FundDPDay;

public class MM_UIUtils {

    public static void setTextViewInformation(Activity a, TextView tv, float v, int countMissing) {
        if (v != D_FundDPDay.FLOAT_NULL) {
            tv.setText(String.format("%.2f", v));
            if (countMissing == 0) {
                if (v >= 0F) {
                    tv.setTextColor(a.getResources().getColor(R.color.green));
                } else {
                    tv.setTextColor(a.getResources().getColor(R.color.red));
                }
            }
        } else {
            tv.setText("N");
        }
    }

    public static int getTextViewAsInt(TextView tv) {
        if (tv == null) {
            throw new AssertionError("Argument TextView was null");
        }
        String s = tv.getText().toString();
        if (s == null) return 0;
        try {
            int r = Integer.parseInt(s);
            return r;
        } catch(NumberFormatException exc) {
            return 0;
        }
    }
}

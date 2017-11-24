package com.pf.fl.analysis;

import com.pf.fl.datamodel.DM_Fund;

public class DataPoint {
    public DataPoint(DM_Fund fund, String friday, Double value) {
        mFund = fund;
        mFridayYYMMDD = friday;
        mValue = value;
    }

    public String toString() {
        return mValue == null ? "null" : String.format("%.2f", mValue.doubleValue());
    }

    public DM_Fund mFund;
    public String mFridayYYMMDD;
    public Double mValue;
}

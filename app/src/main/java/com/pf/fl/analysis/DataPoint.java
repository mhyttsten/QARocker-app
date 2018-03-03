package com.pf.fl.analysis;

import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;

public class DataPoint {
    public DataPoint(D_FundInfo fund, String friday, float value) {
        mFund = fund;
        mFridayYYMMDD = friday;
        mValue = value;
    }

    public String toString() {
        return mValue == D_FundDPDay.FLOAT_NULL ? "null" : String.format("%.2f", mValue);
    }

    public D_FundInfo mFund;
    public String mFridayYYMMDD;
    public float mValue = D_FundDPDay.FLOAT_NULL;
}

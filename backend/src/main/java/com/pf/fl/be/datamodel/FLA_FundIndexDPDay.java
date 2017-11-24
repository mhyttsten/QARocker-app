package com.pf.fl.be.datamodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class FLA_FundIndexDPDay {
	
	public static final Comparator<FLA_FundIndexDPDay> COMPARATOR_DATE = new Comparator<FLA_FundIndexDPDay>() {
		public int compare(FLA_FundIndexDPDay a1, FLA_FundIndexDPDay a2) {
			return a1.mDateYYMMDD.compareTo(a2.mDateYYMMDD);
		}
	};

	public String mDateYYMMDD; // Required, always non-null. On a friday whenever possible
	public double mR1wAvg;
    public double mR1wMax = Double.MIN_VALUE;
    public double mR1wMin = Double.MAX_VALUE;
    public double mR1wMed;
    public int mCount;
	public List<Double> mR1ws = new ArrayList<>();

    public String toSingleLineString() {
        String result =
                mDateYYMMDD.substring(2) +
                        ",ctn:" + mCount +
                        ",med:" + mR1wMed +
                        ",avg:" + mR1wAvg +
                        ",max:" + mR1wMax +
                        ",min:" + mR1wMin;
        return result;
    }

    // This String key is a Long (for the FundInfo mId long)
    public HashMap<String, Void> mFundInfoLongIds = new HashMap<>();

    public boolean add(Long fundInfoId, FLA_FundDPDay fundDPDay) throws Exception {
        if (mDateYYMMDD != null && !mDateYYMMDD.equals(fundDPDay.mDateYYMMDD)) {
            throw new Exception("Expected dates to match");
        }

        String fundInfoIdAsString = String.valueOf(fundInfoId.longValue());

        if (fundDPDay.mR1w == null) {
            return false;
        }
        if (mFundInfoLongIds.containsKey(fundInfoIdAsString)) {
            return true;
        }

        if (mR1wMax < fundDPDay.mR1w) {
            mR1wMax = fundDPDay.mR1w;
        }
        if (mR1wMin > fundDPDay.mR1w) {
            mR1wMin = fundDPDay.mR1w;
        }
        mR1ws.add(fundDPDay.mR1w);
        Collections.sort(mR1ws);

        mCount = mR1ws.size();
        mR1wAvg = 0;
        for (Double d : mR1ws) {
            mR1wAvg += d;
        }
        mR1wAvg /= (double)mCount;

        if (mR1ws.size() == 1) {
            mR1wMed = mR1ws.get(0);
        } else if ((mR1ws.size() % 2) == 0) {
            // Find the middle value = empty space between the two middle values
            int index = mR1ws.size() / 2;
            double d1 = mR1ws.get(index-1);
            double d2 = mR1ws.get(index);
            mR1wMed = (d1+d2)/2.0D;
        } else {
            int index = mR1ws.size() / 2;
            mR1wMed = mR1ws.get(index);
        }

        mFundInfoLongIds.put(fundInfoIdAsString, null);
        return true;
    }

    public String toStringOneLine(boolean includeOrigDate) {
        String result = mDateYYMMDD.substring(2);
        result += ",avg:" + mR1wAvg;
        result += ",med:" + mR1wMed;
        result += ",max:" + mR1wMax;
        result += ",min:" + mR1wMin;
        return result;
    }
}

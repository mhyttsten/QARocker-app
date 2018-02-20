package com.pf.fl.be.datamodel;

import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FLA_FundDPDay {

	public static final Comparator<FLA_FundDPDay> COMPARATOR_DATE_ADJUSTED_REVERSE = new Comparator<FLA_FundDPDay>() {
		public int compare(FLA_FundDPDay a1, FLA_FundDPDay a2) {
			return -a1.mDateYYMMDD.compareTo(a2.mDateYYMMDD);
		}
	};


	public static final Comparator<FLA_FundDPDay> COMPARATOR_DATE_ADJUSTED = new Comparator<FLA_FundDPDay>() {
		public int compare(FLA_FundDPDay a1, FLA_FundDPDay a2) {
			return a1.mDateYYMMDD.compareTo(a2.mDateYYMMDD);
		}
	};
	
	public static final Comparator<FLA_FundDPDay> COMPARATOR_DATE_ORIG = new Comparator<FLA_FundDPDay>() {
		public int compare(FLA_FundDPDay a1, FLA_FundDPDay a2) {
			return a1.mDateYYMMDD_Orig.compareTo(a2.mDateYYMMDD_Orig);
		}
	};
	
	public String mDateYYMMDD_Orig; // Required, never null, always 6 characers and valid date
	public Double mR1w_Orig;        // Optional
	
	public String mDateYYMMDD; // Required, always non-null. On a friday whenever possible
	public String mCurrency;   // Required, always non-null
	public Double mR1d;  // Optional
	public Double mR1w;  // Can be null, but what does that mean
	public Double mR1m;  // Optional
	public Double mR3m;  // Optional
	public Double mR6m;  // Optional
	public Double mR1y;  // Optional
	public Double mR3y;  // Optional
	public Double mR5y;  // Optional
	public Double mR10y; // Optional
	public List<FLA_FundDPYear> mDPYears = new ArrayList<FLA_FundDPYear>(); // Assume sorted descending order

    public static String toStringOneLine(boolean includeOrigDate, List<FLA_FundDPDay> elems) {
        StringBuffer strb = new StringBuffer();
        for (int i=0; i < elems.size(); i++) {
            strb.append(elems.get(i).toStringOneLine(includeOrigDate));
            if (i+1 < elems.size()) {
                strb.append(", ");
            }
        }
        return strb.toString();
    }

    public String toStringOneLine(boolean includeOrigDate) {
        String result = mDateYYMMDD.substring(2);
        if (includeOrigDate) {
           result = mDateYYMMDD;
           result += "[" + mDateYYMMDD_Orig + "]";
        }
        result += ":" + mR1w;
        return result;
    }

    public void addDPYear(FLA_FundDPYear arg) {
		mDPYears.add(arg);
		Collections.sort(mDPYears, FLA_FundDPYear.COMPARATOR);
	}
	
	public List<FLA_FundDPYear> getDPYears() {
		Collections.sort(mDPYears, FLA_FundDPYear.COMPARATOR);
		return mDPYears;
	}
	
	/**
	 * 
	 */
	public static FLA_FundDPDay instantiate(
			String origYYMMDD,
			String currency,
			Double r1d, Double r1w, Double r1m, Double r3m, Double r6m, Double r1y, Double r3y, Double r5y, Double r10y) {
		FLA_FundDPDay r = new FLA_FundDPDay();
		r.mDateYYMMDD_Orig = origYYMMDD;
		r.mCurrency = currency;
		r.mR1w_Orig = r1w;
		r.mDateYYMMDD = origYYMMDD;
		r.mR1d = r1d;
		r.mR1w = r1w;
		r.mR1m = r1m;
		r.mR3m = r3m;
		r.mR6m = r6m;
		r.mR1y = r1y;
		r.mR3y = r3y;
		r.mR5y = r5y;
		r.mR10y = r10y;
		return r;
	}

	/**
	 * 
	 */
	public boolean equals(FLA_FundDPDay a, IndentWriter iw) {
		if (iw == null) {
			iw = new IndentWriter();
		}
		iw.println(this.getClass().getName() + ".equals");
		iw.push();
		if (!MM.equals(mDateYYMMDD_Orig,  a.mDateYYMMDD_Orig,  "mDateYYMMDD_Orig", iw) ||
			!MM.equals(mR1w_Orig,  a.mR1w_Orig,  "mR1w_Orig", iw) ||
			!MM.equals(mDateYYMMDD,  a.mDateYYMMDD,  "mDateYYMMDD", iw) ||
			!MM.equals(mR1d,  a.mR1d,  "mR1d", iw) ||
			!MM.equals(mR1w,  a.mR1w,  "mR1w", iw) ||
			!MM.equals(mR1m,  a.mR1m,  "mR1m", iw) ||
			!MM.equals(mR3m,  a.mR3m,  "mR3m", iw) ||
			!MM.equals(mR6m,  a.mR6m,  "mR6m", iw) ||
			!MM.equals(mR1y,  a.mR1y,  "mR1y", iw) ||
			!MM.equals(mR3y,  a.mR3y,  "mR3y", iw) ||
			!MM.equals(mR5y,  a.mR5y,  "mR5y", iw) ||
			!MM.equals(mR10y,  a.mR10y,  "mR10y", iw)) {
			iw.pop();
			return false;
		}
		
		if (mDPYears == null && a.mDPYears == null) {
			iw.pop();
			return true;
		}
		
		if (mDPYears == null || a.mDPYears == null) {
			iw.println("mDPYears, one was null but not the other");
			iw.pop();
			return false;
		}

		if (mDPYears.size() != a.mDPYears.size()) {
			iw.println("mDPYears, size differs");
			iw.pop();
			return false;
		}
		
		for (int i=0; i < mDPYears.size(); i++) {
			FLA_FundDPYear a1 = mDPYears.get(i);
			FLA_FundDPYear a2 = a.mDPYears.get(i);
			if (!a1.equals(a2, iw)) {
				iw.pop();
				return false;
			}
		}
		
		iw.pop();
		return true;
	}	
}

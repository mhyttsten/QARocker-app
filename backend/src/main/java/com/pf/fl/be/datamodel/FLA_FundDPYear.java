package com.pf.fl.be.datamodel;

import com.pf.shared.IndentWriter;
import com.pf.shared.MM;

import java.util.Comparator;

public class FLA_FundDPYear {

	public static final Comparator<FLA_FundDPYear> COMPARATOR = new Comparator<FLA_FundDPYear>() {
		public int compare(FLA_FundDPYear a1, FLA_FundDPYear a2) {
			return (int)(a2.mYear - a1.mYear);
		}
	};
	
	public long   mYear;	       // Always 4 digits or 9999 if Year to Date
	public double mResultFund;     // Required
	public Double mResultCategory; // Optional
	public Double mResultIndex;	   // Optional
	
	public boolean isYearToDate() {
		return mYear == 9999L;
	}

	/**
	 * 
	 */
	public boolean equals(FLA_FundDPYear a, IndentWriter iw) {
		if (iw == null) {
			iw = new IndentWriter();
		}
		iw.println(this.getClass().getName() + ".equals");
		iw.push();
		
		if (mYear != a.mYear) {
			iw.println("Year differs this:" + mYear + ", a.mYear:" + a.mYear);
			iw.pop();
			return false;
		}
		if (mResultFund != a.mResultFund) {
			iw.println("Resul fund differs this:" + mResultFund + ", a.mYear:" + a.mResultFund);
			iw.pop();
			return false;
		}
		if (!MM.equals(mResultCategory,  a.mResultCategory,  "mResultCategory", iw) ||
			!MM.equals(mResultIndex,  a.mResultIndex,  "mResultIndex", iw)) {
			iw.pop();
			return false;
		}
		iw.pop();
		return true;
	}
	
	/**
	 * 
	 */
	public static FLA_FundDPYear instantiate(
			Long year,
			double resultFund, Double resultCategory, Double resultIndex) {
		FLA_FundDPYear r = new FLA_FundDPYear();
		r.mYear = year;
		r.mResultFund = resultFund;
		r.mResultCategory = resultCategory;
		r.mResultIndex = resultIndex;
		return r;
	}
	
}

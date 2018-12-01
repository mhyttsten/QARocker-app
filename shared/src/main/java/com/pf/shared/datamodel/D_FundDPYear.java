package com.pf.shared.datamodel;

import com.pf.shared.utils.IndentWriter;

import java.util.Comparator;

public class D_FundDPYear {
	public static Comparator<D_FundDPYear> COMPARATOR = new Comparator<D_FundDPYear>() {
		@Override
		public int compare(D_FundDPYear o1, D_FundDPYear o2) {
			if (o1.isYearToDate()) {
				return -1;
			}
			// Sort descending order
			return -(o1._year - o2._year);
		}
	};

	public short  _year;	       // Always 4 digits or 9999 if Year to Date
	public float _resultFund;      // Required
	public float _resultCategory;  // Optional
	public float _resultIndex;	   // Optional
	
	public boolean isYearToDate() {
		return _year == 9999L;
	}
	
	public void dumpInfo(IndentWriter iw) {
		iw.print(_year
				+ "[" + D_FundDPDay.f2s(_resultFund)
				+ "," + D_FundDPDay.f2s(_resultCategory)
				+ "," + D_FundDPDay.f2s(_resultIndex) + "]");
	}

	public String toString() {
		IndentWriter iw = new IndentWriter();
		dumpInfo(iw);
		return iw.toString();
	}

	//------------------------------------------------------------------------
	public D_FundDPYear cloneMe() {
		try {
			return (D_FundDPYear)clone();
		} catch(CloneNotSupportedException exc) {
			throw new AssertionError("Assumed this was a clonable object");
		}
	}
}

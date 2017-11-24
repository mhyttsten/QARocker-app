package com.pf.fl.be.datamodel;

public class FLA_FundPortfolioEntry {

	public String mDateYYMMDD_DayFirst; // Required, never null, always 6 characers and valid date
	public String mDateYYMMDD_DayLast;  // Null if not set

	public Long mFundInfoId;
}

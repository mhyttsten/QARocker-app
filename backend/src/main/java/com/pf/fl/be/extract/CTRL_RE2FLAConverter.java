package com.pf.fl.be.extract;

import com.pf.fl.be.datamodel.FLA_FundDPDay;
import com.pf.fl.be.datamodel.FLA_FundDPYear;
import com.pf.fl.be.datamodel.FLA_FundInfo;
import com.pf.fl.be.datastore.DS;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;
import com.pf.fl.be.datamodel_raw.REFundInfo;
import com.pf.fl.be.datamodel_raw.REFundInfo_DPDay;
import com.pf.fl.be.datamodel_raw.REFundInfo_DPYear;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class CTRL_RE2FLAConverter {

	
	private static final Logger log = Logger.getLogger(CTRL_RE2FLAConverter.class.getName());
	private static final String TAG = MM.getClassName(CTRL_RE2FLAConverter.class.getName());
	
	private static final String C_SEK = "SEK";
		
	/**
	 * 
	 */
	public static OTuple2G<Boolean, FLA_FundInfo> convertToFLA_REFundInfo(
            IndentWriter iw,
			REFundInfo msFD) {
        iw.println("CTRL_RE2FLAConverter.convertToFLA_REFundInfo");
        iw.push();

		OTuple2G<Boolean, FLA_FundInfo> result = convertTo_FLAImpl(iw, msFD);
		if (!result._o1) {
            iw.println("Validation Error for: \" + msFD.getAccountType() + \".\" + msFD.getName()");
		}

        iw.pop();
		return result;
	}

	// ***********************************************************************

    public static OTuple2G<Boolean, FLA_FundInfo> convertTo_FLAImpl(
            IndentWriter iw,
            REFundInfo msFD) {
        iw.println("CTRL_RE2FLAConverter.convertTo_FLAImpl");
        iw.push();
        OTuple2G<Boolean, FLA_FundInfo> r = convertTo_FLAImpl2(iw, msFD);
        iw.pop();
        return r;
    }
	private static OTuple2G<Boolean, FLA_FundInfo> convertTo_FLAImpl2(
            IndentWriter iw,
			REFundInfo msFD) {

//		log.info("CTRL_RE2FLAConverter.convertTo_FLAImpl2, msFD.name: " + msFD.getName());

        FLA_FundInfo flaFundInfo = null;
		OTuple2G<Boolean, FLA_FundInfo> fundInfoResult = convertTo_FLA_FundInfo(iw, msFD);
		if (!fundInfoResult._o1) {
			return new OTuple2G<>(false, null);
		}
		flaFundInfo = fundInfoResult._o2;
		
		// Fix DPDays
		List<REFundInfo_DPDay> dpDays = msFD.getDPDays();
		if (dpDays == null) {
			iw.println("Recent returns was null");
			return new OTuple2G<>(false, null);
		} else if (dpDays.size() == 0) {
			iw.println("Recent returns had size 0");
			return new OTuple2G<>(false, null);
		} else if (dpDays.size() > 1) {
			// This is a common case, a USD fund will have recordings for SEK (eg)
			// Info message will be printed as part of checks below
			// iwInfo.println("Recent returns had size >1, it was: " + dpDays.size());
		}

		for (REFundInfo_DPDay msrdArg: dpDays) {
			// Check that all dates are the same as for fundInfo
			if (!msrdArg.getDateYYMMDD().equals(msFD.getDateYYMMDD())) {
				iw.println("Different date for fundData: " + msFD.getDateYYMMDD() + ", and rowData: " + msrdArg.getDateYYMMDD());
				return new OTuple2G<>(false, null);
			}
		}
		
		REFundInfo_DPDay msrd = null;
		for (REFundInfo_DPDay msrdArg: dpDays) {
			if (msrdArg.getCurrency().toUpperCase().equals(C_SEK)) {
				if (msrd != null && msrd.getCurrency().toUpperCase().equals(C_SEK)) {
					iw.println("Currency.1: Currency SEK found for multiple MSRowData items");
					return new OTuple2G<>(false, null);
				} else if (msrd != null){
					iw.println("Currency.2: Currency replaced with SEK, previous one was: " + msrd.getCurrency());
				}
				if (!msFD.getCurrency().toUpperCase().equals(C_SEK)) {
					iw.println("Currency.3: Replaced with SEK, previous one was: " + msrd.getCurrency());
				}
				msrd = msrdArg;
			} else if (msrdArg.getCurrency().equals(msFD.getCurrency())) {
				if (msrd != null && msrd.getCurrency().equals(msrdArg.getCurrency())) {
					iw.println("Currency.4: Two equal currencies detected for same DPDay: " + msrd.getCurrency());
					return new OTuple2G<>(false, null);
				}
				else if (msrd != null) {
					iw.println("Currency.5: Multi-DPDay replaced, it had currency: " + msrd.getCurrency() + ", and we found native fund currency: " + msrdArg.getCurrency());
				}
				msrd = msrdArg;
			} else if (msrd == null) {
				msrd = msrdArg;
			} else {
				iw.println("Currency.6: Multi-DPDay ARG replaced, it had currency: " + msrdArg.getCurrency() + ", existing had: " + msrd.getCurrency() + ", fund had: " + msFD.getCurrency());
			}			
		}
		if (msrd == null) {
			iw.println("Currency.7: No DPDay entry could be found with for fund data with currency: " + msFD.getCurrency());
			return new OTuple2G<>(false, null);
		}		

		OTuple2G<Boolean, FLA_FundDPDay> fundDPDayResult = convertToFLA_REFundInfo_DPDay(iw, msrd);
		if (!fundDPDayResult._o1) {
			return new OTuple2G<>(false, null);
		}
		FLA_FundDPDay fundDPDay = fundDPDayResult._o2;
		flaFundInfo.mDPDays.add(fundDPDay);
		Collections.sort(flaFundInfo.mDPDays, FLA_FundDPDay.COMPARATOR_DATE_ORIG);
		
		// Fix DPYears
		List<REFundInfo_DPYear> years = msFD.getDPYears();
		if (years == null) {
			iw.println("Null for year data points");
			return new OTuple2G<>(false, null);
		}
		for (REFundInfo_DPYear yd : years) {
			OTuple2G<Boolean, FLA_FundDPYear> msYDResult = convertToFLA_REFundInfo_DPYear(iw, yd);
			if (!msYDResult._o1) {
				return new OTuple2G<>(false, null);
			} else if(msYDResult._o2 != null){
				// It can be valid and null, if the data recorded for that year is '-'
				fundDPDay.addDPYear(msYDResult._o2);
			}
		}
		
		return new OTuple2G<>(true, flaFundInfo);
	}

    private static OTuple2G<Boolean, FLA_FundInfo> convertTo_FLA_FundInfo(
            IndentWriter iw,
            REFundInfo msFD) {
        iw.println("CTRL_RE2FLAConverter.convertTo_FLA_FundInfo2");
        iw.push();
        OTuple2G<Boolean, FLA_FundInfo> r = convertTo_FLA_FundInfo2(iw, msFD);
        iw.pop();
        return r;
    }
	private static OTuple2G<Boolean, FLA_FundInfo> convertTo_FLA_FundInfo2(
			IndentWriter iw,
			REFundInfo msFD) {

		String name = null;
		if (msFD.getName() == null || msFD.getName().trim().length() == 0) {
			iw.println("Name is invalid: " + msFD.getName());
			return new OTuple2G<>(false, null);
		}
		name = msFD.getName().trim();
		
		int rating = -1;
		if (msFD.getRating() != null && msFD.getRating().trim().length() > 0) {
			if (msFD.getRating().equals("stars1")) {
				rating = 1;
			} else if (msFD.getRating().equals("stars2")) {
				rating  = 2;
			} else if (msFD.getRating().equals("stars3")) {
				rating = 3;
			} else if (msFD.getRating().equals("stars4")) {
				rating = 4;
			} else if (msFD.getRating().equals("stars5")) {
				rating = 5;
			} else if (msFD.getRating().equals("-")) {
				rating = -1;
			} else {
				iw.println("Rating is invalid: " + msFD.getRating());
				return new OTuple2G<>(false, null);
			}
		}
		
		int ppmNumber = -1;
		if (msFD.getPPMNumber() != null &&
			msFD.getPPMNumber().trim().length() > 0 &&
			!msFD.getPPMNumber().trim().equals("-")){
			try {
				ppmNumber = Integer.parseInt(msFD.getPPMNumber());
			} catch(NumberFormatException exc) {
				iw.println("PPM number is set but not numeric: " + msFD.getPPMNumber());
				return new OTuple2G<>(false, null);
			}
		}
		
		if (msFD.getCurrency() == null || msFD.getCurrency().trim().length() == 0) {
			iw.println("Currency is invalid: " + msFD.getCurrency());
			return new OTuple2G<>(false, null);
		}

		if (msFD.getURL() == null || msFD.getURL().trim().length() == 0) {
			iw.println("URL is invalid: " + msFD.getURL());
			return new OTuple2G<>(false, null);
		}
		
		String accountType = null;
		if (msFD.getAccountType().equals(REFundInfo.ACCOUNT_TYPE_PPM)) {
			accountType = FLA_FundInfo.TYPE_PPM;
		} else if(msFD.getAccountType().equals(REFundInfo.ACCOUNT_TYPE_SEB)) {
			accountType = FLA_FundInfo.TYPE_SEB;
		} else if(msFD.getAccountType().equals(REFundInfo.ACCOUNT_TYPE_SPP)) {
			accountType = FLA_FundInfo.TYPE_SPP;
		} else if(msFD.getAccountType().equals(REFundInfo.ACCOUNT_TYPE_VANGUARD)) {
			accountType = FLA_FundInfo.TYPE_SPP;
		} else {
			iw.println("Account type is invalid: " + msFD.getAccountType());
			return new OTuple2G<>(false, null);
		}
		
		FLA_FundInfo fi = FLA_FundInfo.instantiate(accountType, name, msFD.getURL(), rating, ppmNumber, msFD.getDateYYMMDD());

		if (msFD.getCategory() != null && msFD.getCategory().trim().length() > 0) {
			fi.mCategory = DS.insertOrUpdateFundCategory(msFD.getCategory().trim());
		}
		
		fi.mCurrency = DS.insertOrUpdateCurrency(msFD.getCurrency().trim());
		
		if (msFD.getIndexCompare() != null && msFD.getIndexCompare().trim().length() > 0) {
			fi.mIndexCompare = DS.insertOrUpdateFundIndex(msFD.getIndexCompare().trim());
		}
		
		return new OTuple2G<>(true, fi);
	}
	
	private static OTuple2G<Boolean, FLA_FundDPDay> convertToFLA_REFundInfo_DPDay(
			IndentWriter iw,
			REFundInfo_DPDay msRD) {
		try {
			return convertToFLA_REFundInfo_DPDayImpl(iw, msRD);
		} catch(IOException exc) {
			iw.println("Exception: " + exc.getMessage());
			iw.println(MM.getStackTraceString(exc));
		}
		return new OTuple2G<>(false, null);
	}
	
	private static OTuple2G<Boolean, FLA_FundDPDay> convertToFLA_REFundInfo_DPDayImpl(
			IndentWriter iw,
			REFundInfo_DPDay msRD) throws IOException {
		
		int rc = 0;
		
		Double r1d = null;
		rc = MM.testDouble(msRD.getDateYYMMDD() + "_R1D", msRD.getR1D(), iw);
		if (rc == MM.V_TD_NAN) {
			return new OTuple2G<>(false, null);
		}
		else {
			r1d = MM.getDouble(msRD.getR1D());
		}

		Double r1w = null;
		rc = MM.testDouble(msRD.getDateYYMMDD() + "_R1W", msRD.getR1W(), iw);
		if (rc == MM.V_TD_NAN) {
			return new OTuple2G<>(false, null);
		}
		else {
			r1w = MM.getDouble(msRD.getR1W());
		}

		Double r1m = null;
		rc = MM.testDouble(msRD.getDateYYMMDD() + "_R1M", msRD.getR1M(), iw);
		if (rc == MM.V_TD_NAN) {
			return new OTuple2G<>(false, null);
		}
		else {
			r1m = MM.getDouble(msRD.getR1M());
		}

		Double r3m = null;
		rc = MM.testDouble(msRD.getDateYYMMDD() + "_R3M", msRD.getR3M(), iw);
		if (rc == MM.V_TD_NAN) {
			return new OTuple2G<>(false, null);
		}
		else { 
			r3m = MM.getDouble(msRD.getR3M());
		}

		Double r6m = null;
		rc = MM.testDouble(msRD.getDateYYMMDD() + "_R6M", msRD.getR6M(), iw);
		if (rc == MM.V_TD_NAN) {
			return new OTuple2G<>(false, null);
		}
		else {
			r6m = MM.getDouble(msRD.getR6M());
		}

		Double r1y = null;
		rc = MM.testDouble(msRD.getDateYYMMDD() + "_R1Y", msRD.getR1Y(), iw);
		if (rc == MM.V_TD_NAN) {
			return new OTuple2G<>(false, null);
		}
		else {
			r1y = MM.getDouble(msRD.getR1Y());
		}

		Double r3y = null;
		rc = MM.testDouble(msRD.getDateYYMMDD() + "_R3Y", msRD.getR3Y(), iw);
		if (rc == MM.V_TD_NAN) {
			return new OTuple2G<>(false, null);
		}
		else {
			r3y = MM.getDouble(msRD.getR3Y());
		}

		Double r5y = null;
		rc = MM.testDouble(msRD.getDateYYMMDD() + "_R5Y", msRD.getR5Y(), iw);
		if (rc == MM.V_TD_NAN) {
			return new OTuple2G<>(false, null);
		}
		else {
			r5y = MM.getDouble(msRD.getR5Y());
		}

		Double r10y = null;
		rc = MM.testDouble(msRD.getDateYYMMDD() + "_R10Y", msRD.getR10Y(), iw);
		if (rc == MM.V_TD_NAN) {
			return new OTuple2G<>(false, null);
		}
		else {
			r10y = MM.getDouble(msRD.getR10Y());
		}

		FLA_FundDPDay fundDPDay = FLA_FundDPDay.instantiate(msRD.getDateYYMMDD(), msRD.getCurrency(), r1d, r1w, r1m, r3m, r6m, r1y, r3y, r5y, r10y);
		return new OTuple2G<>(true, fundDPDay);
	}		

	private static OTuple2G<Boolean, FLA_FundDPYear> convertToFLA_REFundInfo_DPYear(
			IndentWriter iw,
			REFundInfo_DPYear msyd) {
		try {
			return convertToFLA_REFundInfo_DPYearImpl(iw, msyd);
		} catch(IOException exc) {
			iw.println("Exception: " + exc.getMessage());
			iw.println(MM.getStackTraceString(exc));
		}
		return new OTuple2G<>(false, null);
		
	}
	
	private static OTuple2G<Boolean, FLA_FundDPYear> convertToFLA_REFundInfo_DPYearImpl(
			IndentWriter iw,
			REFundInfo_DPYear msyd) throws IOException {

		Long year = null;
		if (msyd._year == null || msyd._year.trim().length() == 0) {
			iw.println("Year is null or has length 0: " + msyd._year);
			return new OTuple2G<>(false, null);
		} else if (msyd._year.startsWith("I ")) {
			year = 9999L;
		} else if (msyd._year.trim().length() != 4) {
			iw.println("Year is incorrect, was not Y2D and did not have length 4: " + msyd._year);
			return new OTuple2G<>(false, null);
		} else {
			try { year = Long.parseLong(msyd._year.trim().substring(0, 3)); }
			catch (NumberFormatException exc) { 
				iw.println("Year was incorrect: " + msyd._year);
				return new OTuple2G<>(false, null);
			}
		}
		
		int rc = 0;
		Double resultFund = null;
		rc = MM.testDouble("Fund", msyd._resultFund, iw);
		if (rc == MM.V_TD_NAN) {
			iw.println("MSFundYearData, error for resultFund: " + msyd._resultFund);
			return new OTuple2G<>(false, null);
		} else if (rc == MM.V_TD_NULL || rc == MM.V_TD_DASH) {
			return new OTuple2G<>(true, null);
		} else if (rc == MM.V_TD_OK) {
			resultFund = MM.getDouble(msyd._resultFund);
		} else {
			throw new IOException("Unknown rc: " + rc);
		}
		
		Double resultCategory = null;
		rc = MM.testDouble("Category", msyd._resultCategory, iw);
		if (rc == MM.V_TD_NAN) {
			iw.println("MSFundYearData, error for resultCategory: " + msyd._resultCategory);
			return new OTuple2G<>(false, null);
		} else {
			resultCategory = MM.getDouble(msyd._resultCategory);
		}

		Double resultIndex = null;		
		rc = MM.testDouble("Index", msyd._resultIndex, iw);
		if (rc == MM.V_TD_NAN) {
			iw.println("MSFundYearData, error for resultIndex: " + msyd._resultIndex);
			return new OTuple2G<>(false, null);
		} else {
			resultIndex = MM.getDouble(msyd._resultIndex);
		}
		
		FLA_FundDPYear r = FLA_FundDPYear.instantiate(year, resultFund, resultCategory, resultIndex);
		return new OTuple2G<>(true, r);
	}
}

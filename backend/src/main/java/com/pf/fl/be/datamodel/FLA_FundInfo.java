package com.pf.fl.be.datamodel;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;
import com.pf.fl.be.util.Constants;
import com.pf.shared.utils.IndentWriter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Embedded;

@Entity
public class FLA_FundInfo {
	
	public static final String TYPE_PPM = Constants.ACCOUNT_TYPE_PPM;
	public static final String TYPE_SEB = Constants.ACCOUNT_TYPE_SEB;
	public static final String TYPE_SPP = Constants.ACCOUNT_TYPE_SPP;
	public static final String TYPE_VANGUARD = Constants.ACCOUNT_TYPE_VANGUARD;
    public static final String[] TYPES = new String[] { TYPE_PPM, TYPE_SEB, TYPE_SPP, TYPE_VANGUARD };

    // Not actual types, used in search conditions
    public static final String TYPE_ALL = "ALL";
    public static final String TYPE_INVALID = "INVALID";

	@Id public Long mId;
	@Index public String mURL;  // Required: Non-null and length > 0	

	@Index public String mType; // Required: Must be one of TYPE_...
	@Index public String mName; // Required: Non-null and length > 0
	@Index public String mDateYYMMDD_Updated; // Required (last time we actually updated)
	@Index public String mDateYYMMDD_Update_Attempted; // Required (last time we check Internet with intent to update)
	@Index public long mMSRating;  // -1 if none
	@Unindex public long mPPMNumber; // -1 if none
	@Index public Ref<FLA_FundCategory> mCategory; // Optional, can be null
	@Index public Ref<FLA_FundIndex> mIndexCompare; // Optional, can be null
	@Index public Ref<FLA_Currency> mCurrency; // Required: Non-null (always the original)
	@Unindex @Embedded public List<FLA_FundDPDay> mDPDays = new ArrayList<>();

	// isValid can be because:
	// - URL did not exist
	// - Extracting that URL resulted in another fund name
	@Index public boolean mIsValid = false;
	public static final int IC_NONE = 0;
	public static final int IC_COM_NO_SUCH_URL = 1;
	public static final int IC_DATA_NEVER_EXTRACTED = 3; //
	public static final int IC_DATA_CONVERSION_ERROR = 10;
	public static final int IC_DATA_NAME_MISMATCH = 11; // This should actually never happen as we rename instead
	public static final int IC_DATA_HAD_LENGTH_0 = 12;
	public static final int IC_DATA_WAS_NULL = 13;
	public static final int IC_DATA_FUND_NOT_FOUND = 14;
	public static final int IC_DATA_MSCATEGORY_NOT_FOUND = 15;
	public static final int IC_DATA_NO_R1W = 16;
	public static final int IC_DATA_TICKER_MISMATCH = 17;
	public static final int IC_DATA_TOTAL_RETURNS_NOT_FOUND = 18;
	public static final int IC_DATA_DATE_NOT_FOUND = 19;

	@Unindex public int mInvalidCode = IC_DATA_NEVER_EXTRACTED;
	@Unindex public String mInvalidString;
	public static String getICString(int icCode) {
		switch (icCode) {
			case IC_NONE: return "None (unknown)";
			case IC_COM_NO_SUCH_URL: return "Invalid URL";
			case IC_DATA_NEVER_EXTRACTED: return "Never extracted";
			case IC_DATA_CONVERSION_ERROR: return "Data conversion error";
			case IC_DATA_NAME_MISMATCH: return "Name mismatch";
			case IC_DATA_HAD_LENGTH_0: return "Data had length == 0";
			case IC_DATA_WAS_NULL: return "Data was null";
			case IC_DATA_FUND_NOT_FOUND: return "URL did not get to fund data";
			case IC_DATA_MSCATEGORY_NOT_FOUND: return "MS Category not found";
			case IC_DATA_NO_R1W: return "No R1W value found for fund";
			default: return "Defaulting/Unknown";
		}
	}

	public String getTypeAndName() {
		return mType + "." + mName;
	}

	/**
	 *
	 */
	public FLA_FundInfo createCopy() {
		FLA_FundInfo fi = new FLA_FundInfo();
		fi.mId = mId;
		fi.mURL = mURL;
		fi.mType = mType;
		fi.mName = mName;
		fi.mDateYYMMDD_Updated = mDateYYMMDD_Updated;
		fi.mDateYYMMDD_Update_Attempted = mDateYYMMDD_Update_Attempted;
		fi.mMSRating = mMSRating;
		fi.mPPMNumber = mPPMNumber;
		fi.mCategory = mCategory;
		fi.mIndexCompare = mIndexCompare;
		fi.mCurrency = mCurrency;
		fi.mDPDays = mDPDays;
		fi.mIsValid = mIsValid;
		fi.mInvalidCode = mInvalidCode;
		return fi;
	}

    public String toString() {
        return mType + "." + mName;
    }

    public String getAsOneLiner() {
        StringBuilder strb = new StringBuilder();
        strb.append("FLA_FundInfo: " + mType + "." + mName);
        strb.append(", id:" + mId + ", upd:" + mDateYYMMDD_Updated + ", atm:" + mDateYYMMDD_Update_Attempted);
        strb.append(", dpc:" + mDPDays.size() + ", [");
        int count = 0;
        for (int i=mDPDays.size()-1; i >= 0 && i >= mDPDays.size()-4; i--) {
            FLA_FundDPDay fdpday = mDPDays.get(i);
            strb.append(fdpday.mDateYYMMDD + ":" + fdpday.mR1w + ", ");
        }
        strb.append("], ");
        strb.append(", v:" + mIsValid + ", u:" + mURL + ", r:" + mMSRating);
        strb.append(", ppm:" + mPPMNumber + ", c:" + mCategory + ", ic:" + mIndexCompare + ", c:" + mCurrency);
        return strb.toString();
    }
		
	public void dumpInfo(IndentWriter iw) { 
		iw.println("FLA_FundInfo: " + mType + "." + mName + ", id: " + mId + ", upd: " + mDateYYMMDD_Updated + ", atm: " + mDateYYMMDD_Update_Attempted);
		iw.push();
		iw.println("IsValid: " + mIsValid);
		iw.println("InvalidCode: " + mInvalidCode);
		iw.println("URL: " + mURL);
		iw.println("Rating: " + mMSRating);
		iw.println("PPM: " + mPPMNumber);
		iw.println("Category:" + mCategory);
		iw.println("Index: " + mIndexCompare);
		iw.println("Currency: " + mCurrency);
		iw.print("DPDays count: " + mDPDays.size() + ": ");
		for (int i=mDPDays.size()-1; i >= 0; i--) {
			iw.print(mDPDays.get(i).mDateYYMMDD + ":" + mDPDays.get(i).mR1w);
			if (i > 0) {
				iw.print(", ");
			}
		}
		iw.println();
		iw.pop();
	}

    public String getSummary() {
        StringBuilder strb = new StringBuilder();
        strb.append(mType + "." + mName + "\n");
        strb.append("U:" + mDateYYMMDD_Updated + ", ");
        strb.append("A:" + mDateYYMMDD_Update_Attempted + ", ");
        if (mDPDays.size() > 0) {
            FLA_FundDPDay e = mDPDays.get(mDPDays.size()-1);
            strb.append("[" + e.mDateYYMMDD + ":" + e.mR1w + "], ");
        }
        strb.append(mURL);
        return strb.toString();
    }
	
	/**
	 * 
	 */
	public static FLA_FundInfo instantiate(
			String type,
			String name, 
			String url,
			long rating,
			long ppmNumber,
			String dateYYMMDD) {
		FLA_FundInfo r = new FLA_FundInfo();
		r.mName = name;
		r.mDateYYMMDD_Updated = dateYYMMDD;
		r.mDateYYMMDD_Update_Attempted = dateYYMMDD;
		r.mMSRating = rating;
		r.mPPMNumber = ppmNumber;
		r.mURL = url;
		r.mType = type;
		return r;
	}
	
}



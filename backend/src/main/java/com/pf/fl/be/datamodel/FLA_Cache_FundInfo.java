package com.pf.fl.be.datamodel;

import com.googlecode.objectify.Ref;
import com.pf.fl.be.util.EE;
import com.pf.shared.IndentWriter;
import com.pf.shared.MM;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class FLA_Cache_FundInfo {

    private static final Logger log = Logger.getLogger(FLA_Cache_FundInfo.class.getName());
    private static final String TAG = MM.getClassName(FLA_Cache_FundInfo.class.getName());

    public static final int CACHED_DPS = 26;

    public static Comparator<FLA_Cache_FundInfo> COMPARATOR =
              new Comparator<FLA_Cache_FundInfo>() {
                  public int compare(FLA_Cache_FundInfo a1, FLA_Cache_FundInfo a2) {
                      String a1QN = (a1.mType + a1.mName).toLowerCase();
                      String a2QN = (a2.mType + a2.mName).toLowerCase();
                      return a1QN.compareTo(a2QN);
                  }
              };
	
	public Long mId;

    public Long mFundInfoId; // Required: Non-null
	public String mURL;  // Required: Non-null and length > 0
	public String mType; // Required: Must be one of TYPE_...
	public String mName; // Required: Non-null and length > 0
	public String mDateYYMMDD_Updated; // Required (last time we actually updated)
	public String mDateYYMMDD_Update_Attempted; // Required (last time we check Internet with intent to update)
	public long mMSRating;  // -1 if none
	public long mPPMNumber; // -1 if none
	public Ref<FLA_FundCategory> mCategory; // Optional, can be null
	public Ref<FLA_FundIndex> mIndexCompare; // Optional, can be null
	public Ref<FLA_Currency> mCurrency; // Required: Non-null (always the original)
	public boolean mIsValid = true; // True if latest extraction was in error
    public int mInvalidCode = 0;
    public String mDPWs; // Sorted in descending order (newest == first)

    public String toString() {
        return mType + "." + mName;
    }

    public String getTypeAndName() {
        return mType + "." + mName;
    }

    /**
     *
     */
    public FLA_Cache_FundInfo createCopy() {
        FLA_Cache_FundInfo cfi = new FLA_Cache_FundInfo();
        cfi.mId = mId;
        cfi.mFundInfoId = mFundInfoId;
        cfi.mURL = mURL;
        cfi.mType = mType;
        cfi.mName = mName;
        cfi.mDateYYMMDD_Updated = mDateYYMMDD_Updated;
        cfi.mDateYYMMDD_Update_Attempted = mDateYYMMDD_Update_Attempted;
        cfi.mMSRating = mMSRating;
        cfi.mPPMNumber = mPPMNumber;
        cfi.mCategory = mCategory;
        cfi.mIndexCompare = mIndexCompare;
        cfi.mCurrency = mCurrency;
        cfi.mIsValid = mIsValid;
        cfi.mInvalidCode = mInvalidCode;
        cfi.mDPWs = mDPWs;
        return cfi;
    }


    /**
     *
     */
	public void dumpInfo(IndentWriter iw) {
		iw.println("FLA_FundInfoTinyEmbeddable: " + mType + "." + mName + ", id: " + mId + ", upd: " + mDateYYMMDD_Updated + ", atm: " + mDateYYMMDD_Update_Attempted);
        iw.push();
		iw.println("IsValid: " + mIsValid);
		iw.println("URL: " + mURL);
		iw.println("Rating: " + mMSRating);
		iw.println("PPM: " + mPPMNumber);
		iw.println("Category:" + mCategory.get().mKey_CategoryName);
		iw.println("Index: " + mIndexCompare.get().mKey_IndexName);
		iw.println("Currency: " + mCurrency.get().mKey_CurrencyName);
        iw.println("IsValid: " + mIsValid);
        iw.println("InvalidCode: " + mInvalidCode);
		iw.println();
		iw.pop();
	}

    /**
     *
     */
    public List<FLA_Cache_FundDPWeek> getDPWeeks() throws Exception {
        EE ee = EE.getEE();

        List<FLA_Cache_FundDPWeek> r = new ArrayList<>();
        if (mDPWs.trim().length() == 0) {
            return r;
        }
        String[] splits = mDPWs.trim().split(",");
        if (splits == null || splits.length == 0) {
            // ee.dinfo(log, TAG, "String splits was null or had length 0");
            return r;
        }
        int index = 0;
        while (index < splits.length) {
            String date = splits[index++];
            String r1w = null;
            if (index < splits.length) {
                r1w = splits[index++];
            }
            FLA_Cache_FundDPWeek dpw = FLA_Cache_FundDPWeek.instantiate(mType + "." + mName, date, r1w);
            if (dpw == null)  {
                ee.dsevere(log, TAG, "DPW at index: " + index + " was null from orig: " + mDPWs);
            } else {
                // ee.dinfo(log, TAG, "DPW is: " + dpw);
            }
            r.add(dpw);
        }
        return r;
    }

	/**
	 * 
	 */
	public static FLA_Cache_FundInfo instantiate(FLA_FundInfo fi) throws Exception {
        EE ee = EE.getEE();

        String s = fi.mName.toLowerCase();

		FLA_Cache_FundInfo r = new FLA_Cache_FundInfo();
        r.mFundInfoId = fi.mId;
        r.mType = fi.mType;
        r.mId = fi.mId;
		r.mName = fi.mName;
		r.mDateYYMMDD_Updated = fi.mDateYYMMDD_Updated;
		r.mDateYYMMDD_Update_Attempted = fi.mDateYYMMDD_Update_Attempted;
		r.mMSRating = fi.mMSRating;
		r.mPPMNumber = fi.mPPMNumber;
		r.mURL = fi.mURL;
        r.mCategory = fi.mCategory;
        r.mIndexCompare = fi.mIndexCompare;
        r.mCurrency = fi.mCurrency;
        r.mIsValid = fi.mIsValid;
        r.mInvalidCode = fi.mInvalidCode;
        r.mDPWs = "";

        StringBuffer tmpSB = new StringBuffer();
        List<FLA_FundDPDay> l = new ArrayList<>();
        if (fi.mDPDays.size() > 0) {
            for (int i=fi.mDPDays.size()-1; i >= 0; i--) {
                l.add(fi.mDPDays.get(i));
            }
            StringBuilder strb = new StringBuilder();
            int count = l.size() > FLA_Cache_FundInfo.CACHED_DPS ? FLA_Cache_FundInfo.CACHED_DPS: l.size();
            for (int i=0; i < count; i++) {
                FLA_FundDPDay fdpday = l.get(i);
                FLA_Cache_FundDPWeek cfdw = FLA_Cache_FundDPWeek.instantiate(fdpday);
                tmpSB.append("...[" + i + "]: Now adding day: " + fdpday.toStringOneLine(false) + "\n");

                if (i > 0) {
                    strb.append(",");
                }
                if (cfdw != null) {
                    strb.append(cfdw.mDateYYMMDD + ",");
                    if (cfdw.mR1w != null) {
                        strb.append(cfdw.mR1w);
                    }
                }
            }
            r.mDPWs = strb.toString();
        }
		return r;
	}
}



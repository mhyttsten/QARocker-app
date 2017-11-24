package com.pf.fl.be.extract;

import com.pf.fl.be.datamodel.FLA_Cache;
import com.pf.fl.be.datamodel.FLA_FundDPDay;
import com.pf.fl.be.datamodel.FLA_FundInfo;
import com.pf.fl.be.datamodel_raw.FL_MSExtractDetails;
import com.pf.fl.be.datamodel_raw.FL_MSExtractDetailsVanguard;
import com.pf.fl.be.datamodel_raw.REFundInfo;
import com.pf.fl.be.datastore.DS;
import com.pf.fl.be.util.Constants;
import com.pf.fl.be.util.EE;
import com.pf.shared.IndentWriter;
import com.pf.shared.MM;
import com.pf.shared.OTuple2G;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class FLOps1_Ext1_Extract_SingleFund {
    private static final Logger log = Logger.getLogger(FLOps1_Ext1_Extract_SingleFund.class.getName());
    private static final String TAG = MM.getClassName(FLOps1_Ext1_Extract_SingleFund.class.getName());

    private EE mEE;

    public boolean mError;
    public IndentWriter mIWE = new IndentWriter();
    public int mStatusCode = SC_UPDATED_SUCCESS;
    public static final int SC_UPDATED_SUCCESS = 0;
    public static final int SC_BECAME_INVALID = 1;
    public static final int SC_NOT_UPDATED = 2;

    public String mDateNow_YYMMDD;
    public String mDateLastFriday_YYMMDD;

    private void persistFundInfo(
            boolean persist,
            FLA_FundInfo fi,
            boolean isError,
            String errorString,
            int scCode,
            boolean isValid,
            int validityError) throws Exception {
        if (isError) {
            mError = true;
            mIWE.println(errorString);
            mStatusCode = scCode;
        }
        if (!isValid) {
            fi.mIsValid = false;
            fi.mInvalidCode = validityError;
            fi.mInvalidString = errorString;
        }

        if (persist) {
            DS.saveFundInfoAndUpdateFundIndex(null, fi);
        }
    }

    /**
     */
    public void extractFund(
            FLA_FundInfo fiExisting,
            boolean persist) throws Exception {
        try {
            extractFundImpl(fiExisting, persist);
        } catch(Throwable exc) {
            StringBuffer strb = new StringBuffer();
            strb.append("\n*** EXCEPTION CAUGHT WHEN extractFund\n");
            strb.append("...exception: " + exc.getMessage() + "\n");
            strb.append(MM.getStackTraceString(exc) + "\n");
            strb.append("...fund: " + fiExisting.getTypeAndName());
            strb.append("...dpWeek count: " + fiExisting.mDPDays.size());
            strb.append("...detailed info:\n" + mIWE.getString());
            mEE.dsevere(log, TAG, strb.toString());
            mError = true;
            mStatusCode = SC_NOT_UPDATED;
        }
    }
    public void extractFundImpl(
            FLA_FundInfo fiExisting,
            boolean persist) throws Throwable {
        mEE = EE.getEE();
        IndentWriter iw = new IndentWriter();
        ofy().clear();

        mDateNow_YYMMDD = MM.getNowAs_YYMMDD(EE.TIMEZONE_STOCKHOLM);
        mDateLastFriday_YYMMDD = MM.tgif_getLastFridayTodayExcl(mDateNow_YYMMDD);

        fiExisting.mDateYYMMDD_Update_Attempted = mDateNow_YYMMDD;

        StringBuffer strb = new StringBuffer();
        strb.append("extractFund:\n" + fiExisting.getTypeAndName()
                + "\n..." + fiExisting.mURL
                + "\n...dpWeek count: " + fiExisting.mDPDays.size() + "\n");
        if (fiExisting.mDPDays.size() > 0) {
            strb.append("...first DPWeek[0]: " + fiExisting.mDPDays.get(0).mDateYYMMDD + "\n");
            strb.append("...last  DPWeek[" + (fiExisting.mDPDays.size()-1) + "]: " + fiExisting.mDPDays.get(fiExisting.mDPDays.size()-1).mDateYYMMDD + "\n");
        }
        mEE.dinfo(log, TAG, strb.toString());
        mIWE.println("url: " + fiExisting.mURL);

        // Get the raw HTML data
        fiExisting.dumpInfo(mIWE);
        iw = new IndentWriter();
        byte[] htmlDataRaw = FLOps1_Ext1_HTMLGet.htmlGet(
                mEE,
                iw,
                fiExisting.mURL,
                5000,
                6);
        if (htmlDataRaw == null || htmlDataRaw.length == 0) {
            String error = "Did not get any data for URL\n" + iw.getString();
            persistFundInfo(
                    persist,
                    fiExisting,
                    true,
                    error,
                    SC_BECAME_INVALID,
                    false,
                    FLA_FundInfo.IC_COM_NO_SUCH_URL);
            return;
        }
        String htmlDataString = MM.newString(htmlDataRaw, EE.ENCODING_FILE_READ);

        // Take HTML content and convert to REFund
        iw = new IndentWriter();
        fiExisting.dumpInfo(iw);
        OTuple2G<Integer, REFundInfo> reFI = null;
        if (fiExisting.mType.equals(Constants.ACCOUNT_TYPE_VANGUARD)) {
            reFI = FL_MSExtractDetailsVanguard.extractFundDetails(
                    fiExisting.mName,
                    fiExisting.mType,
                    fiExisting.mURL,
                    htmlDataString,
                    iw);
        } else {
            reFI = FL_MSExtractDetails.extractFundDetails(
                    fiExisting.mType,
                    fiExisting.mURL,
                    htmlDataString,
                    iw);
        }
        if (reFI._o2 == null) {
            String error = "Raw HTML data was not decodable\n" + iw.getString();
            persistFundInfo(
                    persist,
                    fiExisting,
                    true,
                    error,
                    SC_BECAME_INVALID,
                    false,
                    reFI._o1);
            return;
        }

        // Take REFund and convert to FLA_FundInfo
        iw = new IndentWriter();
        fiExisting.dumpInfo(iw);
        OTuple2G<Boolean, FLA_FundInfo> rFITuple = CTRL_RE2FLAConverter.convertTo_FLAImpl(iw, reFI._o2);
        if (rFITuple == null || !rFITuple._o1) {
            String error = "REFundInfo could not be converted to FLA_FundInfo\n" + iw.getString();
            persistFundInfo(
                    persist,
                    fiExisting,
                    true,
                    error,
                    SC_BECAME_INVALID,
                    false,
                    FLA_FundInfo.IC_DATA_CONVERSION_ERROR);
            return;
        }
        FLA_FundInfo fiExtracted = rFITuple._o2;

        // AssertEquals: mURL, mType
        // NotExists: mId in fiExtracted

        // Extracted looks ok, clear any previous invalidity
        fiExisting.mIsValid = true;
        fiExisting.mInvalidCode = FLA_FundInfo.IC_NONE;

        // Copy newer from extrated
        fiExisting.mMSRating = fiExtracted.mMSRating;
        fiExisting.mPPMNumber = fiExtracted.mPPMNumber;
        fiExisting.mCategory = fiExtracted.mCategory;
        fiExisting.mIndexCompare = fiExtracted.mIndexCompare;

        // Even if currency changed, we rely on fund to retain its performance characteristics
        // So we still use old DPDay data (which could have been based on previous currency)
        fiExisting.mCurrency = fiExtracted.mCurrency;

        // Adjust with the new DP data
        if (fiExtracted.mDPDays.size() != 1) {
            iw = new IndentWriter();
            fiExtracted.dumpInfo(iw);
            mIWE.println("DPday count for extracted HTML Fund != 1\n" + iw.getString());
            mError = true;
            mStatusCode = SC_NOT_UPDATED;
            return;
        }
        FLA_FundDPDay dpDayExtracted = fiExtracted.mDPDays.get(0);
        if (dpDayExtracted != null && dpDayExtracted.mR1w != null) {
            dpDayExtracted.mDateYYMMDD = mDateLastFriday_YYMMDD;
            dpDayExtracted.mDateYYMMDD_Orig = mDateNow_YYMMDD;
            insertOrReplaceExistingDPDay(fiExisting.mDPDays, dpDayExtracted);
            Collections.sort(fiExisting.mDPDays, FLA_FundDPDay.COMPARATOR_DATE_ADJUSTED);
            fiExisting.mDateYYMMDD_Updated = mDateNow_YYMMDD;
        } else {
            iw = new IndentWriter();
            fiExtracted.dumpInfo(iw);
            String lastUpdatedExisting = getLastDateUpdated(fiExisting);
            String s = "We did not get a R1W for the DPDAy"
                    + "\nThe last updated date for the stored FundInfo was: " + lastUpdatedExisting
                    + "\nThe data we got from the just extracted was:\n" + iw.getString();
            mEE.dinfo(log, TAG, s);
            mIWE.println(s);
        }

        // Names differ, that can be a problem in case URL was reused
        // Set to invalid for investigation
        if (!fiExisting.mName.equals(fiExtracted.mName)) {
            String oldName = fiExisting.mName;
            fiExisting.mName = fiExtracted.mName;
            String error = "...Name differed and was updated to extracted" +
                    "\nOld: " + oldName + "\nExtracted: " + fiExtracted.mName;
            mEE.dwarning(log, TAG, error);
        }

        if (persist) {
            DS.saveFundInfoAndUpdateFundIndex(null, fiExisting);

            ofy().clear();
            FLA_FundInfo fi = DS.getFundInfoById(fiExisting.mId);
            FLA_Cache.updateCacheUpdate(fi);
        }
    }

    /**
     */
    private String getLastDateUpdated(FLA_FundInfo fi) {
        String lastUpdated = null;
        List<FLA_FundDPDay> dpds = fi.mDPDays;
        for (FLA_FundDPDay dpd : dpds) {
            if (lastUpdated == null) {
                lastUpdated = dpd.mDateYYMMDD;
            } else if (lastUpdated.compareTo(dpd.mDateYYMMDD) < 0) {
                lastUpdated = dpd.mDateYYMMDD;
            }
        }
        return lastUpdated;
    }

    /**
     */
    private void insertOrReplaceExistingDPDay(List<FLA_FundDPDay> l, FLA_FundDPDay fExtracted)
            throws Exception {

        HashMap<String, Void> dups = new HashMap<>();
        ArrayList<FLA_FundDPDay> newList = new ArrayList<>();

        for (FLA_FundDPDay dpd: l) {
            if (!dups.containsKey(dpd.mDateYYMMDD)) {
                dups.put(dpd.mDateYYMMDD, null);
                newList.add(dpd);
            }
        }

        if (!dups.containsKey(fExtracted.mDateYYMMDD)) {
            newList.add(fExtracted);
        }

        l.clear();
        l.addAll(newList);
    }
}

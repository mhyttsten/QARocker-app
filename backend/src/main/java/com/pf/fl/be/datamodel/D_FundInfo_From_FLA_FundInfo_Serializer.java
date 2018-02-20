package com.pf.fl.be.datamodel;

import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;

import java.util.Collections;
import java.util.logging.Logger;

public class D_FundInfo_From_FLA_FundInfo_Serializer {
    private static final Logger log = Logger.getLogger(D_FundInfo_From_FLA_FundInfo_Serializer.class.getName());

    public static D_FundInfo convertTo_D_FundInfo(FLA_FundInfo fiOld) {
        D_FundInfo fi = new D_FundInfo();

        fi._url = fiOld.mURL;
        fi._isValid = fiOld.mIsValid;
        fi._errorCode = D_FundInfo.IC_NO_ERROR;
        fi._type = fiOld.mType;
        fi._nameMS = fiOld.mName;
        fi._nameOrig = "";
        fi._dateYYMMDD_Updated = fiOld.mDateYYMMDD_Updated;
        fi._dateYYMMDD_Update_Attempted = fiOld.mDateYYMMDD_Update_Attempted;
        fi._msRating = (int)fiOld.mMSRating;
        fi._ppmNumber = String.valueOf(fiOld.mPPMNumber);

        if (fiOld.mCategory != null && fiOld.mCategory.getKey() != null) {
            fi._categoryName = fiOld.mCategory.getKey().getName();
        }
        if (fiOld.mIndexCompare != null && fiOld.mIndexCompare.getKey() != null) {
            fi._indexName = fiOld.mIndexCompare.getKey().getName();
        }
        if (fiOld.mCurrency != null && fiOld.mCurrency.getKey() != null) {
            fi._currencyName = fiOld.mCurrency.getKey().getName();
        }

        Collections.sort(fiOld.mDPDays, FLA_FundDPDay.COMPARATOR_DATE_ADJUSTED_REVERSE);
        for (FLA_FundDPDay fdpd: fiOld.mDPDays) {
            D_FundDPDay dpd = new D_FundDPDay();
            dpd._dateYYMMDD = fdpd.mDateYYMMDD;
            dpd._dateYYMMDD_Actual = fdpd.mDateYYMMDD_Orig;
            dpd._r1d = D2f(fdpd.mR1d);
            dpd._r1w = D2f(fdpd.mR1w);
            dpd._r1m = D2f(fdpd.mR1m);
            dpd._r3m = D2f(fdpd.mR3m);
            dpd._r6m = D2f(fdpd.mR6m);
            dpd._r1y = D2f(fdpd.mR1y);
            dpd._r3y = D2f(fdpd.mR3y);
            dpd._r5y = D2f(fdpd.mR5y);
            dpd._r10y = D2f(fdpd.mR10y);
            fi._dpDays.add(dpd);
        }
        return fi;
    }

    private static float D2f(Double d) {
        if (d == null) {
            return D_FundDPDay.FLOAT_NULL;
        }
        return (float)d.doubleValue();
    }
}

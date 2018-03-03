package com.pf.shared.extract;

import com.pf.shared.Constants;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;

import java.util.logging.Logger;

public class ExtractFromHTML_Vanguard {
	private static final Logger log = Logger.getLogger(ExtractFromHTML_Vanguard.class.getName());
	private static final String TAG = "FL_MSExtractDetails";

	private static final String URL_MUTUAL_FUND = "https://performance.morningstar.com/perform/Performance/fund/trailing-total-returns.action";
    private static final String URL_MUTUAL_FUND2 = "http://performance.morningstar.com/perform/Performance/fund/trailing-total-returns.action";
	private static final String URL_ETF = "https://performance.morningstar.com/perform/Performance/etf/trailing-total-returns.action";
    private static final String URL_ETF2 = "http://performance.morningstar.com/perform/Performance/etf/trailing-total-returns.action";

    public static int extractFundDetails(
            IndentWriter iw,
            D_FundInfo fi,
            String pageContent) throws Exception {

        boolean isMutualFund = false;
        boolean isETF = false;
        String searchFund = null;
        String searchDateString = null;
        if (fi._url.startsWith(URL_MUTUAL_FUND) || fi._url.startsWith(URL_MUTUAL_FUND2)) {
            isMutualFund = true;
            searchFund = getVanguardTickerFromFundName(fi._nameMS);
            searchDateString = searchFund + "&nbsp;return as of &nbsp;";
        } else if (fi._url.startsWith(URL_ETF) || fi._url.startsWith(URL_ETF2)) {
            isETF = true;
            searchFund = getVanguardTickerFromFundName(fi._nameMS) + " (Price)";
            searchDateString = searchFund + " return as of ";
        } else {
            fi._errorCode = D_FundInfo.IC_VG_NOT_MF_NOR_ETF;
            iw.println("Vanguard fund was neither Mutual or ETF");
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        }

        D_FundDPDay dpd = new D_FundDPDay();

        // First, make sure we have a good date
        int io = pageContent.indexOf(searchDateString);
        if (io == -1) {
            fi._errorCode = D_FundInfo.IC_HTML_VG_DATE_NOT_FOUND;
            iw.println("Could not find date for Vanguard MF: " + isMutualFund + ", or ETF: " + isETF);
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            int index = io + searchDateString.length();
            String dateMMSDDSYYYY = pageContent.substring(index, index + 10);
            String[] dateSplit = dateMMSDDSYYYY.split("/");
            String dateYYMMDD = dateSplit[2].substring(2) + dateSplit[0] + dateSplit[1];
            try {
                java.util.Date d = MM.getDateFrom_YYMMDD(null, dateYYMMDD);
            } catch (Exception exc) {
                fi._errorCode = D_FundInfo.IC_HTML_VG_DATE_NOT_FOUND;
                iw.println("Date conversion threw exception: " + dateYYMMDD + " for Vanguard " + isMutualFund + ", or ETF: " + isETF);
                return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
            }
            dpd._dateYYMMDD_Actual = dateYYMMDD;
        }

        fi._currencyName = "USD";

        OTuple2G<String, String> ot = null;
        int io1 = -1;
        String findTagLoc = null;

        ot = new OTuple2G<String, String>(null, pageContent);

        // Get returns
        findTagLoc = "Total Return";
        io1 = ot._o2.indexOf(findTagLoc);
        if (io1 == -1) {
            fi._errorCode = D_FundInfo.IC_HTML_VG_TOTAL_RETURNS_NOT_FOUND;
            iw.println("Could not find Total Returns for Vanguard fund");
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        }
        ot._o2 = ot._o2.substring(io1 + findTagLoc.length());

        int rc = ExtractFromHTML_Helper.RC_SUCCESS;

        // DPDay for our fund
        String nextTR = MM.assignAndReturnNextTagValue(ot, "<tr");
        rc = parseTR(iw, fi, nextTR);
        if (rc != ExtractFromHTML_Helper.RC_SUCCESS) {
            return rc;
        }
        if (!tr_ticker.equals(searchFund)) {
            iw.println("Vanguard ticker mismatch"
                    + "\nTicker from HTML: " + tr_ticker
                    + "\nTicker from name: " + searchFund
                    + "\nFund name: " + fi._nameMS);
            fi._errorCode = D_FundInfo.IC_HTML_VG_DATA_TICKER_MISMATCH;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        }
        dpd._r1d = tr_dps[0];
        dpd._r1w = tr_dps[1];
        dpd._r1m = tr_dps[2];
        dpd._r3m = tr_dps[3];
        dpd._r1y = tr_dps[4];
        dpd._r3y = tr_dps[5];
        dpd._r5y = tr_dps[6];
        dpd._r10y = tr_dps[7];

        // And we expect 5 more rows, one of them which is the index!
        String indexName = "";
        boolean error = false;
        for (int i=0; i <= 4; i++) {
            error = false;
            nextTR = MM.assignAndReturnNextTagValue(ot, "<tr");
            if (nextTR == null) {
                error = true;
            } else {
                rc = parseTR(iw, fi, nextTR);
            }
            if (rc != ExtractFromHTML_Helper.RC_SUCCESS || error) {
                iw.println("Vanguard Error Parsing DP Rows Table");
                fi._errorCode = D_FundInfo.IC_HTML_VG_DP_ROWS_INCONSISTENCY;
                if (!error) return rc;
                else return rc;
            }

            if (isETF && i == 1) {
                indexName = tr_ticker;
                if (!indexName.endsWith(" (Price)")) {
                    iw.println("Vanguard Index name for ETF did not end with (Price)");
                    fi._errorCode = D_FundInfo.IC_HTML_VG_DP_ROWS_INCONSISTENCY;
                    return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
                }
                indexName = indexName.substring(0, indexName.length()-8);
            } else if(isMutualFund && i == 0) {
                indexName = tr_ticker;
            }
        }

        fi._indexName = indexName;
        fi._dpDays.add(0, dpd);
        return ExtractFromHTML_Helper.RC_SUCCESS;
    }

    private static String tr_ticker;
    private static float[] tr_dps;
    private static int parseTR(IndentWriter iw, D_FundInfo fi, String nextTR) {
        tr_ticker = null;
        tr_dps = new float[8];

		OTuple2G<String, String> ot = new OTuple2G<String, String>(null, nextTR);
		tr_ticker = MM.assignAndReturnNextTagValue(ot, "<th");

		OTuple2G<Boolean, Float> rv = null;
    	rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
		if (!rv._o1) {
            iw.println("Vanguard, r1d ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
		    float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
		    tr_dps[0] = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard, r1w ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[1] = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard, r1m ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[2] = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard, r3m ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[3] = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard, YTD ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard, r1y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[4] = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard, r3y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[5] = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard, r5y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[6] = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard, r10y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            tr_dps[7] = r;
        }
        return ExtractFromHTML_Helper.RC_SUCCESS;

	}

    public static final String getVanguardTickerFromFundName(String name) {
        int io1 = name.lastIndexOf("(");
        int io2 = name.lastIndexOf(")");
        if (io1 == -1 || io2 == -1) {
            return null;
        }
        return name.substring(io1+1, io2);
    }
}

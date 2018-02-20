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
        if (fi._url.startsWith(URL_MUTUAL_FUND) || fi._url.startsWith(URL_MUTUAL_FUND2)) {
            isMutualFund = true;
        } else if (fi._url.startsWith(URL_ETF) || fi._url.startsWith(URL_ETF2)) {
            isETF = true;
        } else {
            fi._errorCode = D_FundInfo.IC_VG_NOT_MF_NOR_ETF;
            iw.println("Vanguard fund was neither Mutual or ETF");
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        }

		fi._currencyName = "USD";

		OTuple2G<String, String> ot = null;
		int io1 = -1;
		String findTagLoc = null;

		ot = new OTuple2G<String, String>(null, pageContent);

		// Getting Date
		// <th scope="row" class="col_head_lbl">Total Return %<span>&nbsp;(
		findTagLoc = "Total Return";
		io1 = ot._o2.indexOf(findTagLoc);
		if (io1 == -1) {
            fi._errorCode = D_FundInfo.IC_HTML_VG_MF_TOTAL_RETURNS_NOT_FOUND;
            iw.println("Could not find Total Returns for Vanguard fund");
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
		}

        D_FundDPDay dpd = new D_FundDPDay();

		ot._o2 = ot._o2.substring(io1+findTagLoc.length());

		String dateMMDDYY = null;
		if (isMutualFund) {
            io1 = ot._o2.indexOf(")");
            dateMMDDYY = ot._o2.substring(0, io1);
            ot._o2 = ot._o2.substring(io1 + 1);
        }
        if (isETF) {
            String nameTicker = getVanguardTickerFromFundName(fi._nameMS);
            nameTicker += " (Price)";
            findTagLoc = nameTicker + " return as of ";
            io1 = ot._o2.indexOf(findTagLoc);
            if (io1 == -1) {
                fi._errorCode = D_FundInfo.IC_HTML_VG_ETF_DATE_NOT_FOUND;
                iw.println("Could not find date for Vanguard ETF fund");
                return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
            }
            dateMMDDYY = ot._o2.substring(io1 + findTagLoc.length());
            io1 = dateMMDDYY.indexOf(".");
            if (io1 == -1) {
                fi._errorCode = D_FundInfo.IC_HTML_VG_ETF_DATE_NOT_FOUND;
                iw.println("Could not find date for Vanguard ETF fund");
                return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
            }
            dateMMDDYY = dateMMDDYY.substring(0, io1);
            if (dateMMDDYY.length() != 10) {
                fi._errorCode = D_FundInfo.IC_HTML_VG_ETF_DATE_NOT_FOUND;
                iw.println("Could not find date for Vanguard ETF fund");
                return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
            }
        }

		String[] dateSplit = dateMMDDYY.split("/");
		String dateYYMMDD = dateSplit[2].substring(2) + dateSplit[0] + dateSplit[1];
		dpd._dateYYMMDD_Actual = dateYYMMDD;

        String dateNow_YYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
        String dateLastFriday_YYMMDD = MM.tgif_getLastFridayTodayExcl(dateNow_YYMMDD);
        dpd._dateYYMMDD = dateLastFriday_YYMMDD;

        // Moving to the data points
		String nextTR = MM.assignAndReturnNextTagValue(ot, "<tr");

		ot = new OTuple2G<String, String>(null, nextTR);
		String ticker = MM.assignAndReturnNextTagValue(ot, "<th");
		String nameTicker = getVanguardTickerFromFundName(fi._nameMS);
    		if (ticker == null || nameTicker == null || !ticker.equals(nameTicker)) {
			iw.println("Vanguard Mutual Fund, ticker mismatch"
					+ "\nTicker from HTML: " + ticker
					+ "\nTicker from name: " + nameTicker
					+ "\nFund name: " + fi._nameMS);
            fi._errorCode = D_FundInfo.IC_HTML_VG_MF_DATA_TICKER_MISMATCH;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
		}

		OTuple2G<Boolean, Float> rv = null;

		rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
		if (!rv._o1) {
            iw.println("Vanguard Mutual Fund, r1d ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
		    float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
		    dpd._r1d = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard Mutual Fund, r1w ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            dpd._r1w = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard Mutual Fund, r1m ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            dpd._r1m = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard Mutual Fund, r3m ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            dpd._r3m = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard Mutual Fund, YTD ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard Mutual Fund, r1y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            dpd._r1y = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard Mutual Fund, r3y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            dpd._r3y = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard Mutual Fund, r5y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            dpd._r5y = r;
        }

        rv = ExtractFromHTML_Helper.validFloat(MM.assignAndReturnNextTagValue(ot, "<td"));
        if (!rv._o1) {
            iw.println("Vanguard Mutual Fund, r10y ExtractFromHTML_Helper.error");
            fi._errorCode = D_FundInfo.IC_HTML_VG_DP_PARSING;
            return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
        } else {
            float r = rv._o2 == null ? D_FundDPDay.FLOAT_NULL : rv._o2.floatValue();
            dpd._r10y = r;
        }

        fi._dpDays.add(0, dpd);
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

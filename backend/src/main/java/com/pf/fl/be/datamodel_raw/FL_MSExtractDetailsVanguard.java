package com.pf.fl.be.datamodel_raw;

import com.pf.fl.be.datamodel.FLA_FundInfo;
import com.pf.fl.be.util.Constants;
import com.pf.shared.IndentWriter;
import com.pf.shared.MM;
import com.pf.shared.OTuple2G;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class FL_MSExtractDetailsVanguard {
	private static final Logger log = Logger.getLogger(FL_MSExtractDetailsVanguard.class.getName());
	private static final String TAG = "FL_MSExtractDetails";

	private static final String URL_MUTUAL_FUND = "https://performance.morningstar.com/perform/Performance/fund/trailing-total-returns.action";
	private static final String URL_ETF = "https://performance.morningstar.com/perform/Performance/etf/trailing-total-returns.action";

	//------------------------------------------------------------------------
	public static OTuple2G<Integer, REFundInfo> extractFundDetails(
			String name,
			String accountType,
			String url,
			String pageContent,
			IndentWriter iw) throws Exception {
		log.info("Vanguard Extracting Begin");
		OTuple2G<Integer, REFundInfo> result = new OTuple2G<>();
        iw.println("FL_MSExtractDetailsVanguard.extractFundDetails");
        iw.println("-accountType: " + accountType);
        iw.println("-url: " + url);
        if (pageContent != null) {
            iw.println("-pageContent: " + pageContent.length());
            if (pageContent.length() == 0) {
                iw.push();
                iw.println("pageContent had length 0, returning null");
                iw.pop();
				result._o1 = FLA_FundInfo.IC_DATA_HAD_LENGTH_0;
				return result;
            }
        } else {
            iw.println("-pageContent: null");
            iw.push();
            iw.println("pageContent was null, returning null");
            iw.pop();
			result._o1 = FLA_FundInfo.IC_DATA_WAS_NULL;
            return result;
        }

        iw.push();
		if (url.startsWith(URL_MUTUAL_FUND)) {
			result = extractFundDetailsImpl_MutualFund(
					name,
					accountType,
					url,
					pageContent,
					iw);
		} else if(url.startsWith(URL_ETF)) {
			result = extractFundDetailsImpl_ETF(
					name,
					accountType,
					url,
					pageContent,
					iw);
		}
        iw.pop();
        if (result._o2 == null) {
            iw.println("REFundInfo was null, returning null");
        } else {
            iw.println("REFundInfo: " + result._o2.getStats());
        }
        return result;
    }

    /**
     *
     */
    private static OTuple2G<Integer, REFundInfo>  extractFundDetailsImpl_MutualFund(
			String name,
            String accountType,
            String url,
            String pageContent,
            IndentWriter iw) throws Exception {
		OTuple2G<Integer, REFundInfo> bigReturnValue = new OTuple2G<>();

		REFundInfo returnValue = new REFundInfo();
		returnValue.setName(name);
		log.info("*** Name is: " + name);
		REFundInfo_DPDay dpd = new REFundInfo_DPDay();
		returnValue.setAccountType(accountType);
		returnValue.setURL(url);
		returnValue.setCurrency("USD");

		OTuple2G<String, String> ot = null;
		int io1 = -1;
		String findTagLoc = null;

		ot = new OTuple2G<String, String>(null, pageContent);

		// Getting Date
		// <th scope="row" class="col_head_lbl">Total Return %<span>&nbsp;(
		findTagLoc = "<th scope=\"row\" class=\"col_head_lbl\">Total Return %<span>&nbsp;(";
		io1 = ot._o2.indexOf(findTagLoc);
		if (io1 == -1) {
			iw.println("Could not find Total Returns secction");
			bigReturnValue._o1 = FLA_FundInfo.IC_DATA_TOTAL_RETURNS_NOT_FOUND;
			return bigReturnValue;
		}
		ot._o2 = ot._o2.substring(io1+findTagLoc.length());
		log.info("1-here: " + MM.getString(ot._o2, 100));
		io1 = ot._o2.indexOf(")");
		String dateMMDDYY = ot._o2.substring(0, io1);
		ot._o2 = ot._o2.substring(io1+1);
		log.info("...date before split: " + dateMMDDYY);
		String[] dateSplit = dateMMDDYY.split("/");
		String dateYYMMDD = dateSplit[2].substring(2) + dateSplit[0] + dateSplit[1];
		dpd.setDataPointDate(dateYYMMDD);
		log.info("...date: " + dateYYMMDD);

		// Moving to the data points
		log.info("Before moving to next <tr\n" + MM.getString(ot._o2, 100));
		String nextTR = MM.assignAndReturnNextTagValue(ot, "<tr");
		log.info("Next tr:\n" + nextTR);

		ot = new OTuple2G<String, String>(null, nextTR);
		String ticker = MM.assignAndReturnNextTagValue(ot, "<th");
		log.info("...ticker: " + ticker);
		String nameTicker = Constants.getVanguardTickerFromFundName(name);
		if (ticker == null || nameTicker == null || !ticker.equals(nameTicker)) {
			iw.println("*** Error"
					+ "\nTicker from HTML: " + ticker
					+ "\nTicker from name: " + nameTicker
					+ "\nFund name: " + name);
			bigReturnValue._o1 = FLA_FundInfo.IC_DATA_TICKER_MISMATCH;
			return bigReturnValue;
		}

		String r1D = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
		dpd.setR1D(r1D);
		log.info("...r1D: " + r1D);
		String r1W = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
		dpd.setR1W(r1W);
		log.info("...r1W: " + r1W);
		String r1M = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
		dpd.setR1M(r1M);
		log.info("...r1M: " + r1M);
		String r3M = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
		dpd.setR3M(r3M);
		log.info("...r3M: " + r3M);
		String rYTD = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
		log.info("...rYTD: " + rYTD);
		String r1Y = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
		dpd.setR1Y(r1Y);
		log.info("...r1Y: " + r1Y);
		String r3Y = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
		dpd.setR3Y(r3Y);
		log.info("...r3Y: " + r3Y);
		String r5Y = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
		dpd.setR5Y(r5Y);
		log.info("...r5Y: " + r5Y);
		String r10Y = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
		dpd.setR10Y(r10Y);
		log.info("...r10Y: " + r10Y);
		String r15Y = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
		log.info("...r15Y: " + r15Y);

		ArrayList<REFundInfo_DPDay> dpds = new ArrayList<REFundInfo_DPDay>();
		dpds.add(dpd);
		returnValue.setDPDays(dpds);

		bigReturnValue._o1 = FLA_FundInfo.IC_NONE;
		bigReturnValue._o2 = returnValue;
		log.info("We are done here, returning successfully");
		return bigReturnValue;
	}

    /**
     *
     */
    private static OTuple2G<Integer, REFundInfo>  extractFundDetailsImpl_ETF(
            String name,
            String accountType,
            String url,
            String pageContent,
            IndentWriter iw) throws Exception {
        OTuple2G<Integer, REFundInfo> bigReturnValue = new OTuple2G<>();

        REFundInfo returnValue = new REFundInfo();
        returnValue.setName(name);
        log.info("*** Name is: " + name);
        REFundInfo_DPDay dpd = new REFundInfo_DPDay();
        returnValue.setAccountType(accountType);
        returnValue.setURL(url);
        returnValue.setCurrency("USD");

        OTuple2G<String, String> ot = null;
        int io1 = -1;
        String findTagLoc = null;

        ot = new OTuple2G<String, String>(null, pageContent);

        // Getting Date
        // <th scope="row" class="col_head_lbl">Total Return %<span>&nbsp;(
        findTagLoc = "<th scope=\"row\" class=\"col_head_lbl\">Total Return %<span>&nbsp;";
        io1 = ot._o2.indexOf(findTagLoc);
        if (io1 == -1) {
            iw.println("Could not find Total Returns secction");
            bigReturnValue._o1 = FLA_FundInfo.IC_DATA_TOTAL_RETURNS_NOT_FOUND;
            return bigReturnValue;
        }
        ot._o2 = ot._o2.substring(io1+findTagLoc.length());
        log.info("1-here: " + MM.getString(ot._o2, 100));

        // Get the date
        findTagLoc = "return as of ";
        io1 = ot._o2.indexOf(findTagLoc);
        if (io1 == -1) {
            iw.println("Could not find start of date section");
            bigReturnValue._o1 = FLA_FundInfo.IC_DATA_DATE_NOT_FOUND;
            return bigReturnValue;
        }
        String dateMMDDYY = ot._o2.substring(io1 + findTagLoc.length());
        io1 = dateMMDDYY.indexOf(".");
        if (io1 == -1) {
            iw.println("Could not find end of date section");
            bigReturnValue._o1 = FLA_FundInfo.IC_DATA_DATE_NOT_FOUND;
            return bigReturnValue;
        }
        dateMMDDYY = dateMMDDYY.substring(0, io1);
        if (dateMMDDYY.length() != 10) {
            iw.println("Date did not have expected length: " + dateMMDDYY);
            bigReturnValue._o1 = FLA_FundInfo.IC_DATA_DATE_NOT_FOUND;
            return bigReturnValue;
        }
        log.info("...date before split: " + dateMMDDYY);
        String[] dateSplit = dateMMDDYY.split("/");
        String dateYYMMDD = dateSplit[2].substring(2) + dateSplit[0] + dateSplit[1];
        dpd.setDataPointDate(dateYYMMDD);
        log.info("...date: " + dateYYMMDD);

        // Moving to the data points
        log.info("Before moving to next <tr\n" + MM.getString(ot._o2, 100));
        String nextTR = MM.assignAndReturnNextTagValue(ot, "<tr");
        log.info("Next tr:\n" + nextTR);

        ot = new OTuple2G<String, String>(null, nextTR);
        String ticker = MM.assignAndReturnNextTagValue(ot, "<th");
        ticker = ticker.substring(0, ticker.indexOf(" "));
        log.info("...ticker: " + ticker);
        String nameTicker = Constants.getVanguardTickerFromFundName(name);
        if (ticker == null || nameTicker == null || !ticker.equals(nameTicker)) {
            iw.println("*** Error name mismatch between ticker from name and ticker extracted"
                    + "\nTicker from HTML: " + ticker
                    + "\nTicker from name: " + nameTicker
                    + "\nFund name: " + name);
            bigReturnValue._o1 = FLA_FundInfo.IC_DATA_TICKER_MISMATCH;
            return bigReturnValue;
        }

        String r1D = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
        dpd.setR1D(r1D);
        log.info("...r1D: " + r1D);
        String r1W = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
        dpd.setR1W(r1W);
        log.info("...r1W: " + r1W);
        String r1M = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
        dpd.setR1M(r1M);
        log.info("...r1M: " + r1M);
        String r3M = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
        dpd.setR3M(r3M);
        log.info("...r3M: " + r3M);
        String rYTD = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
        log.info("...rYTD: " + rYTD);
        String r1Y = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
        dpd.setR1Y(r1Y);
        log.info("...r1Y: " + r1Y);
        String r3Y = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
        dpd.setR3Y(r3Y);
        log.info("...r3Y: " + r3Y);
        String r5Y = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
        dpd.setR5Y(r5Y);
        log.info("...r5Y: " + r5Y);
        String r10Y = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
        dpd.setR10Y(r10Y);
        log.info("...r10Y: " + r10Y);
        String r15Y = validateDouble(MM.assignAndReturnNextTagValue(ot, "<td"));
        log.info("...r15Y: " + r15Y);

        ArrayList<REFundInfo_DPDay> dpds = new ArrayList<REFundInfo_DPDay>();
        dpds.add(dpd);
        returnValue.setDPDays(dpds);

        bigReturnValue._o1 = FLA_FundInfo.IC_NONE;
        bigReturnValue._o2 = returnValue;
        log.info("We are done here, returning successfully");
        return bigReturnValue;
    }

    private static String validateDouble(String s) {
        if (s == null || s.trim().length() == 0) {
            return "-";
        }
        try {
            double d = Double.parseDouble(s);
        } catch(Exception exc) {
            return "-";
        }
        return s.trim();
    }

	// ***********************************************************************
}

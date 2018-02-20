package com.pf.shared.extract;

import com.pf.shared.Constants;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundDPYear;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ExtractFromHTML_Morningstar {
	private static final Logger log = Logger.getLogger(ExtractFromHTML_Morningstar.class.getName());
	private static final String TAG = "FL_MSExtractDetails";

	//------------------------------------------------------------------------
	public static int extractFundDetails(
			IndentWriter iw,
			D_FundInfo fi,
			String pageContent) throws Exception {

		OTuple2G ot = null;
		String findTagLoc = null;
		String findAfter = null;
		String findTo = null;
		String result = null;
		int returnCode = ExtractFromHTML_Helper.RC_SUCCESS;
		
		ot = new OTuple2G<String, String>(null, pageContent);
		String fundName = MM.assignAndReturnNextTagValue(ot, "<h2");
		if(fundName == null || (fundName.startsWith("S") && fundName.endsWith("k Fonder"))) {
			iw.println("Invalid URL encountered, irrecoverable error");
			fi._errorCode = D_FundInfo.IC_INVALID_URL_IRRECOVERABLE;
			return ExtractFromHTML_Helper.RC_ERROR_REMOVE_FUND;
		}

		fundName = MM.htmlReplaceHTMLCodes(fundName);
		if (!fi._nameMS.equals(fundName)) {
			iw.println("Fund changed name. Before: " + fi._nameMS + ". Now: " + fundName);
			returnCode = ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED;
		}
		fi._nameMS = fundName;

		// MS Category
		findTagLoc = "<span class=\"quicktakecolContainer\" title=\"Morningstar Kategori";
		findAfter = ">";
		findTo = "</span>";
		String ahref = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		findTagLoc = "<a";
		findAfter = "href=\"";
		findTo = "\"";
		if (ahref == null) {
			iw.println("Fund changed name. Before: " + fi._nameMS + ". Now: " + fundName);
			fi._errorCode = D_FundInfo.IC_HTML_MS_CATEGORY;
			return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
		}
		String msCategoryHTML = MM.getRegExp(null, ahref, findTagLoc, findAfter, findTo, true);
		findTagLoc = ">";
		findAfter = "";
		findTo = "</a>";
		String msCategoryText = MM.getRegExp(null, ahref, findTagLoc, findAfter, findTo, true);
		msCategoryText = MM.htmlReplaceHTMLCodes(msCategoryText);
		if (!fi._categoryName.equals(msCategoryText)) {
			iw.println("Fund category change. Before: " + fi._categoryName + ". Now: " + msCategoryText);
			returnCode = ExtractFromHTML_Helper.RC_SUCCESS_BUT_DATA_WAS_UPDATED;
		}
		fi._nameMS = fundName;

		// MS Rating
		// <span class="quicktakecolContainer" title="Morningstar Rating"
		findTagLoc = "<span class=\"quicktakecolContainer\" title=\"Morningstar Rating";
		findAfter = "class=\"";
		findTo = "\"";
		String msRatingHTML = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		msRatingHTML = MM.htmlReplaceHTMLCodes(msRatingHTML);
		if (msRatingHTML != null && msRatingHTML.startsWith("section_separator")) {
			msRatingHTML = "-";
		}
		String msRating = msRatingHTML.trim();
		int rating = -1;
		if (msRating != null && msRating.length() > 0) {
			if (msRating.equals("stars1")) {
				rating = 1;
			} else if (msRating.equals("stars2")) {
				rating = 2;
			} else if (msRating.equals("stars3")) {
				rating = 3;
			} else if (msRating.equals("stars4")) {
				rating = 4;
			} else if (msRating.equals("stars5")) {
				rating = 5;
			} else if (msRating.equals("-")) {
				rating = -1;
			} else {
				iw.println("FundInfo, rating could not be parsed: " + msRating);
				fi._errorCode = D_FundInfo.IC_HTML_MS_RATING;
				return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
			}
		} else {
			iw.println("FundInfo, rating could not be parsed because it was null");
			fi._errorCode = D_FundInfo.IC_HTML_MS_RATING;
			return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
		}
		fi._msRating = rating;

		// PPM Number
		findTagLoc = "<span class=\"quicktakecolContainer\" title=\"PPM-nummer\""; 
		findAfter = ">";
		findTo = "</span>";
		String ppmNumber = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		ppmNumber = MM.htmlReplaceHTMLCodes(ppmNumber).trim();
        fi._ppmNumber = ppmNumber;

        D_FundDPDay dpd = new D_FundDPDay();
        fi._dpDays.add(0, dpd);

		// Yearly returns (table), I??r!!!
		findTagLoc = "<th class=\"decimal\">Fond</th><th class=\"decimal\">Kategori</th><th class=\"decimal\">Index</th>";
		findAfter = "</tr>";
		findTo = "</table>";
		String yearlyReturnTable = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		int dpy_rc = processYearlyReturnTable(iw, fi, yearlyReturnTable);
		if (dpy_rc != ExtractFromHTML_Helper.RC_SUCCESS) {
			return dpy_rc;
		}

		findTagLoc = "<b>Kursdatum";
		findAfter = "</b>:";
		findTo = "<br />";
		String yearlyReturnsLastUpdatedDate = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);

		// Comparison category
		findTagLoc = "<b>Kategorins j";
		findAfter = "</b>:";
		findTo = "<br />";
		String indexCompare = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		fi._indexName = indexCompare;

		// Get currency in which NAV is measured
		findTagLoc = "<td>Senaste NAV</td>";
		findAfter = "<td>";
		findTo = "</td>";
		result = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		result = getFromFirstLetter(result);
		fi._currencyName = result;

		// Return (daily, month, 3m, 6m, 1y)
		findTagLoc = "Avkastning %";
		findAfter = "<table class=\"alternatedtoplist\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">";
		findTo = "</table>";
		String recentReturnTable = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		int dpd_rv = processRecentReturnsTable(iw, fi, recentReturnTable);
		if (dpd_rv != ExtractFromHTML_Helper.RC_SUCCESS) {
			return dpd_rv;
		}

		// Returns: Date of last recorded data point
		findTagLoc = "Avkastning %";
		findAfter = "<table class=\"alternatedtoplist\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">";
		findTo = "</html>";
		result = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		findTagLoc = "<b>Kursdatum";
		findAfter = "</b>:";
		findTo = "</div>";
		String returnsLastDate = MM.getRegExp(null, result, findTagLoc, findAfter, findTo, true);
		returnsLastDate = returnsLastDate.trim();
		returnsLastDate = MM.dateConvert_YYYYDMMDDD_To_YYMMDD(returnsLastDate);

		String dateNow_YYMMDD = MM.getNowAs_YYMMDD(Constants.TIMEZONE_STOCKHOLM);
		String dateLastFriday_YYMMDD   = MM.tgif_getLastFridayTodayExcl(dateNow_YYMMDD);
		String dateLastSaturday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.SATURDAY);
		String dateLastSunday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.SUNDAY);
		String dateLastMonday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.MONDAY);
		String dateLastTuesday_YYMMDD   = MM.tgif_getNextWeekday(dateLastFriday_YYMMDD, Calendar.TUESDAY);
		String dateLastThursday_YYMMDD = MM.tgif_getPrevWeekday(dateLastFriday_YYMMDD, Calendar.THURSDAY);

		// We have a valid extraction if actualy date is last Friday or Thursday
		if (returnsLastDate.equals(dateLastFriday_YYMMDD)
				|| returnsLastDate.equals(dateLastThursday_YYMMDD)
				|| returnsLastDate.equals(dateLastSaturday_YYMMDD)
				|| returnsLastDate.equals(dateLastSunday_YYMMDD)
				|| returnsLastDate.equals(dateLastMonday_YYMMDD)
				|| returnsLastDate.equals(dateLastTuesday_YYMMDD)) {
			fi._dpDays.get(0)._dateYYMMDD_Actual = returnsLastDate;
			fi._dpDays.get(0)._dateYYMMDD = dateLastFriday_YYMMDD;
			return ExtractFromHTML_Helper.RC_SUCCESS;
		}
		// Otherwise, we should wait until next data point
		else {
			fi._dpDays.remove(0);
			iw.println("Want to extract for friday: " + dateLastFriday_YYMMDD + ", but DP is not in range: " + dateLastThursday_YYMMDD + "-" + dateLastTuesday_YYMMDD);
			return ExtractFromHTML_Helper.RC_WARNING_NO_DPDAY_FOUND;
		}
	}

	// ***********************************************************************

	//------------------------------------------------------------------------
	public static int processRecentReturnsTable(
			IndentWriter iw,
			D_FundInfo fi,
			String recentReturnTable) throws Exception {

		/*
		<tr>
		   <th>&nbsp;</th>
		   <th class="decimal">GBP</th>
		   <th class="decimal">SEK</th>
		</tr>
		<tr>
			<td title="1 dag">1 dag</td>
			<td title="-0,2" class="decimal">-0,2</td>
			<td title="-0,4" class="decimal">-0,4</td>
		</tr>
		<tr class="alternate">
			<td title="1 vecka">1 vecka</td>
			<td title="-0,1" class="decimal">-0,1</td>
			<td title="1,0" class="decimal">1,0</td>
		</tr>		
		*/

		List<D_FundDPDay> dpds = new ArrayList<>();
		List<String> entries = MM.getTagValues(recentReturnTable, "<tr");

		// Each column header is its separate currency
		List<String> headers = MM.getTagValues(entries.get(0), "<th");
		for(int i=1; i < headers.size(); i++) {
			String header = headers.get(i);
			D_FundDPDay dpd = new D_FundDPDay();
			dpd._currency = header.trim();
			dpds.add(dpd);
		}

		// Get the daily for each currency (each currency is its separate column)
		for(int i=1; i < entries.size(); i++) {
			ArrayList<String> tdColumns = MM.getTagValues(entries.get(i), "<td");

			String timePeriod = tdColumns.get(0);
			for(int j=1; j < tdColumns.size(); j++) {
				D_FundDPDay dpd = dpds.get(j-1);

				String returns = tdColumns.get(j);
				returns = MM.replaceArgTo(returns, " ", "");
				returns = MM.replaceArgTo(returns, "%", "");
				returns = MM.replaceArgTo(returns, ",", ".");

				OTuple2G<Boolean, Float> rv = ExtractFromHTML_Helper.validFloat(returns);
				if (!rv._o1) {
					iw.println("FundDPDay, a result could not be converted to a number: " + returns);
					fi._errorCode = D_FundInfo.IC_HTML_MS_DAILY_TABLE;
					return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
				}
				float rfloat = D_FundDPDay.FLOAT_NULL;
				if (rv._o2 != null) {
					rfloat = rv._o2.floatValue();
				}

				if(Pattern.matches("1 dag", timePeriod)) {
					dpd._r1d = rfloat;
				}
				else if(Pattern.matches("1 vecka", timePeriod)) {
					dpd._r1w = rfloat;
				}
				else if(Pattern.matches("1 m.nad", timePeriod)) {
					dpd._r1m = rfloat;
				}
				else if(Pattern.matches("3 m.nader", timePeriod)) {
					dpd._r3m = rfloat;
				}
				else if(Pattern.matches("6 m.nader", timePeriod)) {
					dpd._r6m = rfloat;
				}
				else if(Pattern.matches("1 .r", timePeriod)) {
					dpd._r1y = rfloat;
				}
				else if(Pattern.matches("3 .r", timePeriod)) {
					dpd._r3y = rfloat;
				}
				else if(Pattern.matches("5 .r", timePeriod)) {
					dpd._r5y = rfloat;
				}
				else if(Pattern.matches("10 .r", timePeriod)) {
					dpd._r10y = rfloat;
				}
			}
		}

		// Get the Swedish currency or error if there is no Swedish
		D_FundDPDay r = null;
		for (D_FundDPDay dpd: dpds) {
			if (dpd._currency.equals("SEK")) {
				r = dpd;
			}
		}

		if (r == null) {
			IndentWriter iwt = new IndentWriter();
			iwt.println("FundDPDay, could not find result in SEK currency");
			iwt.println("Out of: " + dpds.size() + ", entries");
			iwt.push();
			for (D_FundDPDay dpd: dpds) {
				dpd.dumpInfo(iwt);
			}
			iw.pop();
			String m = iwt.getString();
			iw.println(m);
			fi._errorCode = D_FundInfo.IC_HTML_MS_DAILY_SEK_CURRENCY;
			return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
		} else {
			D_FundDPDay dpd = fi._dpDays.get(0);
			dpd._r1d = r._r1d;
			dpd._r1w = r._r1w;
			dpd._r1m = r._r1m;
			dpd._r3m = r._r3m;
			dpd._r6m = r._r6m;
			dpd._r1y = r._r1y;
			dpd._r3y = r._r3y;
			dpd._r5y = r._r5y;
			dpd._r10y = r._r10y;
		}

		return ExtractFromHTML_Helper.RC_SUCCESS;
	}
	
	//------------------------------------------------------------------------
	public static int processYearlyReturnTable(
			IndentWriter iw,
			D_FundInfo fi,
			String table) throws Exception {
		boolean found = true;

		// <tr>
		//   <td title="I ??r*">I ??r*</td>
		//   <td title="13,5" class="decimal">13,5</td>
		//   <td title="12,8" class="decimal">12,8</td>
		//   <td title="13,9" class="decimal">13,9</td>
  	    // </tr>

		// First get each individual TR tag
		List<String> trTags = new ArrayList<>();
		found = true;
		OTuple2G<String, String> otTR = new OTuple2G<>(null, table);
		do {
			String tagValue = MM.assignAndReturnNextTagValue(otTR, "<tr");
			if(tagValue == null) {
				found = false;
			}
			else {
				String trTag = (String)otTR._o1;
				trTags.add(trTag);
			}
		} while(found);

		// Then parse all TDs within the TR
		int errorCode = ExtractFromHTML_Helper.RC_SUCCESS;
		for(int i=0; i < trTags.size(); i++) {
			String trTag = trTags.get(i);
			OTuple2G<String, String> otTDs = new OTuple2G<>(null, trTag);

			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String yearStr = (String) otTDs._o1.trim();

			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String fundStr = (String) otTDs._o1.trim();

			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String categoryStr = (String) otTDs._o1.trim();

			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String indexStr = (String) otTDs._o1.trim();

			D_FundDPYear dpy = new D_FundDPYear();
			short year = -1;
			boolean error = false;
			if (yearStr == null || yearStr.length() == 0) {
				iw.println("Year was null or had length == 0: " + yearStr);
				fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_YEAR;
				return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
			} else if (yearStr.startsWith("I ")) {
				year = 9999;
			} else if (yearStr.length() != 4) {
				iw.println("Year is incorrect, was not Y2D and did not have length 4: " + yearStr);
				fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_YEAR;
				return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
			} else {
				try {
					year = Short.parseShort(yearStr.substring(0, 4));
				} catch (NumberFormatException exc) {
					iw.println("Year could not be converted to a number: " + yearStr);
					return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
				}
			}
			dpy._year = year;

			OTuple2G<Boolean, Float> rv = null;

			rv = ExtractFromHTML_Helper.validFloat(fundStr);
			if (!rv._o1) {
				iw.println("FundDPYear, fund could not be converted to a number: " + fundStr);
				fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_FUND;
				return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
			}
			dpy._resultFund = rv._o2;

			rv = ExtractFromHTML_Helper.validFloat(categoryStr);
			if (!rv._o1) {
				iw.println("FundDPYear, category could not be converted to a number: " + fundStr);
				fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_CATEGORY;
				return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
			}
			dpy._resultCategory = rv._o2;

			rv = ExtractFromHTML_Helper.validFloat(indexStr);
			if (!rv._o1) {
				iw.println("FundDPYear, index could not be converted to a number: " + fundStr);
				fi._errorCode = D_FundInfo.IC_HTML_MS_YEARLY_TABLE_INDEX;
				return ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
			}
			dpy._resultIndex = rv._o2;

			if (dpy.isYearToDate()) {
				D_FundDPDay dpd = fi._dpDays.get(0);
				dpd._rYTDFund = dpy._resultFund;
				dpd._rYTDCategory = dpy._resultCategory;
				dpd._rYTDIndex = dpy._resultIndex;
			} else {
				boolean foundYear = false;
				for (D_FundDPYear dpyO : fi._dpYears) {
					if (dpyO._year == dpy._year) {
						foundYear = true;
						if (dpyO._resultFund != dpy._resultFund ||
								dpyO._resultCategory != dpy._resultCategory ||
								dpyO._resultIndex != dpy._resultIndex) {
							iw.println("FundDPYear, existing D_FundInfo differed from extracted. Keeping extracted.");
							iw.push();
							iw.println("Existing:  " + dpyO.toString());
							iw.println("Extracted: " + dpy.toString());
							iw.pop();
							errorCode = ExtractFromHTML_Helper.RC_ERROR_KEEP_FUND;
						}
						// Use the new one anyhow!
						dpyO._resultFund = dpy._resultFund;
						dpyO._resultCategory = dpy._resultCategory;
						dpyO._resultIndex = dpy._resultIndex;
					}
				}
				if (!foundYear) {
					iw.println("FundDPYear, adding a new year to FundInfo: " + dpy.toString() + ", previously existed");
					iw.push();
					for (D_FundDPYear dpyO : fi._dpYears) {
						iw.println(dpyO.toString());
					}
					iw.pop();

					fi._dpYears.add(dpy);
					Collections.sort(fi._dpYears, D_FundDPYear.COMPARATOR);
				}
			}
		}

		return errorCode;
	}

	//------------------------------------------------------------------------
	private static String getFromFirstLetter(String result) throws Exception {
		result = result.trim();
		for(int i=0; i < result.length(); i++) {
			if(Character.isLetter(result.charAt(i))) {
				result = result.substring(i);
				result = result.trim();
				break;
			}
		}
		if(result.trim().length() == 0) {
			result = "-";
		}
		return result;
	}
}

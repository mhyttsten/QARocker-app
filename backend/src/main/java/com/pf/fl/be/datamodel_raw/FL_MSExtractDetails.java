package com.pf.fl.be.datamodel_raw;

import com.pf.fl.be.datamodel.FLA_FundInfo;
import com.pf.fl.be.util.EE;
import com.pf.shared.IndentWriter;
import com.pf.shared.MM;
import com.pf.shared.OTuple2G;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class FL_MSExtractDetails {
	private static final Logger log = Logger.getLogger(FL_MSExtractDetails.class.getName());
	private static final String TAG = "FL_MSExtractDetails";
	
	//------------------------------------------------------------------------
	public static OTuple2G<Integer, REFundInfo> extractFundDetails(
			String accountType,
			String url,
			String pageContent,
			IndentWriter iw) throws Exception {
		OTuple2G<Integer, REFundInfo> result = new OTuple2G<>();
        iw.println("FL_MSExtractDetails.extractFundDetails");
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
        result = extractFundDetailsImpl(
                accountType,
                url,
                pageContent,
                iw);
        iw.pop();
        if (result._o2 == null) {
            iw.println("REFundInfo was null, returning null");
        } else {
            iw.println("REFundInfo: " + result._o2.getStats());
        }
        return result;
    }

    private static OTuple2G<Integer, REFundInfo>  extractFundDetailsImpl(
            String accountType,
            String url,
            String pageContent,
            IndentWriter iw) throws Exception {
		OTuple2G<Integer, REFundInfo> bigReturnValue = new OTuple2G<>();

		REFundInfo returnValue = new REFundInfo();
		returnValue.setAccountType(accountType);
		returnValue.setURL(url);

		OTuple2G ot = null;
		String findTagLoc = null;
		String findAfter = null;
		String findTo = null;
		String result = null;
		
		ot = new OTuple2G<String, String>(null, pageContent);
		String fundName = MM.assignAndReturnNextTagValue(ot, "<h2");
		if(fundName == null || (fundName.startsWith("S") && fundName.endsWith("k Fonder"))) {
			iw.println("Error: Not a fund data page");
			// iw.println("Page content: " + MM.getString(pageContent, 20000));
            iw.println("Returning null");
			bigReturnValue._o1 = FLA_FundInfo.IC_DATA_FUND_NOT_FOUND;
			return bigReturnValue;
		}
		fundName = MM.htmlReplaceHTMLCodes(fundName);
        iw.println("Decoded fundName: " + fundName);
		returnValue.setName(fundName);
		
		// MS Category
		findTagLoc = "<span class=\"quicktakecolContainer\" title=\"Morningstar Kategori";
		findAfter = ">";
		findTo = "</span>";
		String ahref = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		//System.out.println("A HREF: " + ahref);
		findTagLoc = "<a";
		findAfter = "href=\"";
		findTo = "\"";
		if (ahref == null) {
			iw.println("Error: Content was null before getting category");
            iw.println("Page content: " + MM.getString(pageContent, 20000));
            iw.println("Returning null");
			bigReturnValue._o1 = FLA_FundInfo.IC_DATA_MSCATEGORY_NOT_FOUND;
            return bigReturnValue;
		}
		String msCategoryHTML = MM.getRegExp(null, ahref, findTagLoc, findAfter, findTo, true);
		// System.out.println("   msCategoryHTML: " + msCategoryHTML);		
		findTagLoc = ">";
		findAfter = "";
		findTo = "</a>";
		String msCategoryText = MM.getRegExp(null, ahref, findTagLoc, findAfter, findTo, true);
		msCategoryText = MM.htmlReplaceHTMLCodes(msCategoryText);
		//System.out.println("   msCategoryText: " + msCategoryText);
		returnValue.setMSCategory(msCategoryText);

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
        iw.println("Setting rating to: " + msRatingHTML + ", from parsed: " + msRatingHTML);
		returnValue.setMSRating(msRatingHTML);
		
		// PPM Number
		findTagLoc = "<span class=\"quicktakecolContainer\" title=\"PPM-nummer\""; 
		findAfter = ">";
		findTo = "</span>";
		String ppmNumber = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		ppmNumber = MM.htmlReplaceHTMLCodes(ppmNumber);
        iw.println("Setting ppmNumber to: " + ppmNumber);
		returnValue.setPPMNumber(ppmNumber);
		//System.out.println("   ppmNumber: " + ppmNumber);
		
		// Yearly returns (table), I??r!!!
		findTagLoc = "<th class=\"decimal\">Fond</th><th class=\"decimal\">Kategori</th><th class=\"decimal\">Index</th>";
		findAfter = "</tr>";
		findTo = "</table>";
		String yearlyReturnTable = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		ArrayList<REFundInfo_DPYear> yearlyReturns = processYearlyReturnTable(yearlyReturnTable);
		// System.out.println("   yearlyReturnTable, entries: " + yearlyReturns.size());
        String sizeOfYearlyReturnTable = "null";
        if (yearlyReturns != null) {
            iw.println("Yearly returns, size of array: " + yearlyReturns.size());
            if (yearlyReturns.size() == 0) {
                iw.println("Error? DPYears had size 0");
            }
            iw.push();
            for (REFundInfo_DPYear dpyear: yearlyReturns) {
                dpyear.dumpInfo(iw);
            }
            iw.pop();
        } else {
            iw.println("Error? DPYears returned null");
        }
		returnValue.setDPYears(yearlyReturns);
		//for(int i=0; i < yearlyReturns.size(); i++) {
		//	System.out.println("        " + yearlyReturns.get(i).getStats());
		//}
		// Yearly returns: Date of last recorded data point
		findTagLoc = "<b>Kursdatum";
		findAfter = "</b>:";
		findTo = "<br />";
		String yearlyReturnsLastUpdatedDate = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
        iw.println("Last returns date for yearly returns: " + yearlyReturnsLastUpdatedDate);
		// System.out.println("   yearlyReturnsLastUpdatedDate: " + yearlyReturnsLastUpdatedDate);

		// Comparison index
		findTagLoc = "<b>Kategorins j";
		findAfter = "</b>:";
		findTo = "<br />";
		String indexCompare = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		returnValue.setIndexCompare(indexCompare);
        iw.println("Index compare: " + indexCompare);
		// System.out.println("   indexCompare: " + indexCompare);

		// Get currency in which NAV is measured
		findTagLoc = "<td>Senaste NAV</td>";
		findAfter = "<td>";
		findTo = "</td>";
		result = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		//System.out.println("   currency: " + result);
		result = getFromFirstLetter(result);
		// System.out.println("Currency is: " + result);
		returnValue.setCurrency(result);
        iw.println("Currency: " + result);

		// Return (daily, month, 3m, 6m, 1y)
		findTagLoc = "Avkastning %";
		findAfter = "<table class=\"alternatedtoplist\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">";
		findTo = "</table>";
		String recentReturnTable = MM.getRegExp(null, pageContent, findTagLoc, findAfter, findTo, true);
		ArrayList<REFundInfo_DPDay> msrd = processRecentReturnsTable(fundName, recentReturnTable);
        if (msrd == null) {
            iw.println("Daily returns 1: Got null");
        } else {
            iw.println("Daily returns 1, got: " + msrd.size() + " entries");
            iw.push();
            for (REFundInfo_DPDay dpday: msrd) {
                iw.println(dpday.getStats());
            }
            iw.pop();
        }
		returnValue.setDPDays(msrd);

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
		// System.out.println("   returnsLastDate: " + returnsLastDate);
		for(int i=0; i < msrd.size(); i++) {
			msrd.get(i).setDataPointDate(returnsLastDate);
		}
        if (msrd == null) {
            iw.println("Daily returns 2: Got null");
        } else {
            iw.println("Daily returns 2, got: " + msrd.size() + " entries");
            iw.push();
            for (REFundInfo_DPDay dpday: msrd) {
                iw.println(dpday.getStats());
            }
            iw.pop();
        }

		REFundInfo_DPNAV navData = processNAVTable(pageContent);
		returnValue.setNAVData(navData);
        if (navData == null) {
            iw.println("NAV data was null");
        } else {
            iw.println("NAV data: " + navData);
        }

		bigReturnValue._o1 = FLA_FundInfo.IC_NONE;
		bigReturnValue._o2 = returnValue;
		return bigReturnValue;
	}

	// ***********************************************************************

	//------------------------------------------------------------------------
	public static REFundInfo_DPNAV processNAVTable(String pageContent) throws Exception {
		
		/*
		<tr>
   		   <td>Senaste NAV</td><td>  8,36 SEK</td><td>2012-03-16</td>
	    </tr>
	    <tr class="modulealternate">
		   <td>H??gst, senaste 12 m??n</td><td>  8,36 SEK</td><td>2012-03-16</td>
	    </tr>
	    <tr>
		   <td>L??gst, senaste 12 m??n</td><td>  6,81 SEK</td><td>2011-08-26</td>
	   </tr>
	   */
		
		REFundInfo_DPNAV result = new REFundInfo_DPNAV();
		String data = null;
		ArrayList<String> entries = null;
		String e0 = null;
		String e1 = null;
		
		data = MM.getRegExp(null, pageContent, "<td>Senaste NAV", "</td>", "</tr>", true);
		entries = MM.getTagValues(data, "<td");
		if(entries.size() != 2) {
			throw new Exception("Strange Senaste NAV format\n" + data);
		}
		e0 = entries.get(0);
		e1 = entries.get(1);
		//System.out.println("FL_MSExtractDetails, e0_0: " + e0);
		//System.out.println("FL_MSExtractDetails, e1_0: " + e1);
		result.setNAVLatest_Value(getUntilNonDigit(e0));
		result.setNAVLatest_Currency(getFromFirstLetter(e0));
		result.setNAVLatest_Date(MM.dateConvert_YYYYDMMDDD_To_YYMMDD(e1.trim()));
		//System.out.println("FL_MSExtractDetails, l_v: " + result.getNAVLatest_Value());
		//System.out.println("FL_MSExtractDetails, l_c: " + result.getNAVLatest_Currency());
		//System.out.println("FL_MSExtractDetails, l_d: " + result.getNAVLatest_Date());
		//MM.sleepInMS(2000);

		data = MM.getRegExp(null, pageContent, "<td>H.gst, senaste 12 m.n", "</td>", "</tr>", true);
		entries = MM.getTagValues(data, "<td");
		if(entries.size() != 2) {
			throw new Exception("Strange Highest NAV format\n" + data);
		}
		e0 = entries.get(0);
		e1 = entries.get(1);
		//System.out.println("FL_MSExtractDetails, e0_1: " + e0);
		//System.out.println("FL_MSExtractDetails, e1_1: " + e1);
		if (!e0.trim().equals("-") && !e1.trim().equals("-")) {
			result.setNAVHighest12M_Value(getUntilNonDigit(e0));
			result.setNAVHighest12M_Currency(getFromFirstLetter(e0));
			result.setNAVHighest12M_Date(MM.dateConvert_YYYYDMMDDD_To_YYMMDD(e1.trim()));
		}
		//System.out.println("FL_MSExtractDetails, h_v: " + result.getNAVLatest_Value());
		//System.out.println("FL_MSExtractDetails, h_c: " + result.getNAVLatest_Currency());
		//System.out.println("FL_MSExtractDetails, h_d: " + result.getNAVLatest_Date());
		//MM.sleepInMS(2000);

		data = MM.getRegExp(null, pageContent, "<td>L.gst, senaste 12 m.n", "</td>", "</tr>", true);
		entries = MM.getTagValues(data, "<td");
		if(entries.size() != 2) {
			throw new Exception("Strange Lowest NAV format\n" + data);
		}
		e0 = entries.get(0);
		e1 = entries.get(1);
		//System.out.println("FL_MSExtractDetails, e0_2: " + e0);
		//System.out.println("FL_MSExtractDetails, e1_2: " + e1);
		if (!e0.trim().equals("-") && !e1.trim().equals("-")) {
			result.setNAVLowest12M_Value(getUntilNonDigit(e0));
			result.setNAVLowest12M_Currency(getFromFirstLetter(e0));
			result.setNAVLowest12M_Date(MM.dateConvert_YYYYDMMDDD_To_YYMMDD(e1.trim()));
		}
		//System.out.println("FL_MSExtractDetails, lat_v: " + result.getNAVLatest_Value());
		//System.out.println("FL_MSExtractDetails, lat_c: " + result.getNAVLatest_Currency());
		//System.out.println("FL_MSExtractDetails, lat_d: " + result.getNAVLatest_Date());

		return result;
	}

	//------------------------------------------------------------------------
	public static ArrayList<REFundInfo_DPDay> processRecentReturnsTable(
			String fundName,
			String recentReturnTable) throws Exception {
		ArrayList<REFundInfo_DPDay> result = new ArrayList<REFundInfo_DPDay>();

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

		ArrayList<String> entries = MM.getTagValues(recentReturnTable, "<tr");

		ArrayList<String> headers = MM.getTagValues(entries.get(0), "<th");
		for(int i=1; i < headers.size(); i++) {
			String header = headers.get(i);
			REFundInfo_DPDay rowData = new REFundInfo_DPDay();
			rowData.setCurrency(header.trim());
			result.add(rowData);
		}
		
		for(int i=1; i < entries.size(); i++) {
			ArrayList<String> tdColumns = MM.getTagValues(entries.get(i), "<td");

			String timePeriod = tdColumns.get(0);
			for(int j=1; j < tdColumns.size(); j++) {
				String returns = tdColumns.get(j);
				returns = MM.replaceArgTo(returns, " ", "");
				returns = MM.replaceArgTo(returns, "%", "");
				returns = MM.replaceArgTo(returns, ",", ".");
				
				REFundInfo_DPDay rowData = result.get(j-1);
				if(Pattern.matches("1 dag", timePeriod)) {
					rowData.setR1D(returns);
				}
				else if(Pattern.matches("1 vecka", timePeriod)) {
					rowData.setR1W(returns);
				}
				else if(Pattern.matches("1 m.nad", timePeriod)) {
					rowData.setR1M(returns);
				}
				else if(Pattern.matches("3 m.nader", timePeriod)) {
					rowData.setR3M(returns);
				}
				else if(Pattern.matches("6 m.nader", timePeriod)) {
					rowData.setR6M(returns);
				}
				else if(Pattern.matches("1 .r", timePeriod)) {
					rowData.setR1Y(returns);
				}
				else if(Pattern.matches("3 .r", timePeriod)) {
					rowData.setR3Y(returns);
				}
				else if(Pattern.matches("5 .r", timePeriod)) {
					rowData.setR5Y(returns);
				}
				else if(Pattern.matches("10 .r", timePeriod)) {
					rowData.setR10Y(returns);
				}
			}
		}
		return result;
	}
	
	//------------------------------------------------------------------------
	public static ArrayList<REFundInfo_DPYear> processYearlyReturnTable(String table) throws Exception {
		ArrayList<REFundInfo_DPYear> result = new ArrayList<REFundInfo_DPYear>();
		boolean found = true;

		// <tr>
		//   <td title="I ??r*">I ??r*</td>
		//   <td title="13,5" class="decimal">13,5</td>
		//   <td title="12,8" class="decimal">12,8</td>
		//   <td title="13,9" class="decimal">13,9</td>
  	    // </tr>		
		
		// First get each individual TR tag
		ArrayList<String> trTags = new ArrayList<String>();
		found = true;
		OTuple2G<String,String> otTR = new OTuple2G<String,String>(null, table);
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
		for(int i=0; i < trTags.size(); i++) {
			String trTag = trTags.get(i);
			OTuple2G<String,String> otTDs = new OTuple2G<String,String>(null, trTag);

			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String year = (String)otTDs._o1;

			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String fund = (String)otTDs._o1;

			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String category = (String)otTDs._o1;
		
			MM.assignAndReturnNextTagValue(otTDs, "<td");
			String index = (String)otTDs._o1;
			
			REFundInfo_DPYear msfyd = new REFundInfo_DPYear(year, fund, category, index);
			result.add(msfyd);
		}

		return result;
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
	
	//------------------------------------------------------------------------
	private static String getUntilNonDigit(String arg) throws Exception {
		String result = "";
		arg = arg.trim();
		for(int i=0; i < arg.length(); i++) {
			char c = arg.charAt(i);
			// If it is a digit or a decimal point, then we process it
			if(c == '.' ||
			   c == ',' ||
  			   Character.isDigit(c)) {
				// All decimal points should be '.'
				if(c == ',') {
					c = '.';
				}
				// Make sure we don't start with a ',' but '.' but then have a 0 in front
				if(c == '.' && result.length() == 0) {
					result = "0";
				}
				result += c;
			}
			// We break on first character which is not a digit or a decimal point
			else {
				break;
			}
		}
		if(result.trim().length() == 0) {
			result = "-";
		}
		return result;
	}
}

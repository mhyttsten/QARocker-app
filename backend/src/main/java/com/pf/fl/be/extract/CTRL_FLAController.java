package com.pf.fl.be.extract;

import com.pf.fl.be.datamodel.*;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.pf.fl.be.datastore.DS;
import com.pf.fl.be.util.EE;
import com.pf.shared.Compresser;
import com.pf.shared.IndentWriter;
import com.pf.shared.MM;
import com.pf.shared.OTuple2G;
import com.pf.fl.be.datamodel_raw.FL_MSExtractDetails;
import com.pf.fl.be.datamodel_raw.REFundInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class CTRL_FLAController {

//	private static final Logger log = Logger.getLogger(CTRL_FLAController.class.getName());
//	private static final String TAG = MM.getClassName(CTRL_FLAController.class.getName());
//
//	public static OTuple2G<Boolean, FLA_FundRawData> addFLA_FundRawData(
//			EE ee,
//			FLA_FundInfo fiExisting,
//			String dateYYMMDD_Update_Attempted,
//			boolean dontPersistOnError,
//			String accountType,
//			String url,
//			byte[] htmlBlob,
//			IndentWriter iw) throws Exception {
//        iw.println("CTRL_FLAController.addFLA_FundRawData");
//        iw.push();
//        iw.println("-dateYYMMDD_Update_Attempted: " + dateYYMMDD_Update_Attempted);
//        iw.println("-dontPersistOnError: " + dontPersistOnError);
//        iw.println("-url: " + url);
//        if (htmlBlob != null) {
//            iw.println("-htmlBlob, size: " + htmlBlob.length);
//
//        } else {
//            iw.println("-htmlBlob: null");
//        }
//        iw.println("-accountType: " + accountType);
//
//		OTuple2G<Boolean, FLA_FundRawData> r = addFLA_FundRawDataImpl(
//				ee,
//				fiExisting,
//				dateYYMMDD_Update_Attempted,
//				dontPersistOnError,
//				accountType,
//				url,
//				htmlBlob,
//				iw);
//        iw.println("Returning, a1: " + r._o1);
//        iw.println("Returning, a2");
//        r._o2.dumpInfo(iw);
//        iw.pop();
//		return r;
//	}
//
//	// ***********************************************************************
//
//	private static OTuple2G<Boolean, FLA_FundRawData> addFLA_FundRawDataImpl(
//			EE ee,
//			FLA_FundInfo fiExisting,
//			String dateYYMMDD_Update_Attempted,
//			boolean dontPersistOnError,
//			String accountType,
//			String url,
//			byte[] htmlDataUncompressed,
//			IndentWriter iw) throws Exception {
//		iw.println("CTRL_FLAController.addFLA_FundRawData with same argument as caller");
//
//		String htmlString = MM.newString(htmlDataUncompressed, EE.ENCODING_FILE_READ);
//
//		// Get the RE from HTML data
//		// ee.dinfo(log, TAG, "addFLA_FundRawDataImpl, perfId: " + REFundInfo.getPerfIdFromURL(url));
//        iw.println("Will now extract fund details");
//		REFundInfo reFundInfo = FL_MSExtractDetails.extractFundDetails(
//				accountType,
//				url,
//				htmlString,
//				iw);
//        iw.println("Fund details extracted");
//
//		// This is a serious error, we cannot decode it
//        if (reFundInfo != null) {
//            iw.println("Fund info not null: " + reFundInfo.getStats());
//        } else {
//            iw.println("Fund info null, decoding failed");
//			if (dontPersistOnError) {
//                iw.println("Don't persist on error is true, returning error");
//				return new OTuple2G<>(false, null);
//			}
//
//            iw.println("Don't persist on error is false, saving the data even if it was an error");
//            iw.println("Will now insert FundRawData");
//			FLA_FundRawData rd = insertFLA_FundRawData(
//					false,
//					fiExisting,
//					accountType,
//					null,
//					url,
//					null,
//					MM.getNowAs_YYMMDD(EE.TIMEZONE_STOCKHOLM),
//					htmlDataUncompressed,
//                    iw);
//			FLA_FundInfo fi = rd.mFundInfo.get();
//			fi.mIsValid = false;
//			fi.mInvalidCode = FLA_FundInfo.IC_COM_HTML_NOT_DECODABLE;
//
//			DS.saveFundInfoAndUpdateFundIndex(iw, fi);
//            iw.println("Setting FundRawData isValid to false, saving it, and returning it with error");
//			return new OTuple2G<>(false, rd);
//		}
//
//        iw.println("Adding FundRawData and FundInfo");
//		return addFLA_FundRawDataFromREFundInfoImpl(
//				ee,
//				fiExisting,
//				reFundInfo,
//				dateYYMMDD_Update_Attempted,
//				dontPersistOnError,
//				accountType,
//				url,
//				htmlDataUncompressed,
//				iw);
//	}
//
//	/**
//	 * This is a split of the original method to help testing The tester can
//	 * enter here with an invalid htmlBlob but valid reFundInfo
//	 */
//	public static OTuple2G<Boolean, FLA_FundRawData> addFLA_FundRawDataFromREFundInfoImpl(
//			EE ee,
//			FLA_FundInfo fiExisting,
//			REFundInfo reFundInfo,
//			String dateYYMMDD_Update_Attempted,
//			boolean dontPersistOnError,
//			String accountType,
//			String url,
//			byte[] htmlBlob,
//			IndentWriter iw) throws Exception {
//
//        iw.println("CTRL_FLAController.addFLA_FundRawDataFromREFundInfoImpl");
//        iw.push();
//
//        String line = reFundInfo.getStats();
//		String name = reFundInfo.getName();
//
//        iw.println("name: " + name);
//        iw.println("line: " + line);
//		if (fiExisting != null) {
//			iw.println("fiExisting.mType: " + fiExisting.mType);
//			iw.println("fiExisting.mName: " + fiExisting.mName);
//		}
//
//		// Convert from RE to FLA
//		OTuple2G<Boolean, FLA_FundInfo> flaInfoResult;
//		flaInfoResult = CTRL_RE2FLAConverter.convertToFLA_REFundInfo(
//                iw,
//                reFundInfo);
//		if (!flaInfoResult._o1) {
//            iw.println("RE to FLA conversion failed for: " + reFundInfo.getAccountType() + "." + reFundInfo.getName());
//			if (dontPersistOnError) {
//            iw.println("Dont persist on error so we return");
//                iw.pop();
//				return new OTuple2G<>(false, null);
//			}
//			FLA_FundRawData rd = insertFLA_FundRawData(
//					false,
//					fiExisting,
//					accountType,
//					name,
//					url,
//					line,
//					reFundInfo.getDateYYMMDD(),
//					htmlBlob,
//                    iw);
//			FLA_FundInfo fi = rd.mFundInfo.get();
//			fi.mIsValid = false;
//			fi.mInvalidCode = FLA_FundInfo.IC_DATA_CONVERSION_ERROR;
//			DS.saveFundInfoAndUpdateFundIndex(iw, fi);
//            iw.println("FundInfo set to Invalid: " + fi.mType + "." + fi.mName + ", id: " + fi.mId + ", url: " + fi.mURL);
//            iw.pop();
//			return new OTuple2G<>(false, rd);
//		}
//
//		// Now you have a valid FLA, this is great == validity
//        iw.println("RE to FLA conversion successful for: " + reFundInfo.getAccountType() + "." + reFundInfo.getName());
//        flaInfoResult._o2.dumpInfo(iw);
//        iw.println("Will now insert FLA_FundRawData");
//		FLA_FundRawData returnValue = insertFLA_FundRawData(
//				true,
//				fiExisting,
//				accountType,
//				name,
//				url,
//				line,
//				reFundInfo.getDateYYMMDD(),
//				htmlBlob,
//                iw);
//
//		// Upgrade the FLA_FundInfo part with the new data
//        iw.println("Will now upgrade FLA_FundInfo with new data");
//		FLA_FundInfo fiOrig = returnValue.mFundInfo.get();
//		upgradeFLA_FundInfoImpl(
//				dateYYMMDD_Update_Attempted,
//				fiOrig,
//				flaInfoResult._o2,
//				iw);
//
//        iw.println("Done returning successful from successful flow");
//		return new OTuple2G<>(true, returnValue);
//	}
//
//	private static FLA_FundRawData insertFLA_FundRawData(
//			boolean isDecodable,
//			FLA_FundInfo fiExisting,
//			String accountType,
//			String name,
//			String url,
//			String line,
//			String dateYYMMDD_DPDay,
//			byte[] htmlBlob,
//            IndentWriter iw) throws Exception {
//        iw.println("CTRL_FLAController.insertFLA_FundRawData");
//        iw.push();
//        iw.println("-isDecodable: " + isDecodable);
//        iw.println("-accountType: " + accountType);
//        iw.println("-name: " + name);
//        iw.println("-url: " + url);
//        iw.println("-dateYYMMDD_DPDay: " + dateYYMMDD_DPDay);
//        FLA_FundRawData frd = insertFLA_FundRawDataImpl(
//				isDecodable,
//				fiExisting,
//				accountType,
//				name,
//				url,
//				line,
//				dateYYMMDD_DPDay,
//				htmlBlob,
//				iw);
//        if (frd == null) {
//            iw.println("Null was returned from insertFLA_FundRawDataImpl");
//        } else {
//            iw.println("Returning non-null FLA_FundRawData");
//            frd.dumpInfo(iw);
//        }
//        iw.pop();
//        return frd;
//    }
//    private static FLA_FundRawData insertFLA_FundRawDataImpl(
//            boolean isDecodable,
//			FLA_FundInfo fiExisting,
//            String accountType,
//            String name,
//            String url,
//            String line,
//            String dateYYMMDD_DPDay,
//            byte[] htmlBlob,
//            IndentWriter iw) throws Exception {
//
//        iw.println("CTRL_FLAController.insertFLA_FundRawDataImpl");
//        iw.push();
//
//        // We only want a rudimentary FLA_FundInfo for now
//        FLA_FundInfo dsFundInfo = null;
//		if (fiExisting != null) {
//			dsFundInfo = fiExisting;
//		} else {
//			dsFundInfo = DS.getFundInfoByTypeAndName(accountType, name);
//		}
//
//		Ref<FLA_FundInfo> dsFundInfoRef = null;
//		if (dsFundInfo == null) {
//            iw.println("Could not an existing FLA_FundInfo, creating shadow (URL: " + url + ")");
//			EE.getEE().dwarning(log, TAG, "CREATING SHADOW FundInfo since URL not found: " + url);
//			dsFundInfo = FLA_FundInfo.instantiate(accountType, name, url, -1, -1, "010101");
//			dsFundInfoRef = DS.saveFundInfoAndUpdateFundIndex(iw, dsFundInfo);
//		} else {
//            iw.println("Found existing FLA_FundInfo for URL: " + url);
//			dsFundInfoRef = Ref.create(Key.create(FLA_FundInfo.class, dsFundInfo.mId));
//		}
//
//		byte[] htmlBlobZIP = Compresser.dataCompress(accountType + "." + name, htmlBlob);
//
//		// Save the FundRawData
//        iw.println("Now saving FundRawData");
//		FLA_FundRawData frd = ofy().load().type(FLA_FundRawData.class).ancestor(dsFundInfoRef.get()).filter("mDateYYMMDD_DPDay", dateYYMMDD_DPDay).first().now();
//		if (frd == null) {
//			iw.println("No previously existing FundRawData found for this date: " + dateYYMMDD_DPDay);
//			frd = new FLA_FundRawData();
//			frd.mFundInfo = dsFundInfoRef;
//			frd.mIsDecodable = isDecodable;
//			frd.mLine = line;
//			frd.mURL = url;
//			frd.mDateYYMMDD_DPDay = dateYYMMDD_DPDay;
//			frd.mEncodingId = REFundInfo.ENCODING_ID;
//			frd.mBlobDataCompressed = htmlBlobZIP;
//			Key<FLA_FundRawData> fundRawDataKey = ofy().save().entity(frd).now();
//		} else {
//            iw.println("Previous FundRawData found for this date: " + dateYYMMDD_DPDay);
//		}
//        iw.pop();
//		return frd;
//	}
//
//	private static void upgradeFLA_FundInfoImpl(
//			String dateYYMMDD_Update_Attempted,
//			FLA_FundInfo fiOrig,
//			FLA_FundInfo fiUpgrade,
//			IndentWriter iw)
//			throws Exception {
//
//        iw.println("CTRL_FLAController.upgradeFLA_FundInfoImpl");
//        iw.push();
//        iw.println("Type: " + fiOrig.mType + "." + fiOrig.mName);
//        iw.println("fiOrig_updated: " + fiOrig.mDateYYMMDD_Updated + ", fiUpgrade.updated: " + fiUpgrade.mDateYYMMDD_Updated);
//        iw.println("DPDays from upgrade: " + printList(fiUpgrade.mDPDays));
//
//		int indexCount = 0;
//		while (indexCount < fiUpgrade.mDPDays.size()) {
//			if (fiUpgrade.mDPDays.get(indexCount).mR1w == null) {
//				fiUpgrade.mDPDays.remove(indexCount);
//			} else {
//				indexCount++;
//			}
//		}
//		iw.println("DPDays from upgrade after null removal : " + printList(fiUpgrade.mDPDays));
//
//		if (fiUpgrade.mDPDays.size() == 0) {
//            iw.println("We are done, we only found nulls");
//			dirtiesInsert(fiOrig, fiOrig.mDPDays, iw);
//			setUpdatedDates(fiOrig, dateYYMMDD_Update_Attempted, iw);
//			Ref<FLA_FundInfo> ref = DS.saveFundInfoAndUpdateFundIndex(iw, fiOrig);
//            fiOrig.dumpInfo(iw);
//            iw.pop();
//			return;
//		}
//
//        iw.println("Now setting update attempted1: " + dateYYMMDD_Update_Attempted);
//		fiOrig.mDateYYMMDD_Update_Attempted = dateYYMMDD_Update_Attempted;
//
//		// Check if FLA part should be upgraded
//		// And if so, upgrade it
//		if (fiOrig.mDateYYMMDD_Updated.compareTo(fiUpgrade.mDateYYMMDD_Updated) <= 0) {
//            iw.println("FLA part should be upgraded");
//			fiOrig.mType = fiUpgrade.mType;
//			fiOrig.mName = fiUpgrade.mName;
//			fiOrig.mMSRating = fiUpgrade.mMSRating;
//			fiOrig.mPPMNumber = fiUpgrade.mPPMNumber;
//			fiOrig.mCategory = fiUpgrade.mCategory;
//			fiOrig.mIndexCompare = fiUpgrade.mIndexCompare;
//			fiOrig.mCurrency = fiUpgrade.mCurrency;
//            iw.println("Upgraded FundInfo to");
//            fiOrig.dumpInfo(iw);
//		}
//
//		// Adjust DPDay - Gather all unique entries from Upgrade
//		// Get to a working set of DPDaysm removing all duplicates
//        iw.println("DPDays orig including dirties: " + printList(fiOrig.mDPDays));
//		dirtiesRemove(fiOrig.mDPDays);
//		HashMap<String, FLA_FundDPDay> hmUpgradeNewKeys = new HashMap<>();
//		for (FLA_FundDPDay eUpgrade : fiUpgrade.mDPDays) {
//			if (!hmUpgradeNewKeys.containsKey(eUpgrade.mDateYYMMDD_Orig)) {
//				hmUpgradeNewKeys.put(eUpgrade.mDateYYMMDD_Orig, eUpgrade);
//			}
//		}
//		for (FLA_FundDPDay eOrig : fiOrig.mDPDays) {
//			if (hmUpgradeNewKeys.containsKey(eOrig.mDateYYMMDD_Orig)) {
//                iw.println("Removing date: " + eOrig.mDateYYMMDD_Orig + " from Upgrade as Orig already had it");
//				hmUpgradeNewKeys.remove(eOrig.mDateYYMMDD_Orig);
//			}
//		}
//		List<FLA_FundDPDay> dpWorkingSet = new ArrayList<>();
//		dpWorkingSet.addAll(fiOrig.mDPDays);
//        iw.println("DPDays from Orig: " + printList(fiOrig.mDPDays));
//		List<FLA_FundDPDay> dpDaysUpgrade = new ArrayList<>(hmUpgradeNewKeys.values());
//        iw.println("DPDays fro Upgrade: " + printList(dpDaysUpgrade));
//		dpWorkingSet.addAll(dpDaysUpgrade);
//		Collections.sort(dpWorkingSet, FLA_FundDPDay.COMPARATOR_DATE_ORIG);
//        iw.println("Working set: " + printList(dpWorkingSet));
//
//		// Make every thursday a friday...
//		// From now on, do not use Orig anymore
//		HashMap<String, FLA_FundDPDay> hmTmp = new HashMap<>();
//		for (FLA_FundDPDay elem : dpWorkingSet) {
//			// There should be no duplicates anymore
//			hmTmp.put(elem.mDateYYMMDD_Orig, elem);
//		}
//		dpWorkingSet.clear();
//		dpWorkingSet.addAll(hmTmp.values());
//		Collections.sort(dpWorkingSet, FLA_FundDPDay.COMPARATOR_DATE_ORIG);
//
//		// Walk dpWorkingSet adjusting everything to all Fridays
//		// The set is already ordered on date
//		String doneFriday = null;
//		List<FLA_FundDPDay> result = new ArrayList<>();
//
//		// Current day in New York
//		String nowYYMMDD = MM.getNowAs_YYMMDD(EE.TIMEZONE_NEW_YORK);
//		// Last friday in New York (even if today is a friday)
//		String fridayDPDayLast = MM.tgif_getLastFridayTodayExcl(nowYYMMDD);
//		// Next friday in New York, possible today
//		String fridayDPDayFuture = MM.tgif_getNextFridayTodayExcl(fridayDPDayLast);
//
//		while (dpWorkingSet.size() > 0) {
//			// If this is the first, initialize the windows parameters
//			if (doneFriday == null) {
//				// Set friday so that current gets included in the next forward
//				// below
//				String startDate = dpWorkingSet.get(0).mDateYYMMDD_Orig;
//				doneFriday = MM.tgif_getLastFridayTodayIncl(startDate);
//				doneFriday = MM.tgif_getLastFridayTodayExcl(doneFriday);
//  				iw.println("Initial, start: " + startDate + ", doneFriday: " + doneFriday);
//			}
//
//			// Get all DPDays that are within next period
//			String fridayStartIncl = MM.tgif_getNextFridayTodayExcl(doneFriday);
//			String fridayEndExcl = MM.tgif_getNextFridayTodayExcl(fridayStartIncl);
//
//			// If we are are now looking to populate a future friday that has not happened
//			// Then we are finished, since we want to wait until that friday happens to get the DP
//			if (fridayDPDayFuture.compareTo(fridayStartIncl) <= 0) {
//				dpWorkingSet.clear();
//				break;
//			}
//
//			// Let's get the best matching DP for this time interval
//			FLA_FundDPDay c = dpDayExtractCandidates(fridayStartIncl, fridayEndExcl, dpWorkingSet, iw);
//			if (c != null) {
//				c.mDateYYMMDD = fridayStartIncl;
//				result.add(c);
//			}
//			doneFriday = fridayStartIncl;
//		}
//
//		// Insert dirties, sort it, set the updates, and save
//		dirtiesInsert(fiOrig, result, iw);
//		fiOrig.mDPDays = result;
//		setUpdatedDates(fiOrig, dateYYMMDD_Update_Attempted, iw);
//		Ref<FLA_FundInfo> ref = DS.saveFundInfoAndUpdateFundIndex(iw, fiOrig);
//        iw.println("Saved FundInfo, result:");
//        fiOrig.dumpInfo(iw);
//	}
//
//	private static FLA_FundDPDay dpDayExtractCandidates(
//			String fridayStartIncl,
//            String fridayEndExcl,
//			List<FLA_FundDPDay> dpWorkingSet,
//            IndentWriter iw) throws Exception {
//        iw.println("CTRL_FLAController.dpDayExtractCandidates");
//        iw.push();
//
//        iw.println("Extracting Candidates, sFriday: " + fridayStartIncl	+ ", eFriday: " + fridayEndExcl);
//        iw.println("WorkingSet: " + printList(dpWorkingSet));
//
//		// Precondition - Do not call unless you want work done
//		if (dpWorkingSet.size() == 0) {
//			throw new Exception("Working set did not have any entries");
//		}
//
//		// Get all entries which are prior to trading day
//		// These are still ensured to be within the same week as trading day
//		// By the way that we move the extraction window
//		List<FLA_FundDPDay> candidatesPreTradingDay = new ArrayList<>();
//		while (dpWorkingSet.size() > 0 && dpWorkingSet.get(0).mDateYYMMDD_Orig.compareTo(fridayStartIncl) < 0) {
//			FLA_FundDPDay e = dpWorkingSet.get(0);
//            iw.println("Considering_Bwd date_Orig: "	+ e.mDateYYMMDD_Orig);
//			candidatesPreTradingDay.add(e);
//			dpWorkingSet.remove(0);
//		}
//
//		// Get all entries on or after trading day
//		// Except next Thursday
//		List<FLA_FundDPDay> candidates = new ArrayList<>();
//		while (dpWorkingSet.size() > 0) {
//			FLA_FundDPDay e = dpWorkingSet.get(0);
//			if (e.mDateYYMMDD_Orig.compareTo(fridayStartIncl) >= 0
//					&& e.mDateYYMMDD_Orig.compareTo(fridayEndExcl) < 0
//					&& !MM.tgif_isThursday(e.mDateYYMMDD_Orig)) {
//                iw.println("Considering_Fwd date_Orig: "	+ e.mDateYYMMDD_Orig);
//				candidates.add(e);
//				dpWorkingSet.remove(0);
//			} else {
//				break;
//			}
//		}
//
//		// Get either the earliest succeeding one
//		// Or the earliest preceeding one
//		FLA_FundDPDay result = null;
//		if (candidates.size() > 0) {
//			result = candidates.remove(0);
//            iw.println("SelectedFwd date_Orig: " + result.mDateYYMMDD_Orig);
//		} else if (candidatesPreTradingDay.size() > 0) {
//			result = candidatesPreTradingDay.get(candidatesPreTradingDay.size() - 1);
//			iw.println("SelectedBwd date_Orig: " + result.mDateYYMMDD_Orig);
//		}
//
//		// We throw away any candidates preceeding trading day
//		// But we shift any successors back for next window
//		if (candidates.size() > 0) {
//			for (int i = candidates.size() - 1; i >= 0; i--) {
//				FLA_FundDPDay fdpday = candidates.get(i);
//                iw.println("Putting back candidate_Orig: " + fdpday.mDateYYMMDD_Orig + ", " + fdpday.mR1w);
//				dpWorkingSet.add(0, candidates.get(i));
//			}
//		}
//        iw.println("WorkingSet_Exit: " + printList(dpWorkingSet));
//
//		if (result == null) {
//            iw.println("No candidate found for period");
//		}
//
//        iw.pop();
//		return result;
//	}
//
//	private static String printList(List<FLA_FundDPDay> dpWorkingSet) throws Exception {
//		String r = "";
//		for (FLA_FundDPDay elem : dpWorkingSet) {
//			r += elem.mDateYYMMDD + "(" + elem.mDateYYMMDD_Orig + "):" + elem.mR1w + ", ";
//		}
//		return r;
//	}
//
//	private static void dirtiesRemove(List<FLA_FundDPDay> l) {
//		int index = 0;
//		while (index < l.size()) {
//			if (l.get(index).mR1w == null) {
//				l.remove(index);
//			} else {
//				index++;
//			}
//		}
//	}
//
//	private static void setUpdatedDates(
//            FLA_FundInfo fi,
//            String updateAttempted,
//            IndentWriter iw) throws Exception {
//        iw.println("CTRL_FLA_Controller.setUpdatedDates");
//        iw.push();
//        iw.println("Now setting update attempted2: " + updateAttempted);
//		fi.mDateYYMMDD_Update_Attempted = updateAttempted;
//		if (fi.mDPDays != null && fi.mDPDays.size() > 0) {
//			FLA_FundDPDay dpday = fi.mDPDays.get(fi.mDPDays.size()-1);
//			if (fi.mDateYYMMDD_Updated == null || fi.mDateYYMMDD_Updated.length() != 6) {
//				throw new Exception("Erroneous date for FundInfo: " + fi.mType + "." + fi.mName + ", id: " + fi.mId + ", date: " + fi.mDateYYMMDD_Updated);
//			} else if (fi.mDateYYMMDD_Updated.compareTo(dpday.mDateYYMMDD) < 0) {
//				fi.mDateYYMMDD_Updated = dpday.mDateYYMMDD;
//			}
//		}
//        iw.println("Result was: " + fi.mDateYYMMDD_Updated);
//        iw.pop();
//	}
//
//	private static void dirtiesInsert(
//			FLA_FundInfo fi,
//            List<FLA_FundDPDay> l,
//            IndentWriter iw) throws Exception {
//        iw.println("CTRL_FLAController");
//        iw.push();
//		if (l == null || l.size() == 0) {
//            iw.println("Null or zero element list, returning");
//            iw.pop();
//			return;
//		}
//		List<FLA_FundDPDay> r = new ArrayList<>();
//		String fridayStart = l.get(0).mDateYYMMDD;
//		r.add(l.remove(0));
//
//		fridayStart = MM.tgif_getNextFridayTodayExcl(fridayStart);
//		String now = MM.getNowAs_YYMMDD(EE.TIMEZONE_STOCKHOLM);
//		String fridayNow = MM.tgif_getLastFridayTodayIncl(now);
//
//		// Do a sanity check
//		for (int i=0; i < l.size(); i++) {
//			FLA_FundDPDay dpday = l.get(i);
//			if (fridayNow.compareTo(dpday.mDateYYMMDD) < 0) {
//				// This should not happen, a datapoint in the future???
//				IndentWriter iwTmp = new IndentWriter();
//				iwTmp.println("### BUG ###: Found DPDay after current trading friday");
//				fi.dumpInfo(iwTmp);
//				iwTmp.push();
//				iwTmp.println("Input DPDays: " + printList(l));
//				iwTmp.println("Debug data so far\n" + iw.getString());
//				EE.getEE().dsevere(log, TAG, iwTmp.getString());
//			}
//		}
//
//		while (fridayStart.compareTo(fridayNow) <= 0) {
//			if (l.size() == 0 || fridayStart.compareTo(l.get(0).mDateYYMMDD) < 0) {
//				FLA_FundDPDay dpday = new FLA_FundDPDay();
//				dpday.mR1w = null;
//				dpday.mDateYYMMDD = fridayStart;
//				dpday.mDateYYMMDD_Orig = fridayStart;
//                iw.println("Dirty added at: " + dpday.mDateYYMMDD);
//				r.add(dpday);
//			} else {
//				r.add(l.remove(0));
//			}
//			fridayStart = MM.tgif_getNextFridayTodayExcl(fridayStart);
//		}
//
//		if (l.size() > 0) {
//			throw new Exception("Did not expect any more entries: " + printList(l));
//		}
//		l.clear();
//
//		if (r.get(r.size()-1).mR1w == null) {
//            iw.println("Removing r1w==null: " + r.get(r.size()-1).mDateYYMMDD + ", window started: " + fridayNow);
//			r.remove(r.size()-1);
//		}
//
//		l.addAll(r);
//		Collections.sort(l, FLA_FundDPDay.COMPARATOR_DATE_ADJUSTED);
//        iw.println("Result list: " + printList(l));
//        iw.pop();
//	}
}

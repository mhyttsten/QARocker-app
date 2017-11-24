package com.pf.fl.be.datamodel;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.pf.shared.OTuple2G;
import com.pf.shared.base64.Base64;
import com.pf.shared.Compresser;
import com.pf.fl.be.util.Constants;
import com.pf.fl.be.util.EE;
import com.pf.shared.MM;
import com.pf.shared.ExtractData;
import com.pf.shared.Pair;
import com.pf.fl.be.datamodel.FLA_Cache_FundInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static com.pf.fl.be.util.EE.ENCODING_FILE_WRITE;

public class GotoFirebase {
    private static final Logger log = Logger.getLogger(GotoFirebase.class.getSimpleName());
    private static final String TAG = GotoFirebase.class.getSimpleName();

    private static final String C = "~";

    public static EE mEE;
    public List<FLA_Cache_FundInfo> mFIs = new ArrayList<>();

    public int mExtract_Total = 0;
    public int mExtract_Done = 0;

    public String doIt() throws Exception {
        mEE = EE.getEE();
        mEE.dinfo(log, TAG, "GotoFirebase.doIt");
        String dateNowYYMMDD = MM.getNowAs_YYMMDD(null);
        mEE.dinfo(log, TAG, "Current date: " + dateNowYYMMDD);
        String dateLastFridayYYMMDD = MM.tgif_getLastFridayTodayIncl(dateNowYYMMDD);
        mEE.dinfo(log, TAG, "Last friday: " + dateLastFridayYYMMDD);
        String dateNextFridayYYMMDD = MM.tgif_getNextFridayTodayExcl(dateNowYYMMDD);
        mEE.dinfo(log, TAG, "Next friday: " + dateNextFridayYYMMDD);
        mFIs = FLA_Cache.getAllCachedFundInfos();
        log.info("Got: " + mFIs.size() + ", number of funds");

        // Get data to transfer to Firebase
        List<FLA_Cache_FundInfo> fundsInvalid = new ArrayList<>();
        List<FLA_Cache_FundInfo> fundsNotUpdated = new ArrayList<>();
        List<String> dates = getDateSequence(mFIs);
        StringBuffer mr = new StringBuffer();
        output_Header(mr, dates);
        ExtractData extractData = new ExtractData();
        int index = 0;
        for (FLA_Cache_FundInfo fi: mFIs) {
            mExtract_Total++;
            if (fi.mDateYYMMDD_Updated != null
                   && fi.mDateYYMMDD_Updated.compareTo(dateLastFridayYYMMDD) > 0) {
                mExtract_Done++;
            } else if (!fi.mIsValid) {
                extractData.urlsInvalid.add(new Pair<String, String>(fi.getTypeAndName(), fi.mURL));
                fundsInvalid.add(fi);
            } else {
                extractData.urlsNotUpdated.add(new Pair<String, String>(fi.getTypeAndName(), fi.mURL));
                fundsNotUpdated.add(fi);
            }
            output_FundInfo(dates, mr, fi, index);
            index++;
        }

        {
            String fnup = "Funds not updated, total: " + fundsNotUpdated.size() + "\n";
            for (int i=0; i < fundsNotUpdated.size(); i++) {
                FLA_Cache_FundInfo fci = fundsNotUpdated.get(i);
                fnup += "...[" + i + "]: " + fci.getTypeAndName() + ", " + fci.mURL + "\n";
            }
            log.info(fnup);
        }

        // Do the data
        String base = Constants.url_extractdata + "/" + dateLastFridayYYMMDD + ".json";
        {
            String s = "{"
                    + "\"date\": \"" + String.valueOf(dateLastFridayYYMMDD) + "\","
                    + "\"extract_total\":" + String.valueOf(mExtract_Total) + ","
                    + "\"extract_updated\":" + String.valueOf(mExtract_Done) + ","
                    + "\"invalid_count\":" + fundsInvalid.size() + ",";
            if (fundsInvalid.size() > 0) {
                s += "\"invalid_entries\": [";
                for (int i=0; i < fundsInvalid.size(); i++) {
                    FLA_Cache_FundInfo cfi = fundsInvalid.get(i);
                    s += "{" +
                            "\"name\": \"" + cfi.getTypeAndName() + "\"," +
                            "\"url\": \"" + cfi.mURL + "\"" +
                         "}";
                    if (i >= 15) {
                        s += ",{" +
                                "\"name\": \"<More, but not listing more here>\"," +
                                "\"url\": \"<More, but not listing more here>\"" +
                                "}";
                        break;
                    }
                    if (i+1 < fundsInvalid.size()) {
                        s += ",";
                    }
                }
                s += "],";
            }

            s += "\"not_updated_count\":" + fundsNotUpdated.size();
            if (fundsNotUpdated.size() > 0) {
                s += ",\"not_updated_entries\": [";
                for (int i=0; i < fundsNotUpdated.size(); i++) {
                    FLA_Cache_FundInfo cfi = fundsNotUpdated.get(i);
                    s += "{" +
                            "\"name\": \"" + cfi.getTypeAndName() + "\"," +
                            "\"url\": \"" + cfi.mURL + "\"" +
                            "}";
                    if (i >= 15) {
                        s += ",{" +
                                "\"name\": \"<More, but not listing more here>\"," +
                                "\"url\": \"<More, but not listing more here>\"" +
                                "}";
                        break;
                    }
                    if (i+1 < fundsNotUpdated.size()) {
                        s += ",";
                    }
                }
                s += "]";
            }
            s += "}";
            log.info("basedata string: " + s);
            StringBuffer strb = new StringBuffer();
            int rc = MM.urlRequest(strb, "PUT", base, s.getBytes(ENCODING_FILE_WRITE));
            log.info("Posted baseinfo, responseCode: " + rc);
            log.info("Return string: " + strb.toString());
        }

//        String base = Constants.url_extractdata + "/" + dateLastFridayYYMMDD;
//        {
//            String baseinfo = base + "/baseinfo.json";
//            String s = "{"
//                    + "\"extract_total\":" + "\"" + String.valueOf(mExtract_Total) + "\","
//                    + "\"extract_updated\":" + "\"" + String.valueOf(mExtract_Done) + "\","
//                    + "\"count_invalid\":" + "\"" + fundsInvalid.size() + "\","
//                    + "\"notupdated_count\":" + "\"" + fundsNotUpdated.size() + "\""
//                    + "}";
//            log.info("basedata string: " + s);
//            int rc = MM.urlRequest("PUT", baseinfo, s.getBytes(ENCODING_FILE_WRITE));
//            log.info("Posted baseinfo, responseCode: " + rc);
//        }
//        {
//            for (int i = 0; i < fundsInvalid.size(); i++) {
//                FLA_Cache_FundInfo fi = fundsInvalid.get(i);
//                String n = getFirebaseChildString(fi.getTypeAndName());
//                String data = "{ \"url\":" + "\"" + fi.mURL + "\"" + "}";
//                log.info("fundinvalid name: " + n + ", data: " + data);
//                int rc = MM.urlRequest("PUT",
//                        base + "/invalid/" + n + ".json",
//                        data.getBytes(ENCODING_FILE_WRITE));
//                log.info("Posted baseinfo, responseCode: " + rc);
//            }
//            StringBuffer strb = new StringBuffer();
//            strb.append("Funds not updated, size: " + fundsNotUpdated.size() + "\n");
//            for (int i = 0; i < fundsNotUpdated.size(); i++) {
//                FLA_Cache_FundInfo fi = fundsNotUpdated.get(i);
//                String n = getFirebaseChildString(fi.getTypeAndName());
//                String data = "{ \"url\":" + "\"" + fi.mURL + "\"" + "}";
//                int rc = MM.urlRequest("PUT",
//                        base + "/notupdated/" + n + ".json",
//                        data.getBytes(ENCODING_FILE_WRITE));
//                strb.append("[" + i + "]: " + n + ", rc: " + rc + "\n");
//            }
//            log.info(strb.toString());
//        }

        // Bulk Funddata
        {
            log.info("Raw bulk funddaata storage now being performed");
            String s = mr.toString();
            String file = storeCS(s);
            log.info("...Data stored to CS with file: " + file);
            log.info("...Raw bulk funddaata storage now being performed");
            log.info("...Retrieved string, length: " + s.length() + ", content: " + MM.getString(s, 500));
            byte[] ba = s.getBytes(ENCODING_FILE_WRITE);
            log.info("...Raw byte array: " + ba.length);
            ba = Compresser.dataCompress("Name", ba);
            log.info("...Raw compressed byte array: " + ba.length);
            s = Base64.encodeBase64String(ba);
            log.info("...Raw uuencoded string, size: " + s.length() + ", content: " + MM.getString(s, 500));
            s = "{ \"data\":" + "\"" + s + "\"" + "}";
            StringBuffer strb = new StringBuffer();
            int rc = MM.urlRequest(strb, "PUT", Constants.url_funddata, s.getBytes());
            log.info("...Posted, we are done, responseCode: " + rc);
            log.info("...Return string: " + strb.toString());

        }

        return mr.toString();
    }

    public void getLatestEarliestDates() throws Exception {
        String dateEarliest = null;
        String dateLatest = null;
        mEE.dinfo(log, TAG, "Now ");
        for (FLA_Cache_FundInfo fi: mFIs) {
            List<FLA_Cache_FundDPWeek> dpds = fi.getDPWeeks();
            for (FLA_Cache_FundDPWeek dpd: dpds) {
                if (dpd.mDateYYMMDD != null) {
                    if (dateEarliest == null) {
                        dateEarliest = dpd.mDateYYMMDD;
                    }
                    if (dateLatest == null) {
                        dateLatest = dpd.mDateYYMMDD;
                    }
                    if (dpd.mDateYYMMDD.compareTo(dateEarliest) < 0) {
                        dateEarliest = dpd.mDateYYMMDD;
                    }
                    if (dpd.mDateYYMMDD.compareTo(dateLatest) > 0) {
                        dateLatest = dpd.mDateYYMMDD;
                    }
                }
            }
        }
    }

    private static void output_Header(StringBuffer mr,
                                      List<String> dates) throws Exception {
        mr.append("Id");
        mr.append(C + "Type");
        mr.append(C + "Name");
        mr.append(C + "MSRating");
        mr.append(C + "PPMNumber");
        mr.append(C + "Category");
        mr.append(C + "Index");
        mr.append(C + "Currency");
        for (String s: dates) {
            mr.append(C + s);

        }
        mr.append("\n");
    }

    private static void output_FundInfo(
            List<String> dates,
            StringBuffer mr,
            FLA_Cache_FundInfo fi,
            int index) throws Exception {
        StringBuffer vl = new StringBuffer();

        mr.append(String.valueOf(fi.mId));
        vl.append(String.valueOf(fi.mId));
        mr.append(C + fi.mType);
        vl.append(fi.mType);
        mr.append(C + fi.mName);
        vl.append(fi.mName);
        mr.append(C + fi.mMSRating);  // -1 if none
        vl.append(fi.mMSRating);  // -1 if none
        mr.append(C + fi.mPPMNumber);  // -1 if none
        vl.append(fi.mPPMNumber);  // -1 if none

        if (fi.mCategory == null) {
            mr.append(C);
        } else {
            FLA_FundCategory fc = fi.mCategory.get();
            mr.append(C + fc.mKey_CategoryName);
            vl.append(fc.mKey_CategoryName);
        }
        if (fi.mIndexCompare == null) {
            mr.append(C);
        } else {
            FLA_FundIndex findex = fi.mIndexCompare.get();
            mr.append(C + findex.mKey_IndexName);
            vl.append(findex.mKey_IndexName);
        }
        if (fi.mCurrency == null) {
            mr.append(C);
        } else {
            FLA_Currency fc = fi.mCurrency.get();
            mr.append(C + fc.mKey_CurrencyName);
            vl.append(fc.mKey_CurrencyName);
        }
        List<FLA_Cache_FundDPWeek> dpds = fi.getDPWeeks();
        for (String d: dates) {
            mr.append(C);
            boolean found = false;
            int count = 0;
            for (FLA_Cache_FundDPWeek dpd : dpds) {
                if (d.equals(dpd.mDateYYMMDD)) {
                    if (index < 4 & count < 4) {
                        mEE.dinfo(log, TAG, ".....Fund: " + fi.getTypeAndName() + ", " + dpd.mDateYYMMDD + ":" + String.format("%.2f", dpd.mR1w));
                        count++;
                    }
                    Double dvalue = dpd.mR1w;
                    String ds = "";
                    if (d != null) {
                        ds = String.format("%.2f", dvalue);
                    }
                    mr.append(d + ":" + ds); // here()
                    vl.append(dpd.mDateYYMMDD);
                    found = true;
                }
            }
            if (!found) {
                mr.append(d + ":");
                vl.append("");
            }
        }

        String vlv = vl.toString();
        if (vlv.contains(C)) {
            throw new AssertionError("Fund: " + fi.getTypeAndName() + ", contained char " + C
                    + "\n..." + vlv);
        }

        mr.append("\n");
    }

    private static String getFirebaseChildString(String s) {
        s = s.replace('.', '|');
        s = s.replace('$', 'D');
        s = s.replace('[', '(');
        s = s.replace(']', ')');
        s = s.replace('#', 'H');
        s = s.replace(' ', '_');
        return s;
    }

    private static List<String> getDateSequence(List<FLA_Cache_FundInfo> l) throws Exception {
        // Find the earliest and latest dates
        // Validate all dates are non-null and fridays!
        String dateLatest = null;
        String dateEarliest = null;
        for (FLA_Cache_FundInfo f : l) {
            List<FLA_Cache_FundDPWeek> ws = f.getDPWeeks();
            for (FLA_Cache_FundDPWeek w : ws) {
                if (w.mDateYYMMDD == null) {
                    throw new AssertionError("Fund: " + f.getTypeAndName() + ", encountered a null date");
//                    mEE.dwarning(log, TAG, "Fund: " + f.getTypeAndName() + ", encountered a null date");
                } else if (!MM.tgif_isFriday(w.mDateYYMMDD)) {
                    throw new AssertionError("Fund: " + f.getTypeAndName() + ", encountered a non-Friday: " + w.mDateYYMMDD);
//                    mEE.dwarning(log, TAG, "Fund: " + f.getTypeAndName() + ", encountered a non-Friday: " + w.mDateYYMMDD);
                } else {
                    if (dateEarliest == null || dateEarliest.compareTo(w.mDateYYMMDD) > 0) {
                        dateEarliest = w.mDateYYMMDD;
                    }
                    if (dateLatest == null || dateLatest.compareTo(w.mDateYYMMDD) < 0) {
                        dateLatest = w.mDateYYMMDD;
                    }
                }
            }
        }

        // Construct the sequence of dates
        List<String> dates = new ArrayList<>();
        String cdate = dateEarliest;
        while (!cdate.equals(dateLatest)) {
            dates.add(cdate);
            cdate = MM.tgif_getNextFridayTodayExcl(cdate);
        }
        dates.add(dateLatest);
        Collections.reverse(dates);
        log.info("...date[0]: " + dates.get(0) + ", dates[last]: " + dates.get(dates.size()-1) + ", num elems: " + dates.size());
        return dates;
    }

    private static String storeCS(String data)  {

        Storage storage = null;
        storage = StorageOptions.getDefaultInstance().getService();

        String fname = "fund-info-" + MM.getNowAs_YYMMDDHHMMSS(null) + ".csv";
        try {
            Blob blob =
                    storage.create(
                            BlobInfo.newBuilder("mh-export", fname).build(),
                            data.getBytes(EE.ENCODING_FILE_WRITE));
        } catch(Exception exc) {
            throw new AssertionError(exc);
        }
        return fname;
    }

}



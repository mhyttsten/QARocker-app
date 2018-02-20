package com.pf.fl.be.datamodel;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;
import com.googlecode.objectify.cmd.Query;
import com.pf.fl.be.datastore.DS;
import com.pf.fl.be.util.EE;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.Embedded;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Entity
public class FLA_Cache {
    private static final Logger log = Logger.getLogger(FLA_Cache.class.getName());
    private static final String TAG = MM.getClassName(FLA_Cache.class.getName());

    public static final String KEY_CACHE = "CacheObject";

    @Id public String mKey = KEY_CACHE;
    @Unindex @Embedded public List<FLA_Cache_FundInfo> mFunds = new ArrayList<>();

    private static FLA_Cache mCache = null;

    public static synchronized void loadCache() throws Exception {
        if (mCache == null) {
            mCache = ofy().load().type(FLA_Cache.class).first().now();
            ofy().clear();
        }
    }

    /**
     *
     */
    public static synchronized void updateCacheAdd(FLA_FundInfo fi) throws Exception {
        FLA_Cache_FundInfo cfi = FLA_Cache_FundInfo.instantiate(fi);
        FLA_Cache cache = ofy().load().type(FLA_Cache.class).first().now();
        EE.getEE().dinfo(log, TAG, "Now adding: " + cfi.mType + "." + cfi.mName);
        cache.mFunds.add(cfi);
        Collections.sort(cache.mFunds, FLA_Cache_FundInfo.COMPARATOR);
        ofy().save().entity(cache).now();
        ofy().clear();
        mCache = cache;
        // mCache = null;
        // loadCache();
    }

    /**
     *
     */
    public static synchronized void updateCacheUpdate(FLA_FundInfo fi) throws Exception {
        FLA_Cache_FundInfo cfi = FLA_Cache_FundInfo.instantiate(fi);
        FLA_Cache cache = ofy().load().type(FLA_Cache.class).first().now();
        for (int i=0; i < cache.mFunds.size(); i++) {
            FLA_Cache_FundInfo elemCFI = cache.mFunds.get(i);
            if (elemCFI.mFundInfoId.longValue() == fi.mId.longValue()) {
                cache.mFunds.remove(i);
                cache.mFunds.add(cfi);
                Collections.sort(cache.mFunds, FLA_Cache_FundInfo.COMPARATOR);
                ofy().save().entity(cache).now();
                ofy().clear();
                mCache = cache;
                // mCache = null;
                // loadCache();
                return;
            }
        }
        ofy().clear();
        throw new Exception("Could not find for cache update: " + fi.mType + "." + fi.mName);
    }

    /**
     *
     */
    public static synchronized void updateCacheDelete(FLA_FundInfo fi) throws Exception {
        FLA_Cache_FundInfo cfi = FLA_Cache_FundInfo.instantiate(fi);
        FLA_Cache cache = ofy().load().type(FLA_Cache.class).first().now();
        for (int i=0; i < cache.mFunds.size(); i++) {
            FLA_Cache_FundInfo elemCFI = cache.mFunds.get(i);
            if (elemCFI.mFundInfoId.longValue() == fi.mId.longValue()) {
                cache.mFunds.remove(i);
                Collections.sort(cache.mFunds, FLA_Cache_FundInfo.COMPARATOR);
                ofy().save().entity(cache).now();
                mCache = cache;
                // mCache = null;
                // loadCache();
                ofy().clear();
                return;
            }
        }
        ofy().clear();
        throw new Exception("Could not find for cache delete: " + fi.mType + "." + fi.mName);
    }

    /**
     *
     */
    public static synchronized List<FLA_Cache_FundInfo> getCacheVersions(List<Ref<FLA_FundInfo>> fis) throws Exception {
        if (fis == null) {
            return new ArrayList<FLA_Cache_FundInfo>();
        }
        List<FLA_Cache_FundInfo> cfis = new ArrayList<>();
        for (int i=0; i < fis.size(); i++) {
            FLA_FundInfo fi = fis.get(i).get();
            FLA_Cache_FundInfo fci = FLA_Cache_FundInfo.instantiate(fi);
            cfis.add(fci);
        }
        cfis = cacheFundInfosImpl(cfis);
        return cfis;
    }

    /**
     *
     */
    public static synchronized FLA_Cache_FundInfo cachePPMFundByNumber(long ppmNumber) throws Exception {
        loadCache();
        for (FLA_Cache_FundInfo cfi : mCache.mFunds) {
            if (cfi.mType.equals(FLA_FundInfo.TYPE_PPM) && cfi.mPPMNumber == ppmNumber) {
                return cfi;
            }
        }
        return null;
    }

    /**
     *
     */
    public static synchronized List<FLA_Cache_FundInfo> cacheFundInfosByIndexAndType(String type, String indexName) throws Exception {
        loadCache();
        List<FLA_Cache_FundInfo> result = new ArrayList<>();
        for (FLA_Cache_FundInfo cfi : mCache.mFunds) {
            if (type.equals(cfi.mType)
                    && cfi.mIndexCompare != null) {
                FLA_FundIndex index = cfi.mIndexCompare.get();
                if (index != null
                        && index.mKey_IndexName != null
                        && indexName.equals(index.mKey_IndexName)) {
                    result.add(cfi);
                }
            }
        }
        return result;
    }

    /**
     *
     */
    public static synchronized List<FLA_Cache_FundInfo> cacheFundInfoById(String argIdStr) throws Exception {
        loadCache();

        long argId = Long.parseLong(argIdStr);
        List<FLA_Cache_FundInfo> r = new ArrayList<>();
        for (FLA_Cache_FundInfo fci : mCache.mFunds) {
            if (fci.mFundInfoId.longValue() == argId) {
                r.add(fci);
                break;
            }
        }
        return r;
    }

    /**
     *
     */
    public static synchronized List<FLA_Cache_FundInfo> cacheFundInfosByTypeOrNull(String type) throws Exception {
        loadCache();
        EE ee = EE.getEE();
        // ee.dinfo(log, TAG, "Will now load FLA_Cache, type is: " + type);
        // ee.dinfo(log, TAG, "...finished retrieving, total number of funds: " + mCache.mFunds.size());

        List<FLA_Cache_FundInfo> r = new ArrayList<>();
        for (FLA_Cache_FundInfo fci : mCache.mFunds) {
            if (type != null && type.equals(FLA_FundInfo.TYPE_INVALID) && !fci.mIsValid) {
                r.add(fci);
            }
            else if (type == null || type.equals(FLA_FundInfo.TYPE_ALL) || fci.mType.equals(type)) {
                r.add(fci);
            }
        }
        // ee.dinfo(log, TAG, "...type was " + type + ", size to return is: " + r.size());

        r = cacheFundInfosImpl(r);


        return r;
    }

    /**
     *
     */
    public static synchronized List<FLA_Cache_FundInfo> cacheFundInfosByIndex(String index) throws Exception {
        loadCache();
        EE ee = EE.getEE();
        ee.dinfo(log, TAG, "Will now load FLA_Cache, index is: " + index);
        List<FLA_Cache_FundInfo> cfis = new ArrayList<>();
        for (FLA_Cache_FundInfo fi : mCache.mFunds) {
            if (fi.mIndexCompare == null && index.equals("-")) {
                cfis.add(fi);
            } else if (fi.mIndexCompare != null) {
                FLA_FundIndex findex = fi.mIndexCompare.get();
                if (index.equals(findex.mKey_IndexName)) {
                    cfis.add(fi);
                }
            }
        }
        ee.dinfo(log, TAG, "...finished retrieving, total number of funds: " + cfis.size());
        List<FLA_Cache_FundInfo> r = cacheFundInfosImpl(cfis);
        return r;
    }

    private static synchronized List<FLA_Cache_FundInfo> cacheFundInfosImpl(List<FLA_Cache_FundInfo> r) throws Exception {
        EE ee = EE.getEE();

        Collections.sort(r, FLA_Cache_FundInfo.COMPARATOR);

        List<FLA_Cache_FundInfo> others = new ArrayList<>();
        List<FLA_Cache_FundInfo> sebStartSEB = new ArrayList<>();
        List<FLA_Cache_FundInfo> sebNotStartSEB = new ArrayList<>();
        List<FLA_Cache_FundInfo> spp = new ArrayList<>();
        for (FLA_Cache_FundInfo elem: r) {
            if (elem.mType.equals(FLA_FundInfo.TYPE_SEB)) {
                if (elem.mName.startsWith("SEB")) {
                    sebStartSEB.add(elem);
                } else {
                    sebNotStartSEB.add(elem);
                }
            } else if (elem.mType.equals(FLA_FundInfo.TYPE_SPP)) {
                spp.add(elem);
            } else {
                others.add(elem);
            }
        }
        Collections.sort(others, FLA_Cache_FundInfo.COMPARATOR);
        Collections.sort(spp, FLA_Cache_FundInfo.COMPARATOR);
        Collections.sort(sebStartSEB, FLA_Cache_FundInfo.COMPARATOR);
        Collections.sort(sebNotStartSEB, FLA_Cache_FundInfo.COMPARATOR);
        r.clear();
        r.addAll(sebStartSEB);
        r.addAll(sebNotStartSEB);
        r.addAll(spp);
        r.addAll(others);
        return r;
    }

    /**
     *
     */
    public static String updateCacheStatus() {
        return updateCacheStatus_Str;
    }
    private static String updateCacheStatus_Str;

    /**
     *
     */
    public static List<FLA_FundInfo> getAllFundInfos() throws Exception{
        EE ee = EE.getEE();
        int statsCount = 0;
        long statsStartTime = System.currentTimeMillis();

        ee.dinfo(log, TAG, "Now querying all FundInfo instances");
        int count = 0;
        HashMap<String, FLA_FundInfo> dupHM = new HashMap<>();

        boolean foundAnything = false;
        int foundCount = 0;
        List<FLA_FundInfo> fundInfoList = new ArrayList<>();
        do {
            foundAnything = false;
            // mEE.dinfo(log, TAG, "...Doing another round, we've fetched: " + count + " so far");
            Query<FLA_FundInfo> query = ofy()
                    .load()
                    .type(FLA_FundInfo.class)
                    .offset(foundCount)
                    .limit(50);
//            QueryResultIterator<FLA_FundInfo> qri = query.iterator();
//            List<FLA_FundInfo> qri = query.iterator();
            List<FLA_FundInfo> qri = query.list();
            for (FLA_FundInfo fi: qri) {
//            while (qri.hasNext()) {
                foundCount++;
                count++;
//                FLA_FundInfo fi = qri.next();
                fundInfoList.add(fi);
                foundAnything = true;

                // Report any duplicates detected
                String key = fi.mType + "." + fi.mName;
                if (dupHM.containsKey(key)) {
                    FLA_FundInfo fiDup = dupHM.get(key);
                    ee.dsevere(log, TAG, "Found duplicate key: " + key + ", iterKey: " + fi.mId + ", prevIterKey: " + fiDup.mId);
                }
                dupHM.put(key, fi);

                statsCount++;
                int durationInS = (int) ((System.currentTimeMillis() - statsStartTime) / 1000);
                updateCacheStatus_Str = "Processed: " + count + ", Duration: " + durationInS + "s, Last: " + fi.mType + "." + fi.mName;
                if ((count % 50) == 0) {
                    ofy().clear();
                }
                if ((count % 100) == 0) {
                    // ee.dinfo(log, TAG, "...updated " + count + " entries, last was: " + cfi.mType + "." + cfi.mName + ": " + cfi.mDPWs);
                }
            }
            ofy().clear();
        } while (foundAnything);
        return fundInfoList;
    }

    /**
     *
     */
    public static void updateCache() throws Exception {
        EE ee = EE.getEE();

        List<FLA_Cache_FundInfo> r = new ArrayList<>();
        List<FLA_FundInfo> fis = getAllFundInfos();
        for (FLA_FundInfo fi: fis) {
            FLA_Cache_FundInfo fci = FLA_Cache_FundInfo.instantiate(fi);
            r.add(fci);
        }

        Collections.sort(r, FLA_Cache_FundInfo.COMPARATOR);

        // log.info("Now updating maintenance object, # fund entries: " + r.size());
        FLA_Cache cache = ofy().load().type(FLA_Cache.class).first().now();
        if (cache == null) {
            cache = new FLA_Cache();
        }

        synchronized (FLA_Cache.class) {
            cache.mFunds = r;
            ofy().save().entity(cache).now();
            // log.info("Maintenance object updated");
            ofy().clear();
            mCache = cache;
            // mCache = null;
            // MM.sleepInMS(10000);
            // loadCache();
        }

        ee.dinfo(log, TAG, "FLA_Cache object updated, number of fund entries: " + r.size());
    }

    /**
     *
     */
    public static List<FLA_Cache_FundInfo> getAllCachedFundInfos() throws Exception{
        loadCache();
        return mCache.mFunds;
    }

    /**
     *
     */
    private static boolean isDataEmpty(FLA_Cache_FundInfo cfi) throws Exception {
        List<FLA_Cache_FundDPWeek> dpWeeks = cfi.getDPWeeks();
        int emptyWeeksBeforeEmpty = 4;
        int count = 0;
        for (int i=0; i < dpWeeks.size(); i++) {
            FLA_Cache_FundDPWeek dpWeek = dpWeeks.get(i);
            if (dpWeek.mR1w != null) {
                return false;
            }
            count++;
            if (count >= emptyWeeksBeforeEmpty) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     */
    private static FLA_Cache_FundInfo getCFI(String typeAndName) {
        for (FLA_Cache_FundInfo cfi: mCache.mFunds) {
            if (typeAndName.equals(cfi.getTypeAndName())) {
                return cfi;
            }
        }
        return null;
    }

    /**
     *
     */
    private static String printListStr(IndentWriter iw, String header, List<FLA_Cache_FundInfo> l) throws Exception {
        if (iw == null) {
            iw = new IndentWriter();
        }
        if (l == null) {
            iw.println(header);
            return iw.getString();

        }
        iw.println(header + ". Count: " + l.size() + ".");
        iw.push();
        if (l != null) {
            for (int i = 0; i < l.size(); i++) {
                FLA_Cache_FundInfo fi = l.get(i);
                iw.print("[" + i + "]: " + fi.mType + "." + fi.mName
                        + ", upd: " + fi.mDateYYMMDD_Updated
                        + ", attempted: " + fi.mDateYYMMDD_Update_Attempted);
                if (!fi.mIsValid) {
                    iw.println(", ic: " + fi.mInvalidCode);
                } else {
                    iw.println(", valid");
                }
                iw.push();
                iw.println(fi.mURL);
                iw.println("id: " + fi.mId);
                List<FLA_Cache_FundDPWeek> fiDPWs = fi.getDPWeeks();
                iw.print("DPWs: " + fiDPWs.size());
                for (int j = 0; j < fiDPWs.size() && j < 6; j++) {
                    iw.print(", " + fiDPWs.get(j).mDateYYMMDD + ":" + fiDPWs.get(j).mR1w);
                }
                iw.println();
                iw.pop();
            }
        }
        iw.pop();
        return iw.getString();
    }

    /**
     *
     */
    public static void performMaintenance() throws Exception {
        EE ee = EE.getEE();

        ee.dinfo(log, TAG, "Will now update cache");
        updateCache();
        ee.dinfo(log, TAG, "...cache has been updated");

        // Log warning for funds with no DP for a long time
        ee.dinfo(log, TAG, "Checking for funds with no DP for a long time");
        List<FLA_Cache_FundInfo> cfis = mCache.mFunds; // Sorted in descending order (newest == first)
        List<FLA_Cache_FundInfo> cfiNoDPs = new ArrayList<>();
        for (int i=0; i < cfis.size(); i++) {
            FLA_Cache_FundInfo cfi = cfis.get(i);
            if (cfi.mIsValid && isDataEmpty(cfi)) {
                cfiNoDPs.add(cfi);
                FLA_FundInfo fi = DS.getFundInfo(Key.create(FLA_FundInfo.class, cfi.mId));
                if (fi == null) {
                    throw new Exception("No fund found: " + cfi.mId + ", " + cfi.mType + "." + cfi.mName);
                }
                fi.mIsValid = false;
                fi.mInvalidCode = FLA_FundInfo.IC_DATA_NO_R1W;
                ofy().save().entity(fi).now();
                IndentWriter iwTmp = new IndentWriter();
                fi.dumpInfo(iwTmp);
                ee.dwarning(log, TAG, "No data for fund set to invalid: " + fi.mType + "." + fi.mName + "\n" + iwTmp.getString());
            }
        }
        ee.dinfo(log, TAG, "...done: Checking for funds with no DP for a long time");
        if (cfiNoDPs.size() > 0) {
            ee.dinfo(log, TAG, "cfiNoDPs.size() > 0, updating cache");
            ee.dwarning(log, TAG, printListStr(null, "Valid funds with no data points", cfiNoDPs));
            updateCache();
            ee.dinfo(log, TAG, "...done: cfiNoDPs.size() > 0, updating cache");
        }

        // Log warning for all invalid funds
        ee.dinfo(log, TAG, "Logging warnings for all invalid funds");
        cfis = mCache.mFunds;
        List<FLA_Cache_FundInfo> cfiInvalids = new ArrayList<>();
        for (int i=0; i < cfis.size(); i++) {
            FLA_Cache_FundInfo cfi = cfis.get(i);
            if (!cfi.mIsValid) {
                cfiInvalids.add(cfi);
            }
        }
        if (cfiInvalids.size() > 0) {
            IndentWriter iw = new IndentWriter();
            iw.setFlowChar('.');
            ee.dwarning(log, TAG, printListStr(null, "Invalid fund list", cfiInvalids));
        } else {
            ee.dinfo(log, TAG, "No invalid funds found");
        }
        ee.dinfo(log, TAG, "...done: Logging warnings for all invalid funds");

        // Print funds that have not been updated yet
        ee.dinfo(log, TAG, "Printing funds that have not been updated");
        String nowYYMMDD = MM.getNowAs_YYMMDD(EE.TIMEZONE_STOCKHOLM);
        String nowHHMMSS = MM.getNowAs_HHMMSS(EE.TIMEZONE_STOCKHOLM);
        int hour = Integer.parseInt(nowHHMMSS.substring(0, 2));
        String fridayLastYYMMDD = MM.tgif_getLastFridayTodayExcl(nowYYMMDD);
        int dayOfWeek = MM.tgif_getDayOfWeek(nowYYMMDD);
        if (dayOfWeek == Calendar.FRIDAY) {
            ee.dinfo(log, TAG, "Funds not updated: We don't report this on Fridays");
        } else if (dayOfWeek == Calendar.SATURDAY && hour < 9) {
            ee.dinfo(log, TAG, "Funds not updated: We don't report this on Saturdays before 09:00");
        } else {
            cfis = getAllCachedFundInfos();
            List<FLA_Cache_FundInfo> cfisNotUpdated = new ArrayList<>();
            for (FLA_Cache_FundInfo cfi : cfis) {
                List<FLA_Cache_FundDPWeek> dpw = cfi.getDPWeeks();
//                ee.dinfo(log, TAG, "For fund: " + cfi.getTypeAndName());
//                ee.dinfo(log, TAG, "...lastFriday: " + fridayLastYYMMDD);
//                ee.dinfo(log, TAG, "...dpw[0]: " + dpw.get(0).mDateYYMMDD + ":" + dpw.get(0).mR1w);
//                ee.dinfo(log, TAG, "...dpw[last]: " + dpw.get(dpw.size()-1).mDateYYMMDD + ":" + dpw.get(dpw.size()-1).mR1w);
                if (dpw.size() <= 0 || dpw.get(0).mDateYYMMDD == null) {
                    cfisNotUpdated.add(cfi);
                } else if (cfi.mIsValid) {
                    FLA_Cache_FundDPWeek cfw = dpw.get(0);
                    if (!cfw.mDateYYMMDD.equals(fridayLastYYMMDD)) {
                        cfisNotUpdated.add(cfi);
                    }
                }
            }
            ofy().clear();
            if (cfisNotUpdated.size() > 0) {
                ee.dwarning(log, TAG, printListStr(null, "Funds not updated", cfisNotUpdated));
            }
            else {
                ee.dinfo(log, TAG, printListStr(null, "All funds updated!", null));
            }
        }
        ee.dinfo(log, TAG, "...done: Printing funds that have not been updated");

        // Check that all portfolios have valid funds
//        Iterator<FLA_FundPortfolio> iterPortfolios = ofy()
//                .load()
//                .type(FLA_FundPortfolio.class)
//                .iterator();
//        boolean didRemoveMaster = false;
//        while (iterPortfolios.hasNext()) {
//            FLA_FundPortfolio fp = iterPortfolios.next();
//            List<Ref<FLA_FundInfo>> rfis = fp.mFunds;
//
//            int count = 0;
//            boolean didRemove = false;
//            while (count < rfis.size()) {
//                Ref<FLA_FundInfo> rfi = rfis.get(count);
//                FLA_FundInfo fi = rfi.get();
//                if (fi == null) {
//                    ee.dwarning(log, TAG, "Will delete null fund in portfolio type: " + fp.mType + ", name: " + fp.mName + ", fundKey: " + rfi.getKey().getId());
//                    rfis.remove(count);
//                    didRemove = true;
//                    didRemoveMaster = true;
//                } else {
//                    count++;
//                }
//            }
//            if (didRemove) {
//                ee.dwarning(log, TAG, "Saved portfolio again, type: " + fp.mType + ", name: " + fp.mName);
//                ofy().save().entity(fp).now();
//            }
//        }
//        if (!didRemoveMaster) {
//            ee.dinfo(log, TAG, "Portfolios: No dangling fund pointers found");
//
//        }

        // Send a mail
//        try {
//            Properties props = new Properties();
//            Session session = Session.getDefaultInstance(props, null);            Message msg = new MimeMessage(session);
//            msg.setFrom(new InternetAddress("nobody@example.com", "Sender not important"));
//            msg.addRecipient(Message.RecipientType.TO,
//                    new InternetAddress("magnus.hyttsten@gmail.com", "Magnus Hyttsten"));
//            msg.setSubject("Report for: " + new java.util.Date().toString());
//            msg.setText("Hello, how are you<b>I am doing fine</b><br>" +
//                    "<a href=\"http://www.google.com\">Go to Google</a>");
//            Transport.send(msg);
//
//        } catch (AddressException e) {
//            // ...
//        } catch (MessagingException e) {
//            // ...
//        }
    }


}

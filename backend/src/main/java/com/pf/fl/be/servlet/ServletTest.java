package com.pf.fl.be.servlet;

import com.googlecode.objectify.cmd.Query;
import com.pf.fl.be.datamodel.FLA_Cache;
import com.pf.fl.be.datamodel.FLA_Cache_FundInfo;
import com.pf.fl.be.datamodel.FLA_FundInfo;
import com.pf.fl.be.datamodel.D_FundInfo_From_FLA_FundInfo_Serializer;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.fl.be.extract.FLOps1_Ext1_Extract_New;
import com.pf.fl.be.datamodel_raw.FL_MSExtractDetails;
import com.pf.fl.be.datamodel_raw.REFundInfo;
import com.pf.fl.be.datastore.DS;
import com.pf.fl.be.extract.FLOps1_Ext1_Extract_SingleFund;
import com.pf.fl.be.extract.FLOps1_Ext1_HTMLGet;
import com.pf.fl.be.util.Constants;
import com.pf.fl.be.util.EE;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;
import com.pf.shared.datamodel.D_FundInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import com.pf.fl.be.FLOps1_Ext1_Extract;

import static com.googlecode.objectify.ObjectifyService.ofy;

public class ServletTest extends HttpServlet {
    private static final Logger log = Logger.getLogger(ServletTest.class.getSimpleName());
    private static final String TAG = ServletTest.class.getSimpleName();

    private EE mEE;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        EE ee = EE.getEE();
        mEE = EE.getEE();
        try {
            ee.dinfo(log, TAG, "Now executing servletTest");
            doItAll();
            // cvsRead();
            // cvsGenerate();
            // fixDPYear();
            // collectAVanguard(req, resp);
            // deleteDuplicates(req, resp);
        } catch(IOException exc) {
            throw exc;
        } catch(Exception exc) {
            exc.printStackTrace();
            String stackTrace = MM.getStackTraceString(exc);
            System.out.println("Exception: " + exc);
            System.out.println("Stack trace:\n" + stackTrace);
            ee.dsevere(log, TAG, "ERROR, STACK TRACE:\n" + stackTrace);
            throw new IOException(exc.toString());
        }
    }

    public void doItAll() throws Exception {
        EE ee = EE.getEE();

        List<D_FundInfo> funds = null;

        ee.dinfo(log, TAG, "Now reading FLA_FundInfos");
        List<FLA_FundInfo> fundsOld = get_FLA_FundInfos();
        ee.dinfo(log, TAG, "...Done reading FLA_FundInfos");

        ee.dinfo(log, TAG, "Now converting FLA_FundInfos");
        funds = convert_FLA_FundInfos(fundsOld);
        ee.dinfo(log, TAG, "...Done converting FLA_FundInfos");

        String lFriday = MM.tgif_getLastFridayTodayExcl(MM.getNowAs_YYMMDD(null));
        String llFriday = MM.tgif_getLastFridayTodayExcl(lFriday);
        String llSaturday = MM.tgif_getNextWeekday(llFriday, Calendar.SATURDAY);
        int count = 0;
        for (D_FundInfo fi : funds) {
            fi._dateYYMMDD_Updated = llSaturday;
            fi._dateYYMMDD_Update_Attempted = llSaturday;

            D_FundDPDay dpd = fi._dpDays.remove(0);
            if (!dpd._dateYYMMDD.equals(lFriday)) {
                ee.dsevere(log, TAG, "Count: " + count + ". Expected: " + lFriday + ", got: " + dpd._dateYYMMDD + ", fund: " + fi.getTypeAndName());
                return;
            }
            count++;
        }
        ee.dinfo(log, TAG, "Successfully removed last Friday DPD");

        ee.dinfo(log, TAG, "Now writing BIN file");
        FLOps1_Ext1_Extract_New.saveFundList(funds);
        ee.dinfo(log, TAG, "...Done writing BIN file");

        ee.dinfo(log, TAG, "Now reading BIN file");
        funds = FLOps1_Ext1_Extract_New.readFundList();
        ee.dinfo(log, TAG, "...Done reading BIN file, number of entries: " + funds.size());

// Check that null was managed correctly
//        FLA_FundInfo fiOld = null;
//        for (FLA_FundInfo fiOldIter: fundsOld) {
//            fiOld = fiOldIter;
//            for (FLA_FundDPDay dpdO: fiOld.mDPDays) {
//                if (dpdO.mR1w == null) {
//                    break;
//                }
//            }
//        }
//        if (fiOld == null) {
//            ee.dsevere(log, TAG, "Could not find a single NULL day");
//        } else {
//            for (D_FundInfo fi: funds) {
//                if (fi.getTypeAndName().equals(fiOld.getTypeAndName())) {
//                    ee.dinfo(log, TAG, "Here is one with NULL\n" + fi.toString());
//                }
//            }
//        }

        D_FundInfo fi1 = funds.get(0);
        D_FundInfo fi2 = funds.get(funds.size()-1);
        IndentWriter iw = new IndentWriter();
        fi1.dumpInfo(iw);
        fi2.dumpInfo(iw);
        ee.dinfo(log, TAG, "Funds\n" + iw.getString());

        ee.dinfo(log, TAG, "Returning successfully");
    }

    public List<FLA_FundInfo> get_FLA_FundInfos() throws Exception {
        EE ee = EE.getEE();

        ee.dinfo(log, TAG, "Now querying all FundInfo instances");
        int count = 0;
        HashMap<String, FLA_FundInfo> dupHM = new HashMap<>();

        boolean foundAnything = false;
        int foundCount = 0;
        List<FLA_FundInfo> fis = new ArrayList<>();
        do {
            foundAnything = false;
            mEE.dinfo(log, TAG, "...Doing another round, we've fetched: " + count + " so far");
            Query<FLA_FundInfo> query = ofy()
                    .load()
                    .type(FLA_FundInfo.class)
                    .offset(foundCount)
                    .limit(50);
            List<FLA_FundInfo> qri = query.list();
            for (FLA_FundInfo fiOld: qri) {
                foundCount++;
                count++;
                foundAnything = true;

                // Report any duplicates detected
                String key = fiOld.mType + "." + fiOld.mName;
                if (dupHM.containsKey(key)) {
                    FLA_FundInfo fiDup = dupHM.get(key);
                    ee.dsevere(log, TAG, "Found duplicate key: " + key + ", iterKey: " + fiOld.mId + ", prevIterKey: " + fiDup.mId);
                }
                dupHM.put(key, fiOld);

                fis.add(fiOld);
//                if (true) {
//                    return fis;
//                }

                if ((count % 50) == 0) {
                    ofy().clear();
                }
            }
            ofy().clear();
        } while (foundAnything);
        return fis;
    }

    public List<D_FundInfo> convert_FLA_FundInfos(List<FLA_FundInfo> l) throws Exception {
        List<D_FundInfo> fis = new ArrayList<>();
        for (FLA_FundInfo fiOld: l) {
            D_FundInfo fi = D_FundInfo_From_FLA_FundInfo_Serializer.convertTo_D_FundInfo(fiOld);
            fis.add(fi);
        }
        return fis;
    }

//    public void testFireStore(HttpServletRequest req, HttpServletResponse resp) throws Exception {
//        mEE = EE.getEE();
//
//        // Use the application default credentials
//        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredentials(credentials)
//                .setProjectId("pffundlifter")
//                .build();
//        FirebaseApp.initializeApp(options);
//
//        Firestore db = FirestoreClient.getFirestore();
//
//        DocumentReference docRef = null;
//        Map<String, Object> data = null;
//        ApiFuture<WriteResult> result = null;
//
//        // Add document with id alovelace to the users collection
//        docRef = db.collection("users").document("alovelace");
//        docRef = db.document("users/alovelace");
//        data = new HashMap<>();
//        data.put("first", "Ada");
//        data.put("last", "Lovelace");
//        data.put("born", 1815);
//        result = docRef.set(data);  // Asynchronous write
//        System.out.println("Update time : " + result.get().getUpdateTime());  // get() blocks
//
//        // Add document with id aturing to the users collection
//        docRef = db.collection("users").document("aturing");
//        docRef = db.document("users/aturing");
//        data = new HashMap<>();
//        data.put("first", "Alan");
//        data.put("middle", "Mathison");
//        data.put("last", "Turing");
//        data.put("born", 1912);
//        result = docRef.set(data);  // Asynchronous write
//        System.out.println("Update time : " + result.get().getUpdateTime());  // get() blocks
//
//        // When doing set, any previous content will be deleted
//        // You can use merge to merge the two datasets
//        //        ApiFuture<WriteResult> writeResult =
//        //                db
//        //                        .collection("cities")
//        //                        .document("BJ")
//        //                        .set(update, SetOptions.merge());
//
//
//        CollectionReference cref = db.collection("users");
//        ApiFuture<QuerySnapshot> query = cref.get();  // Asynch
//        QuerySnapshot querySnapshot = query.get();  // get() blocks
//        List<DocumentSnapshot> documents = querySnapshot.getDocuments();
//        for (DocumentSnapshot document : documents) {
//            System.out.println("User: " + document.getId());
//            System.out.println("First: " + document.getString("first"));
//            if (document.contains("middle")) {
//                System.out.println("Middle: " + document.getString("middle"));
//            }
//            System.out.println("Last: " + document.getString("last"));
//            System.out.println("Born: " + document.getLong("born"));
//        }
//
//        // Subcollections
//        // Notice the alternating pattern of collections and documents.
//        // Your collections and documents must always follow this pattern.
//        // You cannot reference a collection in a collection or a document in a document.
//        // You can nest data up to 100 levels deep.
//        // WARNING: When you delete a document that has associated subcollections,
//        // the subcollections are not deleted. They are still accessible by reference.
//        // For example, there may be a document referenced by
//        // db.collection('coll').doc('doc').collection('subcoll').doc('subdoc') even though
//        // the document referenced by db.collection('coll').doc('doc') no longer exists.
//        // If you want to delete documents in subcollections when deleting a document,
//        // you must do so manually, as shown in Delete Collections.
//        DocumentReference document =
//                db.collection("rooms")
//                        .document("roomA")
//                        .collection("messages")
//                        .document("message1");
//
//        // ArrayList within a document: List is enclosed in doc (cant be queried & contributes to dod size
//        // Subcollection within document: Can be queried separately and does not contribute to size
//        // Rootcollection: Maximum query flexibility & scalability
//
//        // Data type: Reference references another doc
//
//
//
//    }

    public void deleteDuplicates(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        mEE = EE.getEE();

        long[] ids = new long[] { };
//                5762767671787520L,
//                5726828727631872L,
//                5705162228236288L,
//                5163878774210560L,
//                5133118755307520L,
//                4794601982394368L };

        for (long id: ids) {
            FLA_FundInfo fi = DS.getFundInfoById(id);
            if (fi == null) {
                resp.getWriter().println("No fund for id: " + id);
            } else {
                DS.deleteFundInfo(fi);
                resp.getWriter().println(fi.getTypeAndName() + " DELETED");
            }
        }
    }

    public void collectAVanguard(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        mEE = EE.getEE();

        // Test
        log.info("Log test: info");
        //log.warning("Log test: warning");
        //log.severe("Log test: severe");
        System.out.println("Log test: System.out");
        resp.getOutputStream().write((ServletTest.class.getName()
                + ", v5 executed at: " + new java.util.Date().toString()).getBytes());
        log.info("That was it from ServletTest");

        List<FLA_Cache_FundInfo> l = FLA_Cache.cacheFundInfosByTypeOrNull(Constants.ACCOUNT_TYPE_VANGUARD);
        log.info("Total # funds: " + l.size());
        for (FLA_Cache_FundInfo cfi: l) {
            if (!cfi.mName.toLowerCase().contains("etf")) {
                continue;
            }
//            if (!cfi.mName.contains("Vanguard Mega Cap Value Index Fund ETF Shares")) {
//                continue;
//            }
            FLA_FundInfo fi = DS.getFundInfoById(cfi.mId);

            // Collect the data
            FLOps1_Ext1_Extract_SingleFund esf = new FLOps1_Ext1_Extract_SingleFund();
            esf.extractFund(fi, true);
            if (esf.mError) {
                log.warning("ERROR WHILE EXTRACTING FUND: " + fi.getTypeAndName() + "\n"
                    + esf.mIWE.getString());
            } else {
                IndentWriter iw = new IndentWriter();
                fi.dumpInfo(iw);
                log.info("EXTRACTED SUCCESSFULLY: " + fi.getTypeAndName() + "\n" + iw.getString());
            }
//            break;
        }


//        checkURL();
    }



    public void checkURL() {
        try {
            checkURLImpl();
        } catch(Exception exc) {
            System.out.println("Exception: " + exc.getMessage());
            System.out.println(MM.getStackTraceString(exc));
        }
    }
    private void checkURLImpl() throws Exception {
        IndentWriter iw = new IndentWriter();

        String url = "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000I3LA&programid=0000000000";
        byte[] pageDataUncompressed = FLOps1_Ext1_HTMLGet.htmlGet(
                mEE,
                iw,
                url,
                5000,
                6);

        String htmlString = MM.newString(pageDataUncompressed, EE.ENCODING_FILE_READ);
        OTuple2G<Integer, REFundInfo> reFundInfo = FL_MSExtractDetails.extractFundDetails(
                Constants.ACCOUNT_TYPE_SEB,
                url,
                htmlString,
                iw);
        iw = new IndentWriter();
        reFundInfo._o2.addString(iw);
        System.out.println(iw.getString());
    }

//    private void checkURLImpl() throws Exception {
//        IndentWriter iw = new IndentWriter();
//
//        String url = "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000I3LA&programid=0000000000";
//
//        byte[] pageContent = MM.getURLContentBA(url);
//        // MM.fileWrite("pageContent.html", pageContent);
//        String htmlString = MM.newString(pageContent, EE.ENCODING_FILE_READ);
//        REFundInfo reFundInfo = FL_MSExtractDetails.extractFundDetails(
//                Constants.ACCOUNT_TYPE_SEB,
//                url,
//                htmlString,
//                iw);
//        iw = new IndentWriter();
//        reFundInfo.addString(iw);
//        System.out.println("**** RESULT IS:\n" + iw.getString());
//    }



}

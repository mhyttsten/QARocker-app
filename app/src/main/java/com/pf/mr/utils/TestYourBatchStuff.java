package com.pf.mr.utils;

import android.os.AsyncTask;

import com.pf.shared.extract.ExtractFromHTML_Helper;

import java.util.logging.Logger;

public class TestYourBatchStuff {
    private static final Logger log = Logger.getLogger(TestYourBatchStuff.class.getName());

    public static void testYourBatchStuff() {
        try {
            testYourBatchStuffImpl();
        } catch(Exception exc) {
            log.severe("Exception, exc: " + exc);
        }
    }

    public static void testYourBatchStuffImpl() {
        Misc.getFirebaseStorageFile();

    }

//        MM.testLog();
//        Log.w(TAG, MM.stripHTMLComments("hello  <!--how are you-->This is not <!--complicated--> at all"));
//        Log.w(TAG, MM.stripHTMLComments("hello  <!--how are you-->This is not<!--complicated-->" + "]"));
//        Misc.getFirebaseStorageFile();
//        Misc.extractFundInfo();

//        Log.e(TAG, "Will now try GCS API");
//        try {
//            List<Blob> bs = D_BEDB.gcsGetBlobsInAscendingOrder(com.pf.shared.Constants.PREFIX_FUNDINFO_DB);
//            for (Blob b: bs) {
//                Log.e(TAG, "Found blob: " + b.getBucket() + "." + b.getName());
//            }
//        } catch (IOException exc) {
//            Log.e(TAG, "Caught exception: " + exc.getLocalizedMessage());
//        }




}

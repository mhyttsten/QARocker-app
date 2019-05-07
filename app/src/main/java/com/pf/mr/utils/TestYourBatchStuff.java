package com.pf.mr.utils;

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
//        Misc.getFirebaseStorageFile();
    }
}

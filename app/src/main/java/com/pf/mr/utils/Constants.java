package com.pf.mr.utils;

/**
 * Created by magnushyttsten on 3/27/16.
 */
public class Constants {
    public static final String APP_ID = "com.pf.mr";

    public static final String FPATH_BASE        = "https://brilliant-fire-9867.firebaseio.com/";
    public static final String FPATH_SETS        = "https://brilliant-fire-9867.firebaseio.com/sets";
    public static final String FPATH_TOKEN2EMAIL = "https://brilliant-fire-9867.firebaseio.com/token2email";

    public static final String FPATH_STATFORUSER = "https://brilliant-fire-9867.firebaseio.com/statforuser";
    public static final String FPATH_STATFORRAW  = "https://brilliant-fire-9867.firebaseio.com/statforraw";

    public static final String USER_TOKEN = APP_ID + ".User_IDToken";
    public static final String USER_EMAIL = APP_ID + ".User_Email";

    public static final String EMAIL_TO_FIREBASEPATH(String email) {
        String emod = email.replace("_", "__");
        emod = emod.replace(".", "_");
        return emod;
    }

    // Rehearsal parameters
    public static final int TERMS_PER_ROUND = 5;
    public static final long REHEARSAL_TIME_LB2 =   1L * 12L * 3600L * 1000L;
    public static final long REHEARSAL_TIME_LB3 =   7L * 24L * 3600L * 1000L;
    public static final long REHEARSAL_TIME_LB4 =  28L * 24L * 3600L * 1000L;
    public static final long REHEARSAL_TIME_LB5 = 365L * 24L * 3600L * 1000L;
    public static final long NEXT_REHEARSAL_TIME_LB1() { return -1; }
    public static final long NEXT_REHEARSAL_TIME_LB2() { return System.currentTimeMillis() + REHEARSAL_TIME_LB2; };
    public static final long NEXT_REHEARSAL_TIME_LB3() { return System.currentTimeMillis() + REHEARSAL_TIME_LB3; };
    public static final long NEXT_REHEARSAL_TIME_LB4() { return System.currentTimeMillis() + REHEARSAL_TIME_LB4; };
    public static final long NEXT_REHEARSAL_TIME_LB5() { return System.currentTimeMillis() + REHEARSAL_TIME_LB5; };


}

package com.pf.mr.utils;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by magnushyttsten on 3/27/16.
 */
public class Constants {
    public static final String APP_ID = "com.pf.mr";

    public static final void set_FPATH_BASE(String dbname) {
//        if (dbname == null || dbname.trim().length() == 0) {
//            FPATH_BASE = "https://ql-gutester.firebaseio.com/";
//        } else {
//            FPATH_BASE = "https://" + dbname.trim() + ".firebaseio.com/";
//        }
    }

    // public static String FPATH_BASE = "https://ql-gutester.firebaseio.com/";
    public static String FPATH_BASE = "https://ql-magnushyttsten.firebaseio.com/";

    public static final String FPATH_SETS() { return FPATH_BASE + "/sets"; }

    public static final String FPATH_STATFORUSER() { return FPATH_BASE + "/statforuser"; }
    public static final String FPATH_STATFORRAW() { return FPATH_BASE + "/statforraw"; }

    public static final String USER_TOKEN = APP_ID + ".User_IDToken";
    // public static final String USER_EMAIL = APP_ID + ".User_Email";
    public static final String SETNAME = APP_ID + ".SetName";
    public static final String SETID = APP_ID + ".SetId";

    public static final String SETNAME_ALL = "All";

    public static final int COLOR_RED = 0xCC0000;
    public static final int COLOR_GREEN = 0x00CC00;

    // Rehearsal parameters
    public static final int TERMS_PER_ROUND = 5;
//    public static final long REHEARSAL_TIME_LB1 =  600L * 1000L;
//    public static final long REHEARSAL_TIME_LB2 =  1200L * 1000L;
//    public static final long REHEARSAL_TIME_LB3 =  1800L * 1000L;
//    public static final long REHEARSAL_TIME_LB4 =  2400L * 1000L;
//    public static final long REHEARSAL_TIME_LB5 =  3000L * 1000L;
    public static final long REHEARSAL_TIME_LB1 =   3L * 24L * 3600L * 1000L;
    public static final String REHEARSAL_TIME_LB1_STR = "Next rehearsal: In 3 days!";
    public static final long REHEARSAL_TIME_LB2 =   7L * 24L * 3600L * 1000L;
    public static final String REHEARSAL_TIME_LB2_STR = "Next rehearsal: In 7 days!";
    public static final long REHEARSAL_TIME_LB3 =   14L * 24L * 3600L * 1000L;
    public static final String REHEARSAL_TIME_LB3_STR = "Next rehearsal: In 14 days!";
    public static final long REHEARSAL_TIME_LB4 =  60L * 24L * 3600L * 1000L;
    public static final String REHEARSAL_TIME_LB4_STR = "Next rehearsal: In 2 months!";
    public static final long REHEARSAL_TIME_LB5 = 365L * 24L * 3600L * 1000L;
    public static final String REHEARSAL_TIME_LB5_STR = "Next rehearsal: In 1 year!";
    public static final long NEXT_REHEARSAL_TIME_LB1() { return System.currentTimeMillis() + REHEARSAL_TIME_LB1; };
    public static final long NEXT_REHEARSAL_TIME_LB2() { return System.currentTimeMillis() + REHEARSAL_TIME_LB2; };
    public static final long NEXT_REHEARSAL_TIME_LB3() { return System.currentTimeMillis() + REHEARSAL_TIME_LB3; };
    public static final long NEXT_REHEARSAL_TIME_LB4() { return System.currentTimeMillis() + REHEARSAL_TIME_LB4; };
    public static final long NEXT_REHEARSAL_TIME_LB5() { return System.currentTimeMillis() + REHEARSAL_TIME_LB5; };
}

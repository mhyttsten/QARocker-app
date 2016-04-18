package com.pf.mr.utils;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by magnushyttsten on 3/27/16.
 */
public class Constants {
    public static final String APP_ID = "com.pf.mr";

    public static final void set_FPATH_BASE(String dbname) {
        if (dbname == null || dbname.trim().length() == 0) {
            FPATH_BASE = "https://ql-gutester.firebaseio.com/";
        } else {
            FPATH_BASE = "https://" + dbname.trim() + ".firebaseio.com/";
        }
    }

    public static String FPATH_BASE = "https://ql-gutester.firebaseio.com/";

    public static final String FPATH_SETS() { return FPATH_BASE + "/sets"; }

    public static final String FPATH_STATFORUSER() { return FPATH_BASE + "/statforuser"; }
    public static final String FPATH_STATFORRAW() { return FPATH_BASE + "/statforraw"; }

    public static final String USER_TOKEN = APP_ID + ".User_IDToken";
    public static final String USER_EMAIL = APP_ID + ".User_Email";
    public static final String SETNAME = APP_ID + ".SetName";
    public static final String SETID = APP_ID + ".SetId";

    public static final String SETNAME_ALL = "All";

    public static final String EMAIL_TO_FIREBASEPATH(String email) {
        String emod = email.replace("_", "__");
        emod = emod.replace(".", "_");
        return emod;
    }

    // Rehearsal parameters
    public static final int TERMS_PER_ROUND = 5;
    public static final long REHEARSAL_TIME_LB1 =  600L * 1000L;
    public static final long REHEARSAL_TIME_LB2 =  1200L * 1000L;
    public static final long REHEARSAL_TIME_LB3 =  1800L * 1000L;
    public static final long REHEARSAL_TIME_LB4 =  2400L * 1000L;
    public static final long REHEARSAL_TIME_LB5 =  3000L * 1000L;
//    public static final long REHEARSAL_TIME_LB1 =   1L * 12L * 3600L * 1000L;
//    public static final long REHEARSAL_TIME_LB2 =   3L * 24L * 3600L * 1000L;
//    public static final long REHEARSAL_TIME_LB3 =   7L * 24L * 3600L * 1000L;
//    public static final long REHEARSAL_TIME_LB4 =  28L * 24L * 3600L * 1000L;
//    public static final long REHEARSAL_TIME_LB5 = 365L * 24L * 3600L * 1000L;
    public static final long NEXT_REHEARSAL_TIME_LB1() { return System.currentTimeMillis() + REHEARSAL_TIME_LB1; };
    public static final long NEXT_REHEARSAL_TIME_LB2() { return System.currentTimeMillis() + REHEARSAL_TIME_LB2; };
    public static final long NEXT_REHEARSAL_TIME_LB3() { return System.currentTimeMillis() + REHEARSAL_TIME_LB3; };
    public static final long NEXT_REHEARSAL_TIME_LB4() { return System.currentTimeMillis() + REHEARSAL_TIME_LB4; };
    public static final long NEXT_REHEARSAL_TIME_LB5() { return System.currentTimeMillis() + REHEARSAL_TIME_LB5; };
}

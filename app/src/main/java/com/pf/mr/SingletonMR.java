package com.pf.mr;

import com.google.firebase.auth.FirebaseUser;
import com.pf.mr.execmodel.ESet;

import java.util.ArrayList;
import java.util.List;

public class SingletonMR {

    public static FirebaseUser mFirebaseUser;
    public static String mUserToken;

    public static void reset() {
        mQuizList = null;
        mCurrentESet = null;
    }

    public static ESet[] mQuizList;
    public static ESet mCurrentESet;
    public static void setCurrentSet(String name) {
        for (ESet e : mQuizList) {
            if (e.getSetTitle().equals(name)) {
                mCurrentESet = e;
                return;
            }
        }
        throw new AssertionError("Could not find ESet with name: " + name);
    }

}

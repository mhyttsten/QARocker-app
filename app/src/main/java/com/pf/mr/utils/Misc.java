package com.pf.mr.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.pf.mr.datamodel.QLSet;
import com.pf.mr.datamodel.StatTermForUser;
import com.pf.mr.execmodel.ESet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Misc {
    public static final String TAG = Misc.class.getSimpleName();

    public static Intent getIntentWithUserId(Context context, Class c, String userId) {
        Intent i = new Intent(context, c);
        i.putExtra(Constants.USER_EMAIL, userId);
        return i;
    }


    public static String toString(Object o) throws Exception {
        IndentWriter iw = new IndentWriter();
        toStringObject(iw, o);
        return iw.toString();
    }

    public static void toStringObject(IndentWriter iw, Object o) throws Exception {
        Class c = o.getClass();
        iw.setIndentChar('.');
        iw.println("[" + o.getClass().getSimpleName() + "] {");
        iw.push();

        Field[] fields = c.getFields();
        if (fields != null) {
            for (Field f: fields) {
                iw.print(f.getName() + ": ");
                Class ctype = f.getType();
                if (ctype.equals(Boolean.class)) {
                    iw.println(String.valueOf(f.getBoolean(o)));
                } else if (ctype.equals(Byte.class)) {
                    iw.println(String.valueOf(f.getByte(o)));
                } else if (ctype.equals(Double.class)) {
                    iw.println(String.valueOf(f.getDouble(o)));
                } else if (ctype.equals(Float.class)) {
                    iw.println(String.valueOf(f.getFloat(o)));
                } else if (ctype.equals(Integer.class)) {
                    iw.println(String.valueOf(f.getInt(o)));
                } else if (ctype.equals(Long.class)) {
                    iw.println(String.valueOf(f.getLong(o)));
                } else if (ctype.equals(Short.class)) {
                    iw.println(String.valueOf(f.getShort(o)));
                } else {
                    toStringObject(iw, o);
                }
            }
            iw.println("");
        }

        iw.pop();
        iw.println("}");
    }

    /**
     *
     */
    public static void getESets(final String email,
                             final String setName,
                             final List<ESet> eSetsResult,
                             final Runnable resultNotifier) {
        new Misc().getESetsImpl(email, setName, eSetsResult, resultNotifier);
    }

    public void getESetsImpl(final String email,
                               final String setName,
                               final List<ESet> eSetsResult,
                               final Runnable resultNotifier) {
        Firebase ref = new Firebase(Constants.FPATH_SETS());
        Query qref = ref.orderByKey();

        final List<QLSet> qlSets = new ArrayList<>();

        qref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot qs) {
                Log.i(TAG, "Result count: " + qs.getChildrenCount());
                Iterator<DataSnapshot> iter = qs.getChildren().iterator();
                while (iter.hasNext()) {
                    QLSet s = (QLSet) iter.next().getValue(QLSet.class);
                    if (setName == null
                            || setName.toLowerCase().equals(Constants.SETNAME_ALL)
                            || setName.equals(s.title)) {
                        qlSets.add(s);
                    }
                }
                if (qlSets.size() == 0) {
                    resultNotifier.run();
                    return;
                }

                getESetsIter(email, qlSets, eSetsResult, resultNotifier);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    private void getESetsIter(final String email,
                                    final List<QLSet> work,
                                    final List<ESet> result,
                                    final Runnable resultNotifier) {
        final QLSet qlset = work.remove(0);

        Firebase ref = new Firebase(Constants.FPATH_STATFORUSER())
                .child(String.valueOf(qlset.id))
                .child(Constants.EMAIL_TO_FIREBASEPATH(email));
        Query qref = ref.orderByKey();
        qref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot qs) {
                List<StatTermForUser> terms = new ArrayList<>();
                Iterator<DataSnapshot> iter = qs.getChildren().iterator();
                while (iter.hasNext()) {
                    StatTermForUser stfu = iter.next().getValue(StatTermForUser.class);
                    terms.add(stfu);
                }
                ESet eset = new ESet(qlset, email, terms);
                result.add(eset);
                if (work.size() == 0) {
                    resultNotifier.run();
                    return;
                }
                getESetsIter(email, work, result, resultNotifier);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

}



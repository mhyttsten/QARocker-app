package com.pf.mr.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pf.mr.datamodel.QLSet;
import com.pf.mr.datamodel.StatTermForUser;
import com.pf.mr.execmodel.ESet;
import com.pf.mr.execmodel.ETerm;
import com.pf.shared.extract.ExtractFromHTML_Helper;
import com.pf.shared.utils.Compresser;
import com.pf.shared.utils.MM;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

public class Misc {
    public static final String TAG = Misc.class.getSimpleName();

    public static byte[] getFirebaseStorageFile() {
        Log.w(TAG, "*** Will now try to download content");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference sr = storage.getReference(com.pf.shared.Constants.FUNDINFO_DB_MASTER_BIN);
        Log.w(TAG, "...bucket: " + sr.getBucket());
        Log.w(TAG, "...path: " + sr.getPath() + ", name: " + sr.getName());
        Task<byte[]> t = sr.getBytes(20*1024*1024);
        t.addOnCompleteListener(
                new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        Log.w(TAG, "...Task is complete");
                        if(task.isComplete()) {
                            Log.w(TAG, "...Task is complete");
                        } else {
                            Log.w(TAG, "...Task is not complete");
                        }
                        if(task.isSuccessful()) {
                            Log.w(TAG, "...Task is successful");
                        } else {
                            Log.w(TAG, "...Task is not successful");
                        }
                        if (task.isSuccessful()) {
                            byte[] rdata = task.getResult();
                            Log.w(TAG, "...Result size: " + task.getResult().length);
                            Log.w(TAG, "...Now uncompressing");
                            rdata = Compresser.dataUncompress(rdata);
                            Log.w(TAG, "...Done uncompressing, new data: " + rdata.length);

                        }
                    }
                });

//        Log.w(TAG, "*** Bucket is: " + sr.getBucket());
//        "gs://ql-magnushyttsten.appspot.com/backend/fundinfo-db-180220.bin.zip_MASTER_180216.zip";
        return null;

    }

    public static void extractFundInfo() {
        @SuppressLint("StaticFieldLeak") AsyncTask<URL, Integer, Long> at = new AsyncTask<URL, Integer, Long>() {
            protected Long doInBackground(URL... urls) {
//                String url = "https://www.google.com";
//                try {
//                    Log.i(TAG, "About to retrieve web page");
//                    byte[] ba = MM.getURLContentBA(url);
//                    Log.i(TAG, "Retrieved web page, size: " + ba.length);
//                } catch(Exception exc) {
//                    Log.i(TAG, "Exception when trying to get webpage: " + exc.toString());
//                    Log.i(TAG, MM.getStackTraceString(exc));
//                }
                ExtractFromHTML_Helper.extractFund();
                return 0L;
            }

            protected void onProgressUpdate(Integer... progress) { }
            protected void onPostExecute(Long result) { }
        }.execute();
    }

    public static String getPartStr(String s) {
        s = s.replace('\n', ' ');
        int l = 10;
        if (s.length() < 10) {
            l = s.length();
        }
        return s.substring(0, l);
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

    static boolean hasSetLogLevel = false;
    public static DatabaseReference getDatabaseReference(String url) {
        FirebaseDatabase fd = FirebaseDatabase.getInstance();
        if (!hasSetLogLevel) {
//            fd.setLogLevel(Logger.Level.DEBUG);
            hasSetLogLevel = true;
        }
        DatabaseReference dr = fd.getReferenceFromUrl(url);
        return dr;
    }

    /**
     *
     */
    public static void getESets(final String userToken,
                             final String setName,
                             final List<ESet> eSetsResult,
                             final Runnable resultNotifier) {
        new Misc().getESetsImpl(userToken, setName, eSetsResult, resultNotifier);
    }


    private static List<String> sets = new ArrayList<>();
    public void getESetsImpl(final String userToken,
                               final String setName,
                               final List<ESet> eSetsResult,
                               final Runnable resultNotifier) {
        DatabaseReference ref = Misc.getDatabaseReference(Constants.FPATH_SETS());
        Query qref = ref.orderByKey();

        final List<QLSet> qlSets = new ArrayList<>();
        qref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot qs) {
                Log.i(TAG, "Result count: " + qs.getChildrenCount());
                Iterator<DataSnapshot> iter = qs.getChildren().iterator();
                while (iter.hasNext()) {
                    DataSnapshot dsh = iter.next();
                    Object o = dsh.getValue();
                    QLSet s = null;
                    try {
                        s = (QLSet) dsh.getValue(QLSet.class);
                    } catch(Exception exc) {
                        System.out.println("Error");
                        exc.printStackTrace();
                        throw exc;
                    }
                    if (s != null) {
                        sets.add(s.title);
                        Log.i(TAG, "Fetched set: " + s.title + " from Firebase");
                    } else {
                        Log.i(TAG, "Fetched NULL QLSet, previous");
                        for (String setStr: sets) {
                            Log.i(TAG, setStr);
                        }
                    }
                    if (setName == null
                            || setName.toLowerCase().equals(Constants.SETNAME_ALL.toLowerCase())
                            || setName.equals(s.title)) {
                        Log.i(TAG, "...adding it to result list");
                        qlSets.add(s);
                    }
                }
                if (qlSets.size() == 0) {
                    resultNotifier.run();
                    return;
                }

                getESetsIter(userToken, qlSets, eSetsResult, resultNotifier);
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                System.out.println("*** onCancelled");
            }
        });
    }

    private void getESetsIter(final String userToken,
                              final List<QLSet> work,
                              final List<ESet> result,
                              final Runnable resultNotifier) {
        final QLSet qlset = work.remove(0);

        DatabaseReference ref = getDatabaseReference(Constants.FPATH_STATFORUSER())
                .child(String.valueOf(qlset.id))
                .child(userToken);
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
                ESet eset = new ESet(qlset, userToken, terms);
                result.add(eset);
                if (work.size() == 0) {
                    resultNotifier.run();
                    return;
                }
                getESetsIter(userToken, work, result, resultNotifier);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    public static String getNowAs_YYMMDD_HHMMSS(long time) {
        return getAs_YYMMDD_HHMMSS(new java.util.Date().getTime());
    }

    public static String getAs_YYMMDD_HHMMSS(long time) {
        Calendar c = new GregorianCalendar();
        c.setTime(new java.util.Date(time));
        int yy = c.get(Calendar.YEAR) % 100;
        int mm = c.get(Calendar.MONTH)+1;
        int dd = c.get(Calendar.DAY_OF_MONTH);
        int hh = c.get(Calendar.HOUR_OF_DAY);
        int mi = c.get(Calendar.MINUTE);
        int ss = c.get(Calendar.SECOND);
        System.out.println("yy:" + yy  + ", mm:" + mm + ", dd:" + dd + ", hh:" + hh + ", mi: " + mi + ", ss: " + ss);
        String s = String.format("%02d%02d%d %02d%02d%02d", yy, mm, dd, hh, mm, ss);
        return s;
    }

}



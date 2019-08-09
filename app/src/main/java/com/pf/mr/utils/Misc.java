package com.pf.mr.utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pf.mr.datamodel.QLSet;
import com.pf.mr.datamodel.StatTermForUser;
import com.pf.mr.execmodel.ESet;
import com.pf.shared.BackgroundWorker;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.extract.ExtractFromHTML_Helper;
import com.pf.shared.utils.Compresser;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class Misc {
    public static final String TAG = Misc.class.getSimpleName();

    public static void executedAtAppStartInBackground(AppCompatActivity a) {
        System.out.println("Misc.executedAtAppStart");

        String s = "Debug: ";
        String fstr = "No debug file";
        File file = new File(a.getApplicationContext().getFilesDir(), BackgroundWorker.FILE_NAME);
        if (file.exists()) {
            System.out.println("...file existed: " + BackgroundWorker.FILE_NAME);
            try {
                fstr = new String(MM.fileReadFrom(file));
                s += MM.getString(fstr, 100);
                System.out.println("...Content of file:\n" + s);
            } catch(Exception exc) {
                System.out.println("Exception caught: " + exc.getMessage());
            }
        } else {
            s += "Not exist";
            System.out.println("...file did not exist: " + BackgroundWorker.FILE_NAME);
        }

        File f2 = new File(a.getApplicationContext().getFilesDir(), com.pf.shared.Constants.FUNDINFO_DB_MASTER_BIN_APP);
        s += "\nFundDB: ";
        if (f2.exists()) {
            s += new java.util.Date(f2.lastModified()).toString();
            Toast.makeText(a, "Version [2019-02-26 13:06:00]\n" + s, Toast.LENGTH_LONG).show();
        } else {
            s += "Not found";

            // Get the DB file
            System.out.println("*************************************************************");
            System.out.println("**** LOCAL DB FILE NOT FOUND, Creating thread to get the file");
            final AppCompatActivity a2 = a;
            Toast.makeText(a, "No DB found, now fetching it", Toast.LENGTH_LONG).show();
            BackgroundWorker.initializeDB(false,null, a2.getApplicationContext());
//            @SuppressLint("StaticFieldLeak") AsyncTask<URL, Integer, Long> at = new AsyncTask<URL, Integer, Long>() {
//                protected Long doInBackground(URL... urls) {
//                    System.out.println("We are in the thread");
//                    BackgroundWorker.initializeDB(null, a2.getApplicationContext());
//                    return 0L;
//                }
//                protected void onProgressUpdate(Integer... progress) { }
//                protected void onPostExecute(Long result) { }
//            }.execute();
        }

        System.out.println("*** DEBUG file\n" + s);

        PeriodicWorkRequest wr = new PeriodicWorkRequest.Builder(
                BackgroundWorker.class,
                60,
                TimeUnit.MINUTES)
                .setConstraints(new Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .setRequiresStorageNotLow(false)
                        .setRequiresDeviceIdle(false)
                        .setRequiresCharging(false)
                        .setRequiredNetworkType(NetworkType.CONNECTED)  // Any type of NW is ok (also roaming)
                        .build())
                .build();
        WorkManager.getInstance().enqueueUniquePeriodicWork(
                "FUNDINFO_UPDATE_DB",
                ExistingPeriodicWorkPolicy.KEEP,
                wr);

//        try {
//            @SuppressLint("StaticFieldLeak") AsyncTask<URL, Integer, Long> at = new AsyncTask<URL, Integer, Long>() {
//                protected Long doInBackground(URL... urls) {
//                    try {
//                        Log.e(TAG, "*** In background");
//                        Log.e(TAG, "Will now get HTML data");
//                        IndentWriter iw_html_debug = new IndentWriter();
//                        byte[] htmlDataRaw = HtmlRetriever.htmlGet(
//                                iw_html_debug,
//                                "https://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P0000HLO5&programid=0000000000",
//                                5000,
//                                6);
//                        if (htmlDataRaw == null) {
//                            Log.e(TAG, "Received NULL");
//                        } else {
//                            Log.e(TAG, "Retrieved: " + htmlDataRaw.length + " bytes");
//                        }
//
//                        boolean error = false;
//                        if (htmlDataRaw == null || htmlDataRaw.length == 0) {
//                            Log.e(TAG, "HTML info null");
//                        }
//                        String htmlDataString = MM.newString(htmlDataRaw, com.pf.shared.Constants.ENCODING_FILE_READ);
//                        if (htmlDataString == null || htmlDataString.length() == 0) {
//                            Log.e(TAG, "HTML could not create String from bytep[]");
//                        }
//                    } catch(Exception exc) {
//                        Log.i(TAG, "Exception when trying to get webpage: " + exc.toString());
//                        Log.i(TAG, MM.getStackTraceString(exc));
//                    }
//                    Log.e(TAG, "Returning without error");
//                    return 0L;
//                }
//
//                protected void onProgressUpdate(Integer... progress) { }
//                protected void onPostExecute(Long result) { }
//            }.execute();
//
//        } catch(Exception exc) {
//            Log.e(TAG, exc.toString());
//        }
//        Log.e(TAG, "*** Now exiting from executeAtAppStartImpl");
//        throw new AssertionError("We've just executed the executeAtAppStartImpl");
    }

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

    public static void doTestImpl() {
        Log.w(TAG, "*** Entered doTest");
        @SuppressLint("StaticFieldLeak") AsyncTask<URL, Integer, Long> at = new AsyncTask<URL, Integer, Long>() {
            protected Long doInBackground(URL... urls) {
                try {
                    Log.w(TAG, "*** About to retrieve web page");
                    D_FundInfo fi = new D_FundInfo();
                    fi._url = "http://www.morningstar.se/Funds/Quicktake/Overview.aspx?perfid=0P00018UZ6&programid=0000000000";
                    ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
                    IndentWriter iwd = new IndentWriter();
                    int rc = eh.extractFundDetails(fi, iwd);
                    Log.i(TAG, "Result code: " + String.valueOf(rc));
                    Log.i(TAG, "Debug information: " + iwd.getString());
                } catch(Exception exc) {
                    Log.i(TAG, "Exception when trying to get webpage: " + exc.toString());
                    Log.i(TAG, MM.getStackTraceString(exc));
                }
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



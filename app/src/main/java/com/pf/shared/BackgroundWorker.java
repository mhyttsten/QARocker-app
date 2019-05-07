package com.pf.shared;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pf.fl.datamodel.DB_FundInfo_UI_Callback;
import com.pf.shared.analyze.FLAnalyze_DataPreparation;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.datamodel.D_FundInfo_Serializer;
import com.pf.shared.datamodel.D_Portfolio;
import com.pf.shared.utils.Compresser;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.Timer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileLockInterruptionException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BackgroundWorker extends Worker {
    public static final String TAG = BackgroundWorker.class.getSimpleName();

    public static final String FILE_NAME = "qa_rocker.txt";

    public BackgroundWorker(@NonNull Context context,
                            @NonNull WorkerParameters params) {
        super(context, params);
    }

    private IndentWriter _iw;

    @Override
    public Result doWork() {
        Result r = null;
        try {
            r = doWorkImpl();
        } catch(IOException exc) {
            Log.e(TAG, "Exception caught: " + exc.getMessage());
            Log.e(TAG, MM.getStackTraceString(exc));
            _iw.println("Exception caught: " + exc.getMessage());
            _iw.println(MM.getStackTraceString(exc));
            r = Result.success();
        }
        debugOutput(getApplicationContext(), _iw.getString());
        _iw = null;
        return r;
    }

    //------------------------------------------------------------------------
    private Result doWorkImpl() throws IOException {
        System.out.println("Will now do BackgroundWorkder.doWorkImpl");
        _iw = new IndentWriter();
        _iw.setIndentChar('.');
        _iw.println("---");
        _iw.println("BackgroundWorker.doWorkImpl: " + MM.getNowAs_YYMMDD_HHMMSS(null));
        _iw.push();

//        executeTheFileThing();

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        _iw.println("Hour: " + hour + ", minute: " + minute);
        if (hour > 0) {
            File file = new File(getApplicationContext().getFilesDir(), Constants.FUNDINFO_DB_MASTER_BIN_APP);
            String sfile = MM.getYYMMDDFromDate(new Date(file.lastModified()));
            String snow = MM.getNowAs_YYMMDD(null);
            _iw.println("SNow: " + snow + ", SFile: " + sfile);
            if (sfile.compareTo(snow) < 0) {
                _iw.println("Reinitializing DB");
                initializeDB(_iw, getApplicationContext());
            }
        } else {
            _iw.println("Will NOT reinitialize DB");
        }

        // Indicate success or failure with your return value:
        // (Returning Result.retry() tells WorkManager to try this task again
        // later; Result.failure() says not to try again.)
        System.out.println("DONE with BackgroundWorkder.doWorkImpl");
        _iw.println("Ok, were done, returning success");
        _iw.pop();
        return Result.success();
    }

    //------------------------------------------------------------------------
    public static void initializeDB(IndentWriter iw, final Context context) {
        final String fileName = Constants.FUNDINFO_DB_MASTER_BIN;
        if (iw == null) {
            iw = new IndentWriter();
        }
        iw.println("initializeDB, we will reinitialize fund DB");
        System.out.println("BackgroundWorkder.initializeDB, doing it: " + fileName);
        Log.i(TAG, "initializeDB, fileName: " + fileName + ",  time: " + System.currentTimeMillis());

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference sr = storage.getReference(fileName);
        Task<byte[]> t = sr.getBytes(20 * 1024 * 1024);
        t.addOnCompleteListener(
                new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        MM.sleepInMS(1000);
                        IndentWriter iw = new IndentWriter();
                        iw.println("************************");
                        iw.println("Aynchronous callback at: " + MM.getNowAs_YYMMDD_HHMMSS(null));
                        Log.e(TAG, "...initializeDB loaded file, time: " + System.currentTimeMillis());
                        boolean isError = false;
                        String errorMessage = null;
                        if (!task.isSuccessful()) {
                            isError = true;
                            errorMessage = task.getException().getMessage();
                            Log.e(TAG, "...initializeDB, fileName: " + fileName + ", error: " + errorMessage);
                            iw.println("ERROR: " + task.getException().getMessage());
                        } else {
                            iw.println("SUCCESS");
                            byte[] data = task.getResult();
                            iw.println("Number of bytes: " + data.length);
                            Log.e(TAG, "...initializeDB, fileName: " + fileName + ", success: " + data.length + ", time: " + System.currentTimeMillis());
                            if (fileName.equals(Constants.FUNDINFO_DB_MASTER_BIN)) {
                                iw.println("Will now processFundInfoDB");
                                processFundInfoDB(context.getFilesDir(), data);
                            }
                            iw.println("All done");
                        }
                        System.out.println(iw.getString());
                        debugOutput(context, iw.getString());
                    }
                });
        iw.println("initializeDB, exiting: But we're still waiting for the async callback");
    }

    //------------------------------------------------------------------------
    private static void processFundInfoDB(File file, byte[] data) {
        try {
            processFundInfoDBImpl(file, data);
        } catch(IOException exc) {
            Log.e(TAG, "Error processFundInfoDB: " + exc.getMessage());
            Log.e(TAG, "Stack trace: " + MM.getStackTraceString(exc));
        }
    }
    private static void processFundInfoDBImpl(File fileArg, byte[] data) throws IOException {
        System.out.println("BackgroundWorker.processFundInfoDBImpl entered with size: " + data.length);
        data = Compresser.dataUncompress(data);
        System.out.println("...decompressed size: " + data.length);

        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(bin);
        List<D_FundInfo> l = new ArrayList<>();
        while (din.available() > 0) {
            D_FundInfo fi = D_FundInfo_Serializer.decrunch_D_FundInfo(din);
            l.add(fi);
        }
        data = null;
        bin = null;
        din = null;
        Collections.sort(l, new Comparator<D_FundInfo>() {
            @Override
            public int compare(D_FundInfo lh, D_FundInfo rh) {
                return lh._nameMS.compareTo(rh._nameMS);
            }
        });
        System.out.println("...number of funds: " + l.size());
        FLAnalyze_DataPreparation.fillVoids(null, l);
        data = D_FundInfo_Serializer.crunchFundList(l, false);
        System.out.println("...crunched data size: " + data.length);

        File file = new File(fileArg, Constants.FUNDINFO_DB_MASTER_BIN_APP);
        if (file.exists()) {
            file.delete();
        }
        file = new File(fileArg, Constants.FUNDINFO_DB_MASTER_BIN_APP);
        FileOutputStream fout = new FileOutputStream(file);
        fout.write(data);
        fout.close();
        System.out.println("...BackgroundWorker.processFundInfoDBImpl done, wrote: " + data.length);
    }

    //------------------------------------------------------------------------
//    private void executeTheFileThing() throws IOException {
//        _iw.println("executeTheFileThing entered");
//        System.out.println("BackgroundWorker.executeTheFileThing, doing it");
//        String timeStamp = MM.getNowAs_YYMMDD_HHMMSS(null);
//
//        File file = new File(getApplicationContext().getFilesDir(), FILE_NAME);
//        if (file.exists()) {
//            file.delete();
//        }
//
//        System.out.println("...File: " + file.getAbsolutePath());
//        FileOutputStream fout = new FileOutputStream(file);
//        System.out.println("Creating file with content");
//        Log.e(TAG, "Creating file with content");
//        fout.write(timeStamp.getBytes());
//        fout.close();
//        _iw.println("executeTheFileThing exit successfully");
//    }

    //-----------------------------------------------------------------------
    private static final String DEBUG_NAME = "fl_debug.txt";
    private static void debugOutput(Context c, String str) {
        try {
            File file = new File(c.getFilesDir(), DEBUG_NAME);
            if (file.exists()) {
                FileInputStream fin = new FileInputStream(file);
                byte[] data = MM.readData(fin);
                if (data != null && data.length > 5000) {
                    file.delete();
                }
            }
            debugOutputImpl(c, str);
        } catch(Exception exc) {
        }


    }
    private static void debugOutputImpl(Context c, String str) throws IOException {
        File file = new File(c.getFilesDir(), FILE_NAME);
        if (file.exists()) {
            FileInputStream fin = new FileInputStream(file);
            byte[] data = MM.readData(fin);
            if (data != null && data.length > 0) {
                String s = new String(data);
                str = str + s;
            }
            file.delete();
        }

        file = new File(c.getFilesDir(), FILE_NAME);
        FileOutputStream fout = new FileOutputStream(file);
        fout.write(str.getBytes());
        fout.close();
    }

}


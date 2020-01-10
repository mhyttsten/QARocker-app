package com.pf.fl.datamodel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

//import com.pf.fl.datamodel.DM_DPMatrix.RowElem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pf.shared.Constants;
import com.pf.shared.analyze.DPSeries;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.datamodel.D_FundInfo_Serializer;
import com.pf.shared.datamodel.D_Portfolio;
import com.pf.shared.utils.Compresser;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.Timer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DB_FundInfo_UI {
    public static final String TAG = DB_FundInfo_UI.class.getSimpleName();

    //------------------------------------------------------------------------
    public static void savePortfolios(final Context c) {
        StorageReference sr = FirebaseStorage.getInstance().getReference();
        sr = sr.child(Constants.PORTFOLIO_DB_MASTER_BIN);

        Collection<D_Portfolio> psC = _portfoliosHM.values();
        List<D_Portfolio> ps = new ArrayList<>();
        Iterator<D_Portfolio> psI = psC.iterator();
        while (psI.hasNext()) {
            ps.add(psI.next());
        }

        Collections.sort(ps, new Comparator<D_Portfolio>() {
            public int compare(D_Portfolio lh, D_Portfolio rh) {
                return lh._name.compareTo(rh._name);
            }
        });

        byte[] data = null;
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            for (D_Portfolio p : ps) {
                D_FundInfo_Serializer.crunch_D_Portfolio(dout, p);
            }
            dout.flush();
            data = bout.toByteArray();
            data = Compresser.dataCompress("noname", data);
        } catch(IOException exc) {
            Toast.makeText(c, "Error crunching portfolio DB: " + exc.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        UploadTask ut = sr.putBytes(data);
        ut.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(c, "Error putBytes portfolio DB: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //------------------------------------------------------------------------
    public static void initializeDB_Master(AppCompatActivity activity) {
        try {
            initializeDB_MasterImpl(activity);
        } catch(IOException exc) {
            Log.e(TAG, exc.getLocalizedMessage() + "\n" + MM.getStackTraceString(exc));
        }
    }
    private static void initializeDB_MasterImpl(AppCompatActivity activity) throws IOException {
//        long start = System.currentTimeMillis();
        File file = new File(activity.getApplicationContext().getFilesDir(), Constants.FUNDINFO_DB_MASTER_BIN_APP);
        FileInputStream fin = new FileInputStream(file);
        byte[] data = MM.readData(fin);
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(bin);
        List<D_FundInfo> l = new ArrayList<>();
        while (din.available() > 0) {
            D_FundInfo fi = D_FundInfo_Serializer.decrunch_D_FundInfo(din);
            l.add(fi);
        }
        Collections.sort(l, new Comparator<D_FundInfo>() {
            @Override
            public int compare(D_FundInfo lh, D_FundInfo rh) {
                return lh.getNameMS().compareTo(rh.getNameMS());
            }
        });
        DB_FundInfo_UI.initialize_Funds(l);

//        long end = System.currentTimeMillis();
//        System.out.println("New startup time: " + (end - start));
    }

    //------------------------------------------------------------------------
    public static Timer _timeInit = new Timer();
    public static void initializeDB(final String fileName, final DB_FundInfo_UI_Callback cb) {
        Log.i(TAG, "initializeDB, fileName: " + fileName + ",  time: " + System.currentTimeMillis());
        if (fileName.equals(Constants.FUNDINFO_DB_MASTER_BIN)) {
            _timeInit.start();
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference sr = storage.getReference(fileName);
        Task<byte[]> t = sr.getBytes(20 * 1024 * 1024);
        t.addOnCompleteListener(
                new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        Log.e(TAG, "...initializeDB loaded file, time: " + System.currentTimeMillis());
                        boolean isError = false;
                        String errorMessage = null;
                        if (!task.isSuccessful()) {
                            isError = true;
                            errorMessage = task.getException().getMessage();
                            Log.e(TAG, "...initializeDB, fileName: " + fileName + ", error: " + errorMessage);
                            cb.callback(true, errorMessage, null);
                        } else {
                            try {
                                byte[] data = task.getResult();
                                Log.e(TAG, "...initializeDB, fileName: " + fileName + ", success: " + data.length + ", time: " + System.currentTimeMillis());
                                if (fileName.equals(Constants.FUNDINFO_DB_MASTER_BIN)) {
                                    throw new AssertionError("Was not supposed to get MASTER DB THIS WAY");
//                                    Log.e(TAG, "...initializeDB, processing master");
//                                    _timeInit.mark("Read file successfully: " + data.length + " bytes");
//                                    data = Compresser.dataUncompress(data);
//                                    _timeInit.mark("Uncompressed complete");
//                                    ByteArrayInputStream bin = new ByteArrayInputStream(data);
//                                    DataInputStream din = new DataInputStream(bin);
//                                    List<D_FundInfo> l = new ArrayList<>();
//                                    _timeInit.mark("Now starting to decrunch funds");
//                                    while (din.available() > 0) {
//                                        D_FundInfo fi = D_FundInfo_Serializer.decrunch_D_FundInfo(din);
//                                        l.add(fi);
//                                    }
//                                    _timeInit.mark("Done: " + l.size() + " decrunched");
//                                    Collections.sort(l, new Comparator<D_FundInfo>() {
//                                        @Override
//                                        public int compare(D_FundInfo lh, D_FundInfo rh) {
//                                            return lh._nameMS.compareTo(rh._nameMS);
//                                        }
//                                    });
//                                    Log.e(TAG, "...initializeDB, fileName: " + fileName + ", done entries : " + l.size() + ", time: " + System.currentTimeMillis());
//                                    Log.e(TAG, "...timer info\n" + _timeInit.toString());
//                                    cb.callback(false, null, l);
//                                    return;
                                } else if (fileName.equals(com.pf.shared.Constants.PORTFOLIO_DB_MASTER_BIN)) {
                                    Log.i(TAG, "...processing portfolio DB");
                                    data = Compresser.dataUncompress(data);
                                    ByteArrayInputStream bin = new ByteArrayInputStream(data);
                                    DataInputStream din = new DataInputStream(bin);

                                    List<D_Portfolio> l = new ArrayList<>();
                                    while (din.available() > 0) {
                                        D_Portfolio p = D_FundInfo_Serializer.decrunch_D_Portfolio(din);
                                        l.add(p);
                                        Log.i(TAG, "...read portfolio: " + p._name);
                                    }
                                    cb.callback(false, null, l);
                                    return;
                                } else if (fileName.equals(Constants.FUNDINFO_LOGS_EXTRACT_MASTER_TXT)) {
                                    Log.i(TAG, "...processing extract text");
                                    String s = new String(data, Constants.ENCODING_FILE_READ);
                                    cb.callback(false, null, s);
                                    return;
                                } else {
                                    cb.callback(true, "Software Error: Neither Funds nor Portfolios requested", null);
                                    return;
                                }
                            } catch (IOException exc) {
                                isError = true;
                                errorMessage = exc.toString();
                            }
                        }
                        cb.callback(true, errorMessage, null);
                    }
                });
    }

    //------------------------------------------------------------------------
    public static void initialize_Funds(List<D_FundInfo> fis) {
        Log.i(TAG, "*** initialize_Funds entered: " + fis.size());

        // Validate funds and set min/max friday
        // This takes a lot of time, and it's already done in extraction phase, so let's not do this
//        D_FundInfo_Validator fiv = new D_FundInfo_Validator(fis);
//        fiv.process();
//        if (fiv._error) {
//            throw new AssertionError(fiv._iwErrors.toString());
//        }
//        _fridayMax = fiv._fridayMax_YYMMDD;
//        _fridayMin = fiv._fridayMin_YYMMDD;

        Log.i(TAG, "...initialize_Funds 1");
        _funds = fis;

        // Funds by type & URL
        for (D_FundInfo fi: fis) {
            String key = getKeyForTypeAndURL(fi._type, fi._url);
            if (!_fundsByTypeAndURL.containsKey(key)) {
                _fundsByTypeAndURL.put(key, fi);
            }

            List<D_FundInfo> typeFIs = _fundsByType.get(fi._type);
            if (typeFIs == null) {
                typeFIs = new ArrayList<>();
                _fundsByType.put(fi._type, typeFIs);
            }
            typeFIs.add(fi);
        }
        Log.i(TAG, "...initialize_Funds 2");


        // Sort each respective type fund lists
        Iterator<String> iter = _fundsByType.keySet().iterator();
        while (iter.hasNext()) {
            List<D_FundInfo> tmp = _fundsByType.get(iter.next());
            Collections.sort(tmp, new Comparator<D_FundInfo>() {
                @Override
                public int compare(D_FundInfo o1, D_FundInfo o2) {
                    return o1.getNameMS().compareTo(o2.getNameMS());
                }
            });
        }
        Log.i(TAG, "...initialize_Funds exited: " + fis.size());
    }

    //------------------------------------------------------------------------
    public static void initialize2_Portfolios(List<D_Portfolio> ps) {
        // Make sure we have one for each type
        Map<String, Void> tmp = new HashMap<>();
        for (D_Portfolio p: ps) {
            tmp.put(p._name, null);
        }
        for (String t: D_FundInfo.TYPES) {
            if (!tmp.containsKey(t)) {
                D_Portfolio np = new D_Portfolio();
                np._name = t;
                ps.add(np);
            }
        }

        // Assign the list and hashmap structures
        _portfolios = ps;
        Collections.sort(_portfolios, new Comparator<D_Portfolio>() {
            @Override
            public int compare(D_Portfolio o1, D_Portfolio o2) {
                return o1._name.compareTo(o2._name);
            }
        });
        for (D_Portfolio p: _portfolios) {
            _portfoliosHM.put(p._name, p);
        }
    }

    //------------------------------------------------------------------------
    public static void initialize3_ExtractStatistics(String statistics) {
        Log.i(TAG, "*** initialize3: " + statistics.length());
        _extractStatistics = statistics;
    }

    //------------------------------------------------------------------------
    public static String getKeyForTypeAndURL(String type, String url) { return type + "_" + url; }
    public static List<D_FundInfo> getFundsForPortfolio(String name) {
        List<D_FundInfo> r = new ArrayList<>();
        D_Portfolio p = _portfoliosHM.get(name);
        for (String url: p._urls) {
            String keyForTypeAndURL = getKeyForTypeAndURL(name, url);
            r.add(_fundsByTypeAndURL.get(keyForTypeAndURL));
        }
        return r;
    }

    // ***********************************************************************

    public static boolean _initialized;
//    public static String _fridayMin = null;
//    public static String _fridayMax = null;
    public static String _extractStatistics;

    // Data structures
    public static List<D_FundInfo> _funds;
    public static Map<String, List<D_FundInfo>> _fundsByType = new HashMap<>();
    public static Map<String, D_FundInfo> _fundsByTypeAndURL = new HashMap<>();

    public static List<D_Portfolio> _portfolios = new ArrayList<>();
    public static HashMap<String, D_Portfolio> _portfoliosHM = new HashMap<>();

    //------------------------------------------------------------------------
    public static List<DPSeries> getPortfolioStats(String name) {
        List<D_FundInfo> funds = getFundsForPortfolio(name);
        List<DPSeries> dpss = DPSeries.getDPSeriesForFunds(funds, 4);
        return dpss;
    }
    public static DPSeries getPortfolioSummaryStats(String name) {
        List<DPSeries> t = getPortfolioStats(name);
        DPSeries r = DPSeries.getDPSeriesForDPSerieses(name, t);
        return r;
    }
}

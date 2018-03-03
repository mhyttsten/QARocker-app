package com.pf.fl.datamodel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

//import com.pf.fl.datamodel.DM_DPMatrix.RowElem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pf.fl.screens.ListImpl;
import com.pf.fl.screens.MainActivity;
import com.pf.shared.Constants;
import com.pf.shared.datamodel.D_FundDPDay;
import com.pf.shared.datamodel.D_FundInfo_Serializer;
import com.pf.shared.datamodel.D_Portfolio;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.utils.Compresser;
import com.pf.shared.utils.D_Utils;
import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FL_DB {
    public static final String TAG = FL_DB.class.getSimpleName();

    public static final String INTENT_EXTRA_PORTFOLIO_NAME = "EXTRA_PORTFOLIO_NAME";

    public static void savePortfolios(final Context c) {
        StorageReference sr = FirebaseStorage.getInstance().getReference();
        sr = sr.child(Constants.PORTFOLIO_DB_MASTER_BIN);

        Collection<D_Portfolio> psC = portfoliosHM.values();
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

    public static void initializeDB(final String fileName, final FL_DBCallback cb) {
        Log.i(TAG, "initializeDB, fileName: " + fileName);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference sr = storage.getReference(fileName);
        Task<byte[]> t = sr.getBytes(20 * 1024 * 1024);
        t.addOnCompleteListener(
                new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        boolean isError = false;
                        String errorMessage = null;
                        if (!task.isSuccessful()) {
                            isError = true;
                            errorMessage = task.getException().getMessage();
                            Log.i(TAG, "...initializeDB, fileName: " + fileName + ", error: " + errorMessage);
                        } else {
                            try {
                                byte[] data = task.getResult();
                                Log.i(TAG, "...initializeDB, fileName: " + fileName + ", success: " + data.length);
                                if (fileName.equals(com.pf.shared.Constants.FUNDINFO_DB_MASTER_BIN)) {
                                    data = Compresser.dataUncompress(data);
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
                                            return lh._nameMS.compareTo(rh._nameMS);
                                        }
                                    });
                                    Log.i(TAG, "...done reading funds DB, entries: " + l.size());
                                    cb.callback(false, null, l);
                                    return;
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

    public static List<D_Portfolio> getPortfolios() {
        List<D_Portfolio> l = new ArrayList<>();
        Collection<D_Portfolio> portfolioC = portfoliosHM.values();
        Iterator<D_Portfolio> portfolioI = portfolioC.iterator();
        while (portfolioI.hasNext()) {
            l.add(portfolioI.next());
        }
        Collections.sort(l, new Comparator<D_Portfolio>() {
            @Override
            public int compare(D_Portfolio lh, D_Portfolio rh) {
                return lh._name.compareTo(rh._name);
            }
        });
        return l;
    }

    public static boolean _initialized;
    public static String fridayMin = null;
    public static String fridayMax = null;
    public static String _extractStatistics;
    public static Map<String, List<CheckableFund>> cfundsByTypeHM = new HashMap<>();
    public static Map<String, D_FundInfo> fundsByURL = new HashMap<>();
    public static HashMap<String, D_Portfolio> portfoliosHM = new HashMap<>();

    public static void initialize1_Funds(List<D_FundInfo> fis) {
        Log.i(TAG, "*** initialize1: " + fis.size());
        List<CheckableFund> fis_cf = null;
        for (D_FundInfo fi: fis) {
            if (!cfundsByTypeHM.containsKey(fi._type)) {
                cfundsByTypeHM.put(fi._type, new ArrayList<CheckableFund>());
            }
            if (!portfoliosHM.containsKey(fi._type)) {
                portfoliosHM.put(fi._type, new D_Portfolio());
            }
            fis_cf = cfundsByTypeHM.get(fi._type);
            fis_cf.add(new CheckableFund(fi));

            fundsByURL.put(fi._url, fi);
            processDPDays(fi);
        }
        Collections.sort(fis_cf, new Comparator<CheckableFund>() {
            public int compare(CheckableFund lh, CheckableFund rh) {
                return lh.fund._nameMS.compareTo(rh.fund._nameMS);
            }
        });
    }
    private static void processDPDays(D_FundInfo fi) {
        List<D_FundDPDay> dpds = fi._dpDays;
        if (dpds == null || dpds.size() == 0) return;

        String last = null;
        for (D_FundDPDay dp: fi._dpDays) {
            String s = dp._dateYYMMDD;
            if (s == null) throw new AssertionError("D_FundDPDay, date was null");
            if (!MM.tgif_isFriday(s)) throw new AssertionError("D_FundDPDay was not a Friday: " + s);
            if (fridayMax == null || s.compareTo(fridayMax) > 0) fridayMax = s;
            if (fridayMin == null || s.compareTo(fridayMin) < 0) fridayMin = s;
            if (last == null) {
                last = s;
            } else if (s.compareTo(last) >= 0) {
                throw new AssertionError("Higher index had more recent date");
            }
        }
    }

    public static void initialize2_Portfolios(List<D_Portfolio> ps) {
        Log.i(TAG, "*** initialize2: " + ps.size());
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

        for (D_Portfolio p: ps) {
            portfoliosHM.put(p._name, p);
        }
    }

    public static void initialize3_ExtractStatistics(String statistics) {
        Log.i(TAG, "*** initialize3: " + statistics.length());
        _extractStatistics = statistics;
    }

    public static ListImpl listContent = new ListImpl();
    public static void listPopulatePortfolioView(String portfolioName) {
        listContent.mHeaderAndBody.clear();

        listContent.mTitle = portfolioName;

        // Header
        String[] ss = getRecentDates(4);
        ListImpl.HeaderAndBody hb = new ListImpl.HeaderAndBody(MM.strArray2CSV(ss), "");
        listContent.mHeaderAndBody.add(hb);

        // Funds
        D_Portfolio p = portfoliosHM.get(portfolioName);
        for (String url : p._urls) {
            D_FundInfo f = fundsByURL.get(url);
            hb = new ListImpl.HeaderAndBody();
            hb.mHeader = f._nameMS;
            hb.mBody = D_Utils.getR1WsAsCSV(ss, f);
            listContent.mHeaderAndBody.add(hb);
        }
    }
    private static String[] getRecentDates(int count) {
        List<String> l = new ArrayList<>();
        String current = fridayMax;
        l.add(current);
        count--;
        while (count > 0) {
            current = MM.tgif_getLastFridayTodayExcl(current);
            l.add(current);
            count--;
        }
        return (String[])l.toArray(new String[l.size()]);
    }


    public static class CheckableFund {
        public CheckableFund(D_FundInfo f) { fund = f; }
        public D_FundInfo fund;
        public boolean isChecked;
    }
}


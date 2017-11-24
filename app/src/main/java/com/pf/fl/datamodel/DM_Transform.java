package com.pf.fl.datamodel;

import android.util.Log;

import com.pf.fl.analysis.MasterTrend;
import com.pf.fl.datamodel.DM_DPMatrix.RowElem;
import com.pf.fl.screens.ListImpl;
import com.pf.shared.MM;
import com.pf.shared.OTuple2G;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DM_Transform {
    public static final String TAG = DM_Transform.class.getSimpleName();

    public static final String EXTRA_PORTFOLIO_NAME = "EXTRA_PORTFOLIO_NAME";

    public static final String T_SEB = "SEB";
    public static final String T_VANGUARD = "VGD";
    public static final String T_PPM = "PPM";
    public static final String T_SPP = "SPP";

    public static String[] dateSequence;

    public static List<DMA_ExtractInfo> extractInfos;

    public static DM_DPMatrix fundsMatrix;
    public static HashMap<String, DM_DPMatrix> indexToFundsHM = new HashMap<>();
    public static DM_DPMatrix indexAverages;

    public static HashMap<String, DM_Fund> fundsByNameHM = new HashMap<>();
    public static HashMap<Long, DM_Fund> fundsByIdHM = new HashMap<>();
    public static List<DM_Fund> getFunds() {
        List<DM_Fund> l = new ArrayList<>();
        Iterator<DM_Fund> i = fundsByNameHM.values().iterator();
        while (i.hasNext()) {
            l.add(i.next());
        }
        return l;
    }

    public static HashMap<String, DMA_Portfolio> portfoliosHM = new HashMap<>();

    // ******

    // What ListActivity will display, populate before starting that activity
    public static ListImpl listContent = new ListImpl();
    public static void listPopulatePortfolioView(String portfolioName) {
        listContent.mHeaderAndBody.clear();

        listContent.mTitle = portfolioName;

        // Header
        String s = getFirstXValuesCSVString(dateSequence, 4);
        ListImpl.HeaderAndBody hb = new ListImpl.HeaderAndBody(s, "");
        listContent.mHeaderAndBody.add(hb);

        // Funds
        DMA_Portfolio p = portfoliosHM.get(portfolioName);
        for (Long id : p.fund_ids) {
            DM_Fund f = fundsByIdHM.get(id);
            hb = new ListImpl.HeaderAndBody();
            hb.mHeader = f.name;
            hb.mBody = getFirstXValuesCSVString(f.mDPs, 4);
            listContent.mHeaderAndBody.add(hb);
        }
    }
    private static String getFirstXValuesCSVString(Object[] o, int firstX) {
        firstX = firstX > o.length ? o.length : firstX;
        StringBuffer strb = new StringBuffer();
        for (int i=0; i < firstX; i++) {
            strb.append(o[i]);
            if ((i+1) < firstX) {
                strb.append(", ");
            }
        }
        return strb.toString();
    }
    private static String getLastXValuesCSVString(Object[] o, int lastX) {
        lastX = lastX > o.length ? o.length : lastX;
        int cindex = o.length-1;
        StringBuffer strb = new StringBuffer();
        while (lastX > 0) {
            strb.append(o[cindex]);
            if (lastX > 1) {
                strb.append(", ");
            }
            cindex--;
            lastX--;
        }
        return strb.toString();
    }

    // ******

    public static class CheckableFund {
        public DM_Fund fund;
        public boolean isChecked;
    }

    public static List<CheckableFund> getFunds(String type) {
        List<CheckableFund> l = new ArrayList<>();
        Collection<DM_Fund> s = fundsByNameHM.values();
        Iterator<DM_Fund> iter = s.iterator();
        while (iter.hasNext()) {
            DM_Fund f = iter.next();
            if (type == null || f.type.equals(type)) {
                CheckableFund cf = new CheckableFund();
                cf.fund = f;
                l.add(cf);
            }
        }
        Collections.sort(l, new Comparator<CheckableFund>() {
            public int compare(CheckableFund lh, CheckableFund rh) {
                return lh.fund.name.compareTo(rh.fund.name);
            }
        });
        return l;
    }

    /**
     *
     */
    public static void initializePortfolios(List<DMA_Portfolio> l) {
        portfoliosHM = new HashMap<>();
        for (DMA_Portfolio p : l) {
            portfoliosHM.put(p.name, p);
        }
    }

    public static void portfolioAdd(DMA_Portfolio p) {
        portfoliosHM.put(p.name, p);
    }

    /**
     *
     */
    public static boolean isDBInitialized = false;
    public static boolean isExtractsInitialized = false;

    public static void initializeFromExtractList(List<DMA_ExtractInfo> l) {
        if (isExtractsInitialized) {
            return;
        }
        Collections.sort(l, new Comparator<DMA_ExtractInfo>() {
            @Override
            public int compare(DMA_ExtractInfo t0, DMA_ExtractInfo t1) {
                return -t0.date.compareTo(t1.date);
            }
        });
        extractInfos = l;
    }

    public static void initializeFromRawDB(String rawString) {
        if (isDBInitialized) {
            return;
        }

        // Each column separated by ~
        // First row is the headers
        String[] l = rawString.split("\n");
        if (l == null || l.length <= 1) {
            return;
        }

        for (int i=0; i < 4; i++) {
            Log.i(TAG, "Line[" + i + "]: " + l[i]);
        }

        // Dates are coming in order most recent -> most old
        String[] dateSequence = getDateSequence(l[0]);


        String r = "";
        for (int i = 0; i < dateSequence.length; i++) {
            r += dateSequence[i] + ", ";
        }
        Log.i(TAG, "The dates:\n" + r);

        fundsMatrix = DM_DPMatrix.instantiate(null, dateSequence);
        int count = 0;
        for (int i = 1; i < l.length; i++) {
            DM_Fund f = DM_Fund.instantiate(dateSequence, l[i]);
            if (count < 4) {
                Log.i(TAG, f.toString());
                count++;
            }

            if (fundsByNameHM.containsKey(f.getName())) {
                throw new AssertionError("Duplicate fund entry: " + f.getName());
            }

//                if (f.name.equals("HSBC GIF Brazil Equity AC")) {
//                    System.out.println("FOUND THE FUND 1");
//                    MM.printArray(f.fridaysYYMMDD);
//                    MM.printArray(f.values);
//                    System.out.println("FOUND THE FUND 2");
//                    MM.reverse(f.fridaysYYMMDD);
//                    MM.reverse(f.values);
//                    MM.printArray(f.fridaysYYMMDD);
//                    MM.printArray(f.values);
//                }

            fundsByNameHM.put(f.getName(), f);
            fundsByIdHM.put(Long.valueOf(f.id), f);
            RowElem re = new RowElem();
            re.entity = f;
            re.fridaysYYMMDD = f.fridaysYYMMDD;
            re.values = f.values;
            fundsMatrix.add(re);

            if (!indexToFundsHM.containsKey(f.index.getName())) {
                DM_DPMatrix dpm = DM_DPMatrix.instantiate(f.index, dateSequence);
                indexToFundsHM.put(f.index.getName(), dpm);
            }
            DM_DPMatrix dpm = indexToFundsHM.get(f.index.getName());
            dpm.add(re);
        }

        // Average change per index
        List<DM_DPMatrix> indexes = new ArrayList<>();
        MM.hmValuesToList(indexToFundsHM, indexes);
        indexAverages = DM_DPMatrix.instantiate(null, dateSequence);
        for (int i = 0; i < indexes.size(); i++) {
            DM_DPMatrix e = indexes.get(i);
            RowElem index = averageAllRows(e);
            indexAverages.add(index);
        }

        // Build Trends
        List<CheckableFund> cfs = getFunds(T_SEB);
        count=0;
        long timeStart = System.currentTimeMillis();
        for (CheckableFund cf : cfs) {
//            System.out.println("Now doing fund: " + cf.fund.name);
            MasterTrend mt = new MasterTrend();
            mt.initialize(cf.fund);
            cf.fund.mTrends = mt.mTrends;
            count++;
        }
        long timeEnd = System.currentTimeMillis();
///        System.out.println("*** Total time: " + (timeEnd-timeStart) + ", for: " + count + " funds");
        isDBInitialized = true;
    }

    // ******

    private static String[] getDateSequence(String header) {
        String[] s = header.split("~");

        List<String> r = new ArrayList<>();
        for (int i=8; i < s.length; i++) {
            r.add(s[i]);
        }
        return (String[])r.toArray(new String[r.size()]);
    }

    private static OTuple2G<String, String> getDatesForOne(String sa) {
        String[] s = sa.split("~");
        if (s.length < 9) {
            return null;
        }

        String dateEarliest = null;
        String dateLatest = null;
        for (int i = 8; i < s.length; i++) {
            String[] sp = s[i].split(":");
            String date = sp[0];
            if (!MM.tgif_isFriday(date)) {
                Log.e(TAG, "Found a non-friday: " + date);
            }

            if (dateEarliest == null || dateEarliest.compareTo(date) > 0) {
                dateEarliest = date;
            }
            if (dateLatest == null || dateLatest.compareTo(date) < 0) {
                dateLatest = date;
            }
        }
        return new OTuple2G<>(dateEarliest, dateLatest);
    }

    private static Comparator<RowElem> ENTITY_NAME = new Comparator<RowElem>() {
        public int compare(RowElem lhs, RowElem rhs) {
            return lhs.entity.getName().compareTo(rhs.entity.getName());
        }
    };

    private static RowElem averageAllRows(DM_DPMatrix e) {
        RowElem r = new RowElem();
        r.entity = e.entity;
        r.fridaysYYMMDD = e.weeks;
        r.values = new Double[e.weeks.length];
        for (int i = 0; i < r.values.length; i++) {
            r.values[i] = new Double(0);
        }
        for (int i = 0; i < r.values.length; i++) {
            Double d = new Double(0);
            int count = 0;
            for (int j = 0; j < e.rows.size(); j++) {
                RowElem cre = e.rows.get(j);
                Double cre_d = cre.getValueAsDouble(i);
                if (cre_d != null) {
                    d += cre_d;
                    count++;
                }
            }
            r.values[i] = null;
            if (count != 0) {
                r.values[i] = d / count;
            }
        }
        return r;
    }

    // ***

    public static final int TS_LENGTH = 1;
    public static final int TS_VALUE = 2;
    public static final int TS_UP = 3;
    public static final int TS_DOWN = 4;
    public void trendSort(final int trendType, int trendValue) {
        List<CheckableFund> cfs = getFunds(T_SEB);
        List<DM_Fund> funds = new ArrayList();
        for (CheckableFund cf: cfs) {
            funds.add(cf.fund);
        }

//        Collections.sort(funds, new Comparator<DM_Fund>() {
//            @Override
//            public int compare(DM_Fund o1, DM_Fund o2) {
//                if ()
//
//                return 0;
//            }
//        })




    }
}


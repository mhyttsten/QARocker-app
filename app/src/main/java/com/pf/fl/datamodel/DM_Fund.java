package com.pf.fl.datamodel;

import com.google.firebase.database.Exclude;
import com.pf.fl.analysis.DataPoint;
import com.pf.fl.analysis.TrendEntity;
import com.pf.shared.utils.IndentWriter;

import java.util.ArrayList;
import java.util.List;

//mr.append("Id");
//        mr.append("~Type");
//        mr.append("~Name");
//        mr.append("~MSRating");
//        mr.append("~PPMNumber");
//        mr.append("~Category");
//        mr.append("~Index");
//        mr.append("~Currency");
//
//        String cdate = mDateLatest;
//        while (cdate.compareTo(mDateEarliest) >= 0) {
//        mr.append("~" + cdate);
//        cdate = MM.tgif_getLastFridayTodayExcl(cdate);
//        }
//        mr.append("\n");


public class DM_Fund implements DM_NameId {
    public static final String TAG = DM_Fund.class.getSimpleName();

    public long id;
    public String type;
    public String name;
    public int msRating;
    public Integer ppmNumber;
    public String category;
    public DM_Index index;
    public String currency;

    public String[] fridaysYYMMDD;
    public Double[] values;
    public DataPoint[] mDPs;

    @Exclude public List<TrendEntity> mTrends;

    public String getName() { return type + "." + name; }

    public String toString() {
        IndentWriter iw = new IndentWriter();
        iw.println("Fund: " + getName());
        iw.println("  msRating: " + msRating);
        iw.println("  ppm: " + ppmNumber);
        iw.println("  category: " + category);
        iw.println("  index: " + index.getName());
        iw.println("  currency: " + currency);
        for (int i=0; i < mDPs.length; i++) {
            DataPoint dp = mDPs[i];
            iw.print(dp.mFridayYYMMDD + ":" + String.format("%.2f", dp.mValue) + ", ");
        }
        iw.println();
        return iw.toString();
    }

    public static DM_Fund instantiate(String[] sDates, String sArg) {
        DM_Fund f = new DM_Fund();
        String[] s = sArg.split("~");
        f.id = Long.parseLong(s[0]);
        f.type = s[1];
        f.name = s[2];
        f.msRating = Integer.parseInt(s[3]);
        f.ppmNumber = Integer.parseInt(s[4]);
        f.category = s[5];

//        Log.d(TAG, "sDates: " + MM.asStrArray(sDates, 0));
//        Log.d(TAG, "s: " + MM.asStrArray(s, 0));
//        Log.d(TAG, "s,datepart: " + MM.asStrArray(s, 7));

        String indexName = "<null>";
        if (s[6].length() != 0) {
            indexName = s[6];
        }
        f.index = new DM_Index(indexName);

        f.currency = s[7];
        f.fridaysYYMMDD = sDates;

        List<Double> doubles = new ArrayList<>();
        int count = 0;
        for (int i=8; i < s.length; i++) {
            if (s[i] != null && s[i].trim().length() > 0) {
                Double dvalue = null;
                int io = s[i].indexOf(":");
                if ((io + 1) < s[i].length()) {
                    dvalue = Double.parseDouble(s[i].substring(io+1));
                }
                doubles.add(dvalue);
                count++;
//                Log.d(TAG, "Added: " + s[i] + ", total count now: " + count);
            } else {
                doubles.add(null);
                count++;
//                Log.d(TAG, "Added: null, total count now: " + count);
            }
        }
        if (doubles.size() != sDates.length) {
            throw new AssertionError("Mismatch in sizes, doubles: " + doubles.size() + ", sDates: " + sDates.length);
        }
        Double[] dbs = (Double[])doubles.toArray(new Double[doubles.size()]);
        f.values = dbs;

        f.mDPs = new DataPoint[dbs.length];
        for (int i=0; i < dbs.length; i++) {
            f.mDPs[i] = new DataPoint(f, f.fridaysYYMMDD[i], f.values[i]);
        }

        return f;
    }
}

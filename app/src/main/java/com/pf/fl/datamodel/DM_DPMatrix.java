package com.pf.fl.datamodel;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DM_DPMatrix {
    public static final String TAG = DM_DPMatrix.class.getSimpleName();

    public static class RowElem {
        public DM_NameId entity;
        public String[] fridaysYYMMDD;
        public Double[] values;
        public String getName() { return entity.getName(); }
        public Double getValueAsDouble(int index) { return values[index]; }
        public String getValueAsString(int index) {
            if (values[index] == null) { return "-"; }
            return String.format("%.2f" ,values[index]);
        }
    }

    public DM_NameId entity;
    public String[] weeks;
    public List<RowElem> rows = new ArrayList<>();

    public static DM_DPMatrix instantiate(DM_NameId entityArg, String[] weeksArg) {
        DM_DPMatrix e = new DM_DPMatrix();
        e.entity = entityArg;
        e.weeks = weeksArg;
        return e;
    }

    public void sort(Comparator<RowElem> c) {
        if (c == null) {
            Log.e(TAG, "c was null");
        }
        Collections.sort(rows, c);
    }

    public void add(RowElem rw) {
        rows.add(rw);
    }
}

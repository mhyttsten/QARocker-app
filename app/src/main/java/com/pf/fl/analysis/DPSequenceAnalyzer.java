package com.pf.fl.analysis;

import android.content.Intent;
import android.util.Log;

import com.pf.fl.datamodel.DMA_Portfolio;
import com.pf.fl.datamodel.DM_Fund;
import com.pf.fl.datamodel.DM_Index;
import com.pf.fl.datamodel.DM_Transform;
import com.pf.fl.screens.ListActivity;
import com.pf.fl.screens.ListImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DPSequenceAnalyzer {
    public static final String TAG = DPSequenceAnalyzer.class.getSimpleName();

    public double _bodyTotal;
    public double _bodyMean;
    public double _bodyStdDeviation;
    public List<DataPoint> _bodyDPs = new ArrayList<>(10);
    public double _headTotal;
    public double _headMean;
    public double _headStdDeviation;
    public List<DataPoint> _headDPs = new ArrayList<>(10);

    private DM_Fund _fund;
    private DataPoint[] _dps;
    private int _indexBodyStart;
    private int _indexBodyEnd;

    public void initialize(DM_Fund fund,
                           int indexBodyStart,
                           int indexBodyEnd) throws AssertionError {
        Log.i(TAG, "Initialize with fund: " + fund.getName());
        _fund = fund;
        _dps = _fund.mDPs;
        _indexBodyStart = indexBodyStart;
        _indexBodyEnd = indexBodyEnd;

        // Validate decending date order
        boolean first = true;
        String lastDate = null;
        for (DataPoint dp : _dps) {
            if (first) {
                first = false;
                lastDate = dp.mFridayYYMMDD;
                // Log.i(TAG, "..."+ dp.mFridayYYMMDD);
            } else {
                if (dp.mFridayYYMMDD == null) {
                    throw new AssertionError("Found a null friday date");
                } else {
                    // Dates are in ascending order
                    if (lastDate.compareTo(dp.mFridayYYMMDD) >= 0) {
                        throw new AssertionError("Date order error, prev: " + lastDate + ", this: " + dp.mFridayYYMMDD);
                    }
                }
                lastDate = dp.mFridayYYMMDD;
                // Log.i(TAG, "..."+ dp.mFridayYYMMDD);
            }
        }
        _bodyMean = calculateMean(_indexBodyStart, _indexBodyEnd);
        _bodyStdDeviation = calculateStdDeviation(_bodyMean, _indexBodyStart, _indexBodyEnd);
        _bodyTotal = calculateTotal(_bodyDPs, _indexBodyStart, _indexBodyEnd);
        if (indexBodyStart > 0) {
            _headMean = calculateMean(0, _indexBodyStart);
            _headStdDeviation = calculateStdDeviation(_headMean, 0, _indexBodyStart);
            _headTotal = calculateTotal(_bodyDPs, 0, _indexBodyStart);
        }
    }

    private boolean allPositive(List<DataPoint> dps) {
        for (DataPoint dp: dps) {
            if (dp.mValue == null || dp.mValue < 0) {
                return false;
            }
        }
        return true;
    }

    private double calculateTotal(List<DataPoint> dps, int start, int end) {
        double sum = 1.0;
        for (int i = start; i < end; i++) {
            DataPoint dp = _dps[i];
            if (dp.mValue != null) {
                dps.add(dp);
                double v = dp.mValue + 1.0;
                sum *= v;
            }
        }
        return sum;
    }

    private double calculateMean(int start, int end) {
        double sum = 0;
        int count = 0;
        for (int i = start; i < end; i++) {
            DataPoint dp = _dps[i];
            if (dp.mValue != null) {
                sum += dp.mValue;
                count++;
            }
        }
        double r = sum / count;
        return r;
    }

    private double calculateStdDeviation(double mean, int start, int end) {
        double sum = 0;
        int count = 0;
        for (int i = start; i < end; i++) {
            DataPoint dp = _dps[i];
            if (dp.mValue != null) {
                double part = (dp.mValue - mean);
                sum = part * part;
                count++;
            }
        }
        double r = Math.sqrt(sum / count);
        return r;
    }

    public static void createList() {

        DM_Transform.listContent.mHeaderAndBody.clear();
        DPSequenceAnalyzer dpsa = null;
        ListImpl.HeaderAndBody hb = null;
        List<DPSequenceAnalyzer> l = null;
        int end = -1;

        DM_Transform.listContent.mTitle = "Show Changes";

        // Best performing funds
        hb = new ListImpl.HeaderAndBody("*** Best Funds", "");
        DM_Transform.listContent.mHeaderAndBody.add(hb);
        l = getBestFunds(0, 2);
        end = l.size() > 5 ? 5 : l.size();
        for (int i=0; i < end; i++) {
            dpsa = l.get(i);
            hb = new ListImpl.HeaderAndBody(dpsa._fund.getName(), String.format("%.2f", dpsa._bodyTotal));
            DM_Transform.listContent.mHeaderAndBody.add(hb);
        }

//        // List of best indexes
//        hb = new ListImpl.HeaderAndBody("", "");
//        DM_Transform.listContent.mHeaderAndBody.add(hb);
//        hb = new ListImpl.HeaderAndBody("*** Best Indexes", "");
//        DM_Transform.listContent.mHeaderAndBody.add(hb);
//        l = getBestIndexes(0, 2);
//        end = l.size() > 5 ? 5 : l.size();
//        for (int i=0; i < end; i++) {
//            dpsa = l.get(i);
//            hb = new ListImpl.HeaderAndBody(dpsa._fund.getName(), String.format("%.2f", dpsa._bodyTotal));
//            DM_Transform.listContent.mHeaderAndBody.add(hb);
//        }

//        // List of fund change (positive)
//        hb = new ListImpl.HeaderAndBody("", "");
//        DM_Transform.listContent.mHeaderAndBody.add(hb);
//        hb = new ListImpl.HeaderAndBody("*** Best Fund Change", "");
//        DM_Transform.listContent.mHeaderAndBody.add(hb);
//        l = getBestFundChange(2, -1);
//        end = l.size() > 5 ? 5 : l.size();
//        for (int i=0; i < end; i++) {
//            dpsa = l.get(i);
//            hb = new ListImpl.HeaderAndBody(dpsa._fund.getName(), String.format("%.2f", dpsa._bodyTotal));
//            DM_Transform.listContent.mHeaderAndBody.add(hb);
//        }
//
//        // List of fund change (positive)
//        hb = new ListImpl.HeaderAndBody("", "");
//        DM_Transform.listContent.mHeaderAndBody.add(hb);
//        hb = new ListImpl.HeaderAndBody("*** Best Index Change", "");
//        DM_Transform.listContent.mHeaderAndBody.add(hb);
//        l = getBestIndexChange(2, -1);
//        end = l.size() > 5 ? 5 : l.size();
//        for (int i=0; i < end; i++) {
//            dpsa = l.get(i);
//            hb = new ListImpl.HeaderAndBody(dpsa._fund.getName(), String.format("%.2f", dpsa._bodyTotal));
//            DM_Transform.listContent.mHeaderAndBody.add(hb);
//        }
    }

    private static List<DPSequenceAnalyzer> getBestFunds(int start, int end) {
        List<DPSequenceAnalyzer> r = new ArrayList<>();
        List<DM_Fund> funds = DM_Transform.getFunds();
        for (DM_Fund f: funds) {
            DPSequenceAnalyzer dpsa = new DPSequenceAnalyzer();
            dpsa.initialize(f, start, end);
            r.add(dpsa);
        }
        Collections.sort(r, new Comparator<DPSequenceAnalyzer>() {
            @Override
            public int compare(DPSequenceAnalyzer t0, DPSequenceAnalyzer t1) {
                return (int)((t0._bodyMean - t1._bodyMean) * 1000);
            }
        });
        return r;
    }

//    private static List<DPSequenceAnalyzer> getBestIndexes(int start, int end) {
//        List<DPSequenceAnalyzer> r = new ArrayList<>();
//        List<DM_Index> indexes = DM_Transform.
//        List<DM_Fund> funds = DM_Transform.getFunds();
//        for (DM_Fund f: funds) {
//            DPSequenceAnalyzer dpsa = new DPSequenceAnalyzer();
//            dpsa.initialize(f, start, end);
//            r.add(dpsa);
//        }
//        Collections.sort(r, new Comparator<DPSequenceAnalyzer>() {
//            @Override
//            public int compare(DPSequenceAnalyzer t0, DPSequenceAnalyzer t1) {
//                return (int)((t0._bodyMean - t1._bodyMean) * 1000);
//            }
//        });
//        return r;
//    }


}

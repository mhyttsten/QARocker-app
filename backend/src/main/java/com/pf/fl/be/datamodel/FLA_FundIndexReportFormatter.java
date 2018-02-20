package com.pf.fl.be.datamodel;

import com.pf.shared.utils.MM;
import com.pf.shared.utils.OTuple2G;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class FLA_FundIndexReportFormatter {
    private static final Logger log = Logger.getLogger(FLA_FundIndexReportFormatter.class.getName());
    private static final String TAG = MM.getClassName(FLA_FundIndexReportFormatter.class.getName());

    /**
     */
    public static String createDestructiveDateReport(List<FLA_FundIndex> fundIndexList) throws Exception {
        HashMap<String, List<OTuple2G<FLA_FundIndex, FLA_FundIndexDPDay>>> hm = new HashMap<>();

        for (int i=0; i < fundIndexList.size(); i++) {
            FLA_FundIndex fi = fundIndexList.get(i);
            List<FLA_FundIndexDPDay> fiDPDays = fi.mDPDays;
            while (fiDPDays.size() > 0) {
                FLA_FundIndexDPDay fiDPDay = fiDPDays.remove(0);
                fiDPDay.mFundInfoLongIds = null;
                fiDPDay.mR1ws = null;
                hmAdd(hm, fiDPDay.mDateYYMMDD, new OTuple2G<FLA_FundIndex, FLA_FundIndexDPDay>(fi, fiDPDay));
            }
        }

        // List of dates to process
        Iterator<String> dateIter = hm.keySet().iterator();
        hm.values().iterator();
        List<String> dateList = new ArrayList<>();
        while (dateIter.hasNext()) {
            dateList.add(dateIter.next());
        }
        dateIter = null;
        Collections.sort(dateList);

        // Now iterate over dates and fundIndexes
        HashMap<String,List<FLA_FundIndexDPDay>> hmIndexName2DPDayList = new HashMap<>();
        for (int i=dateList.size()-1; i >= 0; i--) {
            String date = dateList.get(i);
            for (int j = 0; j < fundIndexList.size(); j++) {
                FLA_FundIndex fundIndex = fundIndexList.get(j);
                List<OTuple2G<FLA_FundIndex, FLA_FundIndexDPDay>> flist = hm.get(date);
                FLA_FundIndexDPDay fundIndexDPDay = findIndex(flist, fundIndex);
                List<FLA_FundIndexDPDay> tmpList = hmIndexName2DPDayList.get(fundIndex.mKey_IndexName);
                if (tmpList == null) {
                    tmpList = new ArrayList<>();
                    hmIndexName2DPDayList.put(fundIndex.mKey_IndexName, tmpList);
                }
                tmpList.add(0, fundIndexDPDay); // desc order like the dates...
            }
        }

        // *** Generate the report

        // HEADER INFORMATION (2 ROWS)
        StringBuffer reportStrb = new StringBuffer();
        // Generate the header
        boolean isFirst = true;
        for (int i=dateList.size()-1; i >= 0; i--) {
            if (isFirst) {
                reportStrb.append(",");
                isFirst = false;
            }
            reportStrb.append(dateList.get(i) + ",*,*,*,*,*");
        }
        reportStrb.append("\n");
        reportStrb.append("Fund Index Name,");
        for (int i=dateList.size()-1; i >= 0; i--) {
            reportStrb.append(HEADER);
        }
        reportStrb.append("\n");

        // hmIndexName2DPDayList now contains all FundIndex name
        // And all lists will have the FundIndexDPDay entries in dateList desc order
        HashMap<String,String> hmReportByCount = new HashMap<>();
        Iterator<String> nameIter = hmIndexName2DPDayList.keySet().iterator();
        while (nameIter.hasNext()) {
            StringBuffer lineItem = new StringBuffer();
            String fundIndexName = nameIter.next();
            List<FLA_FundIndexDPDay> dpdays = hmIndexName2DPDayList.get(fundIndexName);
            if (dpdays.size() != dateList.size()) {
                throw new Exception("List sizes differ, dpdays: " + dpdays.size() + ", datelist: " + dateList.size());
            }
            lineItem.append(fundIndexName + ",");
            isFirst = true;
            String countStr = null;
            for (int i=dpdays.size()-1; i >= 0; i--) {
                FLA_FundIndexDPDay fundIndexDPDay = dpdays.get(i);
                if (countStr == null && fundIndexDPDay != null) {
                    countStr = String.format("%04d", fundIndexDPDay.mCount);
                }

                addToReport(lineItem, fundIndexDPDay);
                if (i > 0) {
                    lineItem.append(",");
                }
            }
            lineItem.append("\n");
            if (countStr == null) {
                countStr = "-9999";
            }
            hmReportByCount.put(countStr + ":" + fundIndexName, lineItem.toString());
        }
        // Sort it in count order
        Iterator<String> reportKeyIter = hmReportByCount.keySet().iterator();
        List<String> reportList = new ArrayList<>();
        while (reportKeyIter.hasNext()) {
            reportList.add(reportKeyIter.next());
        }
        Collections.sort(reportList);
        for (int i=reportList.size()-1; i >=0; i--) {
            String key = reportList.get(i);
            String str = hmReportByCount.get(key);
            reportStrb.append(str);
        }
        return reportStrb.toString();
    }

    private static final String HEADER = "Cnt,Med,Avg,Hig,Low,";
    private static void addToReport(StringBuffer strb, FLA_FundIndexDPDay dpday) {
        String count = "-";
        String med = "-";
        String avg = "-";
        String hig = "-";
        String low = "-";
        if (dpday != null) {
            count = String.valueOf(dpday.mCount);
            med = String.format("%.2f", dpday.mR1wMed);
            avg = String.format("%.2f", dpday.mR1wAvg);
            hig = String.format("%.2f", dpday.mR1wMax);
            low = String.format("%.2f", dpday.mR1wMin);
        }
        strb.append(count + "," + med + "," + avg + "," + hig + "," + low);
    }

    private static FLA_FundIndexDPDay findIndex(
            List<OTuple2G<FLA_FundIndex, FLA_FundIndexDPDay>> list,
            FLA_FundIndex fundIndex) {
        for (int i=0; i < list.size(); i++) {
            OTuple2G<FLA_FundIndex, FLA_FundIndexDPDay> e = list.get(i);
            if (e._o1.mKey_IndexName.equals(fundIndex.mKey_IndexName)) {
                return e._o2;
            }
        }
        return null;
    }

    private static void hmAdd(
            HashMap<String, List<OTuple2G<FLA_FundIndex, FLA_FundIndexDPDay>>> hm,
            String dateKey,
            OTuple2G<FLA_FundIndex, FLA_FundIndexDPDay> value) {
        List<OTuple2G<FLA_FundIndex, FLA_FundIndexDPDay>> l = hm.get(dateKey);
        if (l == null) {
            l = new ArrayList<OTuple2G<FLA_FundIndex, FLA_FundIndexDPDay>>();
            hm.put(dateKey, l);
        }
        l.add(value);
    }
}


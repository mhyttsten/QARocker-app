package com.pf.phototimestampmanager;

import java.util.List;

public class Misc {

    public static String getReport(List<ExifElem> l, ExifElem.ExifStat es) {
        IndentWriter riw = new IndentWriter();
        riw.println("Report of analysis");
        riw.push();
        riw.println("Total entries: " + l.size());
        int ioEarliest = getIndexOfEarliestDate(l);
        int ioLatest = getIndexOfLatestDate(l);
        printEntry(riw, "Earlist: ", ioEarliest, l);
        printEntry(riw, "Latest: ", ioLatest, l);
        es.dumpInfo(riw);
        riw.pop();
        return riw.getString();
    }

    public static void printEntry(IndentWriter iw, String text, int io, List<ExifElem> l) {
        iw.print(text);
        if (io == -1) {
            iw.println("Does not exist (-1)");
            return;
        }
        ExifElem ee = l.get(io);
        iw.println(ee.getMyDate().toString());
    }

    public static int getIndexOfEarliestDate(List<ExifElem> l) {
        int io = -1;
        for (int i=0; i < l.size(); i++) {
            ExifElem eeNow = l.get(i);
            if (io == -1 && eeNow.getMyDate() != null) {
                io = i;
            } else if (eeNow.getMyDate() != null) {
                ExifElem eeEarliest = l.get(io);
                if (eeNow.getMyDate().compareTo(eeEarliest.getMyDate()) < 0) {
                    io = i;
                }
            }
        }
        return io;
    }

    public static int getIndexOfLatestDate(List<ExifElem> l) {
        int io = -1;
        for (int i=0; i < l.size(); i++) {
            ExifElem eeNow = l.get(i);
            if (io == -1 && eeNow.getMyDate() != null) {
                io = i;
            } else if (eeNow.getMyDate() != null) {
                ExifElem eeLatest = l.get(io);
                if (eeNow.getMyDate().compareTo(eeLatest.getMyDate()) > 0) {
                    io = i;
                }
            }
        }
        return io;
    }
}

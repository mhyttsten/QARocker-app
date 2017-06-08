package com.pf.fl.datamodel;

import java.util.ArrayList;
import java.util.List;

public class DMA_ExtractInfo {
    public static class NameAndURL {
        public String name;
        public String url;
    };

    public String date;
    public int extract_total;
    public int extract_updated;
    public int invalid_count;
    public List<NameAndURL> invalid_entries = new ArrayList<>();
    public int not_updated_count;
    public List<NameAndURL> not_updated_entries = new ArrayList<>();

    public String toTextViewString() {
        String s = "date: " + date + "\n"
                + "extract_total: " + extract_total + "\n"
                + "extract_updated: " + extract_updated + "\n"
                + "invalid_count: " + invalid_count + "\n"
                + "not_updated_count: " + not_updated_count + "\n";
        if (invalid_count > 0) {
            s += "Click below link to fix invalids\n";
            s += "pffundlifter.appspot.com/JSP_FundURLData_Display.jsp?p_type=INVALID\n";
        }
        if (not_updated_count > 0) {
            s += "Click below to see some funds not updated\n";
            int count = 1;
            for (NameAndURL nu: not_updated_entries) {
                if ((count % 3) == 0) {
                    s += "....SPACE YOU CAN GRAB & SCROLL...\n";
                    s += "....SPACE YOU CAN GRAB & SCROLL...\n";
                }
                String url = nu.url;
                int io = url.indexOf("&programid");
                if (io != -1) {
                    url = url.substring(0, io);
                }
                s += url + "\n";
                count++;
            }
        }
        return s;
    }
}



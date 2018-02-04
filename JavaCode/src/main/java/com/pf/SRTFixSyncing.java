package com.pf;


import com.pf.dedup.MM;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static java.io.File.separator;

public class SRTFixSyncing {


    public static class TS {
        public int hour;
        public int minute;
        public int second;
        public int millis;
        public String resultString;
        public TS(int h, int m, int s, int mi) {
            hour = h; minute = m; second = s; millis = mi;
        }

        public String getResultString() {
            return resultString;
        }

        public void add(int secondAdjust) {
            GregorianCalendar gnow = new GregorianCalendar();
            gnow.set(GregorianCalendar.HOUR_OF_DAY, hour);
            gnow.set(GregorianCalendar.MINUTE, minute);
            gnow.set(GregorianCalendar.SECOND, second);
            gnow.add(GregorianCalendar.SECOND, secondAdjust);

            String s = String.format("%02d:%02d:%02d,%03d",
                    gnow.get(GregorianCalendar.HOUR_OF_DAY),
                    gnow.get(GregorianCalendar.MINUTE),
                    gnow.get(GregorianCalendar.SECOND),
                    millis);
            resultString = s;
        }

        public static TS instantiate(String s) throws Exception {
            String hhS = s.substring(0,2);
            String mmS = s.substring(3, 5);
            String ssS = s.substring(6, 8);
            String miS = s.substring(9);

//            System.out.println("My time parse: " + hhS + "|" + mmS + "|" + ssS +"|" + miS);

            int hh = Integer.parseInt(hhS);
            int mm = Integer.parseInt(mmS);
            int ss = Integer.parseInt(ssS);
            int mi = Integer.parseInt(miS);
            return new TS(hh, mm, ss, mi);
        }
    }

    public static void main(String[] args) {
        try {
            mainImpl("/Users/magnushyttsten/Desktop/untitled_folder/And.Then.There.Were.None.S01E01.HDTV.x264.srt");
            System.out.println("Done");
        } catch(Exception exc) {
            exc.printStackTrace();
            System.out.println(exc.toString());
        }
    }

    public static void saveFile(String fname, Object[] c) throws Exception {
        Writer writer =
                new OutputStreamWriter(
                        new FileOutputStream(fname), "ISO-8859-1");
        BufferedWriter fout = new BufferedWriter(writer);
        for (int i=0; i < c.length; i++) {
            if (c[i] instanceof Integer) {
                fout.write(String.valueOf((Integer)c[i]));
            } else {
                fout.write(((String)c[i]));
            }
            fout.newLine();
        }
        fout.close();
    }

    public static boolean processFile_wasChanged = false;
    public static void mainImpl(String fname) throws Exception {


//        String file = "/Users/magnushyttsten/Desktop/srt/file.txt";
//        FileOutputStream fout = new FileOutputStream("/Users/magnushyttsten/Desktop/srt/filer.txt");
//        byte[] fileContent = MM.fileReadFrom(file);
//        String fileContentStr = new String(fileContent, "ISO-8859-1");
//        BufferedReader d = new BufferedReader(new StringReader(fileContentStr));
//        String line = null;
//        do {
//            line = d.readLine();
//            if(line != null) {
//                System.out.println(line);
//                fout.write(line.getBytes("ISO-8859-1"));
//            }
//        } while(line != null);
//        fout.close();

//        String file = "/Users/magnushyttsten/Desktop/srt/file.txt";
//        FileInputStream fin = new FileInputStream(fname);
//        BufferedReader br = new BufferedReader(new InputStreamReader(fin, "ISO-8859-1"));
//
//        Writer writer =
//                new OutputStreamWriter(
//                        new FileOutputStream("/Users/magnushyttsten/Desktop/srt/filer.txt"), "ISO-8859-1");
//        BufferedWriter fout = new BufferedWriter(writer);
//        while (true) {
//            String index = br.readLine();
//            System.out.println(index);
//            if (index == null) {
//                break;
//            }
//            fout.write(index + "\n");
//        }
//        fout.close();
//
//        if (true)
//            return;

        Object[] c = processFile(fname);
        if (processFile_wasChanged) {
            System.out.println("*** Exiting because indexes were changed so you may want to consider that");
            saveFile(fname, c);
            return;
        }

        int index = 413;
        int adjustment = 3;
//        int index = 10;
//        int adjustment = 10;
        if (index >= 0) {
            processArray(c, index, adjustment);
        }

        System.out.println("Did all time adjustments");
        saveFile(fname, c);
    }

    public static void changeFrom(Object[] c, int index, int secondAdjust) throws Exception {
        String KEY = " --> ";
        do {
            index++;
            String time = (String)c[index];
            int io = time.indexOf(KEY);
            if (io == -1) {
                throw new Exception("Did not find --> at line: " + index + ", instead found: " + time);
            }
            String t1S = time.substring(0, io).trim();
            String t2S = time.substring(io+KEY.length()).trim();

            TS t1 = TS.instantiate(t1S);
            TS t2 = TS.instantiate(t2S);
            t1.add(secondAdjust);
            t2.add(secondAdjust);
            c[index] = t1.getResultString() + " --> " + t2.getResultString();
            System.out.println("index: " + c[index-1] + ", result: " + c[index]);

            // Go to next index and continue change
            while (index < c.length && !(c[index] instanceof Integer)) {
                index++;
            }

        } while(index < c.length);
    }

    public static void processArray(Object[] c, int index, int adjustment) throws Exception {
        for (int i=0; i < c.length; i++) {
            if (c[i] instanceof Integer) {
                if ((Integer)c[i] == index) {
                    changeFrom(c, i, adjustment);
                }
            }
        }
    }

    public static Object[] processFile(String fname) throws Exception {
        List<Object> al = new ArrayList<>();

        FileInputStream fin = new FileInputStream(fname);
        BufferedReader br = new BufferedReader(new InputStreamReader(fin, "ISO-8859-1"));
        while (true) {
            String index = br.readLine();

            // End of file
            if (index == null) {
                break;
            }

            al.add(index);
        }

        boolean didCR = true;
        int currIndex = 1;
        for (int i=0; i < al.size(); i++) {
            if (didCR) {
                Integer cindex = getInteger((String)al.get(i));
                if (cindex == null) {
                    throw new Exception("Expected integer but got: " + al.get(i));
                }
                if (cindex != currIndex) {
                    System.out.println("Will adjust unexpected. Was expecting: " + currIndex + ", but got: " + cindex);
                    al.remove(i);
                    al.add(i, new Integer(currIndex));
                    processFile_wasChanged = true;
                } else {
                    al.remove(i);
                    al.add(i, new Integer(currIndex));
                }
                currIndex++;
                didCR = false;
            }
            else if(((String)al.get(i)).trim().length() == 0) {
                didCR = true;
            }
        }

        Object[] r = new Object[al.size()];
        for (int i=0; i < al.size(); i++)
            r[i] = al.get(i);
        return r;
    }

    public static Integer getInteger(String s) {
        try {
            int index = Integer.parseInt(s.trim());
            return index;
        } catch(NumberFormatException exc) {
            return null;
        }
    }
}

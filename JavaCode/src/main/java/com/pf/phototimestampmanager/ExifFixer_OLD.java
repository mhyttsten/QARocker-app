package com.pf.phototimestampmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExifFixer_OLD {

    public ExifFixer_OLD(int index, List<ExifElem> l) {
    }

    public static List<ExifElem> getOptimumChangeList(List<ExifElem> l) throws Exception {

        //IndentWriter iw = new IndentWriter();
        //iw.setIndentChar('.');
        IndentWriter iw = null;
        Object[] correctionCount = new Object[l.size()];

        for (int i = 0; i < l.size(); i++) {
            // Initialize, e.g. null dates are no alternative
            correctionCount[i] = null;
        }

        //printList(iw, "*** Getting optimum list, incoming: ", l);

        boolean didOne = false;
        List<ExifElem> c = createCopy(l);

        for (int i = 0; i < c.size(); i++) {
            //iw.println("Doing candidate: " + i + ", c.size: " + c.size());
            //iw.push();

            if (c.get(i).getMyDate() != null) {
                ExifElem eeToken = c.get(i);
                FilenameElem fne = new FilenameElem(eeToken.mFilename);
                //iw.println("Non-null candidate, this is sequence number: " + fne.mSequenceString);
                didOne = true;

                List<ExifElem> ldown = new ArrayList<>();
                if (i > 0) {
                    List<ExifElem> lbefore = c.subList(0, i);
                    //printList(iw, "DownList to process: ", lbefore);
                    //iw.push();
                    ldown = getDownList(iw, lbefore, eeToken);
                    //iw.pop();
                    //printList(iw, "Resulting downList: ", ldown);
                } else {
                    //iw.println("Index 0, so did not do downList");
                }

                List<ExifElem> lup = new ArrayList<>();
                if ((i + 1) < c.size() - 1) {
                    List<ExifElem> lafter = c.subList(i + 1, c.size());
                    printList(iw, "UpList to process: ", lafter);
                    //iw.push();
                    lup = getUpList(iw, lafter, eeToken);
                    //iw.pop();
                    //printList(iw, "Resulting upList: ", lup);
                } else {
                    //iw.println("Index i, so did not do upList since c.size is: " + c.size());
                }

                ldown.add(eeToken);
                ldown.addAll(lup);
                correctionCount[i] = ldown;
                System.out.println("...Now analyzed: " + i);
                System.out.println("......Correction count: " + countModifications(ldown));
                //printList(iw, "Composed list: ", ldown);
            }
            //iw.pop();

            iw = new IndentWriter();
            if ((i % 50) == 0) {
                System.out.println("...Now analyzed: " + i + " entries");
            }

            System.out.println("Analyzed another index: " + i);

        }

        if (!didOne) {
            throw new Exception("There was not a single file with a timestamp for reference");
        }

        // Find one with the lowest penalty
        //iw.println("***NO DEBUG YET, CONTINUE HERE, SELECTING BEST ALTERANTIVE");
        List<ExifElem> correctionLowest = null;
        for (int i = 0; i < correctionCount.length; i++) {
            List<ExifElem> current = (List<ExifElem>) correctionCount[i];
            if (correctionLowest == null) {
                correctionLowest = (List<ExifElem>) correctionCount[i];
            } else if (current != null &&
                    countModifications(current) <
                            countModifications(correctionLowest)) {
                correctionLowest = (List<ExifElem>) correctionCount[i];
            }
        }

        // printList(iw, "Optimized list to return to process: ", correctionLowest);

        iw = null;
        iw = new IndentWriter();
        // System.out.println(iw.getString());
        return correctionLowest;
    }


    public static List<ExifElem> getDownList(IndentWriter iw, List<ExifElem> l, ExifElem eeToken) throws Exception {
        FilenameElem fne = new FilenameElem(eeToken.mFilename);
        //printList(iw, "getDownList[token=" + fne.mSequenceNumber + "]: ", l);
        //iw.push();
        l = getDownListImpl(iw, l, eeToken);
        //iw.pop();
        return l;
    }

    public static List<ExifElem> getDownListImpl(IndentWriter iw, List<ExifElem> l, ExifElem eeToken) throws Exception {
        if (eeToken.getMyDate() == null) {
            throw new Exception("Argument date was not set for filename: " + eeToken.mFilename);
        }

        ExifElem myEE = l.get(l.size() - 1);
        FilenameElem fne = new FilenameElem(myEE.mFilename);

        // Last iteration, return after potential adjustment
        if (l.size() == 1) {
            //iw.println("I was the last one (" + fne.mSequenceNumber + ")");
            bwd_setDateIfNeeded(myEE, eeToken);
            l = new ArrayList<>();
            l.add(myEE);
            return l;
        }

        // Not last iteration

        List<ExifElem> rest1 = createCopy(l);
        rest1.remove(rest1.size() - 1);
        List<ExifElem> rest2 = createCopy(rest1);

        // If myEE is greater, then there is no alternative, we must adjust down
        if (myEE.getMyDate() == null || myEE.getMyDate().compareTo(eeToken.getMyDate()) >= 0) {
            myEE.setMyDate(getDateAdjusted(eeToken.getMyDate(), -1), null);
            List<ExifElem> newl = getDownList(iw, rest1, myEE);
            newl.add(myEE);
            return newl;
        }
        // If myEE is less, will keeping it be more effective than adjusting close to ee?
        else {
            ExifElem myEE2 = myEE.createCopy();

            List<ExifElem> newl1 = getDownList(iw, rest1, myEE);
            newl1.add(myEE);

            List<ExifElem> newl2 = getDownList(iw, rest2, myEE2);
            newl2.add(myEE2);

            return countModifications(newl1) < countModifications(newl2) ? newl1 : newl2;
        }
    }

    public static List<ExifElem> getUpList(IndentWriter iw, List<ExifElem> l, ExifElem eeToken) throws Exception {
        FilenameElem fne = new FilenameElem(eeToken.mFilename);
        //printList(iw, "getUpList[token=" + fne.mSequenceNumber + "]: ", l);
        //iw.push();
        l = getUpListImpl(iw, l, eeToken);
        //iw.pop();
        return l;
    }

    public static List<ExifElem> getUpListImpl(IndentWriter iw, List<ExifElem> l, ExifElem eeToken) throws Exception {
        if (eeToken.getMyDate() == null) {
            throw new Exception("Argument date was not set for filename: " + eeToken.mFilename);
        }

        ExifElem myEE = l.get(0);
        FilenameElem fne = new FilenameElem(myEE.mFilename);

        // Last iteration, return after potential adjustment
        if (l.size() == 1) {
            //iw.println("I was the last one (" + fne.mSequenceNumber + ")");
            fwd_setDateIfNeeded(myEE, eeToken);
            l = new ArrayList<>();
            l.add(myEE);
            return l;
        }

        // Not last iteration

        List<ExifElem> rest1 = createCopy(l);
        rest1.remove(0);
        List<ExifElem> rest2 = createCopy(rest1);

        // If myEE is less, then there is no alternative, we must adjust up
        if (myEE.getMyDate() == null || myEE.getMyDate().compareTo(eeToken.getMyDate()) <= 0) {
            myEE.setMyDate(getDateAdjusted(eeToken.getMyDate(), 1), null);
            List<ExifElem> newl = getUpList(iw, rest1, myEE);
            newl.add(0, myEE);
            return newl;
        }
        // If myEE is greater, will keeping it be more effective than adjusting close to ee?
        else {
            ExifElem myEE2 = myEE.createCopy();

            List<ExifElem> newl1 = getUpList(iw, rest1, myEE);
            newl1.add(0, myEE);

            List<ExifElem> newl2 = getUpList(iw, rest2, myEE2);
            newl2.add(0, myEE2);

            return countModifications(newl1) < countModifications(newl2) ? newl1 : newl2;
        }
    }


    public static int countModifications(List<ExifElem> l) {
        int count = 0;
        for (ExifElem ee : l) {
            if (ee.mDidChangeDate) {
                count++;
            }
        }
        return count;
    }

    public static void bwd_setDateIfNeeded(ExifElem ee, ExifElem eeToken) {
        if (ee.getMyDate() == null) {
            ee.setMyDate(getDateAdjusted(eeToken.getMyDate(), -1), null);
        } else if (ee.getMyDate().compareTo(eeToken.getMyDate()) >= 0) {
            ee.setMyDate(getDateAdjusted(eeToken.getMyDate(), -1), null);
        }
    }

    public static void fwd_setDateIfNeeded(ExifElem ee, ExifElem eeToken) {
        if (ee.getMyDate() == null) {
            ee.setMyDate(getDateAdjusted(eeToken.getMyDate(), 1), null);
        } else if (ee.getMyDate().compareTo(eeToken.getMyDate()) <= 0) {
            ee.setMyDate(getDateAdjusted(eeToken.getMyDate(), 1), null);
        }
    }

    public static List<ExifElem> createCopy(List<ExifElem> l) throws Exception {
        List<ExifElem> r = new ArrayList<>();
        for (int i = 0; i < l.size(); i++) {
            ExifElem ee = l.get(i);
            r.add(ee.createCopy());
        }
        return r;
    }

    public static Date getDateAdjusted(Date e, int factor) {
        Date newDate = new Date(e.getTime() + (factor * 2000));
        //System.out.println("Old date: " + e);
        //System.out.println("New date: " + newDate);
        return newDate;
    }

    private static void printList(IndentWriter iw, String header, List<ExifElem> l) throws Exception {
        //iw.print(header);
        for (int i = 0; i < l.size(); i++) {
            if (i > 0) {
                //iw.print(", ");
            }
            ExifElem ee = l.get(i);
            FilenameElem fne = new FilenameElem(ee.mFilename);
            //iw.print(String.valueOf(fne.mSequenceNumber));
        }
        //iw.println();
    }
}

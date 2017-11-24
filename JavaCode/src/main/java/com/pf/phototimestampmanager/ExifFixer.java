package com.pf.phototimestampmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExifFixer {

    private Date mDateMin;
    private Date mDateMax;
    private List<ExifElem> mOriginalList;

    /**
     *
     */
    public ExifFixer(
            String dateMin_yyyymmddhhmmss,
            String dateMax_yyyymmddhhmmss,
            List<ExifElem> l) throws Exception {
        if (dateMin_yyyymmddhhmmss != null) {
            mDateMin = MM.getDateFrom_YYYYMMDDHHMMSS(dateMin_yyyymmddhhmmss);
        } else {
            mDateMin = MM.getDateFrom_YYYYMMDDHHMMSS("19800101000001");
        }
        if (dateMax_yyyymmddhhmmss != null) {
            mDateMax = MM.getDateFrom_YYYYMMDDHHMMSS(dateMax_yyyymmddhhmmss);
        } else {
            mDateMax = MM.getDateFrom_YYYYMMDDHHMMSS("20990101235959");
        }
        mOriginalList = l;
        System.out.println("ExifFixer\n   dateMin: " + mDateMin + "\n   dateMax: " + mDateMax);
    }

    /**
     *
     */
    public OTuple3<List<ExifElem>, Integer, Void> getOptimumChangeList(int index, boolean debug) throws Exception {

        // processGPSChanges();

        Object[] correctedLists = new Object[mOriginalList.size()];
        IndentWriter[] iws = new IndentWriter[mOriginalList.size()];
        for (int i=0; i < correctedLists.length; i++) {
            correctedLists[i] = null;
        }
        printList(null, "*** Getting optimum list, incoming: ", mOriginalList);

        boolean didOne = false;
        List<ExifElem> c = createCopy(mOriginalList);

        System.out.println("...Now checking optimizations");
        int indexStart = 0;
        int indexEnd = c.size();
        if (index != -1) {
            indexStart = index;
            indexEnd = index+1;
        }

        for(int i=indexStart; i < indexEnd; i++) {
            ExifElem ee = c.get(i);
            FilenameElem fne = new FilenameElem(ee.mFilename);

            IndentWriter iw = null;
            if (debug) {
                iw = new IndentWriter();
                iw.setIndentChar('.');
                iw.println("*** Optimization, checking for index: " + i);
                iw.push();
            }

            List<ExifElem> candidateList = ExifFixer.createCopy(mOriginalList);
            initializeCandidateList(candidateList);
            candidateList = doIndex(i, candidateList, iw);
            if (debug) {
                iw.pop();
            }

            correctedLists[i] = candidateList;
            iws[i] = iw;


            if (debug) {
                System.out.println(iw.getString());
            }

            /*
            if (true) {
                System.out.println("\n*** DEBUG REPORT");
                System.out.println(iw.getString());
                return new OTuple3<List<ExifElem>, Integer, Void>(candidateList, 0, null);
            }
            */


            if ((i % 100) == 0) {
                System.out.println("...Now analyzed: " + i + " entries");
            }
        }

        // Find one with the lowest penalty
        System.out.println("...Checking the one with lowest penalty");
        List<ExifElem> correctionLowest = null;
        int mostOptimumIndex = -1;
        for (int i=0; i < correctedLists.length; i++) {
            List<ExifElem> current = (List<ExifElem>)correctedLists[i];
            FilenameElem fne = new FilenameElem(mOriginalList.get(i).mFilename);

            if (correctionLowest == null && current != null) {
                correctionLowest = current;
                mostOptimumIndex = i;
            } else if (current != null &&
                    countModifications(current)  <
                    countModifications(correctionLowest)) {
                correctionLowest = current;
                mostOptimumIndex = i;
            }
        }
        if (correctionLowest == null) {
            throw new Exception("There was not a single file with a timestamp for reference\n" +
                    "We have no code to manage this, but should be easy with a given start date + increment for each artifact");
        }

        //System.out.println("*** Debug data for most optimum");
        //System.out.println(iws[mostOptimumIndex].getString() + "\n");

        printList(null, "Optimized list to return to process: ", correctionLowest);
        return new OTuple3<List<ExifElem>, Integer, Void>(correctionLowest, mostOptimumIndex, null);
    }

    /**
     *
     */
    public List<ExifElem> doIndex(
            int index,
            List<ExifElem> l,
            IndentWriter iw) throws Exception {

        if (iw == null) {
            iw = new IndentWriter();
        }

        iw.println("ExifFixer.doIndex: " + index + ", dateMin: " + mDateMin + ", dateMax: " + mDateMax);

        ExifElem ee = l.get(index);
        FilenameElem fne = new FilenameElem(ee.mFilename);
        Date edate = ee.getMyDate();

        if (edate == null) {
            iw.println("Returning null. Date at this index was null, returning");
            return null;
        }
        if (mDateMin != null && mDateMin.compareTo(edate) > 0) {
            iw.println("Returning null. Min date set at: " + MM.getAs_YYYYMMDD(mDateMin) + ", and I am earlier: " + MM.getAs_YYYYMMDD(edate));
            return null;
        }
        if (mDateMax != null && mDateMax.compareTo(edate) < 0) {
            iw.println("Returning null. Max date set at: " + MM.getAs_YYYYMMDD(mDateMax) + ", and I am later: " + MM.getAs_YYYYMMDD(edate));
            return null;
        }

        // Arriving here means index is non-null and within min and max dates

        // *** Checking best alternative going upwards
        ExifElem lastEE = ee;
        Date lastDate = ee.getMyDate();
        iw.println("Testing UP. Date: " + MM.getAs_YYYYMMDD(lastDate) + ". Filename: " + lastEE.mFilename);
        iw.push();
        for (int i = index + 1; i < l.size(); i++) {
            Date tuckDate = tuckTo(lastEE.getMyDate(), 1);
            ExifElem currentEE = l.get(i);
            Date currentDate = currentEE.getMyDate();

            iw.println("");
            iw.println("[" + i + "]. Me: " + MM.getAs_YYYYMMDD_HHMMSS(currentDate) + ". Tuck: " + MM.getAs_YYYYMMDD_HHMMSS(tuckDate) + ". Filename: " + currentEE.mFilename);
            iw.push();

            // If the one we ar looking at is null, then tuck it into the last valid value
            if (currentDate == null) {
                iw.println("Tuck is used, since I was null");
                currentEE.setMyDate(tuckDate, "");
            }
            // Unacceptable, it must adhere to last order
            else if (currentDate.compareTo(lastDate) <= 0) {
                iw.println("Tuck is used since lastDate is later [me: " + MM.getAs_YYYYMMDD_HHMMSS(currentDate) + ", last: " + MM.getAs_YYYYMMDD_HHMMSS(lastDate) + "]");
                currentEE.setMyDate(tuckDate, "");
            }
            else if(currentDate.compareTo(mDateMin) < 0 || currentDate.compareTo(mDateMax) > 0) {
                currentEE.setMyDate(tuckDate, "");
            }
            // Keep this value, it is already smaller than tuck
            else if (currentDate.compareTo(tuckDate) <= 0) {
                // do nothing
            }
            // Current is not null
            // If it's value is used, would it require more than X percent of succeeding to need a change
            // Than if we tucked it next to the last one
            else if (i + 1 < l.size()) {
                iw.println("Question. Retain: " + MM.getAs_YYYYMMDD_HHMMSS(currentDate) + ", or Tuck: " + MM.getAs_YYYYMMDD_HHMMSS(tuckDate));
                double changesCurrent = countChangedUpwards(currentDate, i + 1, l);
                double changesTuck = countChangedUpwards(tuckDate, i + 1, l);
                iw.println("changesCurrent: " + changesCurrent);
                iw.println("changesTuck:    " + changesTuck);
                if (changesCurrent > changesTuck) {
                    double div = changesCurrent / ((double) l.size() - i + 1);
                    iw.println("changeCurrent > changesTuck, and changeCurrent percent against remainder: " + div);
                    if (div > 0.25) {
                        iw.println("Using Tuck, percentage was too large");
                        currentEE.setMyDate(tuckDate, "");
                    } else {
                        iw.println("Using Current, percentage within acceptable window");
                    }
                }
            }
            lastEE = currentEE;
            lastDate = currentEE.getMyDate();

            iw.pop();
        }
        iw.pop();

        // Checking best alternative going DOWNWARDS
        lastEE = ee;
        lastDate = ee.getMyDate();
        iw.println("Testing DOWN. Date: " + MM.getAs_YYYYMMDD(lastDate) + ". Filename: " + lastEE.mFilename);
        for (int i=index-1; i >= 0; i--) {
            Date tuckDate = tuckTo(lastEE.getMyDate(), -1);
            ExifElem currentEE = l.get(i);
            Date currentDate = currentEE.getMyDate();
            // If the one we ar looking at is null, then tuck it into the last valid value
            if (currentDate == null) {
                currentEE.setMyDate(tuckDate, "");
            }
            // Unacceptable, it must adhere to last order
            else if(currentDate.compareTo(lastDate) >= 0) {
                currentEE.setMyDate(tuckDate, "");
            }
            else if(currentDate.compareTo(mDateMin) < 0 || currentDate.compareTo(mDateMax) > 0) {
                currentEE.setMyDate(tuckDate, "");
            }
            // Keep this value, it is already larger than tuck
            else if (currentDate.compareTo(tuckDate) >= 0) {
                // do nothing
            }
            // Current is not null
            // If it's value is used, would it require more than X percent of succeeding to need a change
            // Than if we tucked it next to the last one
            else if(i > 0) {
                double changesCurrent = countChangedDownwards(currentDate, i-1, l);
                double changesTuck = countChangedDownwards(tuckDate, i-1, l);
                if (changesCurrent > changesTuck) {
                    double div = changesCurrent / ((double)l.size()-i+1);
                    if (div > 0.25) {
                        currentEE.setMyDate(tuckDate, "");
                    }
                }
            }
            lastEE = currentEE;
            lastDate = currentEE.getMyDate();
        }
        return l;
    }

    private static int countChangedUpwards(Date d, int index, List<ExifElem> l) {
        int changed = 0;
        for (int i=index; i < l.size(); i++) {
            ExifElem ee = l.get(i);
            Date eeDate = ee.getMyDate();
            if (eeDate != null && eeDate.compareTo(d) < 0) {
                changed++;
            }
        }
        return changed;
    }
    private static int countChangedDownwards(Date d, int index, List<ExifElem> l) {
        int changed = 0;
        for (int i=index; i >= 0; i--) {
            ExifElem ee = l.get(i);
            Date eeDate = ee.getMyDate();
            if (eeDate != null && eeDate.compareTo(d) > 0) {
                changed++;
            }
        }
        return changed;
    }

    /**
     *
     */
    public static int countModifications(List<ExifElem> l) {
        int count = 0;
        for (ExifElem ee: l) {
            if (ee.mDidChangeDate) {
                count++;
            }
        }
        return count;
    }

    /**
     *
     */
    public static List<ExifElem> createCopy(List<ExifElem> l) throws Exception {
        List<ExifElem> r = new ArrayList<>();
        for (int i=0; i < l.size(); i++) {
            ExifElem ee = l.get(i);
            r.add(ee.createCopy());
        }
        return r;
    }

    /**
     *
     */
    /*
    public static int MS_FACTOR_AT_ADJUST = 61000;
    public boolean dateAdjust(
            ExifElem eeAdjust,
            String eeAdjustFN,
            ExifElem eeReference,
            String eeReferenceFN,
            int direction, IndentWriter iw) throws Exception {
        // Direction: -1 == Adjust, Reference
        // Direction:  1 == Reference, Adjust

        Date dAdjust = eeAdjust.getMyDate();
        Date dReference = eeReference.getMyDate();

        // Check that reference time is within bounds
        if (mDateMin != null && dReference.compareTo(mDateMin) < 0) {
            return false;
        }
        if (mDateMax != null && dReference.compareTo(mDateMax) > 0) {
            return false;
        }

        if (direction < 0) {
            // iw.println("Direction: -1. " + eeAdjust.getSequenceString() + ":" + MM.getAs_YYYYMMDD_HHMMSS(eeAdjust.getMyDate()) + ", " + eeReference.getSequenceString() + ":" + MM.getAs_YYYYMMDD_HHMMSS(eeReference.getMyDate()));
            // iw.push();

            if (dAdjust == null || dAdjust.compareTo(dReference) >= 0 ||
                    (mDateMin != null && dAdjust.compareTo(mDateMin) < 0)) {
                eeAdjust.setMyDate(new Date(dReference.getTime() + (direction * MS_FACTOR_AT_ADJUST)),
                        "2. This one (" + MM.getAs_YYYYMMDD_HHMMSS(dAdjust) + "), was greater than next (" + MM.getAs_YYYYMMDD_HHMMSS(dReference) + ")");
                if (mDateMin != null && eeAdjust.getMyDate().compareTo(mDateMin) < 0) {
                    return false;
                }
            }
            return true;
        }

        else if (direction > 0) {
            // System.out.println("dAdjust: " + eeAdjust.getMyDate() + ", dRef: " + dReference + ", max: " + mDateMax);
            // iw.println("Direction: 1. " + eeAdjust.getSequenceString() + ":" + MM.getAs_YYYYMMDD_HHMMSS(eeAdjust.getMyDate()) + ", " + eeReference.getSequenceString() + ":" + MM.getAs_YYYYMMDD_HHMMSS(eeReference.getMyDate()));
            // iw.push();

            if (dAdjust == null || dAdjust.compareTo(dReference) <= 0 ||
                    (mDateMax != null && dAdjust.compareTo(mDateMax) > 0)) {
                System.out.println("....adjust: " + eeAdjustFN);
                System.out.println("....ref:    " + eeReferenceFN);
                eeAdjust.setMyDate(new Date(dReference.getTime() + (direction*MS_FACTOR_AT_ADJUST)),
                        "4. This one (" + MM.getAs_YYYYMMDD_HHMMSS(dAdjust) + "), was less than previous (" + MM.getAs_YYYYMMDD_HHMMSS(dReference) + ")");
                System.out.println(".......done");
                if (mDateMax != null && eeAdjust.getMyDate().compareTo(mDateMax) > 0) {
                    return false;
                }
            }
            return true;
        }
        throw new Exception("We should never arrive here");
    }
*/

    /**
     *
     */
    private static Date tuckTo(Date d, int direction) {
        long time = d.getTime();
        if (direction > 0) {
            time += 61000;
        } else if (direction < 0) {
            time -= 61000;
        }
        return new Date(time);
    }

    /**
     *
     */
    private static void printList(IndentWriter iw, String header, List<ExifElem> l) throws Exception {
        if (iw == null) {
            iw = new IndentWriter();
        }

        iw.println(header);
        iw.push();
        for (int i=0; i < l.size(); i++) {
            ExifElem ee = l.get(i);
            FilenameElem fne = new FilenameElem(ee.mFilename);
            iw.print(String.valueOf(fne.mSequenceNumber) + ":");
            if (ee.getMyDate() == null) {
                iw.println("null");
            } else {
                iw.println(MM.getAs_YYYYMMDD_HHMMSS(ee.getMyDate()));
            }
        }
        iw.pop();
        iw.println();
    }

    /**
     *
     */
    private void initializeCandidateList(List<ExifElem> exifElems) throws Exception {
        Date minDate = null;
        Date maxDate = null;

        for (ExifElem e: exifElems) {
            Date edate = e.getMyDate();
            if (minDate == null || (edate != null && edate.compareTo(minDate) < 0)) {
                minDate = edate;
            }
            if (maxDate == null || (edate != null && edate.compareTo(maxDate) > 0)) {
                maxDate = edate;
            }
        }

        if (minDate == null) {
            if (mDateMin == null) {
                throw new Exception("I cannot find a min date to set");
            } else {
                exifElems.get(0).setMyDate(mDateMin, "There was no minimal date and you are the first one");
            }
        }
        if (maxDate == null) {
            if (mDateMax ==  null) {
                throw new Exception("I cannot find a max date to set");
            } else {
                exifElems.get(exifElems.size()-1).setMyDate(mDateMax, "There was no maximal date and you are the last one");
            }
        }
    }
/*
    private void processGPSChanges() throws Exception {
        boolean existsGPSTimestamp = false;
        for (int i=0; i < mOriginalList.size(); i++) {
            ExifElem ee = mOriginalList.get(i);
            // We found DateStamp in new Nexus 6 photos
            //if (ee.mGPSDateStamp != null) {
            //    throw new Exception("GPS error 0: " + ee.toStringOneLineOriginalData());
            //}
            if (ee.mGPSTimeStamp != null) {
                if (ee.mGPSTimeStamp.trim().length() < 8) {
                    throw new Exception("GPS error 1: " + ee.toStringOneLineOriginalData());
                }
                existsGPSTimestamp = true;
            }
        }

        for (int i=0; i < mOriginalList.size(); i++) {
            ExifElem ee= mOriginalList.get(i);
            if (ee.mGPSTimeStamp == null) {
                boolean set = false;

                // Find GPS data before entry
                if (i > 0) {
                    for (int j=i-1; j >= 0; j--) {
                        ExifElem ee2= mOriginalList.get(j);
                        if (ee2.mGPSTimeStamp != null) {
                            ee.cloneGPSData(ee2);
                            set = true;
                            break;
                        }
                    }
                }
                // Find GPS data after entry
                if (!set) {
                    if (i+1 >= mOriginalList.size()) {
                        throw new Exception("GPS error 3, last element did not get GPS data: " + ee.toStringOneLineOriginalData());
                    }
                    for (int j=i+1; j < mOriginalList.size(); j++) {
                        ExifElem ee2= mOriginalList.get(j);
                        if (ee2.mGPSTimeStamp != null) {
                            ee.cloneGPSData(ee2);
                            set = true;
                            break;
                        }
                    }
                }
            }
        }
    }
*/

//    private static String getAverage

/*
    private void iterate() {
        for (int i=0; i < l.size(); i++) {
            ExifElem ee = l.get(i);
            if (ee.getMyDate() != null && within min/max) {
                testI(i);
            }
        }
    }

    private List<ExifElem> foo(int cindex, List<ExifElem> l) {
        // We are at index cindex
        // cindex is within date ranges and not null
        //



        List<ExifElem> r = new ArrayList<>();

        // Go up
        int cP1Index;
        int mcount = 0;
        if (cP1Index.getDate() == null) {
            cP1Index.setDate(false, tuckTo(cindex, 1));
        } else if cP1Index < cindex {
            cP1Index.setDate(true, tuckTo(cindex, 1));
            mcount = 1;
        }
        if (cP1Index is last){
            return mcount;
        } else {
            mcount += fooIter(cP1Index);
        }




        }
        */
}
// Index i
// if (i is not within min / max range)



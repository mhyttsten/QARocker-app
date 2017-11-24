package com.pf.phototimestampmanager;


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("Starting...");
            new Main().mainImpl(args);
            System.out.println("Exiting without exception...");
        } catch(Exception exc) {
            exc.printStackTrace();
        }
    }

    public static String getDate(String dstr, String hhmmss) throws Exception {
        if (dstr == null || dstr.toLowerCase().equals("null")) {
            return null;
        }
        String result = null;

        if (dstr.length() == 8) {
            java.util.Date d = MM.getDateFrom_YYYYMMDD(null, dstr);
            if (hhmmss == null || hhmmss.length() != 6) {
                throw new Exception("HHMMSS must be 6 characters: " + hhmmss);
            }
            result = dstr + hhmmss;
        } else if (dstr.length() == 14) {
            java.util.Date d = MM.getDateFrom_YYYYMMDDHHMMSS(dstr);
            result = dstr;
        } else {
            throw new Exception("Unknown date format, expected YYYYMMDDHHMMSS: " + dstr);
        }

        // System.out.println("......getDate, returning: " + result + ", from argument: " + dstr);
        return result;
    }

    public static void mainImpl(String[] args) throws Exception {
        String dateMin_yyyymmddhhmmss = null;
        String dateMax_yyyymmddhhmmss = null;

        System.out.println("We are in mainImpl");
        if (args == null || args.length == 0 || args[0].trim().length() == 0) {
            System.out.println("Expected directory to process as argument");
            return;
        }
        System.out.println("Directory: " + args[0]);
        if (args.length > 1) {
            System.out.println("argsv[1]: " + args[1]);
            if (args[1].equals("-list")) {
                System.out.println("*** OK, JUST LISTING THE ELEMENTS");
                List<ExifElem> ees  = readExifElems(args[0], null);
                printExifElems(ees);
                return;
            }
            dateMin_yyyymmddhhmmss = getDate(args[1], "000001");
        }
        if (args.length > 2) {
            System.out.println("argsv[2]: " + args[2]);
            dateMax_yyyymmddhhmmss = getDate(args[2], "235959");
        }
        if (dateMin_yyyymmddhhmmss != null && dateMax_yyyymmddhhmmss != null &&
                dateMin_yyyymmddhhmmss.compareTo(dateMax_yyyymmddhhmmss) >= 0) {
            throw new Exception("DateMin: " +  dateMin_yyyymmddhhmmss + ", is larger than DateMax: " + dateMax_yyyymmddhhmmss);
        }

        System.out.println("\n\n\n");
        System.out.println("****************************************************************");
        System.out.println("****************************************************************");
        System.out.println("****************************************************************");
        System.out.println("****************************************************************");
        System.out.println("Start of execution, analyzing and fixing");
        String directory = args[0].trim();

        // *** Convert to supported file types
        System.out.println("\n*** Phase: Converting Files");
        convertToSupportedFileTypes(directory);

        // *** Get all ExifElem instances
        List<ExifElem> lexifs = getExifData(directory); //, dateMin_YYYYMMDD, dateMax_YYYYMMDD);



        // *** Ask for min / max dates to use
        System.out.print("...Current min date is set to: " + dateMin_yyyymmddhhmmss + ", enter new min one [YYYYMMDD[HHMMSS]]: ");
        String dateNewMin = MM.readLine();
        dateNewMin = MM.getActualStringOr(dateNewMin, null);
        System.out.print("...Current max date is set to: " + dateMax_yyyymmddhhmmss + ", enter new max one [YYYYMMDD[HHMMSS]]: ");
        String dateNewMax = MM.readLine();
        dateNewMax = MM.getActualStringOr(dateNewMax, null);
        dateNewMin = getDate(dateNewMin, "000001");
        if (dateNewMin != null) {
            dateMin_yyyymmddhhmmss = dateNewMin;
        }
        dateNewMax = getDate(dateNewMax, "235959");
        if (dateNewMax != null) {
            dateMax_yyyymmddhhmmss = dateNewMax;
        }
        if (dateMin_yyyymmddhhmmss != null && dateMax_yyyymmddhhmmss != null &&
                dateMin_yyyymmddhhmmss.compareTo(dateMax_yyyymmddhhmmss) >= 0) {
            throw new Exception("DateMin: " +  dateMin_yyyymmddhhmmss + ", is larger than DateMax: " + dateMax_yyyymmddhhmmss);
        }
        if (dateMin_yyyymmddhhmmss == null) {
            System.out.println("...Min date to use: <null> literarly");
        } else {
            System.out.println("...Min date to use: " + dateMin_yyyymmddhhmmss + " (as string)");
        }
        if (dateMax_yyyymmddhhmmss == null) {
            System.out.println("...Max date to use: <null> literarly");
        } else {
            System.out.println("...Max date to use: " + dateMax_yyyymmddhhmmss + " (as string)");
        }



        // *** Now fix the order
        System.out.println("\n*** Phase: Calculate Optimum Order");
        System.out.print("...Press return to continue:");
        MM.readLine();
        OTuple3<List<ExifElem>, Integer, Void> optimumResult = new ExifFixer(
                dateMin_yyyymmddhhmmss,
                dateMax_yyyymmddhhmmss,
                lexifs)
                .getOptimumChangeList(-1, false);
        List<ExifElem> changeList = optimumResult._o1;
        int optimumIndex = optimumResult._o2;

        // Tmp
        /*
        {
            OTuple3<List<ExifElem>, Integer, Void> or2 = new ExifFixer(
                    dateMin_yyyymmddhhmmss,
                    dateMax_yyyymmddhhmmss,
                    lexifs)
                    .getOptimumChangeList(optimumIndex, true);
        }
        */

        ExifElem eeMostOptimumIndex = optimumResult._o1.get(optimumIndex);
        FilenameElem fneMostOptimumSequenceNumber = new FilenameElem(eeMostOptimumIndex.mFilename);
        if (changeList.size() != lexifs.size()) {
            throw new Exception("Changelist size: " + changeList.size() + ", differs from EXIF list size: " + lexifs.size());
        }
        int entriesChange = 0;
        int entriesUpdate = 0;
        int entriesDidChangeGPS = 0;
        int entriesNoChange = 0;
        for (int i = 0; i < changeList.size(); i++) {
            ExifElem ee = changeList.get(i);
            FilenameElem fne = new FilenameElem(ee.mFilename);
            if (ee.mDidChangeDate) {
                entriesChange++;
                System.out.println("...[" + i + "]." + fne.mSequenceString + ".DATE_CHANGE.: " + MM.getAs_YYYYMMDD_HHMMSS(ee.getMyDate())
                        + ", from: " + MM.getAs_YYYYMMDD_HHMMSS(ee.mPreviousDate) + ", REASON: " + ee.mAdjustmentReason);
            } else if (ee.mNeedsUpdate) {
                entriesUpdate++;
                System.out.println("...[" + i + "]." + fne.mSequenceString + ".UPDATE......: " + MM.getAs_YYYYMMDD_HHMMSS(ee.getMyDate()) + "......................." + ", REASON: " + ee.mAdjustmentReason);
            } else if (ee.mDidChangeGPS) {
                entriesDidChangeGPS++;
                System.out.println("...[" + i + "]." + fne.mSequenceString + ".GPS_CHANGE..: " + MM.getAs_YYYYMMDD_HHMMSS(ee.getMyDate()) + "......................." + ", REASON: " + ee.mAdjustmentReason);
            } else {
                entriesNoChange++;
                System.out.println("...[" + i + "]." + fne.mSequenceString + ".NO_CHANGE...: " + MM.getAs_YYYYMMDD_HHMMSS(ee.getMyDate())
                        + "......................." + ", REASON: " + ee.mAdjustmentReason);
            }
        }
        System.out.println("...Changelist, entries: " + changeList.size() + ", input: " + lexifs.size());
        System.out.println("...Optimum index: " + optimumIndex);
        System.out.println("...Number of entries needing date change: " + entriesChange);
        System.out.println("...Number of entries needing GPS change:  " + entriesDidChangeGPS);
        System.out.println("...Number of entries needing update:      " + entriesUpdate);
        System.out.println("...Number of entries with no change:      " + entriesNoChange);
        System.out.println("...First date: " + MM.getAs_YYYYMMDD_HHMMSS(changeList.get(0).getMyDate()));
        System.out.println("...Last date:  " + MM.getAs_YYYYMMDD_HHMMSS(changeList.get(changeList.size() - 1).getMyDate()));

        // *** Modify the Exif Data
        IndentWriter iwManual = new IndentWriter();
        boolean iwManualSet = false;
        IndentWriter iwAlbumReorder = new IndentWriter();
        boolean iwAlbumReorderSet = false;
        iwAlbumReorder.println("###### GOOGLE PHOTOS ALBUM ORDER CHANGE PROBABLY REQUIRED FOR THESE");
        iwAlbumReorder.push();
        boolean didEncounterNonAutomaticFileType = false;
        iwManual.println("###### SET TIME MANUALLY IN GOOGLE PHOTOS");
        iwManual.push();
        System.out.println("\n*** Phase: Modify EXIF data");
        System.out.print("...Press return to continue");
        MM.readLine();
        System.out.println("...Continuing");
        System.out.print("...Has now processed: ");
        for (int i = 0; i < changeList.size(); i++) {
            ExifElem ee = changeList.get(i);
            FilenameElem fne = new FilenameElem(ee.mFilename);

            if (fne.updateAlbumManually()) {
                iwAlbumReorder.println("File: " + fne.mCompleteFilename + ", is probably last and needs to be reordered in Google Photos album");
                iwAlbumReorderSet = true;
            }

            if (!fne.canChangeDate()) {
                didEncounterNonAutomaticFileType = true;
            }

            if ((ee.mDidChangeDate || ee.mNeedsUpdate) && !fne.canChangeDate()) {
                iwManualSet = true;
                iwManual.println("Set:      " + fne.mCompleteFilename + ", to date: " + ee.getMyDate_exifToolFormat()
                        + "\nOriginal:      " + ee.mPreviousDate
                        + "\nDidChangeDate: " + ee.mDidChangeDate
                        + "\nNeedsUpdate:   " + ee.mNeedsUpdate);
            } else if (ee.mDidChangeDate || ee.mNeedsUpdate) {
                String[] command = null;
                command = new String[] {
                        "exiftool",
                        "-F",
                        "-AllDates=" + ee.getMyDate_exifToolFormat(),
                        fne.mCompleteFilename
                };
                execExifToolCommandAndRemoveBackupFile(fne, command);
            }
            if (fne.canChangeGPSTime()) {
                /*
                ee.mGPSTimeStamp = MM.getAs_HHCMMCSS(ee.getMyDate()) + ".1";
                ee.mGPSDateStamp = MM.getAs_YYYYCMMCDD(ee.getMyDate());
                System.out.println("GPSTimeStamp became: " + ee.mGPSTimeStamp);
                System.out.println("GPSDateStamp became: " + ee.mGPSDateStamp);
                String[] command = createGPSChangeCommand(ee, fne);
                execExifToolCommandAndRemoveBackupFile(fne, command);
                */
            }

            if ((i % 10) == 0) {
                System.out.print(i + ", ");
            }
        }
        iwAlbumReorder.pop();
        iwManual.pop();

        // *** Validating modified exif data
        System.out.println("\n****************************************************************");
        System.out.println("End of execution, validating");
        System.out.println("\n*** Phase: Re-reading EXIF data, and verifying correct order");
        System.out.println("...Press return to continue");
        MM.readLine();
        List<ExifElem> newExifList = validate(directory, dateMin_yyyymmddhhmmss, dateMax_yyyymmddhhmmss);
        System.out.println("...First date: " + MM.getAs_YYYYMMDD_HHMMSS(changeList.get(0).getMyDate()));
        System.out.println("...Last date:  " + MM.getAs_YYYYMMDD_HHMMSS(changeList.get(changeList.size()-1).getMyDate()));
        System.out.println("...All date order validation performed, and everything checked out OK");

        if (iwAlbumReorderSet) {
            System.out.println("\n" + iwAlbumReorder.toString());
        }

        if (iwManualSet) {
            System.out.println("\n" + iwManual.toString());
        } else if (didEncounterNonAutomaticFileType) {
            System.out.println("\nI did encounter file types I cannot do exiftool modify on but they do not require any modification so all good");
        }
    }

    private static void assignClosestLongLat(int index, List<ExifElem> l) {
        ExifElem ee = l.get(index);

        ExifElem eePast = null;
        int eePastIndex = -1;
        if (index > 0) {
            for (int i=index-1; i >= 0; i--) {
                eePast = l.get(i);
                if (eePast.mGPSLatitude != null && eePast.mGPSLongitude != null) {
                    eePastIndex = i;
                    break;
                }
                eePast = null;
            }
        }

        ExifElem eeFuture = null;
        int eeFutureIndex = -1;
        if (index + 1 < l.size()) {
            for (int i=index+1; i < l.size(); i++) {
                eeFuture = l.get(i);
                if (eeFuture.mGPSLatitude != null && eeFuture.mGPSLongitude != null) {
                    eeFutureIndex = i;
                    break;
                }
                eeFuture = null;
            }
        }

        if (eePast != null && eeFuture != null) {
            ExifElem eeWinner = (index-eePastIndex) <= (eeFutureIndex-index) ? eePast : eeFuture;
            ee.mGPSLatitude = eeWinner.mGPSLatitude;
            ee.mGPSLongitude = eeWinner.mGPSLongitude;
        } else if (eePast != null) {
            ee.mGPSLatitude = eePast.mGPSLatitude;
            ee.mGPSLongitude = eePast.mGPSLongitude;
        } else if (eeFuture != null) {
            ee.mGPSLatitude = eeFuture.mGPSLatitude;
            ee.mGPSLongitude = eeFuture.mGPSLongitude;
        }
    }

    private static String[] createGPSChangeCommand(ExifElem ee, FilenameElem fne) throws Exception {
        ArrayList<String> al = new ArrayList<>();

        al.add("exiftool");
        if (ee.mGPSTimeStamp == null) {
            throw new Exception("Expected GPS data to be written: " + ee.toStringOneLineOriginalData());
        }

        if (ee.mGPSTimeStamp != null) {
            al.add("-gpstimestamp=" + ee.mGPSTimeStamp);
        }

        if (ee.mGPSDateStamp != null) {
            al.add("-gpsdatestamp=" + ee.mGPSDateStamp);
        }

        /*
        // Below not needed, we are changing all GPS Timestamps to the original timestamp
        if (ee.mGPSLatitude != null) {
            String latitude = ee.mGPSLatitude;
            String latitudeRef = "";
            if (latitude.endsWith(" N")) {
                latitudeRef = "North";
            } else if(latitude.endsWith(" S")) {
                latitudeRef = "South";

            } else {
                throw new Exception("Does not end with N or S: " + ee.toStringOneLineOriginalData() + ", fn: " + ee.mFilename);
            }
            latitude = latitude.substring(0, latitude.length() - 2);
            latitude = latitude.replace("\"", "\\042");
            al.add("-gpslatitude=" + latitude);
            al.add("-gpslatituderef=" + latitudeRef);
        }
        if (ee.mGPSLongitude != null) {
            String longitude = ee.mGPSLongitude;
            String longitudeRef = "";
            if (longitude.endsWith(" W")) {
                longitudeRef = "West";
            } else if(longitude.endsWith(" E")) {
                longitudeRef = "East";
            } else {
                throw new Exception("Does not end with W or E: " + ee.toStringOneLineOriginalData() + ", fn: " + ee.mFilename);
            }
            longitude = longitude.substring(0, longitude.length() - 2);
            longitude = longitude.replace("\"", "\\042");
            al.add("-gpslongitude=" + longitude);
            al.add("-gpslongituderef=" + longitudeRef);
        }
        */
        al.add(fne.mCompleteFilename);

        return al.toArray(new String[al.size()]);
    }

    /**
     *
     */
    private static void execExifToolCommandAndRemoveBackupFile(
            FilenameElem fne,
            String[] command) throws Exception {
        StringBuffer strb = new StringBuffer();

        StringBuffer ncmd = new StringBuffer();
        for (int i=0; i < command.length; i++) {
            command[i] = command[i].replace("\"", "\\\"");
            strb.append(command[i]);
        }
        System.out.println(strb.toString());

        OTuple3<Integer, String, String> ot = exec(command);
        String backupFile = fne.mCompleteFilename + "_original";
        if (!MM.fileExistsIsAFileAndNotHidden(backupFile)) {
            throw new Exception("Could not find an original file after update: " + backupFile);
        } else {
            exec(new String[]{"rm", "-f", backupFile});
            if (MM.fileExistsIsAFileAndNotHidden(backupFile)) {
                throw new Exception("File still existed after rm: " + backupFile);
            }
        }
    }

    /**
     *
     */
    public static List<ExifElem> validate(
            String directory,
            String dateMin_yyyymmddhhmmss,
            String dateMax_yyyymmddhhmmss) throws Exception {
        ExifElem.ExifStat exifStat = new ExifElem.ExifStat();
        List<ExifElem> lexifs = getExifData(directory);

        System.out.println("\nFirst of all, here is a list of all the updated entries");
        printExifElems(lexifs);

        OTuple3<List<ExifElem>, Integer, Void> optimumResult = new ExifFixer(
                dateMin_yyyymmddhhmmss,
                dateMax_yyyymmddhhmmss,
                lexifs)
                .getOptimumChangeList(-1, false);
        List<ExifElem> changeList = optimumResult._o1;
        int optimumIndex = optimumResult._o2;
        System.out.println("...Most optimum index: " + optimumIndex);
        ExifElem eeMostOptimumIndex = optimumResult._o1.get(optimumIndex);
        FilenameElem fneMostOptimumSequenceNumber = new FilenameElem(eeMostOptimumIndex.mFilename);
        System.out.println("...Most optimum sequence number: " + fneMostOptimumSequenceNumber.mSequenceString);
        if (changeList.size() != lexifs.size()) {
            throw new Exception("Changelist size: " + changeList.size() + ", differs from EXIF list size: " + lexifs.size());
        }
        System.out.println("...Changelist, entries: " + changeList.size() + ", input: " + lexifs.size());
        ExifElem ee1 = null;
        FilenameElem fne1 = null;
        for (int i = 0; i < changeList.size(); i++) {
            ExifElem ee2 = changeList.get(i);
            FilenameElem fne2 = new FilenameElem(ee2.mFilename);

            if ((ee2.mDidChangeDate || ee2.mNeedsUpdate)
                    && fne2.canChangeDate()) {
                throw new Exception("A date was marked as change, validation failed: " + fne2.mCompleteFilename);
            }

            boolean error = false;
            if (ee2.getMyDate() == null) {
                error = true;
            }
            if (dateMin_yyyymmddhhmmss != null) {
                Date dMin = MM.getDateFrom_YYYYMMDDHHMMSS(dateMin_yyyymmddhhmmss);
                if (ee2.getMyDate().compareTo(dMin) < 0) {
                    error = true;
                }
            }
            if (dateMax_yyyymmddhhmmss != null) {
                Date dMax = MM.getDateFrom_YYYYMMDDHHMMSS(dateMax_yyyymmddhhmmss);
                if (ee2.getMyDate().compareTo(dMax) > 0) {
                    error = true;
                }
            }
            if (error) {
                throw new Exception("Date either null or out-of bounds:\n" +
                        "File: " + fne2.mCompleteFilename + "\n" +
                        "Date: " + ee2.getMyDate() + "\n" +
                        "Min date: " + dateMin_yyyymmddhhmmss + "\n" +
                        "Max date: " + dateMax_yyyymmddhhmmss);
            }

            if (i == 0) {
                ee1 = changeList.get(i);
                fne1 = new FilenameElem(ee1.mFilename);
            } else {
                if (ee1.getMyDate().compareTo(ee2.getMyDate()) > 0) {
                    throw new Exception("Date order discrepancy found:\n" +
                            "File1: " + fne1.mCompleteFilename + "\n" +
                            "Date1: " + ee1.getMyDate() + "\n" +
                            "File2: " + fne2.mCompleteFilename + "\n" +
                            "Date2: " + ee2.getMyDate());
                } else {
                    ee1 = ee2;
                    fne1 = fne2;
                }
            }
        }
        return changeList;
    }

    /**
     *
     */
    public static OTuple3<Integer, String, String> exec(String[] args) throws Exception {
        // System.out.println("WILL EXEC: " + command);
        Runtime rt = Runtime.getRuntime();
 //       System.out.println("Now executing: $ " + Arrays.toString(args));
        Process p = rt.exec(args);
        InputStream istreamInput = p.getInputStream();
        InputStream istreamError = p.getErrorStream();
        int returnValue = p.waitFor();
        String stringOutput = new String(MM.readData(istreamInput));
        String stringError = new String(MM.readData(istreamError));

        OTuple3<Integer, String, String> ot = new OTuple3<>(new Integer(returnValue), stringOutput, stringError);
        if (returnValue != 0) {
            IndentWriter iw = new IndentWriter();
            iw.println("*** Error for command: " + Arrays.toString(args));
            iw.println("    Exit code: " + ot._o1);
            iw.println("    Output:    " + ot._o2);
            iw.println("    Error:    " + ot._o3);
            throw new Exception(iw.getString());
        }

        if (ot._o3.length() > 0) {
            System.out.println("*** stderr had data for command string: " + Arrays.toString(args));
            System.out.println("    message:  " + ot._o3);
        }

        //System.out.println("*** Output");
        //System.out.println(stringInput);
        //System.out.println("");
        //System.out.println("*** Error");
        //System.out.println(stringError);
        return ot;
    }

    /**
     *
     */
    private static void printReport(List<ExifElem> exifElems) {
        Date dateMin = null;
        Date dateMax = null;
        int countTotal = 0;
        int countWODate = 0;
        int countCorrection = 0;

        ExifElem elast = null;
        for (ExifElem e : exifElems) {
            countTotal++;
            Date eDate = e.getMyDate();
            if (eDate != null) {
                if (dateMin == null || dateMin.compareTo(eDate) > 0) {
                    dateMin = eDate;
                }
                if (dateMax == null || dateMax.compareTo(eDate) < 0) {
                    dateMax = eDate;
                }
            } else {
                countWODate++;
            }

            if (elast != null) {
                if (elast.getMyDate() != null
                    && e.getMyDate() != null
                    && elast.getMyDate().compareTo(e.getMyDate()) >= 0) {
                    // System.out.println("...had to change: " + e.mFilename);
                    countCorrection++;
                }
            }
            elast = e;
        }

        System.out.println("...Report");
        System.out.println("......Total: " + countTotal);
        System.out.println("......Without date: " + countWODate);
        System.out.println("......Corrections:  " + countCorrection);
        if (dateMin != null) {
            System.out.println("......Date min: " + MM.getAs_YYYYMMDD(dateMin));
        } else {
            System.out.println("......Date min: null");
        }
        if (dateMax != null) {
            System.out.println("......Date max: " + MM.getAs_YYYYMMDD(dateMax));
        } else {
            System.out.println("......Date max: null");
        }
    }

    /**
     *
     */
    private static void convertToSupportedFileTypes(String directory) throws Exception {
        System.out.println("Converting Files");
        RetrieveFiles rf = new RetrieveFiles(directory);
        List<File> flist = rf.mTargetFiles;
        System.out.println("...Number of entries to process: " + flist.size());
        int convertCount_png2jpg = 0;
        for (int i=0; i < flist.size(); i++) {
            File f = flist.get(i);
            FilenameElem fnp = new FilenameElem(f.getName());

            // Convert .PNG to .JPG
            if (fnp.mFileType == FilenameElem.FT_PNG) {
                String newFilename = fnp.getFilenameAsJPG();
                // System.out.println("Converting: " + fnp.mCompleteFilename);
                // System.out.println("To: " + newFilename);
                String[] command = new String[] {"convert", fnp.mCompleteFilename, newFilename };
                System.out.println("...Now performing conversion: " + Arrays.toString(command));
                OTuple3<Integer, String, String> ot = exec(command);
                if (!MM.fileExistsIsAFileAndNotHidden(newFilename)) {
                    throw new Exception("Could not find converted file\n...Orig: " + fnp.mCompleteFilename + "\n...Converted: " + newFilename);
                } else {
                    ot = exec(new String[] {"rm", "-f", fnp.mCompleteFilename} );
                    convertCount_png2jpg++;
                }
            }
        }
        System.out.println("...Done. Converted count from .PNG to .JPG: " + convertCount_png2jpg);
    }

    private static void printExifElems(List<ExifElem> l) throws Exception {
        System.out.println("Printing one line items for all ExifElems");
        for (int i=0; i < l.size(); i++) {
            ExifElem ee = l.get(i);
            System.out.println("...[" + i + "]. " + ee.mFilename + ": " + ee.toStringOneLineOriginalData());
        }
        System.out.println("...Done");
    }

    /**
     *
     */
    private static List<ExifElem> readExifElems(String directory, ExifElem.ExifStat exifStat) throws Exception {
        if (exifStat == null) {
            exifStat = new ExifElem.ExifStat();
        }

        System.out.println("Retrieve EXIF data");
        RetrieveFiles rf = new RetrieveFiles(directory);
        List<File> flist = rf.mTargetFiles;
        List<ExifElem> lexifs = new ArrayList<>();
        System.out.print("...Done with: ");
        for (int i=0; i < flist.size(); i++) {
            File f = flist.get(i);

            FilenameElem fne = new FilenameElem(f.getName());
            if (!fne.mIsValidFilename) {
                throw new Exception("Encountered non-valid filename: " + fne.mCompleteFilename);
            }
            OTuple3<Integer, String, String> ot = exec(new String[] {"exiftool", "-AllDates", fne.mCompleteFilename} );
            ExifElem exifElem = new ExifElem(fne.mCompleteFilename, ot._o2, exifStat);
            ot = exec(new String[] {"exiftool", "-gpslatitude", fne.mCompleteFilename} );
            exifElem.setGPSLatitude(getExifString(ot._o2));
            ot = exec(new String[] {"exiftool", "-gpslongitude", fne.mCompleteFilename} );
            exifElem.setGPSLongitude(getExifString(ot._o2));
            ot = exec(new String[] {"exiftool", "-gpstimestamp", fne.mCompleteFilename} );
            exifElem.setGPSTimeStamp(getExifString(ot._o2));
            ot = exec(new String[] {"exiftool", "-gpsdatestamp", fne.mCompleteFilename} );
            exifElem.setGPSDateStamp(getExifString(ot._o2));

            lexifs.add(exifElem);

            if ((i % 10) == 0) {
                System.out.print(i + ", ");
            }
        }
        System.out.println("");
        System.out.println("...Done");
        return lexifs;
    }

    /**
     *
     */
    private static String getExifString(String str) throws Exception {
        int io = str.indexOf(":");
        if (io == -1) {
            return null;
        }
        if (io + 2 >= str.length()) {
            return "";
        }
        return str.substring(io+1).trim();
    }


    /**
     *
     */
    private static List<ExifElem> getExifData(String directory) throws Exception {

        System.out.println("\n*** Phase: Get Exif Data");
        ExifElem.ExifStat exifStat = new ExifElem.ExifStat();
        List<ExifElem> lexifs = readExifElems(directory, exifStat);

        printExifElems(lexifs);

        List<ExifElem> lexifsCopy = new ArrayList<>();
        lexifsCopy.addAll(lexifs);
        Collections.sort(lexifsCopy, new Comparator<ExifElem>() {
            public int compare(ExifElem e1, ExifElem e2) {
                if (e1.getMyDate() != null && e2.getMyDate() == null) {
                    return -1;
                }
                if (e1.getMyDate() == null && e2.getMyDate() != null) {
                    return 1;
                }
                if (e1.getMyDate() == null && e2.getMyDate() == null) {
                    return -1;
                }
                return e1.getMyDate().compareTo(e2.getMyDate());
            }
        });
        System.out.println("Printing result in date order");
        for (int i=0; i < lexifs.size(); i++) {
            ExifElem eeFileOrder = lexifs.get(i);
            ExifElem eeDateOrder = lexifsCopy.get(i);
            System.out.println(
                    eeDateOrder.mFilename + ": " + MM.getAs_YYYYMMDD_HHMMSS(eeDateOrder.getMyDate()) + "\t" +
                            eeFileOrder.mFilename + ": " + MM.getAs_YYYYMMDD_HHMMSS(eeFileOrder.getMyDate()));
        }
        System.out.println("Printing result in file order");
        for (int i=0; i < lexifs.size(); i++) {
            ExifElem eeFileOrder = lexifs.get(i);
            ExifElem eeDateOrder = lexifsCopy.get(i);
            System.out.println(
                    eeFileOrder.mFilename + ": " + MM.getAs_YYYYMMDD_HHMMSS(eeFileOrder.getMyDate()) + "\t" +
                    eeDateOrder.mFilename + ": " + MM.getAs_YYYYMMDD_HHMMSS(eeDateOrder.getMyDate()));
        }

        // *** Print findings, and ask for min/max dates
        System.out.println("\n*** Phase: Print Result and Input Parameter");
        printReport(lexifs);

        return lexifs;
    }
}


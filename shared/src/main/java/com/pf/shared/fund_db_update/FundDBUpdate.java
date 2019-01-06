package com.pf.shared.fund_db_update;

import com.pf.shared.datamodel.DB_FundInfo;
import com.pf.shared.datamodel.D_FundInfo;
import com.pf.shared.datamodel.D_FundInfo_Validator;
import com.pf.shared.extract.ExtractFromHTML_Helper;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FundDBUpdate {

    /* Manual
     1. Create /tmp2/funddbupdate and make sure it is empty

     2. Put your fund TSV, and HTML files here

     a. SEB and SPP
        Update their spreadsheets
        Then download as TSV
        Filenames should be: seb_<index>.tsv, spp_<index>.tsv

     b. PPM: TBD
        Filename should be: ppm_<index>html

     c. Vanguard is HTML
        Go to each table on their HTML page and do 'Save As'
        Use filenames:
           vgd_Vanguard_<index>.html  // For Vanguard funds
           vgd_Aberdeen_<index>.html  // For non-Vanguard funds
           *** OBS: Important that non-Vanguard funds do not contain 'Vanguard' in filename
       List of Vanguard Mutual Funds (first entry: 500 Index Admiral Shares)
          https://investor.vanguard.com/mutual-funds/list#/mutual-funds/name/month-end-returns
       List of Vanguard ETF (first entry: Communication Services ETF)
          https://investor.vanguard.com/mutual-funds/list#/etf/name/month-end-returns
       List of non-Vanguard funds
       Aberdeen
          https://personal.vanguard.com/us/funds/other/bytype?FundFamilyId=31698
       BlackRock
       Fidelity
       Goldman Sachs
       HSBC
       MotelyFool
       Morgan Stanley
       Pimco
       Primecap
       T. Rowe

     3. Download latest fundinfo-db-master.bin into this directory
        https://console.firebase.google.com/project/ql-magnushyttsten/storage/ql-magnushyttsten.appspot.com/files~2Fbackend~2F
    */

    //------------------------------------------------------------------------
    public static void main(String[] args) {
        try {
            mainImpl(args);
        } catch (Exception exc) {
            System.out.println(exc);
            exc.printStackTrace();
        }
    }

    public static class Entry {
        public Entry(String msName, String msURL, String origName) {
            _msName = msName;
            _msURL = msURL;
            _origName = origName;
        }
        public String _msName;
        public String _msURL;
        public String _origName;
    }

    //------------------------------------------------------------------------
    public static final String DIR = "/tmp2/funddbupdate";
    public static final String DB_ABSOLUTE_FILENAME = DIR + "/fundinfo-db-master.bin";
    public static final String FILE_TYPE_SEB = "seb_";
    public static final String FILE_TYPE_SPP = "spp_";
    public static final String FILE_TYPE_VGD = "vgd_";
    public static final String FILE_TYPE_PPM = "ppm_";

    //------------------------------------------------------------------------
    public static void mainImpl(String[] args) throws Exception {
//        doit(true, D_FundInfo.TYPE_SEB, FILE_TYPE_SEB);
//        doit(true, D_FundInfo.TYPE_SPP, FILE_TYPE_SPP);

        doit(false, D_FundInfo.TYPE_VANGUARD, FILE_TYPE_VGD);
    }

    //------------------------------------------------------------------------
    public static void doit(boolean testHTMLExtraction,
                            String type,
                            String filePrefix) throws Exception {

        // Get fund database
        byte[] fileDBDataBA = MM.fileReadFrom(DB_ABSOLUTE_FILENAME);
        if (fileDBDataBA == null || fileDBDataBA.length <= 0) {
            throw new Exception("Could not find: " + DB_ABSOLUTE_FILENAME);
        }
        DB_FundInfo.initialize(fileDBDataBA, true);
        D_FundInfo_Validator fiv = new D_FundInfo_Validator(DB_FundInfo.getAllFundInfos());
        fiv.process();
        if (fiv._error) {
            System.out.println("*** ERROR ****: Fund database in invalid, will not initiate extraction\n" + fiv._iwErrors.getString());
            return;
        }
        System.out.println("Initialized from a valid fund DB file, number of bytes: " + fileDBDataBA.length);

        // Get all the files to analyze
        List<File> tmpList = new ArrayList<>();
        String error = MM.fileList(tmpList, DIR, false);
        if (error != null) {
            System.out.println("Error reading files: " + error);
            return;
        }
        List<D_FundInfo> fisFiles = new ArrayList<>();
        for (File f : tmpList) {
            if (!f.getName().startsWith(filePrefix)) {
                continue;
            }
            System.out.println("Adding file: " + f.getName() + ", for analysis");
            byte[] data = MM.fileReadFrom(f);
            String errorStr = null;
            if (type.equals(D_FundInfo.TYPE_SEB)) {
                errorStr = FundDBUpdate_TSV.update(fisFiles, D_FundInfo.TYPE_SEB, data);
            }
            else if (type.equals(D_FundInfo.TYPE_SPP)) {
                errorStr = FundDBUpdate_TSV.update(fisFiles, D_FundInfo.TYPE_SPP, data);
            }
            else if (type.equals(D_FundInfo.TYPE_PPM)) {
//                errorStr = FundDBUpdate_PPM(fisFiles, data);
            }
            else if (type.equals(D_FundInfo.TYPE_VANGUARD)) {
                errorStr = FundDBUpdate_VGD.update(fisFiles, f.getName(), data);
            }
            if (errorStr != null) {
                System.out.println("Received error string, will not overwrite existing Fund DB\n" + errorStr);
                return;
            }
        }
        System.out.println("Parsed: " + fisFiles.size() + " for type: " + type);
        if (fisFiles.size() == 0) {
            System.out.println("Now work to do, exiting without modifying fund DB");
            return;
        }
        System.out.println("Will now process these entries");
        int rc = processFileFIs(testHTMLExtraction, type, fisFiles);
        if (rc == RC_FAILURE) {
            System.out.println("*** ERROR during processFileFIs, exiting without modifying DB");
            return;
        } else if (rc == RC_SUCCESS_UNMODIFIED) {
            System.out.println("No changes requires, DB and File are in sync");
            return;
        }

        // You sure you want to update?
        byte[] readData = new byte[100];
        int readDataCount = 0;
        try {
            System.out.println("Are you sure you want to update DB [yes/no]: ");
            readDataCount = System.in.read(readData);
            byte[] tmp = new byte[readDataCount];
            for (int i=0; i < readDataCount; i++) {
                tmp[i] = readData[i];
            }
            readData = tmp;
        } catch(IOException exc) {
            System.out.println("IOException encountered when trying to read response: " + exc);
            return;
        }
        String readStr = new String(readData).toLowerCase();
        if (!readStr.equals("yes\n")) {
            System.out.println("You opted out of update, exiting!!!");
            return;
        }

        // Write result fund DB
        System.out.println("About to write updated DB");
        fiv = new D_FundInfo_Validator(DB_FundInfo.getAllFundInfos());
        fiv.process();
        if (fiv._error) {
            System.out.println("*** ERROR ****: Fund database in invalid, will not save results\n" + fiv._iwErrors.getString());
            return;
        }
        byte[] dataToWrite = DB_FundInfo.crunch();
        // Store a backupo
//        MM.fileWrite(DB_ABSOLUTE_FILENAME + ".backup_" + MM.getNowAs_YYMMDD_HHMMSS(null), fileDBDataBA);
//        MM.fileWrite(DB_ABSOLUTE_FILENAME, dataToWrite);
        System.out.println("All done successfully, exiting");
    }

    //------------------------------------------------------------------------
    private static int RC_SUCCESS_MODIFIED = 1;
    private static int RC_SUCCESS_UNMODIFIED = 2;
    private static int RC_FAILURE = 3;
    private static int processFileFIs(boolean testHTMLExtraction,
                                       String type,
                                       List<D_FundInfo> fisFiles) {

        int rc = RC_SUCCESS_UNMODIFIED;

        IndentWriter iwInfo = new IndentWriter();
        IndentWriter iwUpdate = new IndentWriter();
        IndentWriter iwDelete = new IndentWriter();
        IndentWriter iwInsert = new IndentWriter();

        // Checking which in Files were new/already existed in DB
        System.out.println("*** Checking file funds to see if they exist in DB");
        List<D_FundInfo> fisToAdd = new ArrayList<>();
        int countURLMatch = 0;
        int countNewFunds = 0;
        for (D_FundInfo fiFile: fisFiles) {

            D_FundInfo fiDB =  DB_FundInfo.getFundInfosByTypeAndURL(fiFile._type, fiFile._url);
            if (fiDB != null) {
                if (!fiDB._isValid || fiDB._errorCode != D_FundInfo.IC_NO_ERROR) {
                    System.out.println("*** Error. Found match on URL, but the one in DB is invalid");
                    System.out.println(fiDB.getOneLiner());
                    return RC_FAILURE;
                }
                if (!fiFile._nameOrig.equals(fiDB._nameOrig)) {
                    iwUpdate.println("...[UPDATE] Original DB name is: " + fiDB._nameOrig + ", will be set to file: " + fiFile._nameOrig);
                    fiDB._nameOrig = fiFile._nameOrig;
                    rc = RC_SUCCESS_MODIFIED;
                }
                if (!fiFile._nameMS.equals(fiDB._nameMS)) {
                    iwInfo.println("...[INFO] MS name differed, keeping existing in DB. DB: " + fiDB._nameMS + ", file: " + fiFile._nameMS);
                }
                countURLMatch++;
            } else {
                fiDB = DB_FundInfo.getFundInfosByTypeAndName(fiFile._type, fiFile._nameOrig, true);
                if (fiDB != null) {
                    System.out.println("*** Error. Did not find a match on URL but on _nameOrig");
                    System.out.println("...DB Entry:   " + fiDB.getOneLiner());
                    System.out.println("...File Entry: " + fiFile.getOneLiner());
                    return RC_FAILURE;
                }
                fiDB = DB_FundInfo.getFundInfosByTypeAndName(fiFile._type, fiFile._nameMS, false);
                if (fiDB != null) {
                    System.out.println("*** Error. Did not find a match on URL but on _nameMS");
                    System.out.println("...DB Entry:   " + fiDB.getOneLiner());
                    System.out.println("...File Entry: " + fiFile.getOneLiner());
                    return RC_FAILURE;
                }
                countNewFunds++;
                iwInsert.println("...[INSERT] Will insert new fund from file: " + fiFile.getTypeAndName());
                fisToAdd.add(fiFile);
            }
        }
        if (fisToAdd.size() > 0) {
            rc = RC_SUCCESS_MODIFIED;
            DB_FundInfo.addFundInfo(fisToAdd);
        }

        // Checking which in DB did not have an entry in file
        System.out.println("*** Checking DB funds to see if they exist in files");
        int countUniqueInDB = 0;
        List<D_FundInfo> fisDB = DB_FundInfo.getFundInfosByType(type);
        int countOriginalInDB = fisDB.size();
        for (D_FundInfo fiDB: fisDB) {
            boolean found = false;
            for (D_FundInfo fiFi: fisFiles) {
                if (fiDB._url.equals(fiFi._url)) {
                    found = true;
                }
            }
            if (!found) {
                rc = RC_SUCCESS_MODIFIED;
                DB_FundInfo.deleteFundInfoByTypeAndURL(type, fiDB._url);
                countUniqueInDB++;
                iwDelete.println("...[DELETE] Exist in DB but not in files: " + fiDB.getTypeAndName() + ": " + MM.getString(fiDB.getDPDOneLiner(), 40));
            }
        }


        // Try to successfully extract
        if (rc == RC_SUCCESS_MODIFIED) {
            if (testHTMLExtraction) {
                System.out.println("Testing extraction for added funds from files");
                for (int i = 0; i < fisToAdd.size(); i++) {
                    D_FundInfo fi = fisToAdd.get(i);
                    System.out.println((i + 1) + "/" + fisToAdd.size() + " Extracting fund: " + fi.getTypeAndName() + ", " + fi._url);
                    IndentWriter iwd = new IndentWriter();
                    ExtractFromHTML_Helper eh = new ExtractFromHTML_Helper();
                    try {
                        int errorCode = eh.extractFundDetails(fi, iwd);
                        if (errorCode != ExtractFromHTML_Helper.RC_SUCCESS) {
                            System.out.println("*** Error extracting fund: " + errorCode + ", " + fi.getTypeAndName());
                            System.out.println(iwd.getString());
                            return RC_FAILURE;
                        }
                    } catch (IOException exc) {
                        System.out.println("IOException caught: " + exc + "\n" + MM.getStackTraceString(exc));
                        return RC_FAILURE;
                    }
                    System.out.println("...successfully extracted: " + fi.getOneLiner());
                }
            }
        }

        System.out.println("\nSynchronization Report");
        System.out.println(iwInfo.getString());
        System.out.println(iwUpdate.getString());
        System.out.println(iwInsert.getString());
        System.out.println(iwDelete.getString());

        System.out.println("\nTotal in DB: " + fisDB.size());
        System.out.println("Total in files: " + fisFiles.size());
        System.out.println("...Matches between DB/Files: " + countURLMatch);
        System.out.println("...New funds found in files: " + countNewFunds);
        System.out.println("...Funds to remove from DB: " + countUniqueInDB);

        return rc;
    }
}
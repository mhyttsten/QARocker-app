package com.pf.phototimestampmanager;

import java.util.Date;

public class ExifElem {

    public String mFilename;

    public String mDTOriginalStr;
    public Date mDTOriginalDate;

    public String mDTCreateStr;
    public Date mDTCreateDate;

    public String mDTModifyStr;
    public Date mDTModifyDate;

    public Date mPreviousDate = null;
    public boolean mNeedsUpdate = false;
    public boolean mDidChangeDate = false;
    public boolean mDidChangeGPS = false;
    public String mAdjustmentReason;

    // -gpstimestamp, "GPS Time Stamp                  : 05:17:05.7"
    // -gpsdatestamp, ???
    // -gpslatitude,  "GPS Latitude                    : 34 deg 0' 49.20" N"
    // -gpslongitude, "GPS Longitude                   : 84 deg 16' 4.80" W"
    public String mGPSLatitude;
    public String mGPSLongitude;
    public String mGPSTimeStamp;
    public String mGPSDateStamp;
    public void setGPSLatitude(String str)  { mGPSLatitude = str; }
    public void setGPSLongitude(String str) { mGPSLongitude = str; }
    public void setGPSTimeStamp(String str) { mGPSTimeStamp = str; }
    public void setGPSDateStamp(String str) { mGPSDateStamp = str; }

    public void cloneGPSData(ExifElem ee) {
        mDidChangeGPS = true;
        mGPSLatitude = ee.mGPSLatitude;
        mGPSLongitude = ee.mGPSLongitude;
        mGPSTimeStamp = ee.mGPSTimeStamp;
        mGPSDateStamp = ee.mGPSDateStamp;
    }

    public String getSequenceString() throws Exception {
        FilenameElem fne = new FilenameElem(mFilename);
        return fne.mSequenceString;
    }

    public ExifElem createCopy() throws Exception {
        ExifElem r = new ExifElem();
        r.mDidChangeDate = mDidChangeDate;
        r.mDidChangeGPS = mDidChangeGPS;
        r.mNeedsUpdate = mNeedsUpdate;
        r.mFilename = mFilename;
        r.mAdjustmentReason = mAdjustmentReason;
        if (mPreviousDate != null) {
            r.mPreviousDate = new Date(mPreviousDate.getTime());
        }

        r.mDTOriginalStr = mDTOriginalStr;
        if (mDTOriginalDate != null) {
            r.mDTOriginalDate = new Date(mDTOriginalDate.getTime());
            if (!r.mDTOriginalDate.equals(mDTOriginalDate)) {
                throw new Exception("Dates clone was different, filename: " + mFilename);
            }
        }

        r.mDTCreateStr = mDTCreateStr;
        if (mDTCreateDate != null) {
            r.mDTCreateDate = new Date(mDTCreateDate.getTime());
            if (!r.mDTCreateDate.equals(mDTCreateDate)) {
                throw new Exception("Dates clone as different, filename: " + mFilename);
            }
        }

        r.mDTModifyStr = mDTModifyStr;
        if (mDTModifyDate != null) {
            r.mDTModifyDate = new Date(mDTModifyDate.getTime());
            if (!r.mDTModifyDate.equals(mDTModifyDate)) {
                throw new Exception("Dates clone was different, filename: " + mFilename);
            }
        }

        r.mGPSLatitude = mGPSLatitude;
        r.mGPSLongitude = mGPSLongitude;
        r.mGPSTimeStamp = mGPSTimeStamp;
        r.mGPSDateStamp = mGPSDateStamp;

        return r;
    }

    public ExifElem() {
    }

    public ExifElem(String filename, String exifString, ExifStat es) throws Exception {
        mFilename = filename;

        mDTOriginalStr = getEntry(TAG_ORIGINAL, exifString);
        if (mDTOriginalStr != null) {
            mDTOriginalDate = MM.getDateFrom_YYYYMMDDHHMMSS(mDTOriginalStr);
        }
        mDTCreateStr = getEntry(TAG_CREATE, exifString);
        if (mDTCreateStr != null) {
            mDTCreateDate = MM.getDateFrom_YYYYMMDDHHMMSS(mDTCreateStr);
        }
        mDTModifyStr = getEntry(TAG_MODIFY, exifString);
        if (mDTModifyStr != null) {
            mDTModifyDate = MM.getDateFrom_YYYYMMDDHHMMSS(mDTModifyStr);
        }

        mNeedsUpdate = needsExifUpdate(es);
    }

    public Date getMyDate() {
        if (mDTOriginalDate != null) {
            return mDTOriginalDate;
        }
        if (mDTCreateDate != null) {
            return mDTCreateDate;
        }
        if (mDTModifyDate != null) {
            return mDTModifyDate;
        }
        return null;
    }

    public void setMyDate(Date d, String adjustmentReason) {
        mAdjustmentReason = adjustmentReason;
        if (getMyDate() != null) {
            mPreviousDate = getMyDate();
            mDidChangeDate = true;
        } else {
            mDidChangeDate = false;
        }
        mDTOriginalDate = d;
        mDTCreateDate = d;
        mDTModifyDate = d;
    }

    private boolean needsExifUpdate(ExifStat es) {
        if (es ==  null) {
            es = new ExifStat();
        }

        if (mDTOriginalDate == null && mDTCreateDate == null && mDTModifyDate == null) {
            es.noneExists++;
            return true;
        }
        if (mDTOriginalDate != null && mDTCreateDate == null && mDTModifyDate == null) {
            es.onlyOriginal++;
            return true;
        }
        if (mDTOriginalDate != null && mDTCreateDate != null && mDTModifyDate == null) {
            if (mDTOriginalDate.equals(mDTCreateDate)) {
                es.onlyOriginalAndCreate_EQ++;
            } else {
                es.onlyOriginalAndCreate_NEQ++;
            }
            return true;
        }
        if (mDTOriginalDate != null && mDTCreateDate == null && mDTModifyDate != null) {
            if (mDTOriginalDate.equals(mDTModifyDate)) {
                es.onlyOriginalAndModify_EQ++;
            } else {
                es.onlyOriginalAndModify_NEQ++;
            }
            return true;
        }
        if (mDTOriginalDate == null && mDTCreateDate != null && mDTModifyDate == null) {
            es.onlyCreate++;
            return true;
        }
        if (mDTOriginalDate == null && mDTCreateDate != null && mDTModifyDate != null) {
            if (mDTCreateDate.equals(mDTModifyDate)) {
                es.onlyCreateAndModify_EQ++;
            } else {
                es.onlyCreateAndModify_NEQ++;
            }
            return true;
        }
        if (mDTOriginalDate == null && mDTCreateDate == null && mDTModifyDate != null) {
            es.onlyModify++;
            return true;
        }
        if (mDTOriginalDate != null && mDTCreateDate != null && mDTModifyDate != null) {
            if (mDTOriginalDate.equals(mDTCreateDate) && mDTCreateDate.equals(mDTModifyDate)) {
                es.allExist_All_EQ++;
                return false;
            }

            if (mDTOriginalDate.equals(mDTCreateDate) && !mDTCreateDate.equals(mDTModifyDate)) {
                es.allExist_OnlyOriginalAndCreate_EQ++;
                return true;
            }

            if (!mDTOriginalDate.equals(mDTCreateDate) && mDTOriginalDate.equals(mDTModifyDate)) {
                es.allExist_OnlyOriginalAndModify_EQ++;
                return true;
            }

            if (mDTCreateDate.equals(mDTModifyDate) && !mDTModifyDate.equals(mDTOriginalDate)) {
                es.allExist_OnlyCreateAndModify_EQ++;
                return true;
            }

            if (!mDTOriginalDate.equals(mDTCreateDate) && !mDTCreateDate.equals(mDTModifyDate) && !mDTOriginalDate.equals(mDTModifyDate)) {
                es.allExist_All_NEQ++;
                return true;
            }
        }
        return true;
    }

    public String toString() {
        IndentWriter iw = new IndentWriter();
        iw.println("ExifElem, data\n" +
                "...Orig:   " + mDTOriginalStr + ", date: " + mDTOriginalDate +
                "\n" + "...Create:   " + mDTCreateStr + ", date: " + mDTCreateDate +
                "\n" + "...Modify:   " + mDTModifyStr + ", date: " + mDTModifyDate +
                "\n" + "...Previous: " + mPreviousDate +
                "\n" + "...Needs Update: " + mNeedsUpdate +
                "\n" + "...Reason:   " + mAdjustmentReason);
        return iw.getString();
    }

    public String toStringOneLineOriginalData() {
        return mDTOriginalStr
                + ", " + mDTCreateStr
                + ", " + mDTModifyStr
                + ", " + mGPSTimeStamp
                + ", " + mGPSDateStamp
                + ", " + mGPSLatitude
                + ", " + mGPSLongitude;
    }

    public void dumpInfo(IndentWriter iw) {
        iw.println("Original date: " + mDTOriginalDate);
        iw.println("Create date:   " + mDTCreateDate);
        iw.println("Modify date:   " + mDTModifyDate);
        iw.println("Previous date: " + mPreviousDate);
    }

    public String getMyDate_exifToolFormat() {
        Date d = getMyDate();
        return MM.getAs_YYYYCMMCDDSHHCMMCSS(d);
    }

    private String getEntry(String tag, String exifString) throws Exception {
        int io = exifString.indexOf(tag);
        if (io == -1) {
            return null;
        }
        io += tag.length();
        int end = io + DATA_FIELD.length();
        if (end >= exifString.length()) {
            throw new Exception("Not enough data to extract data field\n...exifString: " + exifString + "\n...tag: " + tag);
        }
        String r = exifString.substring(io, end);

        String yyyymmddHHmmss = null;
        try {
            yyyymmddHHmmss =
                    r.substring(0, 4) +
                            r.substring(5, 7) +
                            r.substring(8, 10) +
                            r.substring(11, 13) +
                            r.substring(14, 16) +
                            r.substring(17, 19);
            return yyyymmddHHmmss;
        } catch(Exception exc) {
            exc.printStackTrace();
            System.out.println(exc.toString());
            throw new Exception("ExifElem, error constructing date string from: " + yyyymmddHHmmss);
        }
    }

    private static final String TAG_ORIGINAL = "Date/Time Original              : ";
    private static final String TAG_CREATE =   "Create Date                     : ";
    private static final String TAG_MODIFY =   "Modify Date                     : ";
    private static final String DATA_FIELD =   "2001:01:01 00:00:01";


    public static class ExifStat {
        public int noneExists;
        public int onlyOriginal;
        public int onlyCreate;
        public int onlyModify;
        public int onlyOriginalAndCreate_EQ;
        public int onlyOriginalAndCreate_NEQ;
        public int onlyOriginalAndModify_EQ;
        public int onlyOriginalAndModify_NEQ;
        public int onlyCreateAndModify_EQ;
        public int onlyCreateAndModify_NEQ;
        public int allExist_All_EQ;
        public int allExist_OnlyOriginalAndCreate_EQ;
        public int allExist_OnlyOriginalAndModify_EQ;
        public int allExist_OnlyCreateAndModify_EQ;
        public int allExist_All_NEQ;

        public void dumpInfo(IndentWriter iw) {
            iw.println("ExifStat");
            iw.push();
            iw.println("noneExists:   " + noneExists);
            iw.println("onlyOriginal: " + onlyOriginal);
            iw.println("onlyCreate:   " + onlyCreate);
            iw.println("onlyModify:   " + onlyModify);
            iw.println("onlyOriginalAndCreate_EQ:  " + onlyOriginalAndCreate_EQ);
            iw.println("onlyOriginalAndCrdate_NEQ: " + onlyOriginalAndCreate_NEQ);
            iw.println("onlyOriginalAndModify_EQ:  " + onlyOriginalAndModify_EQ);
            iw.println("onlyCreateAndModify_EQ:    " + onlyCreateAndModify_EQ);
            iw.println("onlyCreateAndModify_NEQ:   " + onlyCreateAndModify_NEQ);
            iw.println("allExist_EQ:  " + allExist_All_EQ);
            iw.println("allExist_NEQ: " + allExist_All_NEQ);
            iw.println("allExist_OnlyOriginalAndCreate_EQ: " + allExist_OnlyOriginalAndCreate_EQ);
            iw.println("allExist_OnlyOriginalAndModify_EQ: " + allExist_OnlyOriginalAndModify_EQ);
            iw.println("allExist_OnlyCreateAndModify_EQ:   " + allExist_OnlyCreateAndModify_EQ);
            iw.pop();
        }
    }

    // All three should be set to the same value

    // ****
    // When you do $ exiftool with modify it creates a _original file

    // MOV
    //Date/Time Original              : 2015:07:20 06:31:25
    //Create Date                     : 2015:07:20 13:31:25
    //Modify Date                     : 2015:07:20 13:31:25
    //
    // Image
    //Date/Time Original              : 2015:07:20 06:31:09
    //Create Date                     : 2015:07:20 06:31:09
    //Modify Date                     : 2015:07:20 06:31:09


    // photos.google.com
    //
    // F: When an image/video which exists in an album, it is also removed from the album
    //     (if you remove all images/videos an album has, the album is also removed)
    // F: Uploading a duplicate photo results in only one
    // F: Uploading a duplicate video results in only one (but it takes time to process and for photos to understand this)
    // F: Changing exif time signature makes photos view it as a completely different photo
    // F: A photo/video is the same even though you have copied it on the Unix filesystem
    // F: Albums are sorted on Original date/time, photo stream is sorted on create or modify time
    //    (if you click on


}

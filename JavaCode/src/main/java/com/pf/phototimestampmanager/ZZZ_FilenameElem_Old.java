package com.pf.phototimestampmanager;

import java.util.Date;

public class ZZZ_FilenameElem_Old {

    public boolean mIsValidFilename = false;

    public String mCompleteFilename;

    public static final int FT_NOT_SUPPORTED = 0;
    public static final int FT_JPG = 1;
    public static final int FT_MOV = 2;
    public static final int FT_PNG = 3;
    public static final int FT_MP4 = 4;
    public static final int FT_AVI = 5;
    public int mFileType = FT_NOT_SUPPORTED;

    public String mPrefix;

    public String mSequenceString;
    public int mSequenceNumber;

    public String mCustomPart;

    public boolean mIsDateComplete = false;
    public Date mDate = null;
    public int mYYYY = -1;
    public int mMM = -1;
    public int mDD = -1;

    public String mError;

    public boolean updateAlbumManually() {
        switch (mFileType) {
            case FT_MOV:
            case FT_MP4:
            case FT_AVI:
                return true;
            default:
                return false;
        }
    }

    public boolean canChangeDate() {
        switch (mFileType) {
            case FT_JPG:
            case FT_MOV:
            case FT_PNG:
            case FT_MP4:
                return true;
            default:
                return false;
        }
    }

    public String toString() {
        IndentWriter iw = new IndentWriter();
        iw.println("Name: " + mCompleteFilename);
        iw.push();
        iw.println("IsValidFilename: " + mIsValidFilename);
        iw.println("Prefix: " + mPrefix);
        iw.println("File type: " + mFileType);
        iw.println("Sequence string: " + mSequenceString);
        iw.println("Sequence number: " + mSequenceNumber);
        iw.println("Custom part: " + mCustomPart);
        iw.println("IsDateComplete: " + mIsDateComplete);
        iw.println("Date:  " + mDate);
        iw.println("Year:  " + mYYYY);
        iw.println("Mount: " + mMM);
        iw.println("Day:   " + mDD);
        iw.pop();
        return iw.getString();
    }

    public ZZZ_FilenameElem_Old(String name) throws Exception {
        if (name.contains(" ")) {
            mError = "Name has spaces: [" + name + "]";
            return;
        }

        String[] fnameParts = name.split("_");
        if (fnameParts == null || fnameParts.length < 6) {
            mError = "Does not have 6 '_' parts in name: [" + name + "]";
            return;
        }

        mCompleteFilename = name;
        mPrefix = fnameParts[0];

        String lastPart = fnameParts[fnameParts.length-1];
        if (!lastPart.contains(".")) {
            mError = "Last part did not contain '.': [" + name + "]";
            return;
        }
        String extension = lastPart.substring(lastPart.indexOf(".")).toLowerCase();
        String sequenceString =  lastPart.substring(0, lastPart.indexOf("."));
        if (sequenceString.length() != 6) {
            mError = "Sequence part did not have 6 characters";
            return;
        }

        // Checking extension
        if (extension.equals(".jpg")) {
            mFileType = FT_JPG;
        } else if (extension.equals(".mov")) {
            mFileType = FT_MOV;
        } else if (extension.equals(".mp4")) {
            mFileType = FT_MP4;
        } else if (extension.equals(".png")) {
            mFileType = FT_PNG;
        } else if (extension.equals(".avi")) {
            mFileType = FT_AVI;
        } else {
            mError = "Did not recognize filetype: [" + name + "]";
            return;
        }

        // Checking sequence number
        String numberParser = sequenceString;
        while (numberParser.length() > 0 && numberParser.startsWith("0")) {
            numberParser = numberParser.substring(1);
        }
        if (numberParser.length() == 0) {
            return;
        }
        try {
            mSequenceNumber = Integer.parseInt(numberParser);
        } catch(Exception exc) {
            mError = "Sequence number was not a number: [" + name + "]";
            return;
        }
        mSequenceString = sequenceString;

        // Parsing th custom part
        String customPart = fnameParts[4];
        if (fnameParts.length > 6) {
            for (int i=5; i < fnameParts.length-1; i++) {
                customPart +=  "_" + fnameParts[i];
            }
        }
        mCustomPart = customPart;

        // Fix the date part
        String strYYYY = fnameParts[1];
        String strMM = fnameParts[2];
        String strDD = fnameParts[3];
        try {
            if (strYYYY.length() != 4
                    || strMM.length() != 2
                    || strDD.length() != 2) {
                mError = "Cannot find YYYY, MM, DD: [" + name + "]";
                return;
            }
            if (strMM.equals("yyyy")) {
                mError = "Cannot support yyyy as year in filename: [" + name + "]";
                return;
            }
            mYYYY = Integer.parseInt(strYYYY);
            if (!strMM.equals("mm")) {
                mMM = Integer.parseInt(strMM);
            }
            if (!strDD.equals("dd")) {
                mDD = Integer.parseInt(strDD);
            }
        } catch(NumberFormatException exc) {
            mError = "Could not convert YYYY, MM, DD into numbers: [" + name + "]";
            return;
        }

        String mm = mMM != -1 ? strMM : "01";
        String dd = mDD != -1 ? strDD : "01";
        mDate = MM.getDateFrom_YYYYMMDD(null, strYYYY + mm + dd);

        if (mMM != -1 && mDD != -1) {
            mIsDateComplete = true;
        }
        mIsValidFilename = true;
    }

    public String getFilenameAsJPG() {
        int io = mCompleteFilename.lastIndexOf(".");
        String newFilename = mCompleteFilename.substring(0, io);
        return newFilename + ".JPG";
    }
}

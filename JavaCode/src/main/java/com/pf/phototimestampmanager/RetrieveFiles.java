package com.pf.phototimestampmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RetrieveFiles {

    List<File> mTargetFiles = new ArrayList<>();
    List<File> mExcludedFiles = new ArrayList<>();


    public RetrieveFiles(String directory) throws Exception {
        if (!directory.endsWith(File.separator)) {
            directory += File.separator;
        }

        System.out.println("...Retrieving files from directory: " + directory);

        // Get sorted target list
        List<File> l = MM.fileList(directory, true);
        Collections.sort(l, new Comparator<File>() {
                    public int compare(File a1, File a2) {
                        return a1.getName().compareTo(a2.getName());
                    }
                }
        );

        // Validate target list
        for (int i = 0; i < l.size(); i++) {
            File f = l.get(i);
            if (f.getName().equals(".DS_Store")) {
                System.out.println("......Excluding: " + f.getName() + ", directory: " + f.isDirectory() + ", hidden: " + f.isHidden());
                mExcludedFiles.add(f);
            } else if (f.isHidden() || f.isDirectory()) {
                throw new Exception("Retrieving files, unexpected directory or hidden file: " + f.getName() + ", directory: " + f.isDirectory() + ", hidden: " + f.isHidden());
            } else {
                FilenameElem fne = new FilenameElem(f.getName());
                if (!fne.mIsValidFilename) {
                    throw new Exception("Retrieving file, unexpected filename: " + f.getName() + "\nReason was: " + fne.mError);
                } else {
                    mTargetFiles.add(f);
                }
            }
        }
    }
}

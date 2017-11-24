package com.pf.dedup;

// Does .png work for iPhoto?
// Add scanning of weird filenames
// - Any character which is not 7-bit ASCII
// - Anything that does not start with alphanumeric
// - Anything that does not have postfix of pictures

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;


public class DedupsMain_Compare {
	
	public final static String MODE_CHECKSUM_STR = "checksum";
	public final static String MODE_CONTENT_STR = "content";
	public final static String MODE_STR(int mode) throws Exception {
		switch (mode) {
		case MODE_CHECKSUM:
			return MODE_CHECKSUM_STR;
		case MODE_CONTENT:
			return MODE_CONTENT_STR;
		default:
			throw new Exception("Unknown mode: " + mode);
		}
	}
	public final static int MODE_CHECKSUM = 1;
	public final static int MODE_CONTENT = 2;
	
	private HashMap<String,ArrayList<PFFileData>> _hmOrigMD5 = new HashMap<String,ArrayList<PFFileData>>();
	private HashMap<String,ArrayList<PFFileData>> _hmOrigDir = new HashMap<String,ArrayList<PFFileData>>();
	private HashMap<String,ArrayList<PFFileData>> _hmDupsMD5 = new HashMap<String,ArrayList<PFFileData>>();
	private HashMap<String,ArrayList<PFFileData>> _hmDupsDir = new HashMap<String,ArrayList<PFFileData>>();

	private DedupsMain _m;
	private int _mode;
	private String _hashFileOrig;
	private String _hashFileDups;
	private String _rmScriptFileName;
	
	public void opCompare(
			DedupsMain main,
			String modeStr,
			String rmFileName,
			String hashFileOrig,
			String hashFileDups) {
		System.out.println("Version 160210 08:53");
			try {
			if (modeStr.equals(MODE_CHECKSUM_STR)) {
				_mode = MODE_CHECKSUM;
			} else if (modeStr.equals(MODE_CONTENT_STR)) {
				_mode = MODE_CONTENT;
			} else {
				throw new Exception("Unknown mode: " + modeStr);
			}
			
			_m = main;
			_hashFileOrig = hashFileOrig;
			_hashFileDups = hashFileDups;
			_rmScriptFileName = rmFileName;
			
			opCompareImpl();
		} catch(Exception exc) {
			_m.L.severe("Exception caught");
			_m.L.severe(exc.getMessage());
			exc.printStackTrace();
		} finally {
			deinitialize();
		}
	}
	
	public void opCompareImpl() throws Exception {
		_m.L.info("Now starting 2");
		_m.L.info("Mode: " + MODE_STR(_mode));
		_m.L.info("Hash file orig: " + _hashFileOrig);
		_m.L.info("Hash file dups: " + _hashFileDups);
		
		_m.L.info("Will now process orig hash file: " + _hashFileOrig);
		processFile(_hashFileOrig, _hmOrigMD5, _hmOrigDir, null, null);
		_m.L.info("Will now process dups hash file: " + _hashFileDups);
		processFile(_hashFileDups, null, null, _hmDupsMD5, _hmDupsDir);

		StringBuilder strb = new StringBuilder();
		ArrayList<PFFileData> dupsValues = null;

		dupsValues = getAsNewArrayList(_hmDupsMD5);

		/*
		_m.L.info("Will now print all files which are not media files");
		for (PFFileData dup: dupsValues) {
			if (!DedupsMain.validMediaFileName(dup.getFileName()) &&
				!dup.getFileName().equals(".DS_Store") &&
				!dup.getFileName().equals("Thumbs.db")) {
				strb.append(dup.getFileName() + "   [" + dup.getRelativeFilenamePath() + "]\n");
			}
		}
		_m.L.info("Files which are not media files\n" + strb.toString() + "\n");
		*/

		printData("Original  hash file: " + _hashFileOrig, _hmOrigMD5);
		printData("Duplicate hash file: " + _hashFileDups, _hmDupsMD5);

		// Compare file names only
		/*
		if (true) {
			System.out.println("*** WILL ONLY COMPARE FILENAME");
			compareFileNames(_hmOrigMD5, _hmDupsMD5);
			System.out.println("*** DONE COMPARING FILENAME");
			return;
		}
		*/

		// Compare and Mark all originals and duplicates
		_m.L.info("Will now compare files between orig and dups");
		Iterator<String> dupsMD5Iter = _hmDupsMD5.keySet().iterator();
		DeltaReporter drCompare = new DeltaReporter();
		drCompare.setInterval("Files",  1000,  "Bytes", 500*1024*1024, "Seconds", 60);
		while(dupsMD5Iter.hasNext()) {
			String dupMD5 = dupsMD5Iter.next();
			if(_hmOrigMD5.containsKey(dupMD5)) {
				ArrayList<PFFileData> pffdOrigs = _hmOrigMD5.get(dupMD5);
				ArrayList<PFFileData> pffdDups  = _hmDupsMD5.get(dupMD5);
				compareAndMark(drCompare, pffdOrigs, pffdDups);
			}
		}
		PFFileData[] dupsValuesA = new PFFileData[dupsValues.size()];
		dupsValues.toArray(dupsValuesA);
		Arrays.sort(dupsValuesA, new Comparator<PFFileData>() {
			@Override
			public int compare(PFFileData o1, PFFileData o2) {
				return o1.getFileName().compareTo(o2.getFileName());
			}
		});
		dupsValues = new ArrayList<PFFileData>();
		for (PFFileData fd: dupsValuesA) {
			dupsValues.add(fd);
		}

		// Write all non-duplicates to the log
		_m.L.info("Will now write all non-duplicates to the log");
		strb = new StringBuilder();
		int count = 0;
		for (PFFileData dup: dupsValues) {
			if (!dup.hasOriginals()) {
				if(!dup.getFileName().equals(".DS_Store") && !dup.getFileName().equals("Thumbs.db")) {
					strb.append(dup.getFileName() + "   [" + dup.getRelativeFilenamePath() + "]\n");
					count++;
				}
			}
			if (count >= 50) {
				break;
			}
		}
		if (count > 0) {
			_m.L.info("FIRST 50 files in duplicate set that are not duplicates, of total: " + dupsValues.size());
			_m.L.info(strb.toString());
		} else {
			_m.L.info("ALL FILES WERE DUPLICATES");
		}

		strb = null;
		
		// Go through dups in orig directory and file order
		// Initiate remove for each duplicate
		 createFileRM(_rmScriptFileName);
		_m.L.info("Generating remove script");
		fileRMOut("#!/bin/sh");
		fileRMOut("# Remove duplicates script - generated at: " + new java.util.Date());
		fileRMOut("#!/bin/sh");
		Iterator<String> origDirsIter = _hmOrigDir.keySet().iterator();
		DeltaReporter drRMScript = new DeltaReporter();
		drRMScript.setInterval("Files", 1000, "Bytes", 500*1024*1024, "Time", 60);
		HashMap<String, Void> hmDupsRemoved = new HashMap<String, Void>();
		while(origDirsIter.hasNext()) {
			String origDir = origDirsIter.next();
			ArrayList<PFFileData> origs = _hmOrigDir.get(origDir);
			for (PFFileData orig : origs) {
				String fnOrig = orig.getRelativeFilenamePath();
				if (orig.hasDuplicates()) {
					for (PFFileData dup: orig.getDuplicates()) {
						String fnDup = dup.getRelativeFilenamePath();
						if (!hmDupsRemoved.containsKey(fnDup)) {
							fileRMOut("rm -f " + fnDup + " # " + orig.getRelativeFilenamePath());
							hmDupsRemoved.put(fnDup,  null);
						}
					}
				}
			}
		}
		for (PFFileData dup: dupsValues) {
			if (dup.getFileName().equals(".DS_Store") || dup.getFileName().equals("Thumbs.db")) {
				String fnDup = dup.getRelativeFilenamePath();
				fileRMOut("rm -f '" + fnDup + "'");
			}
		}
		
		_m.L.info("Remove script generated");
	}
	
	// Mark origs and dups based on whether they are duplicates
	// Either do shallow or deep compare based on what was specified on command line
	private void compareAndMark(
			DeltaReporter dr,
			ArrayList<PFFileData> origs,
			ArrayList<PFFileData> dups) throws Exception {
		boolean fileNameMatch = false;
		for (int i=0; i < origs.size(); i++) {
			PFFileData orig = origs.get(i);
			// System.out.println("*** NOW CHECKING: " + orig.getFileName());
			for (int j=0; j < dups.size(); j++) {
				PFFileData dup = dups.get(j);
				// System.out.println("   --- Is: " + dup.getFileName() + ", a dup?");
				String report = dr.report(1,  dup.getSize());
				// if (report != null) {
				//	_m.L.info("Compare Status Report Progress. " + report);
				// }
				if(_mode == MODE_CHECKSUM) {
					if (!dup.getHash().equals(orig.getHash())) {
						throw new Exception("Checksum not equal for orig: " + orig.getRelativeFilenamePath() + ", and dup: " + dup.getRelativeFilenamePath());
					}
					// System.out.println("   *** DUP BY MD5");
					dup.addOriginal(orig);
					orig.addDuplicate(dup);
					if (dup.getFileName().equals(orig.getFileName())) {
						fileNameMatch = true;
					}
				} else if (_mode == MODE_CONTENT) {
					FileDiff fd = new FileDiff(_m, orig.getRelativeFilenamePath(), dup.getRelativeFilenamePath());
					if (fd.isEqual()) {
						// System.out.println("   *** DUP BY CONTENT");
						dup.addOriginal(orig);
						orig.addDuplicate(dup);
					} else {
						// System.out.println("   *** NODUP BY CONTENT");
					}
				} else {
					// System.out.println("   *** NODUP ***");
				}
			}
		}
		/*
		if (!fileNameMatch) {
			_m.L.info("Match on MD5, but could not find matching filenames");
			printFileData("Origs", origs);
			printFileData("Dups", dups);

		}
		*/
	}
	
	private static void processFile(
			String fileName,
			HashMap<String,ArrayList<PFFileData>> hmOrigMD5,
			HashMap<String,ArrayList<PFFileData>> hmOrigDir,
			HashMap<String,ArrayList<PFFileData>> hmDupsMD5,
			HashMap<String,ArrayList<PFFileData>> hmDupsDir) throws Exception {
		ArrayList<String[]> hashEntries = MM.fileReadToLin_eelements(
				fileName, "#", "\\|", null);
		for (String[] elem : hashEntries) {
			PFFileData d = new PFFileData()
			.setHash(elem[0])
			.setSizeStr(elem[1])
			.setFileName(elem[2])
			.setPath(elem[3]);
			putHM(hmOrigMD5, d.getHash(), d);
			putHM(hmOrigDir, d.getHash(), d);
			putHM(hmDupsMD5, d.getHash(), d);
			putHM(hmDupsDir, d.getPath(), d);
		}
	}
	
	private static void putHM(HashMap<String,ArrayList<PFFileData>> hm, String hash, PFFileData d) {
		if (hm == null) {
			return;
		}
		ArrayList<PFFileData> al = hm.get(hash);
		if (al == null) {
			al = new ArrayList<PFFileData>();
			hm.put(hash, al);
		}
		// TODO: Check for dupliates within that array??? Impossible since duplicate filename cannot
		// exist in the same directory
		al.add(d);
	}
	
	private static ArrayList<PFFileData> getAsNewArrayList(HashMap<String, ArrayList<PFFileData>> hmIn) {
		ArrayList<PFFileData> result = new ArrayList<PFFileData>();
		Collection<ArrayList<PFFileData>> multidim = hmIn.values();
		Iterator<ArrayList<PFFileData>> iter = multidim.iterator();
		while (iter.hasNext()) {
			ArrayList<PFFileData> inElems = iter.next();
			result.addAll(inElems);
		}
		return result;
	}
	
	private FileOutputStream createFileRM(String rmFileName) throws Exception {
		if (_fileRM == null) {
			_fileRM = new FileOutputStream(rmFileName);
		}
		return _fileRM;
	}
	
	private void fileRMOut(String message) throws Exception {
		message += "\n";
		_fileRM.write(message.getBytes());
	}
	
	private void deinitialize() {
		try {
			if (_fileRM != null) {
				_fileRM.close();
			}
		} catch(Exception exc) { }
	}

	private void printData(String header, HashMap<String, ArrayList<PFFileData>> hmIn) {
		ArrayList<PFFileData> r = getAsNewArrayList(hmIn);
		IndentWriter iw = new IndentWriter();
		iw.println(header);
		iw.push();
		iw.println("Number of entries: " + r.size());
		long size = 0;
		for (PFFileData fd: r) {
			size += fd.getSize();
		}
		iw.println("Total size: " + size);
		_m.L.info(iw.getString());
	}

	private void printFileData(String header, ArrayList<PFFileData> al) {
		IndentWriter iw = new IndentWriter();
		iw.println(header + ", number of entries: " + al.size());
		iw.push();
		for (PFFileData fd: al) {
			iw.println(fd.getFileName() + ", " + fd.getRelativeFilenamePath());
		}
		_m.L.info(iw.getString());
	}

	private FileOutputStream _fileRM;


	private void compareFileNames(
			HashMap<String,ArrayList<PFFileData>> md5Origs,
			HashMap<String,ArrayList<PFFileData>> md5Dups) throws Exception {

		ArrayList<PFFileData> origs = getAsNewArrayList(md5Origs);
		ArrayList<PFFileData> dups = getAsNewArrayList(md5Dups);
		System.out.println("Origs size: " + origs.size());
		System.out.println("Dups  size: " + dups.size());
		ArrayList<String> unique = new ArrayList<String>();

		for (PFFileData pfDup: dups) {
			boolean found = false;
			for (PFFileData pfOrig: origs) {
				if (pfDup.getFileName().equals(pfOrig.getFileName())) {
					found = true;
				}
			}
			if (!found) {
				unique.add("### ERROR Unique Dup: " + pfDup.getFileName() + ", " + pfDup.getRelativeFilenamePath());
			}
		}
		Collections.sort(unique);
		for (String s : unique) {
			System.out.println(s);
		}
	}




	
}

package com.pf.dedup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;

public class DedupsMain_Calculate {
	
	private DedupsMain _m;
	private String _hashFileName;
	private ArrayList<String> _inDirectories;
	
	public void opCalculate(
			DedupsMain main,
			String hashFileName,
			ArrayList<String> inDirectories) throws Exception {
		try {
			_m = main;
			_hashFileName = hashFileName;
			_inDirectories = inDirectories;
			opCalculateImpl();
		} catch(Exception exc) {
			_m.L.severe("Exception caught");
			_m.L.severe(exc.getMessage());
		}
		deinitialize();
	}
	
	private void opCalculateImpl() throws Exception {
		_m.L.info("Version 160210 08:22");
		_m.L.info("Scanning and reporting files to generate: " + _hashFileName);

		String hashFilename = _hashFileName.replace('/', '_');
		hashFilename = hashFilename.replace('.', '_');		
		if(hashFilename.endsWith("_")) {
			hashFilename = hashFilename.substring(0, hashFilename.length()-1);
		}
		if(hashFilename.startsWith("_")) {
			hashFilename = hashFilename.substring(1);
		}
		FileOutputStream fileHash = createFileHash(hashFilename);
		
		for (String dstring : _inDirectories) {
			if (dstring == null ||
					dstring.startsWith("/") ||
					dstring.startsWith(".")) {
				throw new Exception("Illegal input directory name, cannot be null or start with . or /");
			}
		}
		
		_m.L.info("Now starting processing of files");
		for (String inDirectory : _inDirectories) {
			File directory = new File(inDirectory);
			calculateDir(fileHash, directory);
		}
		_m.L.info("Finished processing files without exceptions");
	}

	private void calculateDir(
			FileOutputStream fout, 
			File dir) throws Exception { 

		fileHashOut("#****************************************************************");
		fileHashOut("#_Calculating_file_hashes");
		fileHashOut("#_Directory_is:" + dir.getName());
		fileHashOut("#_Date_is_" + MM.getNowAs_YYMMDD_HHMMSS());
		if (!dir.isDirectory()) {
			throw new Exception("dir: " + dir.getName() + ", is not a directory");
		}
		
		File[] files = dir.listFiles();
		if(files == null) {
			_m.L.warning("No data to process, directory was empty");
			return;
		}
		for (int i=0; i < files.length; i++) {
			File f = files[i];
			if(f.isDirectory()) {
				_m.L.info("Processing directory: " + f.getName());
				calculateDir(fout, f);
			} else {
				// MM.printFileInfo(f);;
				String hash = calculateHash(f);
				PFFileData pfFileData = new PFFileData()
				.setRelativeFilePath(f.getPath())
				.setHash(hash)
				.setSize(f.length());
				pfFileData.writeInfo(fout);
				// filesAnalyzed.add(pfFileData);
			}
		}
	}
	
	private String calculateHash(File file) {
		try {
			return calculateHashImpl(file);
		} catch(Exception exc) {
		}
		return null;
	}
	private String calculateHashImpl(File file) throws Exception {
		FileInputStream fin = null;
		String hashStr = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			byte[] data = new byte[1024*1024];
			int length = -1;
			fin = new FileInputStream(file);
			while( (length=fin.read(data)) != -1) {
				md.update(data, 0, length);
				report(REPORT_FILE, length);
			}
			byte[] hash = md.digest();
			if(hash == null || hash.length == 0) {
				if (fin != null) {
					fin.close();
				}
				throw new Exception("Has value was null from digest");
			}
			hashStr = bytesToString(hash);
		} catch(Exception exc) {
			if (fin != null) {
				fin.close();
			}
		}
		if (hashStr == null) {
			throw new Exception("Hash string was null");
		}
		return hashStr;
	}
	private static String bytesToString(byte[] data) throws Exception {
		if(data == null || data.length == 0) {
			throw new Exception("Hash was null or had length 0");
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(byte b: data) {
			if(!first) {
				sb.append(":");
			}
			sb.append(String.format("%02x", b&0xff));
			first = false;
		}
		String result = sb.toString();
		return result;		
	}
	
	private long _reportLength;
	private long _reportLengthLast;
	private long _reportFileCount;
	private long _reportDirectoryCount;
	private static final int REPORT_FILE = 1;
	private static final int REPORT_DIRECTORY = 2;
	private void report(int type, int length) throws Exception {
		if(type == REPORT_FILE) {
			_reportFileCount++;
		} else {
			_reportDirectoryCount++;
		}
		
		_reportLength += length;
		if (_reportLength - _reportLengthLast > 500*1024*1024) {
			_reportLengthLast = _reportLength;
			long mb = _reportLength / (1024*1024);
			_m.L.info("   fileCount: " + String.valueOf(_reportFileCount) + 
					", dirCount: " + String.valueOf(_reportDirectoryCount) +
					", size: " + mb + " mb");
		}
	}
	
	//------------------------------------------------------------------------
	
	private FileOutputStream createFileHash(String hashFileName) throws Exception {
		if (_fileHash == null) {
			_fileHash = new FileOutputStream(hashFileName);
		}
		return _fileHash;
	}
	
	private void fileHashOut(String message) throws Exception {
		message += "\n";
		_fileHash.write(message.getBytes());
	}
	
	private void deinitialize() {
		try {
			if (_fileHash != null) {
				_fileHash.close();
			}
		} catch(Exception exc) { }
	}

	private FileOutputStream _fileHash;
}

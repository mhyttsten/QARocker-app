package com.pf.dedup;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class PFFileData {
	
	public PFFileData setRelativeFilePath(String relativeFilePath) {
		int io = relativeFilePath.lastIndexOf(File.separator);
		if (io == -1) {
			_fileName = relativeFilePath;
			return this;
		}
		_path = relativeFilePath.substring(0, io);
		_fileName = relativeFilePath.substring(io+1);
		return this;
	}
	
	public PFFileData setFileName(String fileName) {
		_fileName = fileName;
		return this;
	}
	
	public PFFileData setPath(String path) {
		_path = path;
		return this;
	}
		
	public String getFileName() {
		return _fileName;
	}
	
	public String getPath() {
		return _path;
	}
	
	public String getRelativeFilenamePath() {
		return _path + File.separator + _fileName;
	}
	
	public PFFileData setSizeStr(String size) {
		_size = Long.parseLong(size);
		return this;
	}
	
	public PFFileData setSize(long size) {
		_size = size;
		return this;
	}
	
	public long getSize() {
		return _size;
	}
	
	public PFFileData setHash(String hash) {
		_hash = hash;
		return this;
	}
	
	public String toString() {
		return " [" + _hash + "]: " + getRelativeFilenamePath();
	}
	
	public String getHash() {
		return _hash;
	}
	
	public void writeInfo(FileOutputStream fout) throws Exception {
		fout.write((_hash + "|" + String.valueOf(_size) + "|" + getFileName() + "|" + getPath() + "\n").getBytes());
	}
	
	public void addOriginal(PFFileData orig) {
		_origs.add(orig);
	}
	public boolean hasOriginals() {
		return _origs.size() > 0;
	}
	public ArrayList<PFFileData> getOriginals() {
		return _origs;
	}
	
	public void addDuplicate(PFFileData dup) {
		_dups.add(dup);
	}
	public boolean hasDuplicates() {
		return _dups.size() > 0;
	}
	public ArrayList<PFFileData> getDuplicates() {
		return _dups;
	}
	
	private String _fileName;
	private ArrayList<PFFileData> _origs = new ArrayList<PFFileData>(5);
	private ArrayList<PFFileData> _dups = new ArrayList<PFFileData>(10);
	private String _path;
	private long _size;
	private String _hash;
}

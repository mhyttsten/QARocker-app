package com.pf.dedup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class FileDiff {
	
	private String _fname1;
	private String _fname2;
	private DedupsMain _m;

	public FileDiff(DedupsMain ddm, String fname1, String fname2) {
		_m = ddm;
		_fname1 = fname1;
		_fname2 = fname2;
	}
	
	private File _file1;
	private File _file2;
	private FileInputStream _fin1;
	private FileInputStream _fin2;
	
	public boolean isEqual() throws Exception {
		try {
			boolean result = isEqualImpl();
			return result;
		} catch(Exception exc) {
			if (_fin1 != null) {
				try { _fin1.close(); } catch(Exception excf1) {}
			}
			if (_fin2 != null) {
				try { _fin2.close(); } catch(Exception excf2) {}
			}
			throw exc;
		}
	}

	private ByteArrayOutputStream _bout1 = new ByteArrayOutputStream();
	private int _readLength1 = -1;
	private ByteArrayOutputStream _bout2 = new ByteArrayOutputStream();
	private int _readLength2 = -1;
	
	private void fillData() throws Exception {
		byte[] data1 = new byte[1024*1024];
		byte[] data2 = new byte[1024*1024];
		
		_readLength1 = _fin1.read(data1);
		if(_readLength1 != -1) {
			_bout1.write(data1, 0, _readLength1);
		}

		_readLength2 = _fin2.read(data2);
		if(_readLength2 != -1) {
			_bout2.write(data2, 0, _readLength2);
		}
	}	
	
	private boolean isEqualImpl() throws Exception {
		_file1 = new File(_fname1);
		_file2 = new File(_fname2);
		
		if (!_file1.exists()) {
			throw new Exception("Filename to compare did not exist: " + _fname1);
		}
		if (!_file2.exists()) {
			throw new Exception("Filename to compare did not exist: " + _fname2);
		}
		
		if(_file1.length() != _file2.length()) {
			_m.L.warning("Comparing payload of two files, but bailing already on size");
			_m.L.warning("   File1 [" + _file1.length() + "]: "+ _fname1);
			_m.L.warning("   File2 [" + _file2.length() + "]: "+ _fname2);
			return false;
		}
		
		_fin1 = new FileInputStream(_file1);
		_fin2 = new FileInputStream(_file2);

		while (true) {
			fillData();

			byte[] bdata1 = _bout1.toByteArray();
			if(bdata1 == null) {
				bdata1 = new byte[0];
			}
			_bout1.reset();

			byte[] bdata2 = _bout2.toByteArray();
			if(bdata2 == null) {
				bdata2 = new byte[0];
			}
			_bout2.reset();

			int length = bdata1.length;
			if(bdata1.length > bdata2.length) {
				length = bdata2.length;
			}
			for(int i=0; i < length; i++) {
				if(bdata1[i] != bdata2[i]) {
					return false;
				}
			}

			// Write back data
			if (bdata1.length > length) {
				_bout1.write(bdata1, length, bdata1.length-length);
			}
			if (bdata2.length > length) {
				_bout2.write(bdata2, length, bdata2.length-length);
			}

			// If f1 is out but f2 is not then they are not equal
			if ( (_readLength1 == -1 && _bout1.size() == 0)  && (_readLength2 != -1 || _bout2.size() != 0) ) {
				return false;
			}

			// If f2 is out but f1 is not then they are not equal
			if ( (_readLength2 == -1 && _bout2.size() == 0)  && (_readLength1 != -1 || _bout1.size() != 0) ) {
				return false;
			}

			// If all is drained, then all data passed the equality test
			if (_readLength1 == -1 && _readLength2 == -1 && _bout1.size() == 0 && _bout2.size() == 0) {
				return true;
			}
		}
	}	
}

package com.pf.dedup;

// Does .png work for iPhoto?
// Add scanning of weird filenames
// - Any character which is not 7-bit ASCII
// - Anything that does not start with alphanumeric
// - Anything that does not have postfix of pictures

import java.io.File;
import java.util.ArrayList;


public class DedupsMain_Delete {

	private DedupsMain _m;
	private String _deleteFileName;
	
	public void opDelete(
			DedupsMain main,
			String deleteFileName) {
		try {
			_m = main;
			_deleteFileName = deleteFileName;
			opDeleteImpl();
		} catch(Exception exc) {
			_m.L.severe("Exception caught");
			_m.L.severe(exc.getMessage());
			exc.printStackTrace();
		} finally {
			deinitialize();
		}
	}
	
	public void opDeleteImpl() throws Exception {
		_m.L.info("Now starting");
		_m.L.info("Delete filename: " + _deleteFileName);
		
		ArrayList<String[]> hashEntries = MM.fileReadToLin_eelements(
				_deleteFileName, "#", "\\|", null);
		for(String[] fileNameLine: hashEntries) {
			String fileName = fileNameLine[0];
			if (fileName == null || fileName.trim().length() == 0) {
				continue;
			}
			_m.L.info("Now trying to delete file: " + fileName);
			
			File f = new File(fileName);
			if (!f.exists()) {
				if (!fileName.equals(".DS_Store")) {
					_m.L.severe("File in delete file - !exists: " + fileName);
				}
				continue;
			}
			if (!f.canRead()) {
				_m.L.severe("File in delete file - !canRead: " + fileName);				
			}
			if (!f.canWrite()) {
				boolean setWritable = f.setWritable(true, false);
				boolean newWrite = f.canWrite();
				if (setWritable) { 
					if (newWrite) {
						_m.L.severe("File in delete file - Was !canWrite but was able to change to canWrite: " + fileName);
					} else {
						_m.L.severe("File in delete file - Was !canWrite and was not able to change to canWrite: " + fileName);
					}
				} else {
					_m.L.severe("File in delete file - Was !canWrite and failed on setWritable: " + fileName);
				}
			}
			boolean deleted = f.delete();
			if (!deleted) {
				_m.L.severe("File in delete file - UNABLE TO DELETE: " + fileName);
				throw new Exception("Unable to delete file: " + fileName);
			}
		}
		_m.L.info("Delete operations finished");
	}
	
	private void deinitialize() {
	}
}

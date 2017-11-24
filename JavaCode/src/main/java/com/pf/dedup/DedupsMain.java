package com.pf.dedup;

// TODO

// Does .png work for iPhoto?
// Add scanning of weird filenames
// - Any character which is not 7-bit ASCII
// - Anything that does not start with alphanumeric
// - Anything that does not have postfix of pictures
// Automatically remove AS LAST STEP anything from target that is .DS_Store or Thumbs.db

/* 
fileRMOut("echo To delete all empty directories");
fileRMOut("echo    Use $ find test -type d -depth -empty -delete");
fileRMOut("echo To list all non-empty directories");
fileRMOut("echo    Use $ find test -type d -depth -not -empty");
find  . -name "*.txt" -type f -delete
*/
// Unlock all the files under Todo_CCC_3
// chflags -R nouchg Todo_CCC_3
//


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

// Logging is part of java.util.logging package
//
// Level.
//  SEVERE
//  WARNING
//  INFO
//  CONFIG  // For HttpTransport most info available here (except Authorization header)
//  FINE
//  FINER
//  FINEST
//  OFF
//  ALL // Also includes the Authorization header
// 
// Handlers receive log message and write them
// Two built-in handlers exist: ConsoleHandler, FileHandler
// Log levels INFO and higher are automatically written to console
//
// Each handler can be configured with a formatter
// Two built-in formatters exist: SimpleFormatter, XMLFormatter
//
// LogManager sets log level for a package through LogManager.setLevel(name, level)
// LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.FINE)
//
// // Get globbal Logger object
// Logger logger = LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME);
// FileHandler fhTxt = new FileHandler("logging.txt");
// fhTxt.setFormatter(new SimpleFormatter());
// logger.addHandler(fhTxt);
//
// public final static Logger LOGGER = Logger.getLogger(MyClass.class.getName());
// LOGGER.setLevel(Level.INFO);
// LOGGER.severe("Info log");
// LOGGER.warning("Info log");
// LOGGER.info("Info log");
//

public class DedupsMain {
	
	public final static Logger L = Logger.getLogger("DEDUP");	
	
	private static final String OP_CALCULATE_STR = "calculate";
	private static final int OP_CALCULATE = 1;
	private static final String OP_COMPARE_STR = "compare";
	private static final int OP_COMPARE = 2;
	private static final String OP_DELETE_STR = "delete";
	private static final int OP_DELETE = 3;
		
	public static void main(String[] args) {
		new DedupsMain().mainImpl(args);
	}
	
	private void mainImpl(String[] args) {
		try {
			String result = mainImpl2(args);
			if (result != null) {
				System.out.println(result);
			}
		} catch(Exception exc) {
			try {
				L.severe("Exception caught in mainImpl2: " + exc.toString());
				exc.printStackTrace();
			} catch(Exception exc2) {
				L.severe("Exception2 caught in mainImpl2: " + exc2.toString());
				exc2.printStackTrace();
			} finally {
				deinitialize();
			}
		}
	}
	
	private String mainImpl2(String[] args)  throws Exception {
		// Set the command line options
		if(args.length < 1 ||
		   (!args[0].equals(OP_CALCULATE_STR) &&
			!args[0].equals(OP_COMPARE_STR) &&
			!args[0].equals(OP_DELETE_STR))) {
			return "Usage\n" +
					"   calculate <targetHashFileName> <soureDir1> ... <sourceDirn>\n" +
					"   compare   [checksum|content]  <targetRNScriptFileName> <origHashFileName> <dupsHashFileName>" + 
					"   delete <deleteFileName>";
		}
		
		_startDirectory = System.getProperty("user.dir");
		_dateNow = MM.getNowAs_YYMMDD_HHMMSS();
		
		Formatter formatter = new MyFormatter();
		FileHandler fh = new FileHandler("dedup_logfile_" + args[0] + "_" + _dateNow + ".txt");
		fh.setFormatter(formatter);
		ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(formatter);
		
		L.setUseParentHandlers(false);
		L.addHandler(fh);
		L.addHandler(ch);
		L.setLevel(Level.ALL);
		
		L.info("Current directory is: " + _startDirectory);
		if (!_startDirectory.endsWith(File.separator)) {
			_startDirectory += File.separator;
		}
		
		if(args[0].equals(OP_CALCULATE_STR)) {
			if(args.length < 3) {
				throw new Exception(OP_CALCULATE_STR + " requires hashFile as second argument, followed by directory list");
			}
			L.info("This command calculates hash values");
			L.info("Checking that all directories exists");
			String hashFileName = args[1];
			ArrayList<String> calculatedDirs = new ArrayList<String>();
			for (int i=2; i < args.length; i++) {
				File f = new File(args[i]);
				if (!f.exists()) {
					throw new Exception("Directory: " + args[i] + " does not exist");
				}
				calculatedDirs.add(args[i]);
			}
			L.info("All directories exists, now starting processing");
			L.info("\n********************************************************");
			System.out.println("You are about to start, please confirm");
			DedupsMain_Calculate dedupsCalculate = new DedupsMain_Calculate();
			dedupsCalculate.opCalculate(this, hashFileName, calculatedDirs);
		} else if(args[0].equals(OP_COMPARE_STR)) {
			if(args.length < 5 || args[2].equals(args[3])) {
				throw new Exception(OP_COMPARE_STR + " requires checksum, rmFileName and two hash-files to compare as argument\n" +
						"args.length: " + args.length + "\n" +
						Arrays.toString(args));
			}
			if(!args[1].equals(DedupsMain_Compare.MODE_CHECKSUM_STR) && !args[1].equals(DedupsMain_Compare.MODE_CONTENT_STR)) {
				throw new Exception(OP_COMPARE_STR + " second argument must indicate diff depth");
			}
			
			DedupsMain_Compare dedupsCompare = new DedupsMain_Compare();
			dedupsCompare.opCompare(this,  args[1],  args[2], args[3], args[4]);
		} else if(args[0].equals(OP_DELETE_STR)) {
			if(args.length < 2) {
				throw new Exception(OP_DELETE_STR + " requires delete file list");
			}
			DedupsMain_Delete dedupsDelete = new DedupsMain_Delete();
			dedupsDelete.opDelete(this,  args[1]);
		}
		
		deinitialize();
		return null;
	}
	
	//------------------------------------------------------------------------
	
	public static boolean validMediaFileName(String fname) {
		fname = fname.toLowerCase();
		if (fname.endsWith(".png") ||
			fname.endsWith(".gif") ||
			fname.endsWith(".jpg") ||
			fname.endsWith(".jpeg") ||
			fname.endsWith(".m4v") ||
			fname.endsWith(".mp4") ||
			fname.endsWith(".mov") ||
			fname.endsWith(".avi") ||
			fname.endsWith(".nef")) {
			return true;
		}
		return false;
	}
	
	public void deinitialize() {		
	}
	
	public String getDateNow() {
		return _dateNow;
	}
	
	private String _dateNow;
	private String _startDirectory;	
}

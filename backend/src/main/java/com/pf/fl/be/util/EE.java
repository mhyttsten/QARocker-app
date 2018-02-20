package com.pf.fl.be.util;

import java.io.IOException;
import java.util.logging.Logger;

import com.pf.fl.be.datastore.DS;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

public class EE {
	
	public static final String TIMEZONE_STOCKHOLM = "Europe/Stockholm";
	public static final String TIMEZONE_NEW_YORK  = "America/New_York";
	private static EE mEE;
	
	//------------------------------------------------------------------------
	public static EE getEE() throws IOException {
		if (mEE != null) {
			return mEE;
		}

		try {
			EE ee = new EE();
			ee.initialize();
			mEE = ee;
		} catch(IOException exc1) {
			throw exc1;
		} catch(Exception exc2) {
			throw new IOException(exc2);
		}
		return mEE;
	}
		
	//------------------------------------------------------------------------
	public static final long INTERVAL_MAX = (8*60) * 1000;
	private static long mTimerStart = (System.currentTimeMillis() - 5000);
	public synchronized static void timerStart() {
		mTimerStart = (System.currentTimeMillis() - 10000);	
	}
	public synchronized static long timerGetStartTime() { return mTimerStart; }
	public synchronized static long timerLeft() {
		long now = System.currentTimeMillis();
		long diff = now - mTimerStart;
		return INTERVAL_MAX - diff;
	}
	public synchronized static boolean timerContinue() {
		long now = System.currentTimeMillis();
		long diff = now - mTimerStart;
		return diff < INTERVAL_MAX;
	}
    public synchronized static boolean timerContinueBigMargin() {
        long now = System.currentTimeMillis();
        long diff = now - mTimerStart;
        return diff < (9*60*1000);
    }

    /**
     *
     */
	public synchronized static String getExecutionDay() {
		return MM.getNowAs_YYMMDD(EE.TIMEZONE_STOCKHOLM);
	}

    /**
     *
     */
	public synchronized String getProjectName() { return "pffundlifter"; }
	
	//------------------------------------------------------------------------
	public static final String ENCODING_HTTP_READ = "UTF-8";
	public static final String ENCODING_FILE_READ = "UTF-8";
	public static final String ENCODING_FILE_WRITE = "UTF-8";
	
	//------------------------------------------------------------------------
	public synchronized void initialize() throws Exception {
        DS.initialize();
		_iwLog = new IndentWriter(false);
		_iwLog.setIndentChar('.');
		_iwLog.setIndentDistance(4);
		_isInitialized = true;
		mEE = this;
	}
	
	//------------------------------------------------------------------------
	public synchronized void deinitialize() throws Exception {
	}

	//------------------------------------------------------------------------
	public synchronized void dpush() throws Exception {
		_iwLog.push();
	}
	public synchronized void dpop() throws Exception { 
		_iwLog.pop(); 
	}
	public synchronized void ddpush() throws Exception {
		_iwLog.push();
		_iwLog.push();
	}
	public synchronized void ddpop() throws Exception {
		_iwLog.pop();
		_iwLog.pop();
	}
	public static final int TYPE_FINE = 1;
	public static final int TYPE_INFO = 2;
	public static final int TYPE_WARNING = 3;
	public static final int TYPE_SEVERE = 4;
	public synchronized void dfine(Logger log, String TAG, String str) throws IOException { d(log, TAG, TYPE_FINE, str); }
	public synchronized void dinfo(Logger log, String TAG, String str) throws IOException { d(log, TAG, TYPE_INFO, str); }
	public synchronized void dwarning(Logger log, String TAG, String str) throws IOException { d(log, TAG, TYPE_WARNING, str); }
	public synchronized void dsevere(Logger log, String TAG, String str) throws IOException { d(log, TAG, TYPE_SEVERE, str); }
	public synchronized void d(Logger log, String TAG, int type, String str) throws IOException {
		try {
			if(_iwLog == null) {
				_iwLog = new IndentWriter();
			}
			str = TAG + ": " + str;
			_iwLog.println(str);
			String msg = _iwLog.getString();
			switch (type) {
			case TYPE_FINE:
				log.fine(msg);
				break;
			case TYPE_INFO:
				log.info(msg);
				break;
			case TYPE_WARNING:	
				log.warning(msg);
				break;
			case TYPE_SEVERE:
				log.severe(msg);
				break;
			default:
				throw new IOException("Unknown type: " + type);
			}
			_iwLog.clear();
		} catch(Exception exc) {
			MM.throwIOException(null, exc);
		}
	}

	// ***********************************************************************

	private IndentWriter _iwLog;
	private boolean _isInitialized = false;




}

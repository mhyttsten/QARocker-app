package com.pf.fl.be.extract;

import com.pf.fl.be.util.EE;
import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.io.IOException;
import java.util.logging.Logger;

public class FLOps1_Ext1_HTMLGet {
	private static final Logger log = Logger.getLogger(FLOps1_Ext1_HTMLGet.class.getName());
	private static final String TAG = MM.getClassName(FLOps1_Ext1_HTMLGet.class.getName());
	
	public static class Response {
		public byte[] _pageContentBA;
		public String _httpURL;
		public String _accountType;
	}

    public static byte[] htmlGet(
            EE ee,
            IndentWriter iw,
            String url,
            int retryIntervalInMS, int retryCount) throws IOException {
        iw.println("FLOps1_Ext1_HTMLGet.htmlGet, entered with URL: " + url + "retryIntervalMS: " + retryIntervalInMS + ", retryCount: " + retryCount);
        byte[] result = htmlGetImpl(ee, iw, url, retryIntervalInMS, retryCount);
        if (result != null) {
            iw.println("Returning: " + result.length + " number of bytes");
        } else {
            iw.println("Returning null");
        }
        return result;
    }

	private static byte[] htmlGetImpl(
			EE ee,
			IndentWriter iw,
			String url,
			int retryIntervalInMS, int retryCount) throws IOException {

		byte[] pageContent = null;
		do {
			try {
				pageContent = MM.getURLContentBA(url);
				return pageContent;
			} catch(Exception exc) {
				ee.dwarning(log, TAG, "*****************************************************\n" +
						"    ERROR ERROR ERROR, error, error, error\n" +
						"    Exception caught while extracting the HTML pages\n" + 
						"    Retry count: " + retryCount + "\n" +
						"    Exception: " + exc.toString() + "\n" +
						"    " + MM.getStackTraceString(exc));
				iw.println("FLMain, exception caught while extracting the HTML pages\n" +
						"   Exception error: " + exc.toString() + "\n" +
						"  " + MM.getStackTraceString(exc) + "\n" +
						"   Retry count: " + retryCount);
				retryCount--;
				if (retryCount == 0) {
					return null;
				}
				MM.sleepInMS(retryIntervalInMS);
			}
		} while(retryCount > 0);
		return null;
	}
}

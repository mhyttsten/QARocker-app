package com.pf.fl.be.datamodel_raw;

import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.util.ArrayList;

public class REFundInfo_DPDay {
	
	//------------------------------------------------------------------------
	public static REFundInfo_DPDay instantiateFrom(ArrayList<String> data, IndentWriter iw) throws Exception {
		if(!isMe(data.get(0))) {
			throw new Exception("This is not an instance of me starting with: " + data.get(0));
		}
 			
		MM.iwPrintln(iw, "MSRowData.instantiateFrom");
		MM.iwPush(iw);
		REFundInfo_DPDay result = new REFundInfo_DPDay();
		String elem1 = data.remove(0);
		MM.iwPrintln(iw, "Elem1: " + elem1);
		result._currency = elem1.substring(4, elem1.length()-1);
		MM.iwPrintln(iw, "Currency: " + result._currency);
		result._dateYYMMDD = data.remove(0);
		MM.iwPrintln(iw, "Date: " + result._dateYYMMDD);
		result._r1d = data.remove(0);
		MM.iwPrintln(iw, "R1D: " + result._r1d);
		result._r1w = data.remove(0);
		MM.iwPrintln(iw, "R1W: " + result._r1w);
		result._r1m = data.remove(0);
		MM.iwPrintln(iw, "R1M: " + result._r1m);
		result._r3m = data.remove(0);
		MM.iwPrintln(iw, "R3M: " + result._r3m);
		result._r6m = data.remove(0);
		MM.iwPrintln(iw, "R6M: " + result._r6m);
		result._r1y = data.remove(0);
		MM.iwPrintln(iw, "R1Y: " + result._r1y);
		result._r3y = data.remove(0);
		MM.iwPrintln(iw, "R3Y: " + result._r3y);
		result._r5y = data.remove(0);
		MM.iwPrintln(iw, "R5y: " + result._r5y);
		result._r10y = data.remove(0);
		MM.iwPrintln(iw, "R10Y: " + result._r10y);
		MM.iwPop(iw);
		return result;
	}

	//------------------------------------------------------------------------
	public static boolean isMe(String s) {
		return s.startsWith("AVK[");
	}
	
	//------------------------------------------------------------------------
	public String toString() {
		return getStats();
	}
	
	//------------------------------------------------------------------------
	public String getStats() {
		String result = "";
		result =  
				"AVK[" + _currency + "]" + "~" +
						_dateYYMMDD + "~" +
						_r1d + "~" +
						_r1w + "~" +
						_r1m + "~" +
						_r3m + "~" +
						_r6m + "~" +
						_r1y + "~" +
						_r3y + "~" +
						_r5y + "~" +
						_r10y;
		return result;
	}

	//------------------------------------------------------------------------
	public void setCurrency(String p) { if(p != null) _currency = p; }
	public void setDataPointDate(String p) { if(p != null) _dateYYMMDD = p; }
	public void setR1D(String p) { if(p != null) _r1d = p; }
	public void setR1W(String p) { if(p != null) _r1w = p; }
	public void setR1M(String p) { if(p != null) _r1m = p; }
	public void setR3M(String p) { if(p != null) _r3m = p; }
	public void setR6M(String p) { if(p != null) _r6m = p; }
	public void setR1Y(String p) { if(p != null) _r1y = p; }
	public void setR3Y(String p) { if(p != null) _r3y = p; }
	public void setR5Y(String p) { if(p != null) _r5y = p; }
	public void setR10Y(String p) { if(p != null) _r10y = p; }
	
	public String getDateYYMMDD() { return _dateYYMMDD; }
	public String getCurrency() { return _currency; }
	public String getR1D() { return _r1d; }
	public String getR1W() { return _r1w; }
	public String getR1M() { return _r1m; }
	public String getR3M() { return _r3m; }
	public String getR6M() { return _r6m; }
	public String getR1Y() { return _r1y; }
	public String getR3Y() { return _r3y; }
	public String getR5Y() { return _r5y; }
	public String getR10Y() { return _r10y; }
	
	//------------------------------------------------------------------------
	public boolean equals(Object o) {
		REFundInfo_DPDay arg = (REFundInfo_DPDay)o;
		if(!MM.equals(_currency, arg._currency, null, null) ||
  		   !MM.equals(_dateYYMMDD, arg._dateYYMMDD, null, null) ||
  		   !MM.equals(_r1d, arg._r1d, null, null) ||
  		   !MM.equals(_r1w, arg._r1w, null, null) ||
  		   !MM.equals(_r1m, arg._r1m, null, null) ||
  		   !MM.equals(_r3m, arg._r3m, null, null) ||
  		   !MM.equals(_r6m, arg._r6m, null, null) ||
  		   !MM.equals(_r1y, arg._r1y, null, null) ||
  		   !MM.equals(_r3y, arg._r3y, null, null) ||
  		   !MM.equals(_r5y, arg._r5y, null, null) ||
  		   !MM.equals(_r10y, arg._r10y, null, null)) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 */
	public void addString(IndentWriter iw)  {
		iw.println("DPDay, date: " + _dateYYMMDD + ", currency: " + _currency);
		iw.push();
		iw.println("r1d:" + _r1d + ", r1w:" + _r1w + ", r1m:" + _r1m + ", r3m:" + _r3m + ", r6m:" + _r6m + ", r1y:" + _r1y + ", r3y:" + _r3y + ", r5y:" + _r5y + ", r10y:" + _r10y);
		iw.pop();
	}
	
	// ***********************************************************************

	public static REFundInfo_DPDay createFrom(REFundInfo_DPDay a) {
		REFundInfo_DPDay r = new REFundInfo_DPDay();
		r._currency = a.getCurrency();
		r._dateYYMMDD = a._dateYYMMDD;
		r._r1d = a.getR1D();
		r._r1w = a.getR1W();
		r._r1m = a.getR1M();
		r._r3m = a.getR3M();
		r._r6m = a.getR6M();
		r._r1y = a.getR1Y();
		r._r3y = a.getR3Y();
		r._r5y = a.getR5Y();
		r._r10y = a.getR10Y();
		return r;
	}
	
	private String _currency = "-";
	private String _dateYYMMDD = "-";
	private String _r1d = "-";
	private String _r1w = "-";
	private String _r1m = "-";
	private String _r3m = "-";
	private String _r6m = "-";
	private String _r1y = "-";
	private String _r3y = "-";
	private String _r5y = "-";
	private String _r10y = "-";
}

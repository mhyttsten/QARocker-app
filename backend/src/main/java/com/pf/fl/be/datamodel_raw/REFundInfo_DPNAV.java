package com.pf.fl.be.datamodel_raw;

import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.util.ArrayList;

public class REFundInfo_DPNAV {

	//------------------------------------------------------------------------
	public void   setNAVLatest_Value(String a) { _navLatest_Value = a; }
	public String getNAVLatest_Value()         { return _navLatest_Value; }
	public void setNAVLatest_Currency(String a) { _navLatest_Currency = a; }
	public String getNAVLatest_Currency()       { return _navLatest_Currency; }
	public void setNAVLatest_Date(String a) { _navLatest_Date = a; }
	public String getNAVLatest_Date()       { return _navLatest_Date; }

	//------------------------------------------------------------------------
	public void   setNAVHighest12M_Value(String a) { _navHighest12M_Value = a; }
	public String getNAVHighest12M_Value()         { return _navHighest12M_Value; }
	public void setNAVHighest12M_Currency(String a) { _navHighest12M_Currency = a; }
	public String getNAVHighest12M_Currency()       { return _navHighest12M_Currency; }
	public void setNAVHighest12M_Date(String a) { _navHighest12M_Date = a; }
	public String getNAVHighest12M_Date()       { return _navHighest12M_Date; }

	//------------------------------------------------------------------------
	public void   setNAVLowest12M_Value(String a) { _navLowest12M_Value = a; }
	public String getNAVLowest12M_Value()         { return _navLowest12M_Value; }
	public void setNAVLowest12M_Currency(String a) { _navLowest12M_Currency = a; }
	public String getNAVLowest12M_Currency()       { return _navLowest12M_Currency; }
	public void setNAVLowest12M_Date(String a) { _navLowest12M_Date = a; }
	public String getNAVLowest12M_Date()       { return _navLowest12M_Date; }
	
	//------------------------------------------------------------------------
	public static int myNumberOfElements() {
		return 9;
	}

	//------------------------------------------------------------------------
	public static REFundInfo_DPNAV instantiateFrom(ArrayList<String> data, IndentWriter iw) throws Exception {
		MM.iwPrintln(iw, "MSNAVData.instantiateFrom");
		MM.iwPush(iw);
		REFundInfo_DPNAV result = new REFundInfo_DPNAV();
		result.setNAVLatest_Value(data.remove(0));
		MM.iwPrintln(iw, "NAVLatest_Value: " + result._navLatest_Value);
		result.setNAVLatest_Currency(data.remove(0));
		MM.iwPrintln(iw, "NAVLatest_Currency: " + result._navLatest_Currency);
		result.setNAVLatest_Date(data.remove(0));
		MM.iwPrintln(iw, "NAVLatest_Date: " + result._navLatest_Date);
		result.setNAVHighest12M_Value(data.remove(0));
		MM.iwPrintln(iw, "NAVHighest12M_Value: " + result._navHighest12M_Value);
		result.setNAVHighest12M_Currency(data.remove(0));
		MM.iwPrintln(iw, "NAVHighest12M_Currency: " + result._navHighest12M_Currency);
		result.setNAVHighest12M_Date(data.remove(0));
		MM.iwPrintln(iw, "NAVHighetst12M_Date: " + result._navHighest12M_Date);
		result.setNAVLowest12M_Value(data.remove(0));
		MM.iwPrintln(iw, "NAVLowest12M_Value: " + result._navLowest12M_Value);
		result.setNAVLowest12M_Currency(data.remove(0));
		MM.iwPrintln(iw, "NAVLowest12M_Currency: " + result._navLowest12M_Currency);
		result.setNAVLowest12M_Date(data.remove(0));
		MM.iwPrintln(iw, "NAVLowest12M_Date: " + result._navLowest12M_Date);
		MM.iwPop(iw);
		return result;
	}

	//------------------------------------------------------------------------
	public String getStats() {
		return 
				getNAVLatest_Value() + "~" +
				getNAVLatest_Currency() + "~" +
				getNAVLatest_Date() + "~" +
				getNAVHighest12M_Value() + "~" +
				getNAVHighest12M_Currency() + "~" +
				getNAVHighest12M_Date() + "~" +
				getNAVLowest12M_Value() + "~" +
				getNAVLowest12M_Currency() + "~" +
				getNAVLowest12M_Date();
	}
	
	//------------------------------------------------------------------------
	public boolean equals(Object o) {
		REFundInfo_DPNAV arg = (REFundInfo_DPNAV)o;
		if(!MM.equals(_navLatest_Value, arg._navLatest_Value, null, null) ||
   		   !MM.equals(_navLatest_Currency, arg._navLatest_Currency, null, null) ||		
   		   !MM.equals(_navLatest_Date, arg._navLatest_Date, null, null) ||		
   		   !MM.equals(_navHighest12M_Value, arg._navHighest12M_Value, null, null) ||		
   		   !MM.equals(_navHighest12M_Currency, arg._navHighest12M_Currency, null, null) ||		
   		   !MM.equals(_navHighest12M_Date, arg._navHighest12M_Date, null, null) ||		
   		   !MM.equals(_navLowest12M_Value, arg._navLowest12M_Value, null, null) ||		
   		   !MM.equals(_navLowest12M_Currency, arg._navLowest12M_Currency, null, null) ||		
   		   !MM.equals(_navLowest12M_Date, arg._navLowest12M_Date, null, null)) {
			return false;
		}
		return true;
	}
	
	// ***********************************************************************
	
	public void addString(IndentWriter iw) {
		iw.println("NAVData");
		iw.push();
		iw.println("Latest[" + _navLatest_Value + ", " + _navLatest_Currency + ", " + _navLatest_Date + "]");
		iw.println("Highest12M[" + _navHighest12M_Value + ", " + _navHighest12M_Currency + ", " + _navHighest12M_Date + "]");
		iw.println("Lowest12M[" + _navLowest12M_Value + ", " + _navLowest12M_Currency + ", " + _navLowest12M_Date + "]");
		iw.pop();
	}
	
	public static REFundInfo_DPNAV createFrom(REFundInfo_DPNAV a) {
		REFundInfo_DPNAV r = new REFundInfo_DPNAV();
		r._navLatest_Value = a.getNAVLatest_Value();
		r._navLatest_Currency = a.getNAVLatest_Currency();
		r._navLatest_Date = a.getNAVLatest_Date();
		r._navHighest12M_Value= a.getNAVHighest12M_Value();
		r._navHighest12M_Currency = a.getNAVHighest12M_Currency();
		r._navHighest12M_Date = a.getNAVHighest12M_Date();
		r._navLowest12M_Value = a.getNAVLowest12M_Value();
		r._navLowest12M_Currency = a.getNAVLowest12M_Currency();
		r._navLowest12M_Date = a.getNAVLowest12M_Date();
		return r;
	}
	
	private String _navLatest_Value;
	private String _navLatest_Currency;
	private String _navLatest_Date;
	private String _navHighest12M_Value;
	private String _navHighest12M_Currency;
	private String _navHighest12M_Date;
	private String _navLowest12M_Value;
	private String _navLowest12M_Currency;
	private String _navLowest12M_Date;
}

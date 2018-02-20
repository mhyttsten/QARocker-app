package com.pf.fl.be.datamodel_raw;

import com.pf.shared.utils.IndentWriter;
import com.pf.shared.utils.MM;

import java.util.ArrayList;

public class REFundInfo_DPYear {

	//------------------------------------------------------------------------
	public REFundInfo_DPYear() {
	}

	//------------------------------------------------------------------------
	public REFundInfo_DPYear(String year, String resultFund, String resultCategory, String resultIndex) {
		_year = year;
		_resultFund = resultFund.replace(',', '.');
		_resultCategory = resultCategory.replace(',', '.');
		_resultIndex = resultIndex.replace(',', '.');
	}

    //------------------------------------------------------------------------
    public void dumpInfo(IndentWriter iw) {
        iw.println("Year: " + _year);
        iw.println("Result fund: " + _resultFund);
        iw.println("Result category: " + _resultCategory);
        iw.println("Result index: " + _resultIndex);
    }


	//------------------------------------------------------------------------
	public static REFundInfo_DPYear instantiateFrom(ArrayList<String> data, IndentWriter iw) throws Exception {
		MM.iwPrintln(iw, "MSFundYearData.instantiateFrom");
		MM.iwPush(iw);
		REFundInfo_DPYear result = new REFundInfo_DPYear();
		result._year = data.remove(0);
		MM.iwPrintln(iw, "Year: " + result._year);
		result._resultFund = data.remove(0);
		MM.iwPrintln(iw, "Fund: " + result._resultFund);
		result._resultCategory = data.remove(0);
		MM.iwPrintln(iw, "Category: " + result._resultCategory);
		result._resultIndex = data.remove(0);
		MM.iwPrintln(iw, "Index: " + result._resultIndex);
		MM.iwPop(iw);
		return result;
	}
	
	//------------------------------------------------------------------------
	public String toString() {
		return getStats();
	}

	//------------------------------------------------------------------------
	public String getStats() {
		return _year + "~" + _resultFund + "~" + _resultCategory + "~" + _resultIndex;
	}
	
	//------------------------------------------------------------------------
	public boolean equals(Object a) {
		REFundInfo_DPYear arg = (REFundInfo_DPYear)a;
		if(!MM.equals(_year, arg._year, null, null) ||
  		   !MM.equals(_resultFund, arg._resultFund, null, null) ||		
  		   !MM.equals(_resultCategory, arg._resultCategory, null, null) ||		
  		   !MM.equals(_resultIndex, arg._resultIndex, null, null)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 */
	public void addString(IndentWriter iw)  {
		iw.println("DPYear:" + _year + ", fund:" + _resultFund + ", category:" + _resultCategory + ", index:" + _resultIndex);
	}
	
	public static REFundInfo_DPYear createFrom(REFundInfo_DPYear a) {
		REFundInfo_DPYear r = new REFundInfo_DPYear();
		r._year = a._year;
		r._resultFund = a._resultFund;
		r._resultCategory = a._resultCategory;
		r._resultIndex = a._resultIndex;
		return r;
	}
	
		
	// ***********************************************************************
	
	public String _year;
	public String _resultFund;
	public String _resultCategory;
	public String _resultIndex;	
}

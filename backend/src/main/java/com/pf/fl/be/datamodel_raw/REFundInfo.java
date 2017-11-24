package com.pf.fl.be.datamodel_raw;

import com.pf.shared.IndentWriter;
import com.pf.shared.MM;

import java.util.ArrayList;
import java.util.List;

public class REFundInfo {
	
	public static final String ENCODING_ID = "140531";

	//------------------------------------------------------------------------
	public static final String ACCOUNT_TYPE_PPM = "PPM";
	public static final String ACCOUNT_TYPE_SEB = "SEB";
	public static final String ACCOUNT_TYPE_SPP = "SPP";
	public static final String ACCOUNT_TYPE_VANGUARD = "VGD";

	//------------------------------------------------------------------------
	public static final String SEPARATOR = "~";
	
	//------------------------------------------------------------------------
	public boolean isAccountTypePPM() { return ACCOUNT_TYPE_PPM.equals(getAccountType()); }
	public boolean isAccountTypeSEB() { return ACCOUNT_TYPE_SEB.equals(getAccountType()); }
	public boolean isAccountTypeSPP() { return ACCOUNT_TYPE_SPP.equals(getAccountType()); }
	
	// Account
	// Name
	// Date
	//

	//------------------------------------------------------------------------
	public static int myNumberOfElements() {
		return 9;
	}
	
	//------------------------------------------------------------------------
	public static REFundInfo instantiate(String line, IndentWriter iw) throws Exception {
		
		if(line == null || line.trim().length() == 0) {
			throw new Exception("Line is null or empty string");
		}
		
		line = line.trim();
		MM.iwPrintln(iw, "MSFundData.instantiate");
		MM.iwPush(iw);
		MM.iwPrintln(iw, "Line: " + line);
		
		String[] elems = line.split(SEPARATOR);
		if(elems == null || elems.length < myNumberOfElements() + REFundInfo_DPNAV.myNumberOfElements()) {
			throw new Exception("Line too short, expected 10 elements: " + line);
		}
		MM.iwPrintln(iw, "Line items");
		MM.iwPush(iw);
		ArrayList<String> data = new ArrayList<String>();
		for(int i=0; i < elems.length; i++) {
			data.add(elems[i]);
			MM.iwPrintln(iw, "[" + String.valueOf(i) + "]: " + elems[i]);
		}
		MM.iwPop(iw);
		
		REFundInfo result = new REFundInfo();
		
		MM.iwPrintln(iw, "Data items");
		MM.iwPush(iw);
		int index = 0;
		result._accountType = data.remove(0);
		MM.iwPrintln(iw, "Account Type: " + result._accountType);
		result._name = data.remove(0);
		MM.iwPrintln(iw, "Name: " + result._name);
		result.setDateYYMMDD(data.remove(0));
		MM.iwPrintln(iw, "Date: " + result.getDateYYMMDD());

		result._msCategory = data.remove(0);
		MM.iwPrintln(iw, "Category: " + result._msCategory);
		result._msRating = data.remove(0);
		MM.iwPrintln(iw, "Rating: " + result._msRating);
		result._currency = data.remove(0);
		MM.iwPrintln(iw, "Currency: " + result._currency);
		result._indexCompare = data.remove(0);
		MM.iwPrintln(iw, "Index: " + result._indexCompare);
		result._ppmNumber = data.remove(0);
		MM.iwPrintln(iw, "PPM: " + result._ppmNumber);
		result._dpNAV = REFundInfo_DPNAV.instantiateFrom(data, iw);
		result._url = data.remove(0);
		MM.iwPrintln(iw, "URL: " + result._url);
		MM.iwPop(iw);
		
		while(data.size() > 0) {
			MM.iwPrintln(iw,  "Now parsing another line for Row or Year data");
			MM.iwPush(iw);
			String e = data.get(0);
			MM.iwPrintln(iw,  "data.get(0) was: " + e);
			if(REFundInfo_DPDay.isMe(e)) {
				REFundInfo_DPDay msrd = REFundInfo_DPDay.instantiateFrom(data, iw);
				result._dpDays.add(msrd);
			} else {
				MM.iwPrintln(iw,  "Not Row Data, so we process it as Year Data");
				REFundInfo_DPYear msyd = REFundInfo_DPYear.instantiateFrom(data, iw);
				result._dpYears.add(msyd);
			}
			MM.iwPop(iw);
		}
		
		return result;
	}
	
	//------------------------------------------------------------------------
	public String getStats() {
		String result = 
				_accountType + SEPARATOR + 
				_name + SEPARATOR + 
				getDateYYMMDD() + SEPARATOR + 
				_msCategory + SEPARATOR + 
				_msRating + SEPARATOR + 
				_currency + SEPARATOR + 
				_indexCompare + SEPARATOR + 
				_ppmNumber + SEPARATOR + 
						(_dpNAV == null ? "" : _dpNAV.getStats()) + SEPARATOR +
				_url;
		for(int i=0; i < _dpDays.size(); i++) {
			REFundInfo_DPDay rowData = _dpDays.get(i);
			result += SEPARATOR + rowData.getStats();
		}
		if (_dpYears != null) {
			for (int i = 0; i < _dpYears.size(); i++) {
				REFundInfo_DPYear msfyr = _dpYears.get(i);
				result += SEPARATOR + msfyr.getStats();
			}
		}
		return result;
	}

	//------------------------------------------------------------------------
	public void setName(String a) { _name = a;}
	public void setMSCategory(String a) { _msCategory = a;}
	public void setMSRating(String a) { _msRating = a; }
	public void setCurrency(String a) { _currency = a; }
	public void setIndexCompare(String a) { _indexCompare = a; }
	public void setPPMNumber(String a) { _ppmNumber = a; }
	public void setDPYears(ArrayList<REFundInfo_DPYear> a) { _dpYears = a; }
	public void setNAVData(REFundInfo_DPNAV a) { _dpNAV = a; }
	public void setURL(String a) { _url = a; }
	public void setDPDays(ArrayList<REFundInfo_DPDay> msrd) { _dpDays = msrd; }
	public void setAccountType(String accountType) { _accountType = accountType; }
	
	//------------------------------------------------------------------------
	public static String getPerfIdFromURL(String url) { return MM.getRegExp(null, url, "perfid=", "", "&", true); }
	public String getURLPerfId() { return MM.getRegExp(null, getURL(), "perfid=", "", "&", true); }
	public List<REFundInfo_DPDay> getDPDays() { return _dpDays; }
	public String getName() { return _name; }
	public String getCurrency() { return _currency; }
	public String getPPMNumber() { return _ppmNumber; }
	public String getRating() { return _msRating; }
	public String getCategory() { return _msCategory; }
	public String getURL() { return _url; }
	public String getAccountType() { return _accountType; }

	//------------------------------------------------------------------------
	public boolean hasSameDPDate(REFundInfo msfd) throws Exception {
		if(_dpDays == null || _dpDays.size() == 0 ||
		   msfd._dpDays == null || msfd._dpDays.size() == 0) {
			throw new Exception("Error, fund had no DP date, fund name: " + _name);
		}
		
		if(!_dpDays.get(0).equals(msfd._dpDays.get(0))) {
			return false;
		}
		
		return true;
	}
	

	public List<REFundInfo_DPYear> getDPYears() {
		return _dpYears;
	}
	
	public String getIndexCompare() {
		return _indexCompare;
	}
	
	public static REFundInfo createFrom(REFundInfo a) {
		REFundInfo r = new REFundInfo();
		r._accountType  = a.getAccountType();
		r._name = a.getName();
		r.setDateYYMMDD(a.getDateYYMMDD());
		r._msCategory = a.getCategory();
		r._msRating = a.getRating();
		r._ppmNumber = a.getPPMNumber();
		r._indexCompare = a.getIndexCompare();
		r._currency = a.getCurrency();
		r._url = a.getURL();
		r._dpNAV = a._dpNAV;
		
		for (REFundInfo_DPYear y : a.getDPYears()) {
			r._dpYears.add(REFundInfo_DPYear.createFrom(y));
		}
		for (REFundInfo_DPDay y : a._dpDays) {
			r._dpDays.add(REFundInfo_DPDay.createFrom(y));
		}
		return r;
	}
	
	/**
	 * 
	 */
	public void addString(IndentWriter iw)  {
		iw.println("MSFundData_v150531, url: " + _url);
		iw.push();
		iw.println("type:      " + _accountType);
		iw.println("name:      " + _name);
		iw.println("date:      " + getDateYYMMDD());
		iw.println("currency:  " + _currency);
		iw.println("category:  " + _msCategory);
		iw.println("index:     " + _indexCompare);
		iw.println("rating:    " + _msRating);
		iw.println("ppmNumber: " + _ppmNumber);
		_dpNAV.addString(iw);
		for (REFundInfo_DPDay msrd : _dpDays) {
			msrd.addString(iw);
		}
		for (REFundInfo_DPYear msyd : _dpYears) {
			msyd.addString(iw);
		}
		iw.pop();
	}
	
	//------------------------------------------------------------------------
	public String getDateYYMMDD() {
		if (_neverUseDirectlyDateYYMMDD == null) {
			if (_dpDays != null && _dpDays.size() > 0) {
				REFundInfo_DPDay dpDay = _dpDays.get(0);
				_neverUseDirectlyDateYYMMDD = dpDay.getDateYYMMDD();
			}
		}
		return _neverUseDirectlyDateYYMMDD;
	}
	public void setDateYYMMDD(String dateYYMMDD) {
		_neverUseDirectlyDateYYMMDD = dateYYMMDD;
	}
	private String _neverUseDirectlyDateYYMMDD;
	
	private String _accountType; // SEB, PPM, SPP, etc
	private String _name;
	private String _msCategory;	
	private String _msRating;
	private String _ppmNumber;
	private String _indexCompare;
	private String _currency;
	private String _url;
	private REFundInfo_DPNAV _dpNAV;
	
	private List<REFundInfo_DPYear> _dpYears = new ArrayList<>(5);
	private List<REFundInfo_DPDay> _dpDays = new ArrayList<>(5);
}

package com.pf.dedup;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MM {
	
	public static void printFileInfo(File f) throws Exception {
		System.out.println("Processing a file:");
		System.out.println(   "getAbsolutePath:  " + f.getAbsolutePath());
		System.out.println(   "getCanonicalPath: " + f.getCanonicalPath());
		System.out.println(   "getName:          " + f.getName());
		System.out.println(   "getParent:        " + f.getParent());
		System.out.println(   "getPath:          " + f.getPath());
	}
	
	//------------------------------------------------------------------------
	public static void main(String[] args) {
		try {
			/*
			System.out.println("120324 F: " + fl_isTradingDay("120324"));
			System.out.println("120325 F: " + fl_isTradingDay("120325"));
			System.out.println("120326 T: " + fl_isTradingDay("120326"));
			System.out.println("120323 T: " + fl_isTradingDay("120323"));
			System.out.println("120916 F: " + fl_isTradingDay("120916"));
			System.out.println("120917 T: " + fl_isTradingDay("120917"));
			System.out.println("120915 F: " + fl_isTradingDay("120915"));
			System.out.println("120914 T: " + fl_isTradingDay("120914"));
			*/
			
			int year = 2012;
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(new Date());
			while(year < 2014) {
				year = gc.get(Calendar.YEAR);
				gc.add(Calendar.DAY_OF_MONTH, 1);
				if(MM.fl_isTradingDay(gc)) {
					System.out.println("Y: " + year + ", trading day is: " + gc.getTime().toString());
				}
			}
			
			
			
			
			//byte[] content = fileReadFrom("/tmp/tmp/FundList_UTF8.txt");
			//String str = new String(content, "UTF-8");
			//System.out.println("Read data is: " + str);
			//content = str.getBytes("UTF-8");
			//FileOutputStream fout = new FileOutputStream("/tmp/tmp/FundList_UTF8.txt");
			//fout.write(content);
		} catch(Exception exc) {
			System.out.println("Exception: " + exc);
			exc.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------------------
	public static boolean equals(Object a1, Object a2, String fieldName, IndentWriter iw) {
		if(fieldName == null) {
			fieldName = "";
		}
		if(iw == null) {
			iw = new IndentWriter();
		}
		
		if(a1 == null && a2 == null) {
			return true;
		}
		if(a1 == null && a2 != null) {
			iw.println(fieldName + ", arg1 was null but arg2 was non-null");
			return false;
		}
		if(a1 != null && a2 == null) {
			iw.println(fieldName + ", arg1 was non-null but arg2 was null");
			return false;
		}
		boolean result = a1.equals(a2);
		if(!result) {
			iw.println(fieldName + ", arg1 and arg2 were non-null but they were not equals");
			iw.push();
			iw.println("arg1: " + a1.toString());
			iw.println("arg2: " + a2.toString());
			iw.pop();
		}
		return result;
	}

	
	//-------------------------------------------------------------------------------------
	public static String getGCString(GregorianCalendar gc) throws Exception {
		
		String year = String.valueOf(gc.get(Calendar.YEAR));
		int monthInt = gc.get(Calendar.MONTH);
		monthInt++;
		String month = String.valueOf(monthInt);
		if(month.length() == 1) {
			month = "0" + month;
		}
		String dayOfMonth = String.valueOf(gc.get(Calendar.DAY_OF_MONTH));
		if(dayOfMonth.length() == 1) {
			dayOfMonth = "0" + dayOfMonth;
		}
		
		String dayOfW_eek = "unknown";
		switch(gc.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			dayOfW_eek = "Monday";
			break;
		case Calendar.TUESDAY:
			dayOfW_eek = "Tuesday";
			break;
		case Calendar.WEDNESDAY:
			dayOfW_eek = "Wednesday";
			break;
		case Calendar.THURSDAY:
			dayOfW_eek = "Thursday";
			break;
		case Calendar.FRIDAY:
			dayOfW_eek = "Friday";
			break;
		case Calendar.SATURDAY:
			dayOfW_eek = "Saturday";
			break;
		case Calendar.SUNDAY:
			dayOfW_eek = "Sunday";
			break;
		}
		
		return year + "-" + month + "-" + dayOfMonth + ": " + dayOfW_eek;
	}
	
	//-------------------------------------------------------------------------------------
	public static int fl_compareToStrings(String o1, String o2) {
		if(o1 == null && o2 == null) {
			return 0;
		} else if(o1 == null && o2 != null) {
			return -1;
		} else if(o1 != null && o2 == null) {
			return 1;
		}
		return o1.compareTo(o2);
	}

	//-------------------------------------------------------------------------------------
	public static int fl_compareTo(GregorianCalendar a1, GregorianCalendar a2) throws Exception {
		int result = a1.getTime().compareTo(a2.getTime());

		System.out.println("MM.fl_compareTo:");
		System.out.println("   a1: " +  MM.getGCString(a1));
		System.out.println("   a2: " +  MM.getGCString(a2));
		System.out.println("   result: " +  result);
		return result;
	}
	
	//-------------------------------------------------------------------------------------
	public static boolean fl_isEqualYYMMDD(GregorianCalendar a1, GregorianCalendar a2) throws Exception {
		return
				a1.get(Calendar.YEAR) == a2.get(Calendar.YEAR) &&
				a1.get(Calendar.MONTH) == a2.get(Calendar.MONTH) &&
				a1.get(Calendar.DAY_OF_MONTH) == a2.get(Calendar.DAY_OF_MONTH);
	}

	//-------------------------------------------------------------------------------------
	public static ArrayList<GregorianCalendar> fl_getTradingDayGapNI(
			GregorianCalendar gcLast,
			GregorianCalendar gcNext) throws Exception {
		if(gcLast.getTime().compareTo(gcNext.getTime()) >=0 ) {
			throw new Exception("Last is not smaller than next. Last: " + gcLast.getTime().toString() + ", next: " + gcNext.getTime().toString());
		}
		if(fl_isTradingDay(gcLast)) {
			throw new Exception("GCLast is not a trading day: " + gcLast.getTime().toString());
		}
		if(fl_isTradingDay(gcNext)) {
			throw new Exception("GCNextt is not a trading day: " + gcNext.getTime().toString());
		}

		ArrayList<GregorianCalendar> result = new ArrayList<GregorianCalendar>();
		GregorianCalendar gcWork = gcLast;
		while(true) {
			gcWork.add(Calendar.DAY_OF_MONTH, 1);
			if(fl_isEqualYYMMDD(gcWork, gcNext)) {
				break;
			}
			if(fl_isTradingDay(gcWork)) {
				result.add(gcWork);
			}
		}
		return result;
	}
	
	//-------------------------------------------------------------------------------------
	public static GregorianCalendar fl_getNextTradingDay(GregorianCalendar arg) throws Exception {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(arg.getTime());
		while(true) {
			gc.add(Calendar.DAY_OF_MONTH, 1);
			if(MM.fl_isTradingDay(gc)) {
				break;
			}
		}
		return gc;
	}
	
	//-------------------------------------------------------------------------------------
	public static GregorianCalendar fl_addDaysToNext(GregorianCalendar arg, int dayFromCalendarClass) throws Exception {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(arg.getTime());
		while(true) {
			gc.add(Calendar.DAY_OF_MONTH, 1);
			if(gc.get(Calendar.DAY_OF_WEEK) == dayFromCalendarClass) {
				break;
			}
		}
		return gc;
	}

	//-------------------------------------------------------------------------------------
	public static boolean fl_isTradingDay(GregorianCalendar gc) throws Exception {
    	int dayOfW_eek = gc.get(GregorianCalendar.DAY_OF_WEEK);
    	//System.out.println("   " + gnow.getTime().toString());
    	
    	switch(dayOfW_eek) {
    	case Calendar.SATURDAY:
    	case Calendar.SUNDAY:
    		return false;
    	}
    	return true;
	}
	public static boolean fl_isTradingDay(String yymmdd) throws Exception {
		String yyStr = yymmdd.substring(0,2);
		String mmStr = yymmdd.substring(2, 4);
		String ddStr = yymmdd.substring(4);
		int yy = Integer.parseInt(yyStr) + 2000;
		int mm = Integer.parseInt(mmStr) - 1;
		int dd = Integer.parseInt(ddStr);
		
    	GregorianCalendar gnow = new GregorianCalendar();    
    	gnow.set(GregorianCalendar.YEAR, yy);
    	gnow.set(GregorianCalendar.MONTH, mm);
    	gnow.set(GregorianCalendar.DAY_OF_MONTH, dd);
    	gnow.set(GregorianCalendar.SECOND, 1);
    	gnow.set(GregorianCalendar.HOUR_OF_DAY, 0);
    	gnow.set(GregorianCalendar.MINUTE, 0);
    	
    	return fl_isTradingDay(gnow);    	
	}
	
	//------------------------------------------------------------------------
	public static String getRegExp(String content, String findTagLoc, String findAfter, String findTo, boolean trim) throws Exception {
		Pattern p = Pattern.compile(findTagLoc);
		Matcher m = p.matcher(content);
		boolean found = m.find();		
		if(!found) {
			return null;
		}
		content = content.substring(m.end());
		
		p = Pattern.compile(findAfter);
		m = p.matcher(content);
		found = m.find();		
		if(!found) {
			return null;
		}
		content = content.substring(m.end());
		
		p = Pattern.compile(findTo);
		m = p.matcher(content);
		found = m.find();		
		if(!found) {
			return null;
		}
		content = content.substring(0, m.end()-findTo.length());
		
		if(trim) {
			content = content.trim();
		}		
		return content;
	}
	
	//-------------------------------------------------------------------------------------
	public static String replaceArgTo(String arg, String search, String replace) throws Exception {
		if(arg == null || search == null || replace == null) {
			return null;
		}
		
		int io = arg.indexOf(search);
		if(io == -1) {
			return arg;
		}
		
		String newSearch = arg.substring(0, io);
		if(io >= arg.length() + search.length() - replace.length()) {
			return newSearch;
		}
		
		newSearch = newSearch + replace + arg.substring(io+search.length());
		String result = replaceArgTo(newSearch, search, replace);
		return result;
	}
	
	//-------------------------------------------------------------------------------------
	public static ArrayList<String[]> fileReadToLin_eelements(
			String fileName,
			String skipStartSeq,
			String separator, 
			String encoding) throws Exception {
		return fileReadToLin_eelements(new File(fileName), skipStartSeq, separator, encoding);
	}
	public static ArrayList<String[]> fileReadToLin_eelements(
			File file,
			String skipStartSeq,
			String separator,
			String encoding) throws Exception {
		byte[] fileContent = fileReadFrom(file);
		String fileContentStr = null;
		if(encoding != null && encoding.trim().length() > 0) {
			fileContentStr = new String(fileContent,encoding);
		}
		else {
			fileContentStr = new String(fileContent);
		}
	    BufferedReader d = new BufferedReader(new StringReader(fileContentStr));
	    ArrayList<String[]> al = new ArrayList<String[]>();
	    String line = null;
	    do {
	    	line = d.readLine();
	    	if(line != null) {
	    		if (skipStartSeq == null || !line.startsWith(skipStartSeq)) {
	    			String[] elems = line.split(separator);
	    			al.add(elems);
	    		}
	    	}
	    } while(line != null);
	    return al;
	}
	
	//-------------------------------------------------------------------------------------
	public static ArrayList<String> fileReadToLines(File file, String encoding) throws Exception {
		byte[] fileContent = fileReadFrom(file);
		String fileContentStr = null;
		if(encoding != null && encoding.trim().length() > 0) {
			fileContentStr = new String(fileContent,encoding);
		}
		else {
			fileContentStr = new String(fileContent);
		}
	    BufferedReader d = new BufferedReader(new StringReader(fileContentStr));
	    ArrayList<String> al = new ArrayList<String>();
	    String line = null;
	    do {
	    	line = d.readLine();
	    	if(line != null) {
	    		line = line.trim();
	    		if(line.length() > 0) {
	    			al.add(line);
	    		}
	    	}
	    } while(line != null);
	    return al;
	}

	//-------------------------------------------------------------------------------------
	public static byte[] getURLContentBA(String url) throws Exception {
		
		URL yahoo = new URL(url);
		URLConnection yc = yahoo.openConnection();
		
		InputStream is = yc.getInputStream();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		int readBytes = -1;
		byte[] data = new byte[500000];
		do {
			readBytes = is.read(data);
			if(readBytes != -1) {
				bout.write(data, 0, readBytes);
			}
		} while(readBytes != -1);
		
		byte[] result = bout.toByteArray();
		return result;
	}
	public static String getURLContent(String url, String charSet) throws Exception {
		
		URL yahoo = new URL(url);
		URLConnection yc = yahoo.openConnection();
		BufferedReader in = null;
		
		if(charSet == null) {
			in = new BufferedReader(
				new InputStreamReader(
						yc.getInputStream()));
		}
		else {
			in = new BufferedReader(
					new InputStreamReader(
							yc.getInputStream(), charSet));			
		}
		StringBuffer strb = new StringBuffer();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			strb.append(inputLine);
			// System.out.println(inputLine);
		}
		in.close();
		return strb.toString();
	}
	

    //--------------------------------------------------------------------------
    public static byte[] fileReadFrom(String fileName) throws Exception {
    	return fileReadFrom(new File(fileName));
    }
    public static byte[] fileReadFrom(File f) throws Exception {
    	FileInputStream fin = new FileInputStream(f);
    	ByteArrayOutputStream bout = new ByteArrayOutputStream();
    	int data = -1;
    	do {
    		data = fin.read();
    		if(data != -1) {
    			bout.write(data & 0xFF);
    		}
    	}
    	while(data != -1);
    	fin.close();
    	return bout.toByteArray();
    }
	
	// ***********************************************************************
	
    //------------------------------------------------------------------------
	public static String htmlReplaceHTMLCodes(String str) throws Exception {
		String result = str;

		String[][] replacements = {
				{ "&Aring;",  "�" },
				{ "&#197;",   "�" },
				{ "&Auml;",   "�" },
				{ "&#196;",   "�" },
				{ "&Ouml;",   "�" },
				{ "&#214;",   "�" },
				{ "&aring;",  "�" },
				{ "&#229;",   "�" },
				{ "&auml;",   "�" },
				{ "&#228;",   "�" },
				{ "&ouml;",   "�" },
				{ "&#246;",   "�" },
				{ "&nbsp;",   " " },
				{ "&amp"  ,   "&" }
		};
		boolean found = true;
		while(found) {
			found = false;
			for(int i=0; i < replacements.length; i++) {
				String[] repEntry = replacements[i];
				int io = result.indexOf(repEntry[0]);
				if(io == -1) {
					continue;
				}
				found = true;
				String newString = result.substring(0, io) + repEntry[1];
				if(result.length() > io+repEntry[0].length()) {
					newString = newString + result.substring(io+repEntry[0].length());
					result = newString;
				}
				break;
			}
		}
		return result;
	}
	
    //------------------------------------------------------------------------
	//    hdata:           HTML block as input
	//    extractStartTag: HTML tag that you want to extract, in the form of e.g "<div"
	//    absoluteTag:     Something unique within the payload where to start
	public static String getHTMLBlock(
			String hdata,
			String extractStartTag,
			String absoluteTag,
			boolean backwards) throws Exception {

		String str = hdata;
		int io = -1;
		
		io = str.indexOf(absoluteTag);
		if(io == -1)
			return null;
		
		// Find the block starting with start tag and all to the end
		int hextractIndexLast = -1;
		String hextractIndexLastStr = null;
		int startIndex = 0;
		int hextractIndex = -1;
		do {
			hextractIndex = str.indexOf(extractStartTag, startIndex);
			// If backwards, we take the last match before passing io
			if(backwards) {
				if(hextractIndex == -1 ||
   				   hextractIndex >= io) {
					break;
				}
			}
			hextractIndexLast = hextractIndex;
			hextractIndexLastStr = str.substring(hextractIndex);
			startIndex = hextractIndex + extractStartTag.length();

			// If forwards, we take the last match after passing io
			if(!backwards) {
				if(hextractIndex == -1 ||
		   		   hextractIndex >= io) {
				   break;
				}
			}			
		} while(true);

		// If we could not find before or after the absoluteTag
		if(hextractIndexLastStr == null)
			return hextractIndexLastStr;
		
		// Take away everything after the close tag (manage recursion)
		String extractEndTag = extractStartTag.substring(0, 1) + "/" + extractStartTag.substring(1);
		String result = getHtmlBlock(hextractIndexLastStr, extractStartTag, extractEndTag);
		return result;
	}
	private static String getHtmlBlock(String hdata, String tagStart, String tagEnd) throws Exception {
		if(!hdata.startsWith(tagStart))
			throw new Exception("Expected to start with tag: " + tagStart + "\ndata: " + hdata);
		
		int recursionLvl = 0;
		int io = tagStart.length();
		int endTagIndex = hdata.indexOf(tagEnd);
		// Find the d_eepest start nesting tag
		while(true) {
			int newIO = hdata.indexOf(tagStart, io);
			if(newIO == -1 || endTagIndex < newIO)
				break;
			recursionLvl++;
			io = newIO + tagStart.length();
		}
		// Find the shallowest end tag
		while(true) {
			int newIO = hdata.indexOf(tagEnd, io);
			if(newIO == -1)
				break;
			io = newIO + tagEnd.length();
			// Found last
			if(recursionLvl == 0) {
				return hdata.substring(0, io+tagEnd.length());
			}
			else {
				recursionLvl--;
			}
		}
		throw new Exception("Error, could not find endTag: " + tagEnd + ", from position:\n" + hdata.substring(io));
	}
	

	//------------------------------------------------------------------------
	public static String convertTo7bitAscii(String fileNameFundDetails) throws Exception {
		byte[] ba = fileNameFundDetails.getBytes();
		for(int i=0; i < ba.length; i++) {
			int a = (((int)ba[i]) & 0xFF);
			if(!(
			   (a >= 0x30 && a <= 0x39) ||
			   (a >= 0x41 && a <= 0x5A) ||
			   (a >= 0x61 && a <= 0x7A))) {
				ba[i] = (byte)(0x5F & 0xFF);
			}
		}
		return new String(ba);
	}

	//------------------------------------------------------------------------
	public static String newString(byte[] arg, String enc) throws Exception {
		if(enc != null && enc.trim().length() > 0) {
			return new String(arg, enc);
		}
		return new String(arg);
	}
	
    //------------------------------------------------------------------------
    public static boolean isDigit(char c) {
    	if(c == '0' ||
    	   c == '1' ||
    	   c == '2' ||
    	   c == '3' ||
    	   c == '4' ||
    	   c == '5' ||
    	   c == '6' ||
    	   c == '7' ||
    	   c == '8' ||
    	   c == '9') {
    		return true;
    	}
    	return false;
    }
			
    //------------------------------------------------------------------------
    public static String getStackTraceString(Throwable e) {
        StackTraceElement[] st = e.getStackTrace();
        String str = "";
        if(st == null)
        	return "";
        for(int i=0; i < st.length; i++)
            str = str + st[i].toString() + "\n";
        return str;
    }

    //------------------------------------------------------------------------
    public static String bytesToHexDumpString(byte[] data) {
    	byte[] ascii_data = new byte[16];
    	StringBuffer strb = new StringBuffer();
    	if(data == null)
    	    return "null";
    	else 
    	    strb.append("Data length: " + data.length + "\n");  

    	int ascii_data_index = 0;
    	int i=0;
    	for(i=0; i < data.length; i++) {
    	    if(i == 0) {
    		strb.append("0x" + bytesToString_to2HexDigits(i) + ": ");
    	    }
    	    int itmp = data[i] & 0xFF;
    	    ascii_data[ascii_data_index++] = (byte)itmp;
     	    String strtmp = bytesToString_to2HexDigits(itmp);
    	    strb.append(strtmp + " ");
    	    if(((i+1) % 16) == 0) {
    		strb.append("   ");		
    		for(int j=0; j < 16; j++) {
    		    if(ascii_data[j] < 32 || ascii_data[j] > 126)
    			ascii_data[j] = 46;
    		}
    		
    		ascii_data_index = 0;
    		strb.append(new String(ascii_data));
    		strb.append("\n");
    		strb.append("0x" + bytesToString_to2HexDigits(i+1) + ": ");
    	    }
    	}
    	while((i % 16) != 0) {
    	    strb.append("   ");
    	    ascii_data[ascii_data_index++] = 46;
    	    i++;
    	}
    	strb.append("   ");
    	strb.append(new String(ascii_data));
    	strb.append("\n");

    	return strb.toString();
    }
    private static String bytesToString_to2HexDigits(int value) {
    	String result = "";
       	if(value < 16)
       		result = "0";
       	result = result + Integer.toHexString(value);
       	return result;
    }
    
    //------------------------------------------------------------------------
    public static String[] getSeparatedElems(String line, char character) {
    	int io = -1;
    	
    	ArrayList<String> result = new ArrayList<String>();
    	while(line != null) {
    		io = line.indexOf(character);
    		if(io == -1) {
    			result.add(line);
    			line = null;
    			break;
    		}
    		String elem = line.substring(0, io);
    		result.add(elem);
    		
    		if(elem.length()+1 == line.length())
    			line = "";
    		else
    			line = line.substring(io+1);
    	}
    	
    	String[] resultStr = new String[result.size()];
    	for(int i=0; i < result.size(); i++)
    		resultStr[i] = result.get(i);
    	return resultStr;
    }
    
    //------------------------------------------------------------------------
    public static String getSecondsAs_HH_MM_SS(int seconds) {
    	int hourCount = seconds / 3600;
    	seconds = seconds % 3600;
    	int minuteCount = seconds / 60;
    	seconds = seconds % 60;

    	String hours = String.valueOf(hourCount);
    	if(hours.length() < 2)
    		hours = "0" + hours;
    	String minutes = String.valueOf(minuteCount);
    	if(minutes.length() < 2)
    		minutes = "0" + minutes;
    	String secondsStr = String.valueOf(seconds);
    	if(secondsStr.length() < 2)
    		secondsStr = "0" + secondsStr;
    	return hours + ":" + minutes + ":" + secondsStr;
    }

    //------------------------------------------------------------------------
    public static void sl_eepInMS(long ms) {
    	long timeNow = System.currentTimeMillis();
    	long timeItShouldBe = timeNow + ms;
    	do {
    		try {
    			Thread.currentThread().sleep(ms);
    		}
    		catch(Exception exc) {
    		}
    		timeNow = System.currentTimeMillis();
    		if(timeNow < timeItShouldBe) {
    			ms = timeItShouldBe - timeNow;
    		}
    		else {
    			ms = 0;
    		}
    	} while(ms > 0);
    }

    //------------------------------------------------------------------------
    public static String stripCComments(String str) {
    	StringBuffer result = new StringBuffer();
    	int nesting_level = 0;
    	for(int i=0; i < str.length(); i++) {
    		char c = str.charAt(i);
    		
    		if(c == '/' && str.charAt(i+1) == '*') {
    				nesting_level++;
   			}
    		else if(c == '*') {
    			if(i+1 < str.length() && str.charAt(i+1) == '/') {
    				nesting_level--;
    				i+=2;
    				if(i >= str.length())
    					return result.toString();
    				c = str.charAt(i);
    			}
    		}
    		
    		if(nesting_level == 0) {
    			result.append(c);
    		}
    	}
    	return result.toString();
    }

    //------------------------------------------------------------------------
    public static String getAs_YYMMDD_HHMMSS(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("yyMMdd_HHmmss");
        return f.format(date);    	
    }
    
    //------------------------------------------------------------------------
    public static String getAs_YYMMDDHHMMSS(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("yyMMddHHmmss");
        return f.format(date);    	
    }

    //------------------------------------------------------------------------
    public static String getAs_YYMMDD(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("yyMMdd");
        return f.format(date);    	
    }
    
    //------------------------------------------------------------------------
    public static String getAs_HHMMSS(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("HHmmss");
        return f.format(date);    	
    }

    //------------------------------------------------------------------------
    public static String getNowAs_YYMMDD() {
    	Date date = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyMMdd");
        return f.format(date);    	
    }

    //------------------------------------------------------------------------
    public static String getNowAs_HHMMSS() {
    	Date date = new Date();
        SimpleDateFormat f = new SimpleDateFormat("HHmmss");
        return f.format(date);    	
    }
    
    //------------------------------------------------------------------------
    public static String getNowAs_YYMMDD_HHMMSS() {
    	Date date = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyMMdd_HHmmss");
        return f.format(date);    	
    }

    //------------------------------------------------------------------------
    public static String getNowAs_YYMMDDHHMMSS() {
    	Date date = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyMMddHHmmss");
        return f.format(date);    	
    }
    
	//--------------------------------------------------------------------------
    public static String dateConvert_GC_To_YYMMDD(GregorianCalendar gc) throws Exception {
    	GregorianCalendar gnow = new GregorianCalendar();    
    	String yy = String.valueOf(gnow.get(GregorianCalendar.YEAR)-2000);
    	String mm = String.valueOf(gnow.get(GregorianCalendar.MONTH));
    	String dd = String.valueOf(gnow.get(GregorianCalendar.DAY_OF_MONTH));
    	
    	if(yy.length() == 1) {
    		yy = "0" + yy;
    	}
    	if(mm.length() == 1) {
    		mm = "0" + mm;
    	}
    	if(dd.length() == 1) {
    		dd = "0" + dd;
    	}
    	return yy + mm + dd;
    }
    
    
	//--------------------------------------------------------------------------
    public static GregorianCalendar dateConvert_YYMMDD_To_GC(String yymmdd) throws Exception {
		String yyStr = yymmdd.substring(0,2);
		String mmStr = yymmdd.substring(2, 4);
		String ddStr = yymmdd.substring(4);
		int yy = Integer.parseInt(yyStr) + 2000;
		int mm = Integer.parseInt(mmStr) - 1;
		int dd = Integer.parseInt(ddStr);
		
    	GregorianCalendar gnow = new GregorianCalendar();    
    	gnow.set(GregorianCalendar.YEAR, yy);
    	gnow.set(GregorianCalendar.MONTH, mm);
    	gnow.set(GregorianCalendar.DAY_OF_MONTH, dd);
    	gnow.set(GregorianCalendar.SECOND, 1);
    	gnow.set(GregorianCalendar.HOUR_OF_DAY, 0);
    	gnow.set(GregorianCalendar.MINUTE, 0);
    	
    	return gnow;
    }
    
    //--------------------------------------------------------------------------
    public static String dateConvert_DDSMM_To_YYMMDD(String ddsmm) throws Exception {
    	GregorianCalendar gnow = new GregorianCalendar();
    	gnow.setTime(new Date());
    
    	int nowYY = gnow.get(GregorianCalendar.YEAR);
    	int nowMM = gnow.get(GregorianCalendar.MONTH);
    	nowMM++;
    	int nowDD = gnow.get(GregorianCalendar.DAY_OF_MONTH);
    	
    	int io = -1;
    	io = ddsmm.indexOf("/");
    	String thenDDStr = ddsmm.substring(0, io);
    	String thenMMStr = ddsmm.substring(io+1);
    	int thenDD = Integer.parseInt(thenDDStr);
    	int thenMM = Integer.parseInt(thenMMStr);
    	if(thenMM > nowMM)
    		nowYY--;
    	else if(thenMM == nowMM && thenDD > nowDD)
    		nowYY--;
    	
    	nowYY -= 2000;
    	String yys = String.valueOf(nowYY);
    	String mms = String.valueOf(thenMM);
    	String dds = String.valueOf(thenDD);
    	if(yys.length() < 2)
    		yys = "0" + yys;
    	if(mms.length() < 2)
    		mms = "0" + mms;
    	if(dds.length() < 2)
    		dds = "0" + dds;
    	return yys + mms + dds;
    }

    //--------------------------------------------------------------------------
    public static String dateConvert_YYYYDMMDDD_To_YYMMDD(String str) throws Exception {
    	str = str.trim();
    	int io = -1;

    	io = str.indexOf("-");
    	String yy = str.substring(0,io);
    	yy = yy.substring(2);
    	str = str.substring(io+1);
    	
    	io = str.indexOf("-");
    	String mm = str.substring(0, io);
    	str = str.substring(io+1);
    	
    	String dd = str;
    	
    	return yy + mm + dd;
    }
    
	//--------------------------------------------------------------------------
    public static void fileMove(String fileSrc, String fileDest) throws Exception {
    	File fsrc = new File(fileSrc);
    	File fdest = new File(fileDest);
    	fsrc.renameTo(fdest);
    }

    //------------------------------------------------------------------------    
    public static Float getFloatFromSWEStr(String sweStr) throws Exception {
    	String javaStr = sweStr.replace(',', '.');
    	Float f = Float.valueOf(javaStr);
    	return f;
    }
    
    //------------------------------------------------------------------------    
    public static String getUntilNext(String next, StringBuffer strb) {
    	if(strb == null || strb.length() == 0) {
    		// System.out.println("MM.gun, returning null");
    		return null;
    	}
    	
    	String s = strb.toString();

    	int io = -1;
    	io = s.indexOf(next);
  		if(io == -1) {
  			strb.delete(0, strb.length());
  			// System.out.println("MM.gun, returning the remains: " + s);
  			return s;
  		}
  		
  		String result = "";
  		if(io > 0) {
  	    	result = s.substring(0, io);
  		}
		strb.delete(0, io+next.length());
		// System.out.println("MM.gun, returning result: " + result);
    	return result;
    }    
}

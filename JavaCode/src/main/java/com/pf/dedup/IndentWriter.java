package com.pf.dedup;

public class IndentWriter {
	
	//------------------------------------------------------------------------
	public static final void main(String[] arg) {
		IndentWriter iw = new IndentWriter();
		iw.println("Hello");
		iw.push();
		iw.println("Indent 1");
		iw.println("Indent 1");
		iw.push();
		iw.println("Indent 2");
		iw.println("Indent 2");
		iw.pop();
		iw.println("Indent 1 again");
		iw.println("Indent 1 again");
		iw.pop();
		iw.println("back... to start");
		System.out.println(iw.toString());
	}
	
	//------------------------------------------------------------------------
	public static final String i01 = " ";
	public static final String i02 = "  ";
	public static final String i03 = "   ";
	public static final String i04 = "    ";
	public static final String i05 = "     ";
	public static final String i06 = "      ";
	public static final String i07 = "       ";
	public static final String i08 = "        ";
	public static final String i09 = "         ";
	public static final String i10 = "          ";
	public static final String i11 = "           ";
	public static final String i12 = "            ";
	public static final String i13 = "             ";
	public static final String i14 = "              ";
	public static final String i15 = "               ";
	public static final String i16 = "                ";
	public static final String i17 = "                 ";
	public static final String i18 = "                  ";
	public static final String i19 = "                   ";
	public static final String i20 = "                    ";
	public static final String i21 = "                     ";
	public static final String i22 = "                      ";
	public static final String i23 = "                       ";
	public static final String i24 = "                        ";
	public static final String i25 = "                         ";
	public static final String i26 = "                          ";
	public static final String i27 = "                           ";
	public static final String i28 = "                            ";
	public static final String i29 = "                             ";
	public static final String i30 = "                              ";
	public static final String getIndent(int indent) {
		switch(indent) {
		case 0: return "";
		case 1: return i01;
		case 2: return i02;
		case 3: return i03;
		case 4: return i04;
		case 5: return i05;
		case 6: return i06;
		case 7: return i07;
		case 8: return i08;
		case 9: return i09;
		case 10: return i10;
		case 11: return i11;
		case 12: return i12;
		case 13: return i13;
		case 14: return i14;
		case 15: return i15;
		case 16: return i16;
		case 17: return i17;
		case 18: return i18;
		case 19: return i19;
		case 20: return i20;
		case 21: return i21;
		case 22: return i22;
		case 23: return i23;
		case 24: return i24;
		case 25: return i25;
		case 26: return i26;
		case 27: return i27;
		case 28: return i28;
		case 29: return i29;
		case 30: return i30;
		}
		return i30;
	}
	
	//------------------------------------------------------------------------
	public IndentWriter(boolean autoFlush) {
		_autoFlush = autoFlush;
		_currentIndent = 0;
		_incIndent = 2;
	}
	
	//------------------------------------------------------------------------
	public IndentWriter() {
		_autoFlush = false;
		_currentIndent = 0;
		_incIndent = 2;
	}
    
	//------------------------------------------------------------------------
	public IndentWriter(int startIndent, int defaultIndent) {
		_startIndent = startIndent;
		_incIndent = defaultIndent;		
		_currentIndent = _startIndent;
	}
	
	//------------------------------------------------------------------------
	public void push() {
		_currentIndent += _incIndent;
	}
    
	//------------------------------------------------------------------------
	public void pop() {
		if(_currentIndent - _incIndent > _startIndent)
			_currentIndent -= _incIndent;
		else
			_currentIndent = _startIndent;
	}
	
	// ***********************************************************************

	//------------------------------------------------------------------------
	public void clear() {
		_buffer = new StringBuffer();
	}
	
	//------------------------------------------------------------------------
	public String toString() {
		return getString();
	}

	//------------------------------------------------------------------------
	public String getString() {
		String returnValue = _buffer.toString();
		return returnValue;
	}

	//------------------------------------------------------------------------
	public void println(String a1) {
		if(a1 == null)
			a1 = "";
		print(a1 + "\n");
		_isFirstWriteLine = true;
	}

	//------------------------------------------------------------------------
	public void println() {
		print("\n");
		_isFirstWriteLine = true;
	}

	//------------------------------------------------------------------------
	public void print(String a1) {
		if(!_isFirstWrite && _isFirstWriteLine) {
			startNewLine();
			_isFirstWriteLine = false;
		}

		_isFirstWrite = false;
		if(a1 == null)
			return;
		_buffer.append(a1);
		if(_autoFlush) {
			String str = getString();
			clear();
			System.out.print(str);
		}
	}
	
	// ***********************************************************************

	//------------------------------------------------------------------------
	private void startNewLine() {		
		_buffer.append(getIndent(_currentIndent));
	}

	private int _startIndent = 0;
	private int _currentIndent = 0;
	private int _incIndent = 2;
	private StringBuffer _buffer = new StringBuffer();
	private boolean _isFirstWrite = true;
	private boolean _isFirstWriteLine = true;
	private boolean _autoFlush;
}

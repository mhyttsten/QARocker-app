import java.util.*;

public class Parenthesis {
    public static void main(String[] args) {
	char[] str = new char[6];
	List<String> list = new ArrayList<>();
	addParen(list, 3, 3, str, 0);
	for (String s: list) {
	    System.out.println(s);
	}
    }

    public static boolean didEmit = false;

    public static void addParen(List<String> list, int leftRem, int rightRem, char[] str, int index) {
	
	if (leftRem < 0) return;

	if (rightRem < leftRem) { System.out.println("*** WHY ***, rightRem: " + rightRem + ", leftRem: " + leftRem); return; }

	if (leftRem == 0 && rightRem == 0) {
	    String snew = String.copyValueOf(str);
	    System.out.println(snew);
	    list.add(snew);
	    didEmit = true;
	} else {
	    //	    System.out.println("left: " + leftRem + ", right: " + rightRem);

	    manageEmit("1", leftRem, rightRem, str, index);
	    str[index] = '(';
	    String snew = String.copyValueOf(str).substring(0, index+1);
      	    System.out.println("...added(, now: " + snew + ". lrem: " + leftRem + ", rrem: " + rightRem + ", index: " + index);
	    addParen(list, leftRem-1, rightRem, str, index+1);
	    
	    manageEmit("2", leftRem, rightRem, str, index);
	    str[index] = ')';
	    snew = String.copyValueOf(str).substring(0, index+1);
      	    System.out.println("...added), now: " + snew + ". lrem: " + leftRem + ", rrem: " + rightRem + ", index: " + index);
	    addParen(list, leftRem, rightRem-1, str, index+1);
	}
    }

    public static void manageEmit(String s, int left, int right, char[] str, int index) {
	if (didEmit) {
	    System.out.println("DIDEMIT[" + s + "], l: " + left + ", r: " + right + ", str: " + String.copyValueOf(str).substring(0, index));
	    didEmit = false;
	}
    }
}


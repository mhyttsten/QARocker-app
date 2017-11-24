public class Permutations {
	

public static void main(String[] argv) { 
	String s = "1234";
	permutation("", s); 

	System.out.println("Result: " + s.substring(0,0));
	System.out.println("Result: " + s.substring(6));

}

private static void permutation(String prefix, String str) {
    if (str.length() == 0) {
    	System.out.println(prefix);
    	return;
    }
    for (int i = 0; i < str.length(); i++) {
       permutation(prefix + str.charAt(i), str.substring(0, i) + str.substring(i+1));
    }
}
}
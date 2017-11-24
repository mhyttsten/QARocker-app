import java.util.*;

public class Hanoi {

    
    public static void main(String[] args) {
	moveDisks(4, "O", "D", "B");
    }

    private static void moveDisks(int n, String o, String d, String b) {
	if (n <= 0) return;

	moveDisks(n-1, o, b, d);
	System.out.println("Moving: " + n + " to " + d);
	moveDisks(n-1, b, d, o);
    }
}



public class Bits {

    public static void main(String[] args) {

	System.out.println(3 << 1);

	byte b = (byte)0b0001;
	System.out.println("#1");
	System.out.println("..." + Integer.toBinaryString(b));
	System.out.println("..." + Integer.toBinaryString(b-1));
	System.out.println("...result: " + onlyOneBitSet(b));

	b = (byte)0b1010;
	System.out.println("#2");
	System.out.println("..." + Integer.toBinaryString(b));
	System.out.println("..." + Integer.toBinaryString(b-1));
	System.out.println("...result: " + onlyOneBitSet(b));

	b = (byte)0b1100;
	System.out.println("#3");
	System.out.println("..." + Integer.toBinaryString(b));
	System.out.println("..." + Integer.toBinaryString(b-1));
	System.out.println("...result: " + onlyOneBitSet(b));

	b = (byte)0b0110;
	System.out.println("#4");
	System.out.println("..." + Integer.toBinaryString(b));
	System.out.println("..." + Integer.toBinaryString(b-1));
	System.out.println("...result: " + onlyOneBitSet(b));

	b = (byte)0b0010;
	System.out.println("#4");
	System.out.println("..." + Integer.toBinaryString(b));
	System.out.println("..." + Integer.toBinaryString(b-1));
	System.out.println("...result: " + onlyOneBitSet(b));
    }

    private static String onlyOneBitSet(byte b) {
	return String.valueOf((b & (b-1)) == 0);
    }

}

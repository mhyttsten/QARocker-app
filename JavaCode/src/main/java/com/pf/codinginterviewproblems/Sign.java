public class Sign {
    public static void main(String[] args) {
	

	byte b0 = 0;
	byte bm1 = -1;
	byte bm2 = -2;
	byte bm128 = (byte)0x80;
	int itest = bm128;

	System.out.println(String.format("%02X", bm1));
	System.out.println(String.format("%02X", bm2));
	System.out.println(String.format("%d", bm128));
			   System.out.println(String.format("%d", itest));
	System.out.println(String.format("%X", itest));





    }

}
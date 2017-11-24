public class PlusPlus {

    public static void main(String[] args) {

	int[] a = { 1, 2, 3};
	int i1 = 0;
	int i2 = 0;
	System.out.println(a[++i1]);
	System.out.println(i1);
	System.out.println(a[i2++]);
	System.out.println(i2);

	System.out.println("New one");
	for (int i=0; i < a.length; ++i) {
	    System.out.println(a[i]);
	}
	
    }

}
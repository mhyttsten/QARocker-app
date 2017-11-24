import java.util.*;
public class Stack2 {
	
	public static class Box {
		int h, w, d;
		String n;
		public Box (String n_, int h_, int w_, int d_) {
			n=n_;
			h=h_;
			w=w_;
			d=d_;
		}
	}

	public static void main(String[] argv) {
		List<Box> l = new ArrayList<>();
		l.add(new Box("1", 1, 1, 1));
		l.add(new Box("2", 3, 3, 4));
		l.add(new Box("3", 3, 3, 3));
//		l.add(new Box("4", 4, 4, 4));
//		l.add(new Box("5", 5, 3, 5));

		permutations(new ArrayList<Box>(), l);
	}

	public static void permutations(List<Box> result, List<Box> list) {
		if (list.size() == 0) {
			for (Box b: result) { System.out.print(b.n + "[" + b.h + "], "); }
			System.out.println("");
			result.clear();
		}

		for (int i=0; i < list.size(); i++) {
			List<Box> l2 = new ArrayList<>(list);
			result.add(l2.remove(i));
			permutations(result, l2);
		}
	}
}





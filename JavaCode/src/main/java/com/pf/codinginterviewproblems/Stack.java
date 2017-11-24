import java.util.*;
public class Stack {
	
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
		l.add(new Box("4", 4, 4, 4));
		l.add(new Box("5", 5, 3, 5));
		StringBuffer strb = new StringBuffer();
		int maxHeight = createStack1(true, strb, l);
		System.out.println("Max height: " + maxHeight);
	}

	private static int createStack1(boolean isTopLevel, StringBuffer strb, List<Box> l) {
		int maxHeight = 0;
		for (int i=0; i < l.size(); i++) {
			Box s = l.get(i);
			List<Box> l2 = new ArrayList<>(l);
			l2.remove(i);
//			StringBuffer strb = new StringBuffer();
			int height = createStack2(strb, s, l2);
			maxHeight = Math.max(maxHeight, height);
			if (isTopLevel) {
	 			System.out.println(strb);
				strb = new StringBuffer();
	 		}
		}
		return maxHeight;
	}

	private static int createStack2(StringBuffer strb, Box start, List<Box> others) {
		if (others.size() == 0) {
			strb.append("DONE");
			return 0;
		}

		int theight = start.h;
		strb.append(start.n + "[" + theight + "] -> ");

		List<Box> o2 = new ArrayList<>(others);
		while (others.size() > 0) {
			Box next = others.remove(0);
			if (start.h <= next.h && start.w <= next.w && start.d <= next.d) {
				int height = createStack1(false, strb, others);
				theight += height;
			}
		}
		return theight;
	}
}





public class BinarySearch {
	
	public static void main(String[] argv) {
		int[] a = { 1, 2, 3, 4, 5, 6, 7};

		int index = search(a, 4, 0, a.length);
		System.out.println("Finished, index: " + index);
	}

	public static int search(int[] a, int x, int start, int end) {
		System.out.println("start: " + start + ", end: " + end);

		int mid = (end+start) / 2;

		if (a[mid] == x) {
			return mid;
		}

		if (start == end || mid == end-1) {
			return -1;
		}

		if (a[mid] > x) {
			return search(a, x, start, mid);
		} else {
			return search(a, x, mid, end);
		}

	}

}



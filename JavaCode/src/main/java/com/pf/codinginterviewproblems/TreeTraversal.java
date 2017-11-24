import java.util.Stack;

public class TreeTraversal {


    public static class Node {
	public Node(String an, Node al, int av, Node ar) { 
	    l = al; 
	    r = ar;
	    v = av;
	    n = an;
	}
	public String n;
	public int v;
	public Node l;
	public Node r;
	public boolean d; // discovered
    };
    
    public static void main(String[] args) {
	//                   08
        //         04                  12  
	//    02        06        10        14    
	//  01 03     05  07    09  11    15  16  

	Node n01 = new Node("", null, 1, null);
	Node n03 = new Node("", null, 3, null);
	Node n02 = new Node("", n01, 2, n03);

	Node n05 = new Node("", null, 5, null);
	Node n07 = new Node("", null, 7, null);
	Node n06 = new Node("", n05, 6, n07);
	
	Node n09 = new Node("", null, 9, null);
	Node n11 = new Node("", null, 11, null);
	Node n10 = new Node("", n09, 10, n11);

	Node n15 = new Node("", null, 15, null);
	Node n16 = new Node("", null, 16, null);
	Node n14 = new Node("", n15, 14, n16);
	
	Node n04 = new Node("", n02, 4, n06);
	Node n12 = new Node("", n10, 12, n14);

	Node root  = new Node("", n04, 8, n12);

	dft_iterative(root);
	System.out.println();
    }

    public static void dft_recursive(Node n) {
	if (n.l != null) dft_recursive(n.l);
	System.out.print(n.v + ", ");
	if (n.r != null) dft_recursive(n.r);
    }

    public static void dft_iterative(Node n) {
	Stack<Node> stack = new Stack<>();

	while (true) {
	    if (n.l != null) {
		stack.push(n);
		n = n.l;
	    }
	    
	}


    }
}

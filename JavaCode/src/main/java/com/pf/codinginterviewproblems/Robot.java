import java.util.*;

public class Robot {

    public static class Path {
	public Path(int row, int col) { r = row; c = col; }
	public int r;
	public int c;
	public String toString() { return "(" + r + "," + c + ")"; }
    };

    public static void main(String[] args) {
        boolean maze[][] = { { true, true, true },
			     { true, true, true },
			     { true, true, true } };
	List<Path> p = new ArrayList<>();
	boolean success = getPath(maze, maze.length-1, maze[0].length-1, p);
	if (success) {
	    System.out.println("SUCCESS");
	    for (Path p_ : p) {
		System.out.println(p_);
	    }
	} else {
	    System.out.println("FAILURE");
	}
	    
    }

    public static boolean getPath(boolean[][] maze, int row, int col,
				  List<Path> p) {
	System.out.println("Considering: (" + row + "," + col + ")");
	if (row < 0 || col < 0 || !maze[row][col]) {
	    System.out.println("...exiting on #1");
	    return false;
	}

	boolean isOrigin = (row == 0) && (col == 0);
	if (isOrigin
	    || getPath(maze, row-1, col, p)
	    || getPath(maze, row, col-1, p)) {
	    
	    Path path = new Path(row, col);
	    p.add(path);
	    return true;
	}

	System.out.println("...exiting on #2");
	return false;
    }
}

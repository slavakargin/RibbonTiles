package xrib;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.TreeMap;

import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.Out;
//import edu.princeton.cs.algs4.StdOut;

/*
 * This class will contain information about height vector function 
 * of a ribbon tiling. 
 * We are going to realize it as a map from a bunch of squares to n-component array. 
 * This map have the meaning of the height at the south-western corner of the square.
 * Originally we should be able only to calculate height at the border of the region. 
 * 
 */
class XHeight {
	XRibTiling tiling; //link to tiling.
	XShape shape;
	int n; //length of each ribbon tile
	TreeMap<Square, ArrayList<Integer>> height;


	/**
	 * This is a constructor from a tiling.
	 * 
	 * @param T 
	 */
	public XHeight(XRibTiling T) {
		this.n = T.n;
		this.tiling = T;
		this.shape = new XShape(T.shape);
		height = new TreeMap<Square, ArrayList<Integer>>();
		// initialization
		// note that height can be defined also on squares that are outside of T.
		for (Square s : T.shape.squares) {
			height.put(s, new ArrayList<Integer>(n));
		}
		calcHeightBorder();
	}
	
	
	/** The constructor is less general then the following. 
	 * This is a constructor directly from a shape and the parameter n, which is the length of the ribbon tiles.
	 * Only height on the border can be calculated. 
	 * 
	 * @param shape
	 * @param n
	 */
	/*
	public XHeight (int n, XShape shape) {
		this.n = n;
		this.shape = new XShape(shape);
		height = new TreeMap<Square, ArrayList<Integer>>();
		// initialization
		// note that height can be defined also on squares that are outside of T.
		for (Square s : shape.squares) {
			height.put(s, new ArrayList<Integer>(n));
		}
		calcHeightBorder();
	}
	*/

	/**
	 * Constructs height on the border of a non-simply connected region.
	 * In this version we allow only for a one-connected region. 
	 * An additional information is a point on the inside border curve.
	 * It will be supplied as a square inside the hole. The point on the left-bottom corner
	 * of this whole will get zero height.
	 * 
	 * @param n the size of ribbons
	 * @param shape the region
	 * @param squares sequence of the squares which will serve as the base points for 
	 *        the calculation of height on the border of the holes
	 *   
	 */
	public XHeight (int n, XShape shape, Square... squares) {
		this.n = n;
		this.shape = new XShape(shape);
		height = new TreeMap<Square, ArrayList<Integer>>();
		// initialization
		// note that height can be defined also on squares that are outside of T.
		for (Square s : shape.squares) {
			height.put(s, new ArrayList<Integer>(n));
		}
		calcHeightBorder(); //this calculates the height on the outside border
		
		ArrayList<Integer> h = new ArrayList<Integer>(n);
		for (int i = 0; i < n; i++) {
			h.add(0);
		}
		for ( Square s : squares) {
			for (int i = 0; i < n; i++) {
				h.set(i, 0);
			}
		    calcHB(s, h); //this calculates the height at the border of the hole. 
		}
	}

	/**
	 * calculate the height on the border of the tiling. The assumption is that 
	 * the height at the "root" square of the shape equals to 0. The root square is one of the 
	 * squares with the lowest level. More precisely, it is the square which is the most to the left.  
	 */
	void calcHeightBorder() {
		ArrayList<Integer> h = new ArrayList<Integer>(n);
		for (int i = 0; i < n; i++) {
			h.add(0);
		}
		//let us find a point on the border and initialize height.
		Square s0 = new Square(0, 0);
		for (Square s : height.keySet()) {
			if (shape.isBorder(s.x, s.y)) {
				s0 = new Square(s.x, s.y);
				break;
			}			
		}		
		calcHB(s0, h);
	}
	
	/*
	 * This function calculates the height on the border of the tiling, under assumption that
	 * the square s0 is at the border and the current value of the height at s0 is h.
	 */
	
	private void calcHB(Square s0, ArrayList<Integer> h) {
		height.put(s0, h); 
		Square sOrigin = new Square(s0);
		//Now we are going to travel along the boundary. 
		Square s1 = shape.moveAroundCCW(s0);	
		while (!s1.equals(sOrigin)) {
			updateHeight(s0, s1);
			s0 = s1;
			s1 = shape.moveAroundCCW(s0);	
		}
	}
	/**
	 * we came to s1 from s0 and we update height at s1. 
	 * @param s0
	 * @param s1
	 */

	private void updateHeight(Square s0, Square s1) {
		int xL, yL, xR, yR, colorL, colorR;
		xL = 0;
		yL = 0;
		xR = 0;
		yR = 0;
		// calculate the squares which are on the left and the right of the movement, respectively
		if (s1.y == s0.y && s1.x > s0.x) { //moved right
			xL = s0.x;
			yL = s0.y;
			xR = s0.x;
			yR = s0.y - 1;
		} else if (s1.y == s0.y && s1.x < s0.x) { //moved left
			xL = s1.x;
			yL = s1.y - 1;
			xR = s1.x;
			yR = s1.y;
		} else if (s1.x == s0.x && s1.y > s0.y) { //moved up
			xL = s0.x - 1;
			yL = s0.y;
			xR = s0.x;
			yR = s0.y;
		} else if (s1.x == s0.x && s1.y < s0.y) { //moved down
			xL = s1.x;
			yL = s1.y;
			xR = s1.x - 1;
			yR = s1.y;
		}
		// the color of this square.
		colorL = (xL + yL + n) % n; 
		colorR = (xR + yR + n) % n;
		ArrayList<Integer> h = new ArrayList<Integer>(height.get(s0));
		h.set(colorL, h.get(colorL) + 1);
		h.set(colorR, h.get(colorR) - 1);
		height.put(s1, h);
	}

	
	//this should be the key piece of code.
	void tileUpdate(XRibTile tile) {
		XShape tileShape = new XShape(tile.squares());
		Square sOrigin = new Square(tile.xmin, tile.ymin);
		Square s0 = new Square(sOrigin);
		//Now we are going to travel along the boundary. 
		Square s1 = tileShape.moveAroundCCW(s0);	
		while (!s1.equals(sOrigin)) {
			updateHeight(s0, s1);
			s0 = s1;
			s1 = tileShape.moveAroundCCW(s0);	
		}	
	}
	

	void draw() {
		draw(n);
	}

	/**
	 * Print all the components of the height which are smaller or equal than k
	 * 
	 * @param dr - window to draw
	 * @param k - component of the height, can take values from 0 to n-1.
	 */

	void draw(int k) {
	    shape.draw();
		shape.myDr.setPenColor(Draw.BLACK);
		for (Square s : height.keySet()) {
			for (int i = 0; i < k; i++){
				if (height.get(s).size() == n) {
					shape.myDr.text(s.x + 0.2 * i, s.y + 0.1, String.valueOf(height.get(s).get(i)));
				}
			}
		}
	}


	/**
	 *  Calculate Height using data on tiles in tiling T.	
	 */
	
	public void calcHeightInside() {
		for (XRibTile t : tiling.tiles()) {
			tileUpdate(t);
		}
	}
	
	
public void saveHeight(String fn) {
	ArrayList<Integer> h;
	Out out = new Out(fn);
	for (Square s : height.keySet()) {
		out.print(s.x + ", " + s.y + ", ");
		h = height.get(s);
		for (int j = 0; j < h.size(); j++) {
			if (j < h.size() - 1) {
				out.print(h.get(j) + ", ");
			} else {
				out.println(h.get(j));
			}			
		}
	}
	out.close();
}

	
	/*
	 * print out the height function
	 */
	/*
	public void print() {
		for (int i = N; i > -1; i--) {
			for (int j = 0; j < M + 1; j++) {
				StdOut.print("(" + heightX[i][j] + ", " + heightY[i][j]
						+ ", " + heightZ[i][j] + ")\t");
			}
			StdOut.println();
		}
	}
	 */



	/**
	 * For testing methods.
	 */
	public static void main(String[] args) {
		int n = 4;
		int N = 6; 
		XRibTile tile1, tile2, tile3;
		ArrayList<Integer> shapeI = new ArrayList<Integer>(N);
		ArrayList<Integer> shapeF = new ArrayList<Integer>(N);
		XRibTiling xrt, xrtCopy;

		/* Text case 1
		 * 
		shapeI.add(0);
		shapeF.add(3);
		shapeI.add(1);
		shapeF.add(7);
		shapeI.add(3);
		shapeF.add(3);
		 */

		/* Test case 2
		 * 
		 */
		/*
		for (int i = 0; i < N; i ++){
			shapeI.add(0);
			shapeF.add(7);
		}
		for (int i = 0; i < N/2; i++){
			shapeF.set(i, 3);
		}
		*/
		/* test case 3
		 * 
		 */
		/*
		n = 3;
		for (int i = 0; i < N; i ++){
			shapeI.add(0);
			shapeF.add(3);
		}
		*/		
		//TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);
		
		
		
		
		/* 
		 * test case 4
		 */
		/*n = 3;
		for (int i = 0; i < 2; i ++){
			shapeI.add(0);
			shapeF.add(20);
			}
			
			for (int i = 2; i < 3; i ++){
			shapeI.add(4);
			shapeF.add(10);
			}
			for (int i = 4; i < 8; i++){
				shapeI.add(3);
				shapeF.add(5);
			}
			for (int i = 9; i < 14; i++){
				shapeI.add(0);
				shapeF.add(15);
			}
			
			TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);
			Square s;
			for (int i = 4; i < 8; i++){
				for (int j = 0; j < 10; j ++){
				   s = new Square(10 + j, i);
				   bag.add(s);
				}
			}	
		s = new Square(0, 12);
		bag.add(s);
		
		s = new Square(1, 12);
		bag.add(s);
		*/
		
		
		//test case 5 (non-simply connected region)
		
		 n = 3;
		for (int i = 0; i < 1; i ++){
		shapeI.add(0);
		shapeF.add(2);
		}

		for (int i = 1; i < 6; i ++){
		shapeI.add(0);
		shapeF.add(0);
		}
		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);	
		Square s;
		for (int i = 1; i < 4; i++) {
		s = new Square(2, i);
		bag.add(s);		
		}
		s = new Square(1, 3);
		bag.add(s);
		
		
		XShape xs = new XShape(bag);
		Square s0 = new Square(1, 1);
		XHeight H = new XHeight(n, xs, s0);
		H.draw();
		
		
		
		/*
		xrt = new XRibTiling(n, bag, "");	
		xrt.H.calcHeightInside();
		xrt.H.draw();
		xrt.H.saveHeight("height.txt");
		
		xrtCopy = new XRibTiling(xrt);	
		XUtility.Glauber(xrtCopy, 1000);
		xrtCopy.H.calcHeightInside();
		xrtCopy.H.draw();
		xrtCopy.H.saveHeight("heightCopy.txt");
		*/
	}
}

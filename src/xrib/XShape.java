package xrib;

import java.util.ArrayList;
import java.util.TreeSet;
//import java.util.Random;

import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;

public class XShape {
	//conceptual variables
	protected TreeSet<Square> squares; // A bag of squares which are inside of the region.

	//Geometric variables
	protected Integer Lmin, Lmax; //the smallest and the largest levels that  have a non-empty intersection with the region.
	protected ArrayList<ArrayList<Integer>> crosses; //the k-th element of this object is 
	//the list of x -coordinates o intersections of
	//the line x + y = k 
	//with the border of the shape.
	protected ArrayList<ArrayList<Integer>> weakCrosses; //almost the same as crosses but here we include the 
	//cases when the intersection is from a joining intervals
	//of from the creation of a new interval.
	protected ArrayList<ArrayList<Square>> crossesHalf1; // intersections with the level lines x + y = k + 1/2 : 
	// we will save an intersection point by keeping
	// two squares so that the intersection is the midpoint of the 
	//roots of these two squares (i.e., their south-west corners)
	//this should help us to compute heights for these points. 

	protected ArrayList<ArrayList<Square>> crossesHalf2;
	//Variables for visualization
	protected Draw myDr; //the window to which the object will draw. 

	/**
	 * Creates an shape from a bag of squares. 
	 * 
	 * 
	 * @param squares
	 */
	public XShape(TreeSet<Square> squares) {
		this.squares = new TreeSet<Square>(squares);

		Lmax = 0;
		for (Square s : squares) {
			if (Lmin == null) {
				Lmin = s.x + s.y;
			} else if (Lmin > s.x + s.y) {
				Lmin = s.x + s.y;
			}
			if (Lmax < s.x + s.y) {
				Lmax = s.x + s.y;
			}
		}
		Lmax++;
		calculateCrosses();
		calcWeakCrosses();
	}

	/**
	 * Copy constructor
	 */
	public XShape(XShape other) {
		this.squares = new TreeSet<Square>(other.squares);
		this.Lmin = other.Lmin;
		this.Lmax = other.Lmax;
		calculateCrosses();
		calcWeakCrosses();
	}


	/**
	 * Creates an analog of the Aztec Diamond with size N.
	 * for a tiling is by ribbon 3-tiles.
	 * 
	 * @param N
	 * @return
	 */		
	public static XShape aztec3(int N) {        
		TreeSet<Square> bag = new TreeSet<Square>();
		for (int l = 0; l < N; l++) {
			for (int i = l; i < N; i++) {
				for (int k = 0; k < 3; k++) {
					Square s = new Square(i, N - 1 - i + 3 * l + k);
					//StdOut.println(s);
					bag.add(s);
				}
			}
		}
		for (int l = 0; l < N; l++) {
			for (int i = N; i < N + 1 + l; i++) {
				for (int k = 0; k < 3; k++) {
					Square s = new Square(i, N - i + 3 * l + k);
					//StdOut.println(s);
					bag.add(s);
				}
			}
		}
		//StdOut.println(bag);
		XShape aD = new XShape(bag);
		return aD;
	}


	/**
	 * Creates an analog of the Aztec Diamond with size N.
	 * for a tiling is by ribbon n-tiles.
	 * 
	 * @param N
	 * @return
	 */		
	public static XShape aztecRibbon(int n, int N) {        
		TreeSet<Square> bag = new TreeSet<Square>();
		for (int l = 0; l < N; l++) {
			for (int i = l; i < N; i++) {
				for (int k = 0; k < n; k++) {
					Square s = new Square(i, N - 1 - i + n * l + k);
					//StdOut.println(s);
					bag.add(s);
				}
			}
		}
		for (int l = 0; l < N; l++) {
			for (int i = N; i < N + 1 + l; i++) {
				for (int k = 0; k < n; k++) {
					Square s = new Square(i, N - i + n * l + k);
					//StdOut.println(s);
					bag.add(s);
				}
			}
		}
		//StdOut.println(bag);
		XShape aD = new XShape(bag);
		return aD;
	}


	/**
	 * Creates an analog of the Aztec Diamond with size N.
	 * for a tiling is by ribbon n-tiles.
	 * This is a variant that has the tiles of all types.
	 * 
	 * @param N
	 * @return
	 */		
	public static XShape aztecRibbon2(int n, int N) {        
		TreeSet<Square> bag = new TreeSet<Square>();
		for (int l = 0; l < N; l++) {
			for (int i = l; i < N; i++) {
				for (int k = 0; k < n; k++) {
					Square s = new Square(i, N - 1 - i + n * l + k);
					//StdOut.println(s);
					bag.add(s);
				}
			}
		}
		for (int l = 1; l <= N; l++) {
			//int l = 1; //level 
			for (int i = 0; i < l; i++) { //x coordinate increment
				int x = N + i * (n - 1); 
				for (int t = 0; t < n - 1; t++) { //types of tiles
					for (int k = 0; k < n; k++) { //squares of a ribbon
						Square s = new Square(x + t, N + (l - 1) * n - x + k);
						//StdOut.println(s);
						bag.add(s);
					}
				}
			}
		}
		//StdOut.println(bag);
		XShape aD = new XShape(bag);
		return aD;
	}

	/**
	 *calculating intersections of level lines of x + y = l with borders of the shape.
	 *If the intersection is a good corner, we do not include it. 
	 *
	 */
	public void calculateCrosses() {
		crosses = new ArrayList<ArrayList<Integer>>();
		//Initialization;
		for (int i = 0; i < Lmax + 1; i++) {
			crosses.add(new ArrayList<Integer>());
		}
		for (int l = 0; l < Lmax + 1; l++) { //cycle over levels
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (int i = 0; i < l + 1; i++) { //cycle within level
				if (isBorder(i, l - i) && !isGoodCorner(i, l - i)) {
					list.add(i);
					/*	if (isGoodCorner(i, l - i)) {
						list.add(i);
					}
					 */
				}
			}
			crosses.set(l, list);
		}
	}

	/** Old version. Keep for compatibility. 
	 *calculating intersections of level lines of x + y = l with borders of the shape.
	 *If the intersection is a good corner, we include it two times.
	 *
	 */

	public ArrayList<ArrayList<Integer>> calculateWeakCrosses() {
		ArrayList<ArrayList<Integer>> wCrosses = new ArrayList<ArrayList<Integer>>();
		//Initialization;
		for (int i = 0; i < Lmax + 1; i++) {
			wCrosses.add(new ArrayList<Integer>());
		}
		for (int l = 0; l < Lmax + 1; l++) { //cycle over levels
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (int i = 0; i < l + 1; i++) { //cycle within level
				if (isBorder(i, l - i)) {
					list.add(i);
					if (isGoodCorner(i, l - i)) {
						list.add(i);
					}
				}
			}
			wCrosses.set(l, list);
		}
		return wCrosses;
	}


	/**
	 *calculating intersections of level lines of x + y = l with borders of the shape.
	 *If the intersection is a good corner, we include it two times.
	 *
	 * TODO  crossesHalf are the intersections at half-integers: x + y = l + 1/2; 
	 *
	 */ 
	public void calcWeakCrosses() {
		weakCrosses = new ArrayList<ArrayList<Integer>>();
		crossesHalf1 = new ArrayList<ArrayList<Square>>();
		crossesHalf2 = new ArrayList<ArrayList<Square>>();
		//Initialization;
		for (int i = 0; i < Lmax + 2; i++) {
			weakCrosses.add(new ArrayList<Integer>());
		}

		for (int i = 0; i < Lmax + 1; i++) {
			crossesHalf1.add(new ArrayList<Square>());
			crossesHalf2.add(new ArrayList<Square>());
		}

		//Calculating weakCrosses
		for (int l = 0; l < Lmax + 2; l++) { //cycle over levels
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (int i = 0; i < l + 1; i++) { //cycle within level
				if (isBorder(i, l - i)) {
					list.add(i);
					if (isGoodCorner(i, l - i)) {
						list.add(i);
					}
				}
			}
			weakCrosses.set(l, list);
		}
		//for halfCrosses we need a modification of the method isBorder(); but fortunately there is no need for isGoodCorner method.

		for (int l = 0; l < Lmax + 1; l++) { //cycle over levels. we are looking at intersections with the line 
			//x + y = l + 1/2
			ArrayList<Square> list1 = new ArrayList<Square>();
			ArrayList<Square> list2 = new ArrayList<Square>();
			for (int i = 0; i < 2 * l + 2; i++) { //cycle within level, the convention is that i is the x coordinate 
				//multiplied by 2, that is, i = 2x. Its maximum possible range is
				//from 0 to 2 l + 1
				if(i % 2 == 0) { //the x coordinate is integer and the y coordinate is half-integer.
					Square s0 = new Square(i/2, l - i/2);
					Square s1 = new Square(i/2 - 1, l - i/2);
					if ((squares.contains(s0) && !squares.contains(s1)) ||
							(!squares.contains(s0) && squares.contains(s1))){ //there is a square on the right but no square on the left
						// or vice versa.
						list1.add(s0);
						list2.add(new Square(i/2, l - i/2 + 1));
					}

				} else { //the x coordinate is half-integer and the y coordinate is integer
					Square s0 = new Square((i - 1)/2, l - (i - 1)/2);
					Square s1 = new Square((i - 1)/2, l - (i - 1)/2 - 1);
					if ((squares.contains(s0) && !squares.contains(s1)) ||
							(!squares.contains(s0) && squares.contains(s1))) { //square above is in the region and the
						//square below is not or vice versa
						list1.add(s0);
						list2.add(new Square((i + 1)/2, l - (i - 1)/2));
					}								
				}
			}
			crossesHalf1.set(l, list1);
			crossesHalf2.set(l, list2);
		}
	}

	/**
	 * checks if a point (x, y) is on the border of the region 
	 * It works by selecting several pairs of neighboring pairs 
	 * and checking the "border condition" for them.
	 * The border condition is satisfied if one of the squares is inside of 
	 * the region and the other is outside. 
	 * 
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Boolean isBorder(int x, int y) {
		boolean in1, in2;
		in1 = squares.contains(new Square(x, y)); //looking at NE and SE squares
		in2 = squares.contains(new Square(x, y - 1));
		if (in1 ^ in2) return true;

		in1 = squares.contains(new Square(x - 1, y)); //looking at NW and SW squares
		in2 = squares.contains(new Square(x - 1, y - 1));
		if (in1 ^ in2) return true;

		in1 = squares.contains(new Square(x, y)); //looking at NE and NW squares
		in2 = squares.contains(new Square(x - 1, y));
		if (in1 ^ in2) return true;

		in1 = squares.contains(new Square(x, y - 1)); //looking at SE and SW squares
		in1 = squares.contains(new Square(x - 1, y - 1));
		if (in1 ^ in2) return true;

		return false;
	}

	/**
	 * checks if a point (x, y) is a corner of the region 
	 * First it checks if this point on the border. If yes, then 
	 * it checks the "corner condition".
	 * The corner condition is satisfied if only one of the four neighbor squares squares is inside the
	 * the region or only one is outside. 
	 * 
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Boolean isCorner(int x, int y) {
		boolean in1, in2, in3, in4, cr1, cr2;
		if (!isBorder(x, y)) return false;
		in1 = squares.contains(new Square(x, y)); //NE neighbor square
		in2 = squares.contains(new Square(x, y - 1)); //SE 
		in3 = squares.contains(new Square(x - 1, y)); //NW
		in4 = squares.contains(new Square(x - 1, y - 1)); //SW

		cr1 = (!in1 && in2 && in3 && in4) || (in1 && !in2 && in3 && in4) || (in1 && in2 && !in3 && in4)
				|| (in1 && in2 && in3 && !in4);
		cr2 = (in1 && !in2 && !in3 && !in4) || (!in1 && in2 && !in3 && !in4) || (!in1 && !in2 && in3 && !in4)
				|| (!in1 && !in2 && !in3 && in4);
		if (cr1 || cr2) return true;

		return false;
	}

	/**
	 * checks if a point (x, y) is a "good" corner of the region.
	 * a corner is good only if it has a tangent line of the type x + y = c
	 * First it checks if this point on the border. If yes, then 
	 * it checks the "good corner condition".
	 * The good corner condition is satisfied if either NE corner is outside and all others are inside
	 * or NE is inside and all others are outside or 
	 * or SW corner is outside and all others are inside or SW is inside and all others outside
	 * 
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Boolean isGoodCorner(int x, int y) {
		boolean in1, in2, in3, in4, cr;
		if (!isBorder(x, y)) return false;
		in1 = squares.contains(new Square(x, y)); //NE neighbor square
		in2 = squares.contains(new Square(x, y - 1)); //SE 
		in3 = squares.contains(new Square(x - 1, y)); //NW
		in4 = squares.contains(new Square(x - 1, y - 1)); //SW

		cr = (!in1 && in2 && in3 && in4) || (in1 && !in2 && !in3 && !in4)  || (in1 && in2 && in3 && !in4)
				|| (!in1 && !in2 && !in3 && in4);
		if (cr) return true;

		return false;
	}

	/**
	 * This functions provides a point on the border. By construction, it is 
	 * the base point of the "smallest" square 
	 */
	public Square getBorderPoint() {
		return squares.first();
	}

	/**
	 * Starting with one border square we go to another one in counterclockwise direction.
	 * The both the input and the output border squares might be outside the region.\
	 *  However, their (x,y) coordinates must be on the border
	 * of the region.
	 * @param s
	 */

	public Square moveAroundCCW(Square s) {

		if (isBorder(s.x + 1, s.y) && squares.contains(s)
				&& !squares.contains(new Square(s.x, s.y - 1))) { //moving right
			return new Square(s.x + 1, s.y) ;
		} 
		if (isBorder(s.x, s.y + 1) && squares.contains(new Square(s.x - 1, s.y))
				&& !squares.contains(s)) { //moving up
			return new Square(s.x, s.y + 1);
		}
		if (isBorder(s.x - 1, s.y) && squares.contains(new Square(s.x - 1, s.y - 1))
				&& !squares.contains(new Square(s.x - 1, s.y))) { //moving left
			return new Square(s.x - 1, s.y);
		}
		if (isBorder(s.x, s.y - 1) && squares.contains(new Square(s.x, s.y - 1))
				&& !squares.contains(new Square(s.x - 1, s.y - 1))) { //moving down
			return new Square(s.x, s.y - 1);
		}		
		return null;
	}

	/**
	 * Starting with one border square we go to another one in clockwise direction.
	 * The both the input and the output border squares might be outside the region.\
	 *  However, their (x,y) coordinates must be on the border
	 * of the region.
	 * @param s
	 */

	public Square moveAroundCW(Square s) {

		if (isBorder(s.x + 1, s.y) && !squares.contains(s)
				&& squares.contains(new Square(s.x, s.y - 1))) { //moving right
			return new Square(s.x + 1, s.y) ;
		} 
		if (isBorder(s.x, s.y + 1) && !squares.contains(new Square(s.x - 1, s.y))
				&& squares.contains(s)) { //moving up
			return new Square(s.x, s.y + 1);
		}
		if (isBorder(s.x - 1, s.y) && !squares.contains(new Square(s.x - 1, s.y - 1))
				&& squares.contains(new Square(s.x - 1, s.y))) { //moving left
			return new Square(s.x - 1, s.y);
		}
		if (isBorder(s.x, s.y - 1) && !squares.contains(new Square(s.x, s.y - 1))
				&& squares.contains(new Square(s.x - 1, s.y - 1))) { //moving down
			return new Square(s.x, s.y - 1);
		}		
		return null;
	}


	public void draw() {
		if (myDr == null) {
			myDr = new Draw();
			myDr.setCanvasSize(800, 800);
		}
		drawShape(myDr);
	}

	public void drawShape(Draw dr) {
		int sizeX = 0;
		int sizeY = 0;
		for (Square s : squares) {
			if (s.x + 1> sizeX) {
				sizeX = s.x + 1;
			} 
			if (s.y + 1> sizeY) {
				sizeY = s.y + 1;
			}
		}
		int size = sizeX;
		if (size < sizeY) {
			size = sizeY;
		}
		dr.setXscale(-0.5, size + 0.5);
		dr.setYscale(-0.5, size + 0.5);
		dr.clear(Draw.LIGHT_GRAY);
		dr.setPenRadius(0.001);


		dr.setPenColor(Draw.BOOK_LIGHT_BLUE);
		dr.setPenRadius(0.005);

		// now we can draw
		// we simply draw all squares in the shape
		for (Square s : squares) {
			s.draw(dr);
		}

		dr.setPenColor(Draw.WHITE);
		dr.setPenRadius(0.001);
		for (int i = 0; i < size; i++) {
			dr.line(i, 0, i, size);
		}
		for (int i = 0; i < size; i++) {
			dr.line(0, i, size, i);
		}

		dr.setPenColor(Draw.BLACK);
		dr.setPenRadius(0.005);
		/*
		Square sOrigin = getBorderPoint();
		Square s0 = new Square(sOrigin);
		Square s1 = moveAroundCCW(s0);
		while (!s1.equals(sOrigin)) {
			dr.line(s0.x, s0.y,  s1.x, s1.y);
			s0 = s1;
			s1 = moveAroundCCW(s0);	
		}
		 */
		dr.show();

	}


	public void drawLevel(int l) {
		if (myDr == null) {
			myDr = new Draw();
			myDr.setCanvasSize(800, 800);
		}
		myDr.setPenColor(Draw.BLUE);
		myDr.line(0, l, l, 0);
		myDr.setPenColor(Draw.BLACK);
	}

	/**
	 * For testing methods
	 * @param args
	 */

	public static void main(String[] args) {
		Square s;
		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();

		/* Test case 1
		 * 	
		 */	
		/*
		for (int i = 0; i < 3; i ++){
			shapeI.add(0);
			shapeF.add(20);
		}
		for (int i = 4; i < 8; i++){
			shapeI.add(0);
			shapeF.add(5);
		}
		for (int i = 9; i < 14; i++){
			shapeI.add(0);
			shapeF.add(15);
		}

		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);
		for (int i = 4; i < 8; i++){
			for (int j = 0; j < 5; j ++){
				s = new Square(10 + j, i);
				bag.add(s);
			}
		}
		 */
		/* Test case 2
		 * 	
		 */	
		/*
		for (int i = 0; i < 1; i ++){
		shapeI.add(0);
		shapeF.add(2);
		}

		for (int i = 1; i < 4; i ++){
		shapeI.add(0);
		shapeF.add(0);
		}
		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);

		XShape shape = new XShape(bag);
		shape.draw();
		StdOut.println("weakCrosses:");
		for (int i = 0; i < shape.weakCrosses.size(); i++){
			StdOut.print("Level = " + i + ": ");
			StdOut.println(shape.weakCrosses.get(i));
		}	
		StdOut.println("crossesHalf:");
		for (int i = 0; i < shape.crossesHalf1.size(); i++){
			StdOut.print("Level = " + i + " + 1/2 : ");
			StdOut.println(shape.crossesHalf1.get(i));
			StdOut.println(shape.crossesHalf2.get(i));
		}
		 */
		/* test case 3
		 * 
		 */

		XShape shape = XShape.aztecRibbon2(3,4);
		shape.draw();
	}
}

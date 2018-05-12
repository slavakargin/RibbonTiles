package xrib;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.TreeMap;

import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdOut;

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
	int n; //length of each ribbon tile
	TreeMap<Square, ArrayList<Integer>> height;


	/**
	 * This is constructor. Its arguments is a tiling and a point (x, y) on the border of the tiling.
	 * At this point the height will be set equal to 0.
	 * 
	 * @param T 
	 * @param x
	 * @param y
	 */
	XHeight(XRibTiling T) {
		this.n = T.n;
		this.tiling = T;
		height = new TreeMap<Square, ArrayList<Integer>>();
		// initialization
		// note that height can be defined also on squares that are outside of T.
		for (Square s : T.shape.squares) {
			height.put(s, new ArrayList<Integer>(n));
		}
		calcHeightBorder();
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
			if (tiling.shape.isBorder(s.x, s.y)) {
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
		Square s1 = tiling.shape.moveAroundCCW(s0);	
		while (!s1.equals(sOrigin)) {
			updateHeight(s0, s1);
			s0 = s1;
			s1 = tiling.shape.moveAroundCCW(s0);	
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
		//we update the heights at the border of the tile. based on the height at the root square. 
		//Square s0 = new Square(tile.xmin, tile.ymin);
		//XRibTiling singleTile = new XRibTiling(n, tile.squares(), "");
		//singleTile.H.calcHB(s0, height.get(s0));
		//for (Square s : singleTile.H.height.keySet()) {
		//	height.put(s, singleTile.H.height.get(s));
		//}
	}
	

	void draw() {
		draw(n);
	}

	/**
	 * Print all the components of the height wich are smaller or equal than k
	 * 
	 * @param dr - window to draw
	 * @param k - component of the height, can take values from 0 to n-1.
	 */

	void draw(int k) {
		tiling.draw();
		tiling.shape.myDr.setPenColor(Draw.BLACK);
		for (Square s : height.keySet()) {
			for (int i = 0; i < k; i++){
				if (height.get(s).size() == n) {
					tiling.shape.myDr.text(s.x + 0.2 * i, s.y + 0.1, String.valueOf(height.get(s).get(i)));
				}
			}
		}
	}


	/**
	 *  Calculate Height using data on tiles in tiling T.	
	 */
	
	public void calcHeightInside() {
		//XRibTile t = tiling.tiles().get(0);
		//StdOut.println("Updating tile " +  t);
		//tileUpdate(t);
		for (XRibTile t : tiling.tiles()) {
			tileUpdate(t);
		}
	}
	
	/*
	public void calcHeightInside() {
		XRibTile tile, tile0;
		Square s0;
		ArrayList<Integer> h0;
		int r, move, move1;
		for (Square s : tiling.shape.squares) {
			if (tiling.shape.isBorder(s.x, s.y)) { //this square is on the border , no need to recalculate
				continue; 
			} else {
				//StdOut.println("Trying to calculate");
				tile = tiling.square2tile.get(s); 
				if (tile.xmin == s.x && tile.ymin == s.y) { //this is the root square of a tile
					s0 = new Square(s.x - 1, s.y); //this square should have a height already calculated
					h0 = new ArrayList<Integer>(height.get(s0));
					//There is a formula for updating, it is supposed to work like this:
					h0.set((s0.x + s0.y) % n, h0.get((s0.x + s0.y) % n) - 1); //first step of updating
					h0.set((s0.x + s0.y - 1 + n) % n, h0.get((s0.x + s0.y - 1 + n) % n) + 1);
					tile0 = tiling.square2tile.get(s0);
					r = s.x + s.y - tile0.level;
					h0.set((s.x + s.y - r + n) % n, h0.get((s.x + s.y - r + n) % n) + 2); //second step of updating
					h0.set((s.x + s.y - r - 1 + n) % n, h0.get((s.x + s.y - r - 1 + n) % n) - 2);  
					height.put(s, h0); 				 
				} else {
					//this is not a root square, so we can update height moving along the border of the 
					// corresponding tile
					r = s.x + s.y - tile.level; 
					move = (tile.typeCode >> r - 1) & 1; //if 0, the tile went right to get to this square
					//if 1, it moved up. 
					if (r == 1) {
						if (move == 1) { //approach from below
							s0 = new Square(s.x, s.y - 1);
							updateHeight(s0, s);
						} else { //approach from left
							s0 = new Square(s.x - 1, s.y);
							updateHeight(s0, s);
						}
					} else { //in this case we also need to know the move before the last one.
						move1 = (tile.typeCode >> r - 2) & 1; // we have 4 cases depending on a combination of 
						//move and move1
						if ((move == 0 && move1 == 0) || (move == 1 && move1 == 0)) { //approach from left
							s0 = new Square(s.x - 1, s.y);
							updateHeight(s0, s);
						} else { //from below
							s0 = new Square(s.x, s.y - 1);
							updateHeight(s0, s);
						}
					}
				}
			}
		}
	}
	*/
	



	
public void saveHeight(String fn) {
	/*
	PrintWriter writer;
	try {
		writer = new PrintWriter(fn, "UTF-8");
	} 
	catch (FileNotFoundException e){
	   StdOut.println("a problem with opening file " + fn);
	   StdOut.println("FileNotFoundException: " + e);
	}
	catch (UnsupportedEncodingException e){
		   StdOut.println("UnsupportedEncodingException: " + e);
	}	
	writer.println("The first line");
	writer.println("The second line");
	writer.close();
	*/
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
		n = 3;
		for (int i = 0; i < N; i ++){
			shapeI.add(0);
			shapeF.add(3);
		}
		
		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);
		xrt = new XRibTiling(n, bag, "");	
		xrt.H.calcHeightInside();
		xrt.H.draw();
		xrt.H.saveHeight("height.txt");
		
		xrtCopy = new XRibTiling(xrt);	
		XUtility.Glauber(xrtCopy, 1000);
		xrtCopy.H.calcHeightInside();
		xrtCopy.H.draw();
		xrtCopy.H.saveHeight("heightCopy.txt");
	}
}

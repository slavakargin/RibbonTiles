package xrib;

import java.util.ArrayList;
import java.util.TreeSet;
//import java.util.Random;

import edu.princeton.cs.algs4.Draw;
//import edu.princeton.cs.algs4.StdOut;

public class XShape {
	  //conceptual variables
	    protected TreeSet<Square> squares; // A bag of squares which are inside of the region.
		
	  //Geometric variables
		protected Integer Lmin, Lmax; //the smallest and the largest levels that  have a non-empty intersection with the region.
		protected ArrayList<ArrayList<Integer>> crosses; //the k-th element of this object is 
                                                         //the list of x -coordinates o intersections of
		                                                 //the line x + y = k 
                                                          //with the border of the shape. 
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
        }
        
        /**
         * Copy constructor
         */
        public XShape(XShape other) {
        	this.squares = new TreeSet<Square>(other.squares);
        	this.Lmin = other.Lmin;
        	this.Lmax = other.Lmax;
        	calculateCrosses();
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
		
		/**
		 *calculating intersections of level lines of x + y = l with borders of the shape.
		 *If the intersection is a good corner, we include it two times.
		 *
		 */
		public ArrayList<ArrayList<Integer>> calculateWeakCrosses() {
			ArrayList<ArrayList<Integer>> weakCrosses = new ArrayList<ArrayList<Integer>>();
		//Initialization;
		for (int i = 0; i < Lmax + 1; i++) {
			weakCrosses.add(new ArrayList<Integer>());
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
			weakCrosses.set(l, list);
		}
		 return weakCrosses;
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
			dr.setPenRadius(0.005);
			
			// now we can draw
			Square sOrigin = getBorderPoint();
			Square s0 = new Square(sOrigin);
			Square s1 = moveAroundCCW(s0);
		    while (!s1.equals(sOrigin)) {
			    myDr.line(s0.x, s0.y,  s1.x, s1.y);
			    s0 = s1;
			    s1 = moveAroundCCW(s0);	
			}
			myDr.show();
		}
		
		
		/**
		 * For testing methods
		 * @param args
		 */

		public static void main(String[] args) {
			
		}
}

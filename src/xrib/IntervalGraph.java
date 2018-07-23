/**
 * 
 */
package xrib;

import java.util.ArrayList;
import java.util.TreeSet;
//import java.util.HashMap;

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.Graph;
import edu.princeton.cs.algs4.Digraph;

/**
 * This class will start with a shape and construct a graph (a tree for simple connected regions).
 * The vertices of this graph represent the intersection intervals of level lines x + y = l with the region R. 
 * The edges show how these intervals join when we go from level l to level l+1 or vice versa. 
 * 
 * The graph will be eventually decorated. Each vertex will be associated with a vector that contains the 
 * intersection numbers of the intervals with tiles of various levels. 
 * 
 * On a more advanced level, it will be associated with a structure that also keep information about order of 
 * the tiles in this intersection. 
 * 
 * The eventual goal for this class is to provide a method that would test if the regions is tileable by
 * ribbon tiles. 
 * 
 * @author vladislavkargin
 *
 */
public class IntervalGraph {

	XShape shape; //the shape for which we construct the graph. 
	ArrayList<Interval> intervals;
	int V; //number of vertices in the interval graph.
	int L; //number of levels
	public Graph G;
	private Draw myDr;

	ArrayList<Boolean> marked; //marks those vertices for which the tile
	//vector has been calculated. (also, when calculating the graph of tiles of the region, we will
	//use this object to check if the tiles intersecting a given interval were ordered. 

	/**
	 * This is the basic constructor. It calculates the graph of 
	 * intervals from a shape
	 *  but it does not assume that the size of ribbon tiles is known 
	 *  so it does not calculate the
	 *  differences in heights for interval end-points 
	 *  or the number of intersections of the interval 
	 *  with tiles of different levels. This should be done separately.
	 *  
	 *  @param shape XShape object. Assumed to be connected. 
	 */
	public IntervalGraph(XShape shape) {
		this.shape = new XShape(shape);
		intervals = new ArrayList<Interval>();
		L = shape.weakCrosses.size();

		ArrayList<Integer> rawLevelIntervals;
		int start, end;		
		// First, we find intervals. 
		for (int l = 0; l < L; l++){
			rawLevelIntervals = shape.weakCrosses.get(l); //if the shape is connected this is 
			//never empty.
			if (rawLevelIntervals.size() == 2) { //there is only one interval in this level. 
				intervals.add(new Interval(2 * rawLevelIntervals.get(0), 2 * rawLevelIntervals.get(1), 2 * l));	
			} else {
				start = 0;
				while (start < rawLevelIntervals.size()) {
					end = start + 1;
					while (end < rawLevelIntervals.size() - 1) { //try to extend the interval
						if (rawLevelIntervals.get(end + 1) == rawLevelIntervals.get(end)) {
							end = end + 2;
						} else {
							break;
						} // at the end of this cycle the interval is maximal possible. 
					}
					intervals.add(new Interval(2 * rawLevelIntervals.get(start), 2 * rawLevelIntervals.get(end), 2 * l));	
					start = end + 1;
				}
			}
			if (l < L - 1) {
				for (int i = 0; i < shape.crossesHalf1.get(l).size(); i = i+2) {
					int x1 = shape.crossesHalf1.get(l).get(i).x + shape.crossesHalf2.get(l).get(i).x;
					int x2 = shape.crossesHalf1.get(l).get(i + 1).x + shape.crossesHalf2.get(l).get(i + 1).x;
					intervals.add(new Interval(x1, x2, 2 * l + 1));	
				}
			}
		}

		//Now build the graph
		V = intervals.size();
		G = new Graph(V);
		//Now we need to connect appropriate vertices. We are going over
		//levels and try to connect each vertex in the level l to each vertex in 
		// levels l - 1/2 and level l + 1/2. They are connected only if the projections
		//of the corresponding intervals on the line x + y have a non-empty intersection.
		for (int v = 0; v < V; v++) {
			for (int w = v + 1; w < V; w++) {
				if (testConnection(v, w)) {
					G.addEdge(v, w);
				}
			}
		}
		marked = new ArrayList<Boolean>();
		for (int v = 0; v < V; v++) {
			marked.add(false);
		}
		calcLeaves();
	}

	private boolean testConnection(int v, int w) {
		Interval iV = intervals.get(v);
		Interval iW = intervals.get(w);
		if (((iV.level - iW.level == 1) || (iV.level - iW.level == -1)) && iV.intersect(iW)) {
			return true;
		}
		return false;
	}
	/**
	 * draws the interval graph for a given region. 
	 */

	public void draw() {
		double x, y;
		double r = 0.01;
		ArrayList<Double> Y = new ArrayList<Double>(); // x and y coordinates of the graph points.
		ArrayList<Double> X = new ArrayList<Double>();
		if (myDr == null) {
			myDr = new Draw();
			myDr.setCanvasSize(600, 600);
		}
		myDr.setXscale(-0.1, 1.1);
		myDr.setYscale(-0.1, 1.1);
		myDr.clear(Draw.LIGHT_GRAY);
		myDr.setPenRadius(0.005);
		for (int v = 0; v < V; v++){
			Interval iv = intervals.get(v);
			y = ((double) iv.level) /(2 * L); 
			Y.add(y);
			x = ((double) (iv.b + iv.a))/(3 * L);
			X.add(x);
			if (marked.get(v)) {
				myDr.setPenColor(Draw.RED);
			}
			myDr.circle(x, y, r);
			myDr.setPenColor(Draw.BLACK);
		}
		// now add edges;
		for (int v = 0; v < V; v++) {
			for (int w : G.adj(v)) {
				myDr.line(X.get(v), Y.get(v), X.get(w), Y.get(w));
			}
		}
	}


	/**
	 *reads the differences of height on the end-points of intervals.
	 * 
	 * n is the size of the tiles. 
	 * 
	 * @param n
	 */	
	private void readHeightDiffs(int n){
		Interval iv;
		int level, x0, y0, x1, y1;
		Square s0, s1, s2, s3;
		XHeight H = new XHeight(n, shape);				
		for (int v = 0; v < V; v++) {
			iv = intervals.get(v);
			level = iv.level;
			//the procedures are different for even and odd levels 
			//(i.e., for integer and odd intervals)
			ArrayList<Integer> diff = new ArrayList<Integer>();
			if (level % 2 == 0) {
				x0 = iv.a / 2;
				x1 = iv.b / 2;
				y0 = level/2 - x0;
				y1 = level/2 - x1;
				s0 = new Square(x0, y0);
				s1 = new Square(x1, y1);			
				for (int i = 0; i < n; i++){
					diff.add(H.height.get(s1).get(i) - H.height.get(s0).get(i));
				}
				iv.setDiffH(diff);
			} else { //half - integer interval
				if ((iv.a % 2 == 0) && (iv.b % 2 == 0)) { // both x - coordinates of end-points are integer.
					s0 = new Square(iv.a / 2, (level - 1)/2 - iv.a / 2);
					s1 = new Square(iv.a / 2, (level + 1)/2 - iv.a / 2);
					s2 = new Square(iv.b / 2, (level - 1)/2 - iv.b / 2);
					s3 = new Square(iv.b / 2, (level + 1)/2 - iv.b / 2);
					for (int i = 0; i < n; i++){
						diff.add((H.height.get(s2).get(i) + H.height.get(s3).get(i) 
								- H.height.get(s1).get(i) - H.height.get(s0).get(i))/2);
					}
				} else if ((iv.a % 2 == 0) && (iv.b % 2 == 1)) { //the x - coordinate of the second end-point is half - integer					
					s0 = new Square(iv.a / 2, (level - 1)/2 - iv.a / 2);
					s1 = new Square(iv.a / 2, (level + 1)/2 - iv.a / 2);
					s2 = new Square((iv.b - 1)/ 2, (level - iv.b) / 2);
					s3 = new Square((iv.b + 1)/ 2, (level - iv.b) / 2);
					for (int i = 0; i < n; i++){
						diff.add((H.height.get(s2).get(i) + H.height.get(s3).get(i) 
								- H.height.get(s1).get(i) - H.height.get(s0).get(i))/2);
					}
				} else if ((iv.a % 2 == 1) && (iv.b % 2 == 0)) { 
					s0 = new Square((iv.a - 1) / 2, (level - iv.a) / 2);
					s1 = new Square((iv.a + 1) / 2, (level - iv.a) / 2);
					s2 = new Square(iv.b / 2, (level - 1)/2 - iv.b / 2);
					s3 = new Square(iv.b / 2, (level + 1)/2 - iv.b / 2);
					for (int i = 0; i < n; i++){
						diff.add((H.height.get(s2).get(i) + H.height.get(s3).get(i) 
								- H.height.get(s1).get(i) - H.height.get(s0).get(i))/2);
					}
				} else {
					s0 = new Square((iv.a - 1) / 2, (level - iv.a) / 2);
					s1 = new Square((iv.a + 1) / 2, (level - iv.a) / 2);
					s2 = new Square((iv.b - 1) / 2, (level - iv.b) / 2);
					s3 = new Square((iv.b + 1) / 2, (level - iv.b) / 2);
					for (int i = 0; i < n; i++){
						diff.add((H.height.get(s2).get(i) + H.height.get(s3).get(i) 
								- H.height.get(s1).get(i) - H.height.get(s0).get(i))/2);
					}
				}
				iv.setDiffH(diff);
			}
		}
	}


	/**
	 * Set the tile vector to zero vector and the local graph to empty graph at all leaves of the graph. 
	 * 
	 */

	private void calcLeaves() {
		ArrayList<Integer> tileVector = new ArrayList<Integer>();
		for (int l = 0; l < L; l++) {
			tileVector.add(0);
		}
		//Digraph Gamma = new Digraph(0);
		for (int v = 0; v < V; v++) {
			if (G.degree(v) == 1) {
				intervals.get(v).setTileVector(tileVector);
				//intervals.get(v).setLocalGraph(Gamma);
				marked.set(v, true);
			}
		}
	}

	/**
	 * This function attempts to calculate the tile vector at v from the tile vectors at vertices below.
	 * We assume here that v is not a leaf and so its degree is at least 2. 
	 * 
	 */
	private boolean calcFromBelow(int n, int v) {
		Interval iv = intervals.get(v);
		if (marked.get(v)) {
			StdOut.println("Warning: the tile vector for vertex v = " + v + " has already been calculated.");
		}
		TreeSet<Integer> below = new TreeSet<Integer>();
		TreeSet<Integer> atLevel = new TreeSet<Integer>();

		for (int u : G.adj(v)) {
			if (intervals.get(u).level < iv.level) {
				below.add(u);
			}
		}
		for (int u : below) {
			for (int w: G.adj(u)) {
				if (w != v && intervals.get(w).level == iv.level) {
					atLevel.add(w);
				}
			}
		}
		ArrayList<Integer> tileVector = new ArrayList<Integer>();
		for (int l = 0; l < L; l++) {
			tileVector.add(0);
		}
		// the calculation depends on whether the interval is integer or half - integer. 
		//if the interval is integer that the calculations goes through if and only if all neighbors below are marked.
		if (iv.level % 2 == 0) { //integer interval
			                     //atLevel vertices do not matter
			int level = iv.level/2;
			for (int s = 0; s < L; s++) {
				if (s < level - n || s > level - 1) {
					tileVector.set(s, 0);
				} else { 
					if (s == level - 1) {
						tileVector.set(s, iv.diffH.get(s % n));
					}
					for (int u: below) {
						if (! marked.get(u)) {
							return false;
						} else {
							Interval iu = intervals.get(u);
							if (s < level - 1) {
								tileVector.set(s, tileVector.get(s) + iu.tileVector.get(s));
							} else {
								int x = 0; 
								if (s >= n) {
									x = iu.tileVector.get(s - n);
								}
								tileVector.set(s, tileVector.get(s) + x - iu.diffH.get(s % n));
							}
						}
					}
				}
			}
			iv.setTileVector(tileVector);
			marked.set(v, true);
		}	else { //for half integer intervals
			//there can be only one vertex below and some vertices at the same level. 
			// All of these should be marked. 

			int level = (iv.level - 1)/2;
			for (int u : below) {
				if (!marked.get(u)) {
					return false;
				} else {
					Interval iu = intervals.get(u);
					for (int s = 0; s < L; s++) {
						if (s < level - n || s > level ) {
							tileVector.set(s, 0);
						} else {	    					    		 
							if (s < level) {
								tileVector.set(s, iu.tileVector.get(s));
							} else {
								int x = 0; 
								if (s >= n) {
									x = iu.tileVector.get(s - n);
								}
								x = x + iv.diffH.get(s % n) - iu.diffH.get(s % n);
								tileVector.set(s, x);
							} 
						}
					}
				}
			}
			for (int w: atLevel) {
				if (!marked.get(w)) {
					return false;
				} else {
					Interval iw = intervals.get(w);
					for (int s = 0; s < L; s++) {
						if (s < level - n || s > level ) {
							tileVector.set(s, 0);
						} else {	    					    		 
							if (s < level) {
								tileVector.set(s, tileVector.get(s) - iw.tileVector.get(s));
							} else {
								int x = iw.tileVector.get(s);
								x = x - iw.diffH.get(s % n);
								tileVector.set(s, tileVector.get(s) - x);
							} 
						}
					}
				}
			}
			iv.setTileVector(tileVector);
			marked.set(v, true);
		}
		return true;
	}


	/**
	 * This function attempts to calculate the tile vector at v from the tile vectors at vertices above.
	 * We assume here that v is not a leaf and so its degree is at least 2. 
	 * 
	 */
	private boolean calcFromAbove(int n, int v) {
		Interval iv = intervals.get(v);
		if (marked.get(v)) {
			StdOut.println("Warning: the tile vector for vertex v = " + v + " has already been calculated.");
		}
		TreeSet<Integer> above = new TreeSet<Integer>();
		TreeSet<Integer> atLevel = new TreeSet<Integer>();

		for (int u : G.adj(v)) {
			if (intervals.get(u).level > iv.level) {
				above.add(u);
			}
		}
		for (int u : above) {
			for (int w: G.adj(u)) {
				if (w != v && intervals.get(w).level == iv.level) {
					atLevel.add(w);
				}
			}
		}
		ArrayList<Integer> tileVector = new ArrayList<Integer>();
		for (int l = 0; l < L; l++) {
			tileVector.add(0);
		}
		// the calculation depends on whether the interval is integer or half - integer. 
		//if the interval is integer that the calculations goes through if and only if all neighbors above are marked.
		if (iv.level % 2 == 0) { //integer interval
			//vertices in atLevel do not matter
			int level = iv.level/2;
			for (int s = 0; s < L; s++) {
				if (s < level - n || s > level - 1) { // far away
					tileVector.set(s, 0);
				} else { 
					if (s == level - n) {
						tileVector.set(s, iv.diffH.get(s % n)); //at s=l - n
					}
					for (int u: above) {
						if (! marked.get(u)) {
							return false;
						} else {
							Interval iu = intervals.get(u);
							if (s > level - n) {
								tileVector.set(s, tileVector.get(s) + iu.tileVector.get(s));
							} else { // s = l - n
								int x = 0; 
								if (s < L - n) { //corner situation
									x = iu.tileVector.get(s + n);
								}
								tileVector.set(s, tileVector.get(s) + x - iu.diffH.get(s % n));
							}
						}
					}
				}
			}
			iv.setTileVector(tileVector);
			marked.set(v, true);
		}	else { //for half integer intervals
			//there can be only one vertex above and some vertices at the same level. 
			// All of these should be marked.
			int level = (iv.level - 1)/2;
			for (int u : above) {
				if (!marked.get(u)) {
					return false;
				} else {
					Interval iu = intervals.get(u);
					for (int s = 0; s < L; s++) {
						if (s < level - n || s > level ) {
							tileVector.set(s, 0);
						} else {	    					    		 
							if (s > level - n) {
								tileVector.set(s, iu.tileVector.get(s));
							} else {
								int x = 0; 
								if (s < L - n) {
									x = iu.tileVector.get(s + n);
								}
								x = x + iv.diffH.get(s % n) - iu.diffH.get(s % n);
								tileVector.set(s, x);
							} 
						}
					}
				}
			}
			for (int w: atLevel) {
				if (!marked.get(w)) {
					return false;
				} else {
					Interval iw = intervals.get(w);
					for (int s = 0; s < L; s++) {
						if (s < level - n || s > level ) {
							tileVector.set(s, 0);
						} else {	    					    		 
							if (s > level - n) {
								tileVector.set(s, tileVector.get(s) - iw.tileVector.get(s));
							} else {
								int x = iw.tileVector.get(s);
								x = x - iw.diffH.get(s % n);
								tileVector.set(s, tileVector.get(s) - x);
							} 
						}
					}
				}
			}
			iv.setTileVector(tileVector);
			marked.set(v, true);
		}	
		return true;
	}

	/**
	 * This function calculates the vector of tiles at each vertex of the interval graph. 
	 * Each entry of this vector equals to the number of tiles of a particular level that have a non-empty intersection
	 * with the interval corresponding to the vector. 
	 * 
	 * @param n the length of ribbon tiles.
	 * 
	 */
	public void calcTileVectors(int n) {

		readHeightDiffs(n);
		//calculate how many vertices are not yet computed. 
		int uncomp = 0;
		for (int v = 0; v < V; v++) {
			if (!marked.get(v)){
				uncomp++;
			}
		}
		
		while (uncomp > 0) {		
		for (int v = 0; v < V; v++){
			if (!marked.get(v)){
				calcFromBelow(n, v);
			}
		}
		for (int v = V - 1; v >= 0; v--){
			if (!marked.get(v)){
				calcFromAbove(n, v);
			}
		}
		uncomp = 0;
		for (int v = 0; v < V; v++) {
			if (!marked.get(v)){
				uncomp++;
			}
		}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//First we will create a shape to work with
		Square s;
		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();

		//test case 1
        /*
		int n = 3;
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
		XShape shape = new XShape(bag);
       */

		//test case 2
		
		int n = 3;
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
		 


		//test case 3
		/*
		int n = 3;
		int M = 4;
		int N = 3;
		XRibTiling region = XRibTiling.stair(n, M, N);
		XShape shape = region.shape;
		 */

		shape.draw();
		int level = 9;
		shape.drawLevel(level);
		
		
		StdOut.println("Lmax = " + shape.Lmax);
		StdOut.println("Size of weakCrosses = " + shape.weakCrosses.size());
		/*
		StdOut.println("weakCrosses:");
		for (int i = 0; i < shape.weakCrosses.size(); i++){
			StdOut.print("Level = " + i + ": ");
			StdOut.println(shape.weakCrosses.get(i));
		}		
		StdOut.println("Content of Intervals: ");
		for (int i = 0; i < ig.intervals.size(); i++) {
			Interval interval = ig.intervals.get(i);
			StdOut.print("Level = " + interval.level + " and interval = ");
			StdOut.println(interval);
		}
		StdOut.println(ig.G);
		*/
		IntervalGraph ig = new IntervalGraph(shape);
		StdOut.println("Intervals: ");
		for (int v = 0; v < ig.V; v++) {
			StdOut.println("Interval" + ig.intervals.get(v));
		}
		
		ig.calcTileVectors(n);        
		ig.draw();

		StdOut.println("tile vectors:");       
		for (int v = 0; v < ig.V; v++) {
			if (ig.intervals.get(v).level % 2 == 0) {
				StdOut.print("Level = " + ig.intervals.get(v).level/2 + ": ");
			}
			StdOut.println(ig.intervals.get(v).tileVector);
		}
		XGraph xg = new XGraph(n, ig);
		StdOut.println("Structure of tiles: ");
		StdOut.println(xg.level2tiles);
		StdOut.println("The starting points of the levels are :");
		StdOut.println(xg.levelStarts);
		StdOut.println("tile2level structure is :");
		StdOut.println(xg.tile2level);
		for (int v = ig.V - 1; v >= 0; v--) {
			StdOut.println("Interval" + ig.intervals.get(v));
			xg.calcFromAbove(v);
			StdOut.println(xg.ig.intervals.get(v).tileSet);
		}
		ig.draw();
	}
}

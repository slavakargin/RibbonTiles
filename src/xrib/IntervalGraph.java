/**
 * 
 */
package xrib;

import java.util.ArrayList;
import java.util.TreeSet;

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.Graph;
import edu.princeton.cs.algs4.Cycle;

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
	int n; //the size of ribbon tiles;
	int V; //number of vertices in the interval graph.
	int L; //number of levels
	int T; //number of tiles
	
	ArrayList<ArrayList<Interval>> level2intervals; //for each level gives a set of intervals in this level.
	
	
	ArrayList<Integer> tile2level; //shows the level of each dot (tile) in the digraph DG
	ArrayList<TreeSet<Interval>> tile2intervals; //maps each tile to the set of intervals to which it belongs.  
	ArrayList<Integer> level2tiles; // number of tiles in each level.
	ArrayList<Integer> levelStarts; // The k-th element in this list shows the index of the first vertex in level k
	
	
	public Graph G;
	private Draw myDr;

	Boolean simplyConnected;
	ArrayList<Boolean> marked; //marks those vertices for which the tile
	//vector has been calculated. (also, when calculating the graph of tiles of the region, we will
	//use this object to check if the tiles intersecting a given interval were ordered. 
	
 

	/**
	 * This is the basic constructor. It calculates the graph of 
	 * intervals from a shape and the size of ribbon tiles.
	 *  Then it proceeds to calculating the vector of intersection numbers for each 
	 *  interval and finally, for each interval it calculates the basket of tiles that intersect this interval.
	 *  
	 *  @param n the size of ribbon tiles.
	 *  @param shape XShape object.  
	 *  @param squares the squares that will serve as base points for the height calculation in the 
	 *                  case when the shape is not simply connected.  
	 */
	public IntervalGraph(int n, XShape shape, Square... squares) {
		this.n = n; 
		this.shape = new XShape(shape);
		intervals = new ArrayList<Interval>();
		L = shape.weakCrosses.size();
		if (shape.squares.size() % n != 0) {
			StdOut.println("Error: the number of squares in the shape is " + shape.squares.size());
			StdOut.println("It is not divisible by n = " + n);
			return;
		}
		T = shape.squares.size()/n;
		
		//we also need to check that there are equal number of squares of each color. 
		//This number should equal to T;
		for (int color = 0; color < n; color++ ) {
			int count = 0; 
		    for (Square s : shape.squares) {
			   if ((s.x + s.y) % n == color) {
				count++;
			   }
		    }
		    if (count != T) {
		    	StdOut.println("Error: the number of squares of color " + color + " is " + count);
		    	StdOut.println(" which is different from the number of tiles T = " + T);
		    	return;
		    }
		}
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
		//It is convenient to have a structure that would give the set of all intervals for a given level.
		//levels of intervals can be half-integer, so we keep them as 2*l  and they range from 
		// 0 to 2L + 1
		//initialize:
		level2intervals = new ArrayList<ArrayList<Interval>>();
		for (int l = 0; l < 2 * L - 1; l++) {
			level2intervals.add(new ArrayList<Interval>());
		}
		for (Interval iv: intervals) {
			level2intervals.get(iv.level).add(iv);
		}

		//Now build the graph of intervals
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
		//test if the graph is a tree. 
        Cycle finder = new Cycle(G);
		if (finder.hasCycle()) {
		    StdOut.println("Warning: The shape is not simply connected.");
		    simplyConnected = false;
		} else {
			simplyConnected = true;
		}
		// calculate the vectors of intersection numbers for each interval.
	    calcTileVectors(squares);
	    if (simplyConnected) {
		calcTile2Level();
		
		// calculate the basket of tiles for each interval.
		marked = new ArrayList<Boolean>();
		for (int v = 0; v < V; v++) {
			marked.add(false);
		}
		//mark the leaves. 
		for (int i = 0; i < V; i++) {
			if (G.degree(i) == 1) {
				marked.set(i, true);
			}
		}
		calcBaskets();
		calcTile2Intervals();
	    }
	}

	private boolean testConnection(int v, int w) {
		Interval iV = intervals.get(v);
		Interval iW = intervals.get(w);
		if (((iV.level - iW.level == 1) || (iV.level - iW.level == -1)) && iV.intersect(iW)) {
			return true;
		}
		return false;
	}
	
	/*
	public void calculateStructure(){
		
	}
	*/
	
	/**
	 * This function calculates:
	 * tile2level; //shows the level of each dot (tile) in the digraph DG
	 * level2tiles; // number of tiles in each level.
	 * levelStarts; // tile that starts each level
	 * 
	 * This function can be called only after the intersection numbers in TileVectors of 
	 * intervals are calculated.
	 */
	private void calcTile2Level() {
		tile2level = new ArrayList<Integer>(); 				
		
		//calculate how many tiles is in each level.
		level2tiles = new ArrayList<Integer>();
		for (int l = 0; l < L; l++) {
			level2tiles.add(0);
		}
		for (int i = 0; i < intervals.size(); i++) {
			Interval interval = intervals.get(i);
			for (int l = 0; l < L; l++) {
				level2tiles.set(l, level2tiles.get(l) + interval.tileVector.get(l));
			}			
		}
		for (int l = 0; l < L; l++) {
			level2tiles.set(l, level2tiles.get(l) / (2 * n + 1));
		}

		//calculate the vertices that start each level
		levelStarts = new ArrayList<Integer>();
		levelStarts.add(0); 
		for (int l = 1; l < L; l++) { 
			//if (level2tiles.get(l) == 0) {
			//	levelStarts.add(levelStarts.get(l - 1)); // it is a bit of abuse of notation since in fact 
			//	                                         //the start point is null 
			//	                                         // but it is convenient
			//} else {
			    levelStarts.add(levelStarts.get(l - 1) + level2tiles.get(l-1)); 
			//}
		}
		
		// We also need a map tile2level
		tile2level = new ArrayList<Integer>();
		//StdOut.println("The number of squares in the shape is " + shape.squares.size());
		//StdOut.println("The number of tiles is " + shape.squares.size()/n);
		//StdOut.println("levelStarts = " + levelStarts);
		//StdOut.println("level2tiles = " + level2tiles);
		for (int v = 0; v < shape.squares.size()/n; v++) {
			tile2level.add(null);
		}
		for (int l = 0; l < L; l++) {
			for (int i = levelStarts.get(l); i < levelStarts.get(l) + level2tiles.get(l); i++){
				tile2level.set(i, l);
				//StdOut.println("Level " + l + "; tile2level = " + tile2level);
			}
		}
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
	 * @param squares the base points for the height of the holes in a non-simply connected region
	 *                empty if the region is simply connected. 
	 */	
	private void readHeightDiffs(int n, Square... squares){
		Interval iv;
		int level, x0, y0, x1, y1;
		Square s0, s1, s2, s3;
		XHeight H = new XHeight(n, shape, squares);		
		//H.draw();
		for (int v = 0; v < V; v++) {
			iv = intervals.get(v);
			level = iv.level;
			//the procedures are different for even and odd levels 
			//(i.e., for integer and odd intervals)
			ArrayList<Integer> diff = new ArrayList<Integer>();
			ArrayList<Integer> leftH = new ArrayList<Integer>();
			if (level % 2 == 0) {
				x0 = iv.a / 2;
				x1 = iv.b / 2;
				y0 = level/2 - x0;
				y1 = level/2 - x1;
				s0 = new Square(x0, y0);
				s1 = new Square(x1, y1);			
				for (int i = 0; i < n; i++){
					diff.add(H.height.get(s1).get(i) - H.height.get(s0).get(i));
					leftH.add(2 * H.height.get(s0).get(i));
				}
				iv.setDiffH(diff);
				iv.setLeftH(leftH);
			} else { //half - integer interval
				if ((iv.a % 2 == 0) && (iv.b % 2 == 0)) { // both x - coordinates of end-points are integer.
					s0 = new Square(iv.a / 2, (level - 1)/2 - iv.a / 2);
					s1 = new Square(iv.a / 2, (level + 1)/2 - iv.a / 2);
					s2 = new Square(iv.b / 2, (level - 1)/2 - iv.b / 2);
					s3 = new Square(iv.b / 2, (level + 1)/2 - iv.b / 2);
				} else if ((iv.a % 2 == 0) && (iv.b % 2 == 1)) { //the x - coordinate of the second end-point is half - integer					
					s0 = new Square(iv.a / 2, (level - 1)/2 - iv.a / 2);
					s1 = new Square(iv.a / 2, (level + 1)/2 - iv.a / 2);
					s2 = new Square((iv.b - 1)/ 2, (level - iv.b) / 2);
					s3 = new Square((iv.b + 1)/ 2, (level - iv.b) / 2);
				} else if ((iv.a % 2 == 1) && (iv.b % 2 == 0)) { 
					s0 = new Square((iv.a - 1) / 2, (level - iv.a) / 2);
					s1 = new Square((iv.a + 1) / 2, (level - iv.a) / 2);
					s2 = new Square(iv.b / 2, (level - 1)/2 - iv.b / 2);
					s3 = new Square(iv.b / 2, (level + 1)/2 - iv.b / 2);
				} else {
					s0 = new Square((iv.a - 1) / 2, (level - iv.a) / 2);
					s1 = new Square((iv.a + 1) / 2, (level - iv.a) / 2);
					s2 = new Square((iv.b - 1) / 2, (level - iv.b) / 2);
					s3 = new Square((iv.b + 1) / 2, (level - iv.b) / 2);
				}
				for (int i = 0; i < n; i++){
					diff.add((H.height.get(s2).get(i) + H.height.get(s3).get(i) 
							- H.height.get(s1).get(i) - H.height.get(s0).get(i))/2);
					leftH.add((H.height.get(s1).get(i) + H.height.get(s0).get(i)));
				}
				iv.setDiffH(diff);
				//StdOut.println("Interval " + iv + "; s0 = " + s0 + "; s1 = " + s1);
				//StdOut.println("Corresponding Heights = " + H.height.get(s0) + " and " + H.height.get(s1));
				//StdOut.println("Interval " + iv + "; leftH = " + leftH);
				iv.setLeftH(leftH);
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
		//StdOut.println("processing vertex with interval iv = " + iv);
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
							} else { //here, at level l-1, some new tiles can appear.
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
			//StdOut.println("processing half-integer vertex with atLevel set " + atLevel);
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
				//StdOut.println("processing atLevel vertex with interval " + intervals.get(w));
				if (!marked.get(w)) {
					//StdOut.println("This interval has not been yet processed.");
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
	private void calcTileVectors(Square... squares) {
		
        //The calculation is based on the differences in heights. 
		readHeightDiffs(n, squares);
				
		//calculate how many vertices are not yet computed. 
		int uncomp = 0;
		for (int v = 0; v < V; v++) {
			if (!marked.get(v)){
				uncomp++;
			}
		}
		int ITER = 100;
		int count = 0;
		while (uncomp > 0 && count < ITER) {		
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
		count++;
		}
	}
	
	
	
	/**
     * For all intervals, calculates the set of tiles (a basket) that intersect this interval. 
	 * 
	 */
	public void calcBaskets() {

		//calculate how many vertices are not yet computed. 
		int uncomp = 0;
		for (int v = 0; v < V; v++) {
			if (!marked.get(v)){
				uncomp++;
			}
		}	
		int ITER = 100;
		int count = 0;
		while (uncomp > 0 && count < ITER ) {		
		for (int v = 0; v < V; v++){
			if (!marked.get(v)){
				calcBasketFromBelow(v);
			}
		}
		for (int v = V - 1; v >= 0; v--){
			//StdOut.println("marked = " + marked);
			if (!marked.get(v)){
				calcBasketFromAbove(v);
			}
		}
		/*
		for (Interval iv : intervals) {
			//StdOut.println("tileVector of interval " + iv + " is " + iv.tileVector);
			StdOut.println("Basket of interval " + iv + " is " + iv.tileSet);
		}
		*/
		uncomp = 0;
		for (int v = 0; v < V; v++) {
			if (!marked.get(v)){
				uncomp++;
			}
		}
		count++;
		}
	}
	
	/**
	 * Calculate the set of tiles ("basket") that intersect a given interval assuming that the baskets of intervals
	 * above and perhaps at the same level are known. 
	 * 
	 * @param i the index of the interval in the interval graph
	 * @return true if the calculation was successful
	 */
	
	private boolean calcBasketFromAbove(int i) {
		Interval iv = intervals.get(i);
		//StdOut.println("Doing Calculation for interval " + iv);
		if (marked.get(i)) {
			StdOut.println("Warning: the order of tiles at interval = " + iv + " has already been calculated.");
		}
		TreeSet<Integer> above = new TreeSet<Integer>();
		TreeSet<Integer> atLevel = new TreeSet<Integer>();
        
		//here we are figuring out the sets "above" and "atLevel"
		for (int u : G.adj(i)) {
			if (intervals.get(u).level > iv.level) {
				above.add(u);
			}
		}
		for (int u : above) {
			for (int w: G.adj(u)) {
				if (w != i && intervals.get(w).level == iv.level) {
					atLevel.add(w);
				}
			}
		}
		if (iv.level % 2 == 0) { //integer interval, so it can only be a join of half-integer intervals.
			                     //in particular vertices in atLevel do not matter
			//the case when level of iv is integer and we approach from above:  
			//we need to combine the sets of dots corresponding to adjacent intervals above. 
			// nothing is created but the tiles at level l should be dropped. 
			int level = iv.level/2;
			for (int u: above) {
				if (! marked.get(u)) {
					//StdOut.println("the interval " + intervals.get(u) + " above has not been calculated");
					return false;
				} else {
					Interval iu = intervals.get(u);
					for (int t : iu.tileSet) {
						if (tile2level.get(t) < level) {
						   iv.tileSet.add(t);
						}
					}							
				}
			}
			//StdOut.println("Calculation for interval " + iv + "has been succesfull");
			marked.set(i, true); 
		} else { //half-integer interval. If atLevel is empty everything is relatively simple. This interval 
			      // at level l + 1/2 is 
			     //homotopic to the interval at level l + 1. So the only thing to handle is tiles
			     //at level l - n that should be added to the basket of i. 
			//StdOut.println("Calculation for half - integer interval " + iv);
			int level = (iv.level - 1)/2;
			for (int u: above) { 
				if (! marked.get(u)) {
					//StdOut.println("the value at the interval" + intervals.get(u) + "above is not calculated yet.");
					return false;
				} else {
					Interval iu = intervals.get(u);
					for (int t : iu.tileSet) {
						   iv.tileSet.add(t);
					}							
				}
			} // now we need to add tiles at level l - n. 
			// we will get all intervals at level l + 1/2. find the number of tiles at level
			// l - n which intersect the intervals to the left of the given one. Then we add
			// an appropriate number of tiles at level l - n to the basket. 
			if (level - n >= 0) {
			   int start = levelStarts.get(level - n);
			   for (Interval intvl : intervals) {
				   if (intvl.level == iv.level && intvl.a < iv.a) {
					   start = start + intvl.tileVector.get(level - n);
				   }						
			   }
			   for (int t = start; t < start + iv.tileVector.get(level - n); t++) {
				   iv.tileSet.add(t);
			   }
			}
			// now we are going to handle the vertices in atLevel set
			for (int w: atLevel) {
					//StdOut.println("processing atLevel vertex with interval " + intervals.get(w));
					if (!marked.get(w)) {
						//StdOut.println("This interval has not been yet processed.");
						iv.tileSet.removeAll(iv.tileSet);
						return false;
					} else { //we need to remove tiles from the basket.
						Interval iw = intervals.get(w);
						for (int t: iw.tileSet) {
							iv.tileSet.remove(t);
						}
					}
				}
			//StdOut.println("Calculation for interval " + iv + "has been succesfull");
			marked.set(i, true);
		}
		return true;
	}

	
	/**
	 * Calculate the set of tiles ("basket") that intersect a given interval assuming that the baskets of intervals
	 * below and perhaps at the same level are known. 
	 * 
	 * @param i the index of the interval in the interval graph
	 * @return true if the calculation was successful
	 */
	
	private boolean calcBasketFromBelow(int i) {
		Interval iv = intervals.get(i);
		if (marked.get(i)) {
			StdOut.println("Warning: the order of tiles at interval = " + iv + " has already been calculated.");
		}
		TreeSet<Integer> below = new TreeSet<Integer>();
		TreeSet<Integer> atLevel = new TreeSet<Integer>();
        
		//here we are figuring out the sets "below" and "atLevel"
		for (int u : G.adj(i)) {
			if (intervals.get(u).level < iv.level) {
				below.add(u);
			}
		}
		for (int u : below) {
			for (int w: G.adj(u)) {
				if (w != i && intervals.get(w).level == iv.level) {
					atLevel.add(w);
				}
			}
		}
		if (iv.level % 2 == 0) { //integer interval, so it can only be a join of half-integer intervals.
			                     //in particular vertices in atLevel do not matter 
			int level = iv.level/2;
			for (int u: below) {
				if (! marked.get(u)) {
					//StdOut.println("the value at the interval" + ig.intervals.get(u) + "below is not calculated yet.");
					return false;
				} else {
					Interval iu = intervals.get(u);
					for (int t : iu.tileSet) {
						if (tile2level.get(t) >= level - n) {
						   iv.tileSet.add(t);
						}
					}							
				}
			}
			marked.set(i, true);
		} else { //half-integer interval. If atLevel is empty everything is relatively simple. This interval 
			      // at level l + 1/2 is 
			     //homotopic to the interval at level l. So the only thing to handle is tiles
			     //at level l that should be added to the basket of i. 
			int level = (iv.level - 1)/2;
			for (int u: below) { 
				if (! marked.get(u)) {
					return false;
				} else {
					Interval iu = intervals.get(u);
					for (int t : iu.tileSet) {
						   iv.tileSet.add(t);
					}							
				}
			} // now we need to add tiles at level l. 
			// we will get all intervals at level l + 1/2. find the number of tiles at level
			// l which intersect the intervals to the left of the given one. Then we add
			// an appropriate number of tiles at level l to the basket. 
			int start = levelStarts.get(level);
			   for (Interval intvl : intervals) {
				   if (intvl.level == iv.level && intvl.a < iv.a) {
					   start = start + intvl.tileVector.get(level);
				   }						
			   }
			   for (int t = start; t < start + iv.tileVector.get(level); t++) {
				   iv.tileSet.add(t);
			   }
			// now we are going to handle the vertices in atLevel set
			for (int w: atLevel) {
					//StdOut.println("processing atLevel vertex for iv = " + iv + "with interval " + intervals.get(w));
					if (!marked.get(w)) {
						//StdOut.println("This interval has not been yet processed.");
						iv.tileSet.removeAll(iv.tileSet);
						return false;
					} else { //we need to remove tiles from the basket.
						Interval iw = intervals.get(w);
						for (int t: iw.tileSet) {
							iv.tileSet.remove(t);
						}
					}
				}
			marked.set(i, true);
		}
		return true;
	}
	
    private void calcTile2Intervals() {
    	tile2intervals = new ArrayList<TreeSet<Interval>>();
    	//initialization
    	for (int t = 0; t < T; t++) {
    		tile2intervals.add(new TreeSet<Interval>());
    	}
    	for (Interval iv: intervals) {
    		for (int t: iv.tileSet) {
    			tile2intervals.get(t).add(iv);
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
		/*
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
		 */


		//test case 3
		/*
		int n = 3;
		int M = 4;
		int N = 3;
		XRibTiling region = XRibTiling.stair(n, M, N);
		XShape shape = region.shape;
		 */
		
		
		//test case 4
		int n = 2;
		int r = 1;
		for (int i = 0; i < r + 1; i ++){
		shapeI.add(r - i);
		shapeF.add(r + i);
		}
		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);	
		shapeI.clear();
		shapeF.clear();
		for (int i = 0; i < r + 1; i ++){
		shapeI.add(3 * r + 1 - i);
		shapeF.add(3 * r + 1 + i);
		}
		TreeSet<Square> bag2 = XUtility.shape2bag(shapeI, shapeF);	
		bag.addAll(bag2);
		
		//test case 5 (non-simply connected region)
		/*
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
		for (int i = 1; i < 4; i++) {
		s = new Square(2, i);
		bag.add(s);		
		}
		s = new Square(1, 3);
		bag.add(s);
		s = new Square(3, 2);
		bag.add(s);
		s = new Square(3, 3);
		bag.add(s);
		
		*/
			
		XShape shape = new XShape(bag);

		shape.draw();
		int level = 9;
		shape.drawLevel(level);
		
		
		
		StdOut.println("Lmax = " + shape.Lmax);
		StdOut.println("Size of weakCrosses = " + shape.weakCrosses.size());
        
		
		IntervalGraph ig = new IntervalGraph(n, shape);
		
		//this is for test case 5 (non simply-connected region);
		/*
		Square s0 = new Square(1, 1); 
		IntervalGraph ig = new IntervalGraph(n, shape, s0);
		*/

		StdOut.println("level2intervals : ");
		for (int l = 0; l < ig.level2intervals.size(); l++) {
		StdOut.println(ig.level2intervals.get(l));
		}
		
		for (Interval iv : ig.intervals) {
			StdOut.println(iv + " tileVector = " + iv.tileVector);
		}
		
		
		StdOut.println("Structure of tiles, level2tiles: ");
		StdOut.println(ig.level2tiles);
		StdOut.println("The starting points of the levels are :");
		StdOut.println(ig.levelStarts);
		StdOut.println("tile2level structure is :");
		StdOut.println(ig.tile2level);

		ig.draw();
		for (int t = 0; t < ig.T; t++) {
		     StdOut.println("Tile " + t + " is in intervals " + ig.tile2intervals.get(t));
		}
		
		//XGraph xg = new XGraph(n, ig);
	}
}

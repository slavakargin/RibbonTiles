package xrib;
import java.util.ArrayList;
//import java.util.HashSet;
import java.util.TreeSet;
//import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.StdOut;


/**
 *  The {@code XGraph} class represents the basic graph that underlies any 
 *  tiling of a region R.
 *  <p>
 *  This implementation assumes that 
 *  (1) the region is simply connected,
 * 
 *  <p>
 *
 *  @author Vladislav Kargin
 */

public class XGraph {

    MyDigraph DG; 
	ArrayList<Integer> tile2level; //shows the level of each dot (tile) in the digraph DG
	int L; //number of tile levels
	ArrayList<Integer> level2tiles; // number of tiles in each level.
	ArrayList<Integer> levelStarts; // The k-th element in this list shows the index of the first vertex in level k

	int n; //number of squares in a ribbon tile
	int V; //total number of tiles;
	//int createdV; //keep track of how many vertices were created if we use constructor that 
	               //is based on the interval graph. 
	IntervalGraph ig;
    
    
	/**
	 * This constructor creates the graph from a tiling
	 * @param tiling the tiling which we use for construction
	 */
		XGraph(XRibTiling T) {
			int V = T.shape.squares.size()/T.n;
			DG = new MyDigraph(V);
			//calculate the graph edges
			for (int u = 0; u < V; u++) {
				for (int v = 0; v < V; v++) {
					if (v == u) continue; 
					if (T.tiles().get(u).compareWeak(T.tiles().get(v)) > 0) {
						DG.addEdge(u, v);
					} 
				}
			}
		}
		/**
		 * This constructor creates a graph from an interval graph that represents a given region. 
		 * It is assumed that the intersection numbers of tiles with different intervals have already been calculated
		 * @param n the size of ribbon tiles,
		 * @param ig the intervalGraph.
		 */
			XGraph(int n, IntervalGraph ig) {
				this.n = n;
				this.ig = ig;
				V = ig.shape.squares.size()/n; //total number of tiles.
				DG = new MyDigraph(V);
				//initialization 
				
				tile2level = new ArrayList<Integer>(); 				
				L = ig.intervals.get(0).tileVector.size();
				
				
				//calculate how many tiles is in each level.
				level2tiles = new ArrayList<Integer>();
				for (int l = 0; l < L; l++) {
					level2tiles.add(0);
				}
				for (int i = 0; i < ig.intervals.size(); i++) {
					Interval interval = ig.intervals.get(i);
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
					if (level2tiles.get(l) == 0) {
						levelStarts.add(levelStarts.get(l - 1)); // it is a bit of abuse of notation since in fact the start point is null 
						                                         // but it is convenient
					} else {
					    levelStarts.add(levelStarts.get(l - 1) + level2tiles.get(l - 1)); 
					}
				}
				
				// We also need a map tile2level
				tile2level = new ArrayList<Integer>();
				for (int v = 0; v < V; v++) {
					tile2level.add(null);
				}
				for (int l = 0; l < L; l++) {
					for (int i = levelStarts.get(l); i < levelStarts.get(l) + level2tiles.get(l); i++){
						tile2level.set(i, l);
					}
				}	
				
				//we will run over intervals 
				//and figure out the distribution of tiles over baskets of these intervals.
				/*
				for (int i = 0; i < ig.intervals.size(); i++) {
					Interval iv = ig.intervals.get(i);
					if (iv.level % 2 == 0) { // no new tiles should appear hear.
						for (int j : ig.G.adj(i)) {
							if (ig.intervals.get(j).level < iv.level) {
								for (int dot : ig.intervals.get(j).tileSet) {
									
								}								
							}
						}
						
					}
					if (iv.level % 2 == 1) {
						int nNewTiles = iv.tileVector.get((iv.level - 1)/2);
						if (nNewTiles > 0) {
							for (int j = 0; j < nNewTiles; j++) {
								
							}
						}
					}
				}	
				*/
				//reset all vertices of the interval graph to unmarked
				for (int i = 0; i < ig.marked.size(); i++) {
					ig.marked.set(i, false);
				}
				//mark the leaves. 
				for (int i = 0; i < ig.marked.size(); i++) {
					if (ig.G.degree(i) == 1) {
						ig.marked.set(i, true);
					}
				}
			}
			
			/**
			 * Tries to calculate the order of tiles that intersect a given interval assuming that the order of intervals
			 * above and perhaps at the same level is known. The integer represent the interval in the
			 * interval graph. 
			 * 
			 * @param i the index of the interval in the interval graph
			 * @return true if the calculation was successful
			 */
			
			public boolean calcFromAbove(int i) {
				Interval iv = ig.intervals.get(i);
				if (ig.marked.get(i)) {
					StdOut.println("Warning: the order of tiles at interval = " + iv + " has already been calculated.");
				}
				TreeSet<Integer> above = new TreeSet<Integer>();
				TreeSet<Integer> atLevel = new TreeSet<Integer>();
                
				//here we are figuring out the sets "above" and "atLevel"
				for (int u : ig.G.adj(i)) {
					if (ig.intervals.get(u).level > iv.level) {
						above.add(u);
					}
				}
				for (int u : above) {
					for (int w: ig.G.adj(u)) {
						if (w != i && ig.intervals.get(w).level == iv.level) {
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
						if (! ig.marked.get(u)) {
							StdOut.println("Calculation for interval " + iv);
							StdOut.println("the value at the interval" + ig.intervals.get(u) + "above is not calculated yet.");
							return false;
						} else {
							Interval iu = ig.intervals.get(u);
							for (int t : iu.tileSet) {
								if (tile2level.get(t) < level) {
								   iv.tileSet.add(t);
								}
							}							
						}
					}
					ig.marked.set(i, true);
				} else { //half-integer interval. If atLevel is empty everything is relatively simple. This interval 
					      // at level l + 1/2 is 
					     //homotopic to the interval at level l + 1. So the only thing to handle is tiles
					     //at level l - n that should be added to the basket of i. 
					int level = (iv.level - 1)/2;
					for (int u: above) { 
						if (! ig.marked.get(u)) {
							StdOut.println("Calculation for interval " + iv);
							StdOut.println("the value at the interval" + ig.intervals.get(u) + "above is not calculated yet.");
							return false;
						} else {
							Interval iu = ig.intervals.get(u);
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
					   for (Interval intvl : ig.intervals) {
						   if (intvl.level == iv.level && intvl.a < iv.a) {
							   start = start + intvl.tileVector.get(level - n);
						   }						
					   }
					   for (int t = start; t < start + iv.tileVector.get(level - n); t++) {
						   iv.tileSet.add(t);
					   }
					}
					ig.marked.set(i, true);
				}
				return true;
			}
	
			
			/**
			 * Tries to calculate the order of tiles that intersect a given interval assuming that the order of intervals
			 * below and perhaps at the same level is known. The integer represent the interval in the
			 * interval graph. 
			 * 
			 * @param i the index of the interval in the interval graph
			 * @return true if the calculation was successful
			 */
			
			public boolean calcFromBelow(int i) {
				Interval iv = ig.intervals.get(i);
				if (ig.marked.get(i)) {
					StdOut.println("Warning: the order of tiles at interval = " + iv + " has already been calculated.");
				}
				TreeSet<Integer> below = new TreeSet<Integer>();
				TreeSet<Integer> atLevel = new TreeSet<Integer>();
                
				//here we are figuring out the sets "below" and "atLevel"
				for (int u : ig.G.adj(i)) {
					if (ig.intervals.get(u).level < iv.level) {
						below.add(u);
					}
				}
				for (int u : below) {
					for (int w: ig.G.adj(u)) {
						if (w != i && ig.intervals.get(w).level == iv.level) {
							atLevel.add(w);
						}
					}
				}
				if (iv.level % 2 == 0) { //integer interval, so it can only be a join of half-integer intervals.
					                     //in particular vertices in atLevel do not matter 
					int level = iv.level/2;
					for (int u: below) {
						if (! ig.marked.get(u)) {
							StdOut.println("the value at the interval" + ig.intervals.get(u) + "above is not calculated yet.");
							return false;
						} else {
							Interval iu = ig.intervals.get(u);
							for (int t : iu.tileSet) {
								if (tile2level.get(t) >= level - n) {
								   iv.tileSet.add(t);
								}
							}							
						}
					}
					ig.marked.set(i, true);
				} else { //half-integer interval. If atLevel is empty everything is relatively simple. This interval 
					      // at level l + 1/2 is 
					     //homotopic to the interval at level l. So the only thing to handle is tiles
					     //at level l that should be added to the basket of i. 
					int level = (iv.level - 1)/2;
					for (int u: below) { 
						if (! ig.marked.get(u)) {
							StdOut.println("the value at the interval" + ig.intervals.get(u) + "above is not calculated yet.");
							return false;
						} else {
							Interval iu = ig.intervals.get(u);
							for (int t : iu.tileSet) {
								   iv.tileSet.add(t);
							}							
						}
					} // now we need to add tiles at level l. 
					// we will get all intervals at level l + 1/2. find the number of tiles at level
					// l which intersect the intervals to the left of the given one. Then we add
					// an appropriate number of tiles at level l to the basket. 
					int start = levelStarts.get(level);
					   for (Interval intvl : ig.intervals) {
						   if (intvl.level == iv.level && intvl.a < iv.a) {
							   start = start + intvl.tileVector.get(level);
						   }						
					   }
					   for (int t = start; t < start + iv.tileVector.get(level); t++) {
						   iv.tileSet.add(t);
					   }
					ig.marked.set(i, true);
				}
				return true;
			}
			
			
			
	/**
	 * For testing methods
	 * @param args
	 */

	public static void main(String[] args) {
		
		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();
		
		/* Test case 1
		 * 	
		 */	
		int n = 4;
		int N = 6; 
		for (int i = 0; i < N; i ++){
		shapeI.add(0);
		shapeF.add(7);
		}
		for (int i = 0; i < N/2; i++){
			shapeF.set(i, 3);
		}
		

		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);
		XRibTiling xrt = new XRibTiling(n, bag, "Test");
		xrt.draw();
		StdOut.print(xrt.xG.DG);
		

		XRibTiling xrtCopy = new XRibTiling(xrt);
		
		/*
		XRibTile t3 = new XRibTile(2, 2, "111");
		XRibTile t4  = new XRibTile(3, 2, "111");
		xrtCopy.flip(t3, t4);
		xrtCopy.draw();
		StdOut.print(xrtCopy.xG.DG);	
		XRibTile t5 = new XRibTile(2, 3, "011");
		XRibTile t6  = new XRibTile(2, 2, "110");
		xrtCopy.flip(t5, t6);
		//xrtCopy.draw();
		StdOut.print(xrtCopy.xG.DG);
		*/
		
		int ITER = 100;
		XUtility.Glauber(xrtCopy,ITER);
		xrtCopy.draw();
		StdOut.print(xrtCopy.xG.DG);
	}
}

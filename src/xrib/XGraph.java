package xrib;
import java.util.ArrayList;
//import java.util.HashSet;
import java.util.TreeSet;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;



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


	int n; //number of squares in a ribbon tile
	int V; //total number of tiles;
	//int createdV; //keep track of how many vertices were created if we use constructor that 
	//is based on the interval graph. 
	IntervalGraph ig;
	XRibTiling tiling; //associated tiling 
	private ArrayList<Boolean> marked; //we will use it when we calculate the completion of the graph 

	private class Edge{
		int u;
		int v;
		Edge(int u, int v) {
			this.u = u;
			this.v = v;
		}
		/**
		 * Returns a string representation of this edge.
		 *
		 * @return a string representation of this edge, using the format
		 *         {@code [u, v]}
		 */
		@Override
		public String toString() {
			return "[" + u + ", " + v + "]";
		}
	}

	ArrayList<Edge> edges; //possible edges in the graph (correspond to pairs of comparable tiles which can be exchanged)

	/**
	 * This constructor creates the graph from a tiling
	 * @param tiling the tiling which we use for construction
	 */
	XGraph(XRibTiling T) {
		this.tiling = T;
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
	 * This constructor creates an Xgraph from a shape of a region. It proceeds by calculating first 
	 * an interval graph that represents the given region, and 
	 * it is assumed that the intersection numbers of tiles with different intervals have already been calculated
	 * @param n the size of ribbon tiles,
	 * @param shape the shape of the region.
	 */
	//XGraph(IntervalGraph ig) {
	XGraph(int n, XShape shape) {
		this.n = n;
		ig = new IntervalGraph(n, shape);
		V = ig.shape.squares.size()/n; //total number of tiles.
		DG = new MyDigraph(V);
		//initialization 
		marked = new ArrayList<Boolean>();
		//We need to add edges between tiles at the same level. 
		for (int l = 0; l < ig.L; l++) {
			if (ig.level2tiles.get(l) > 0) {
				for (int t = ig.levelStarts.get(l); 
						t < ig.levelStarts.get(l) + ig.level2tiles.get(l) - 1; t++) {
					DG.addEdge(t + 1, t);
				}
			}
		}
		//next we need to add more edges using the structure of the IntervalGraph
		//All these edges are forced, so we don't include it in the array edges
		//First, simple thing, adding edges for vertices in different intervals.
		for (int l = 0; l < 2 * ig.L - 1; l++) {
			ArrayList<Interval> intSet = ig.level2intervals.get(l);
			int M = intSet.size();
			if (M > 1) {
				for (int i = 0; i < M - 1; i++) {
					for (int j = i + 1; j < M; j++) {
						Interval iv1 = intSet.get(i);
						Interval iv2 = intSet.get(j);         			  
						if (iv1.a < iv2.a) {
							for (int s : iv1.tileSet) {
								for (int t: iv2.tileSet) {
									if(!DG.isEdge(t, s)) {
										DG.addEdge(t, s);
									}
								}
							}
						} else {
							for (int s : iv1.tileSet) {
								for (int t: iv2.tileSet) {
									if(!DG.isEdge(s, t)) {
										DG.addEdge(s, t);
									}
								}
							}
						}
					}
				}
			}
		}
		//The second thing is more subtle. We should add the edges between intervals that differ by n
		//even if they are in the same interval. for this we need to analyze what is going on with the height
		// on the border of two homotopic intervals. 
		for (int l = n; l < ig.L - 1; l++) {
			ArrayList<Interval> intSet = ig.level2intervals.get(2 * l);
			//int M = intSet.size();
			for (Interval iv : intSet) { //we need to get a homotopic interval above.
				int v = ig.intervals.indexOf(iv);
				for (int u : ig.G.adj(v)) {
					Interval iu = ig.intervals.get(u);
					if (iu.level == 2 * l + 1) { //this is the homotopic interval above.
						ArrayList<Integer> lowTiles = new ArrayList<Integer>();
						ArrayList<Integer> upTiles = new ArrayList<Integer>();
						for (int t : iu.tileSet) { //we need to add edges t_{c - n, i} <- t_{c, i} <- t_{c - n, j} ...
							//however, this is not so simple. First, we need to form two sets of tiles.
							if (ig.tile2level.get(t) == l - n) {
								lowTiles.add(t);
							} else if (ig.tile2level.get(t) == l) {
								upTiles.add(t);
							}                   					
						}
						if(iu.leftH.get(l % n) > iv.leftH.get(l % n)) {
							for (int i = 0; i < lowTiles.size(); i++) {
								if (i < upTiles.size()) {
									DG.addEdge(upTiles.get(i), lowTiles.get(i));
									//StdOut.println("Adding edge from " + upTiles.get(i) + " to " +  lowTiles.get(i));
									if (i + 1 < lowTiles.size()) {
										DG.addEdge(lowTiles.get(i+1), upTiles.get(i));
									}
								}  
							}
						} else {
							for (int i = 0; i < upTiles.size(); i++) {
								if (i < lowTiles.size()) {
									DG.addEdge(lowTiles.get(i), upTiles.get(i));
									/*StdOut.println("left height at interval " + iu + " is " + iu.leftH);
                             				StdOut.println("left height at interval " + iv + " is " + iv.leftH);
                             				StdOut.println("Adding edge from " + lowTiles.get(i) + " to " +  upTiles.get(i));
									 */
									if (i + 1 < upTiles.size()) {
										DG.addEdge(upTiles.get(i+1), lowTiles.get(i));
									}
								}                            			
							}
						}
					}
				}
			}
		}
		buildTiling();
		tiling.xG = this;	
		completeGraph();
		//StdOut.println("tiling.xG.tiling = " + tiling.xG.tiling.tiles());		
	}
    
	/**
	 * copy constructor.
	 */
	
	XGraph(XGraph xg) {
		this.DG = new MyDigraph(xg.DG);
		this.n = xg.n;
		this.V = xg.V;
		this.tiling = new XRibTiling(xg.tiling);
		this.tiling.xG = this;
	}

    /**
     * Compares this Xgraph instance to another and returns true if they are the same.
     * It is assumed that tilings are for the same region and the ribbon tiles have the same number of squares.
     * 
     * The equality happens only if tilings of both Xgraphs coincide. 
     *
     * @param  other the other xgraph
     * @return {@code true} if this xgraph equals {@code other};
     *         {@code false} otherwise
     */
	@Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        XGraph that = (XGraph) other;
        if (this.tiling.equals(that.tiling)) {
        	return true;
        }
		return false;
	}
	
    /**
     * Returns an integer hash code for this xgraph. The hash code equals to the hash code of the tiling
     * 
     * @return an integer hash code for this xgraph
     */
    @Override
    public int hashCode() {
    	return this.tiling.hashCode();
    }
	
	/**
	 * tests the existence of a cycle in the graph
	 * @return
	 */

	public Boolean hasCycle() {
		DirectedCycle dc = new DirectedCycle(DG.DG());
		return dc.hasCycle();
	}
	
	/**
	 * tests if two vertices (tiles) are comparable and the edge is free (potentially possible to flip)
	 * @param u one vertex
	 * @param v another vertex
	 * @return true if the vertices are comparable and the edge is potentially possible to flip
	 */
	public boolean isFreeAndComparable(int u, int v) {
		int lu = ig.tile2level.get(u);
		int lv = ig.tile2level.get(v);
		if ((lu - lv >= n) || (lu - lv <= - n) || (lu == lv)) {
		return false;
		}
		return true;
	}

	// Make sure that all comparable vertices are connected by an edge
	private void completeGraph() {
	   for (int i = 0; i < V; i++) {
		marked.add(false);
	   }
	    ArrayList<Integer> sinkSeq = XUtility.findSinkSequence(DG);
		 for (int i = 0; i < sinkSeq.size() - 1; i++) {
		      int v = sinkSeq.get(i);	
		      int lv = ig.tile2level.get(v);
		      for (int j = i + 1; j < sinkSeq.size(); j++){
		    	  int w = sinkSeq.get(j);
		    	  int lw = ig.tile2level.get(w);
		    	  if ((lv - lw > 0 && lv - lw <= n) || (lv - lw < 0 && lv - lw >= -n)) {
					  if(!DG.isEdge(w, v)) {
						     DG.addEdge(w, v);
						  }
		    	  }
		      }
		 }
	}
	
	
	//calculates a tiling consistent with the orientation in DG. 
	private void buildTiling() {
		buildTiling(V);
	}
	/**
	 * calculates the first k titles of a tiling.
	 * we will assume that level2intervals in interval graph list intervals from left to right. 
	 * @param k
	 */
	private void buildTiling(int k) {

		//
		int v, l0;
		Square s0 = null;
		XRibTile tile; 
		TreeSet<Square> bag = new TreeSet<Square>(ig.shape.squares); //as we build the tiling we will remove the 
		//squares from the bag.
		//completeGraph();
		ArrayList<Integer> sinkSeq = XUtility.findSinkSequence(DG);
		tiling = new  XRibTiling(n, ig.shape.squares);
		for (int count = 0; count < k; count++) {
			v = sinkSeq.get(count);	
			l0 = ig.tile2level.get(v);
			// we need to find a root square for this tile. 
			//For this we look for the smallest squares at level l0 in our bag.
			//As we go by we need to check that they are close to each other. 
			//find the first ("root") square
			for (Square s : bag) {
				if (s.x + s.y == l0) {
					s0 = new Square(s); //this is the desired root of the tile. 
					break;
				}
			}
			//StdOut.println("The root square for tile " + v + " is square " + s0);
			//get the tile with the root at s0 and on the left border
			tile = XUtility.getBorderTile(s0, n, bag);
			bag.removeAll(tile.squares());
			tiling.addTile(v, tile);
		}
		tiling.xG = this; 
	}	
	/**
	 * Creates a random acyclic orientation on the graph. 
	 * The code works only if the XGraph was created from interval graph, so that 
	 * we have variables available
	 * tile2level; //shows the level of each dot (tile) in the digraph DG
	 * level2tiles; // number of tiles in each level.
	 * levelStarts; // tile that starts each level
	 * 
	 */

	public void randomOrientation() {
		//We are going to go over all possible edges and set their orientations in a random fashion, 
		//so we would get a random orientation of a graph.
		//Later, we will convert it to a random tiling
		//first we need to enumerate all possible edges (which means all comparable pairs of tiles)
		edges = new ArrayList<Edge>();
		int E; //number of free edges;
		for (int u = 0; u < V - 1; u++) {
			int lu = ig.tile2level.get(u);
			for (int v = u + 1; v < V; v++) {
				int lv = ig.tile2level.get(v);
				if ((lu - lv < n) && (lv - lu < n) && (lu != lv)) {
					edges.add(new Edge(u, v));
				}
			}
		}
		E = edges.size();
		ArrayList<Boolean> markedEdges = new ArrayList<Boolean>(E);
		for (int i = 0; i < E; i++) {
			markedEdges.add(false);
		}
		int countEdges = 0;
		while (countEdges < E) {
			int e = StdRandom.uniform(E);
			if (!markedEdges.get(e)) {
				Edge edge = edges.get(e);
				//StdOut.println(edge);
				//Let us try to set an orientation on e;
				double coin = StdRandom.uniform();
				if (coin < 1/2) {
					DG.addEdge(edge.u, edge.v);
					if (hasCycle()) {
						DG.removeEdge(edge.u, edge.v);
						DG.addEdge(edge.v, edge.u);
					}
				} else {
					DG.addEdge(edge.v, edge.u);
					if (hasCycle()) {
						DG.removeEdge(edge.v, edge.u);
						DG.addEdge(edge.u, edge.v);
					}
				}
				markedEdges.set(e, true);
				countEdges++;
			}
		}
		buildTiling();
	}

	/**
	 * For testing methods
	 * @param args
	 */

	public static void main(String[] args) {

		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();

		/* Test case 1
		 * 	This is a test for the constructor that is based on an existing tiling
		 */	
		/*
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

		int ITER = 100;
		XUtility.Glauber(xrtCopy,ITER);
		xrtCopy.draw();
		StdOut.print(xrtCopy.xG.DG);
		 */

		/* Test case 2
		 * This is a test for the constructor that works on the basis of a simply connected shape and
		 * aims to calculate the Sheffield graph and the associated tiling or prove that the tiling
		 * does not exist.
		 */
		//test case 2a (complex case)
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
				Square s = new Square(10 + j, i);
				bag.add(s);
			}
		}	
		Square s = new Square(0, 12);
		bag.add(s);		
		s = new Square(1, 12);
		bag.add(s);
		XShape shape = new XShape(bag);
		 */
		//test case 2b (simple case)
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
		//test case 2c (another simple case: rectangle, Aztec Diamond, stair or down-stair)
        
		int k = 5;
		int n = 3;
		int M = n * k;
		int N = n * k;

		
		//XRibTiling region = XRibTiling.aztecDiamond(N);
		XRibTiling region = XRibTiling.rectangle(n, M, N);
		//XRibTiling region = XRibTiling.stair(n, M, N);
		//XRibTiling region = XRibTiling.downStair(n, M, N);
		XShape shape = region.shape;
		IntervalGraph ig = new IntervalGraph(n, shape);
		ig.draw();
		StdOut.println("Structure of tiles, level2tiles: ");
		StdOut.println(ig.level2tiles);
		StdOut.println("tile2level structure is :");
		StdOut.println(ig.tile2level);
        

		// test case 3: a simple example of an un-tileable region. 
		/*		
		int n = 3; 
		shapeI.add(1);
		shapeF.add(1);
		shapeI.add(1);
		shapeF.add(3);
		shapeI.add(0);
		shapeF.add(1);
		TreeSet<Square> bag = XUtility.shape2bag(shapeI, shapeF);
		XShape shape = new XShape(bag);
		 */
		/*
		shape.draw();
		int level = 2;
		shape.drawLevel(level);
        */
		
		//test case 4: an analog of the Aztec diamond for 3-ribbon tiles
		/* n = 3;
		N = 4;
		XShape shape = XShape.aztecRibbon(n, N);
		//StdOut.println(shape.squares);
		//shape.draw();
		
		IntervalGraph ig = new IntervalGraph(n, shape);
		ig.draw();
		StdOut.println("Structure of tiles, level2tiles: ");
		StdOut.println(ig.level2tiles);
		StdOut.println("tile2level structure is :");
		StdOut.println(ig.tile2level);
		
        
		XGraph xg = new XGraph(n, shape);
		//StdOut.println("xG.tiling.xG = " + xg.tiling.xG);
		xg.randomOrientation();
		xg.tiling.draw();
		int ITER = 30000;
		XUtility.Glauber(xg.tiling, ITER);
		StdOut.println("The digraph is ");
		StdOut.println(xg.DG);
		StdOut.println("Has cycle? : " + xg.hasCycle());
		xg.tiling.draw();
		*/
		/*
		n = 4;
		N = 32;
		XUtility.HeightPlot(n, N);
		*/
		//Let us see how the distance from the standard orientations changes when we run the random walk
		XGraph xg1 = new XGraph(n, shape);
		XGraph xg2 = new XGraph(n, shape);
		int ITER = 10000;
		ArrayList<Integer> distances = new ArrayList<Integer>(ITER);
		int dist = XUtility.distance(xg1, xg2);
		distances.add(dist);
		for (int i = 0; i < ITER; i++) {
			if (i % 1000 == 0 & i > 0) {
				StdOut.println("Iteration " + i);
			}
		    XUtility.Glauber(xg2.tiling, 1);
			dist = XUtility.distance(xg1, xg2);
			distances.add(dist);
			//StdOut.println("dist = " + dist);
		}
		XUtility.plot(distances);
		xg2.tiling.draw();

	}
}

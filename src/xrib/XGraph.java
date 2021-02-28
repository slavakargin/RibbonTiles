package xrib;
import java.util.ArrayList;
//import java.util.HashSet;
import java.util.TreeSet;

import edu.princeton.cs.algs4.Digraph;
//import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Draw;
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

	MyDigraph DG; //digraph
	int n; //number of squares in a ribbon tile
	XShape shape;
	int V; //total number of tiles;
	//int createdV; //keep track of how many vertices were created if we use constructor that 
	//is based on the interval graph. 
	final IntervalGraph ig;
	//ArrayList<Integer> tile2level; //this duplicates info in the interval graph ig, 
	                               //but sometimes we will not have ig around and 
	                               //the actual tiles are not yet calculated
	                               //so it might be useful to have this around
	                               
	XRibTiling tiling; //associated tiling 
	Draw dr;
	//private ArrayList<Boolean> marked; //we will use it when we calculate the completion of the graph (???)

	class Edge{
		int u;
		int v;
		Edge(int u, int v) {
			this.u = u;
			this.v = v;
		}
		
	    /**
	     * Compares this edge to another edge and returns true if they are the same,
	     * which happens only if this.u = other.u, this.v = other.v
	     *
	     * @param  other the other  edge
	     * @return {@code true} if this edge equals {@code other};
	     *         {@code false} otherwise
	     */
	    @Override
	    public boolean equals(Object other) {
	        if (other == this) return true;
	        if (other == null) return false;
	        if (other.getClass() != this.getClass()) return false;
	        Edge that = (Edge) other;
	        if (this.u != that.u) return false;
	        if (this.v != that.v) return false;
	        return true;
	    }

	    /**
	     * Returns an integer hash code for this edge. The hash code 
	     * depends only on u and v
	     * @return an integer hash code for this edge
	     */
	    @Override
	    public int hashCode() {
	        return 1299721 * u + v;
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
    
	ArrayList<Edge> forcedEdges;
	ArrayList<Edge> edges; //possible edges in the graph 
	                      //(correspond to pairs of comparable tiles which can be exchanged)

	/**
	 * This constructor creates the graph from a tiling
	 * @param tiling the tiling which we use for construction
	 */
	XGraph(XRibTiling T) {
		this.tiling = T;
	    this.n = T.n;
		this.shape = T.shape;
		this.ig = new IntervalGraph(n, shape);
		this.V = shape.squares.size()/n;
		DG = new MyDigraph(V);
		forcedEdges = new ArrayList<Edge>();
		edges = new ArrayList<Edge>();
		//calculate the graph edges which are forced
		for (int u = 0; u < V; u++) {
			for (int v = 0; v < V; v++) {
				XRibTile a = T.tiles().get(u);
				if (v == u) continue; 
				XRibTile b = T.tiles().get(v);
				if (a.compareWeak(b) > 0) {
					if (a.level - b.level == 0 || a.level - b.level == n || a.level - b.level == -n) {
					       DG.addEdge(u, v); //add forced edge;
					       forcedEdges.add(new Edge(u, v));
					}
				} 
			}
		}
		reduceForced(); //remove redundant forced edges.
		
		//calculate the graph edges which are free
		for (int u = 0; u < V; u++) {
			for (int v = 0; v < V; v++) {
				XRibTile a = T.tiles().get(u);
				if (v == u) continue; 
				XRibTile b = T.tiles().get(v);
				if (a.compareWeak(b) > 0) {
					if (a.level - b.level != 0 && a.level - b.level != n && a.level - b.level != -n) {
						   //StdOut.println("Adding free edge " + u + " and " + v);
						   //StdOut.println("With Levels " + a.level + " and " + b.level);
					       DG.addEdge(u, v); //add free edge;
					       edges.add(new Edge(u,v));
					}
				} 
			}
		}
		reduceFree();
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
		this.shape = shape;
		ig = new IntervalGraph(n, shape);
		//StdOut.println("Interval Graph: " + ig.shape.squares);	
		//StdOut.println("Interval Graph: " + ig.level2tiles);	
		V = ig.shape.squares.size()/n; //total number of tiles.
		DG = new MyDigraph(V);
		//initialization 
		//marked = new ArrayList<Boolean>();
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
		buildDefaultGraph();
		//StdOut.println("Graph created: " + DG);	
		tiling = XRibTiling.fromXGraph(n, this);
		tiling.buildTilingFromXG();
		XGraph aux = new XGraph(tiling);
		forcedEdges = new ArrayList<Edge>(aux.forcedEdges);
		edges = new ArrayList<Edge>(aux.edges);
			
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
		this.ig = xg.ig; //note that we do not create a new interval graph. 
		                 //since we are not going to modify it. 
		this.shape = new XShape(xg.shape);
		this.forcedEdges = new ArrayList<Edge>(xg.forcedEdges);
		this.edges = new ArrayList<Edge>(xg.edges);
	}
	
	/**
	 * Takes an existing XGraph xg and modifies it according to the 
	 * tilings sequence ts
	 * 
	 * @param xg Xgraph to modify
	 * @param ts tiling sequence
	 */
	
	static public void fromTilingSequence(XGraph xg, ArrayList<Integer> ts) {
		
	}
	
	
	/**
	 * creates a random graph from the shape using a variant 
	 * of random flip algorithm
	 * 
	 * @param n size of ribbons 
	 * @param shape the shape on which the graph is based
	 * @param ITER  number of iterations in the flip algorithm
	 * @return a random XGraph that corresponds to a tiling of the shape.
	 */
	static public XGraph random(int n, XShape shape, int ITER) {	
		XGraph xG = new XGraph(n, shape);
		for (int count = 0; count < ITER; count++) {
			int r = StdRandom.uniform(xG.edges.size());
		    Edge e = xG.edges.get(r);
		    xG.flip(e, false); //false means no update of tiling happens 
		}
		xG.updateTiling();
		return xG;
	}
	
	
	/**
	 * creates a random graph from the shape using a variant 
	 * of random flip algorithm which gives a bias to the direction of 
	 * edges.
	 * 
	 * @param n size of ribbons 
	 * @param shape the shape on which the graph is based
	 * @param ITER  number of iterations in the flip algorithm
	 * @param bias  the parameter between -1 and 1 that determines the 
	 * bias of the algorithm. 
	 * @return a random XGraph that corresponds to a tiling of the shape.
	 */
	static public XGraph randomBiased(int n, XShape shape, 
			              int ITER, double bias) {
		XGraph xG = new XGraph(n, shape);
		for (int count = 0; count < ITER; count++) {
			int r = StdRandom.uniform(xG.edges.size());
		    Edge e = xG.edges.get(r);
		    xG.biasedFlip(e, bias);  
		}
		xG.updateTiling();
		return xG;
	}
	
	/**
	 * Creates a random XGraph corresponding to tiling of  a rectangle 
	 * with width M and length N.
	 * 
	 * 
	 * @param n length of ribbon
	 * @param M width (``height'')
	 * @param N length 
	 * @param ITER number of iterations in the algorithm
	 * @return random XGraph corresponding to this rectangular shape.
	 */		
	public static XGraph randomRectangle(int n, int M, int N, int ITER) {
		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();
        for (int i = 0; i < M; i++) {
        	shapeI.add(0);
        	shapeF.add(N - 1);
        }
		TreeSet<Square> squares = XUtility.shape2bag(shapeI, shapeF);
		XShape shape = new XShape(squares);		
		
		if (shape.squares.size() % n != 0) {
			StdOut.println("An error in XGraph constructor.");
			StdOut.println("The number of squares in the shape is " + shape.squares.size()  
			                + ". It must be divisible by " + n);
			StdOut.println("Quitting ...");
		}
		
		boolean test = shape.isTileable(n);
		if (!test) {
			StdOut.println("The region is not tileable. The numbers of squares of different colors are not the same.");
			int[] count = shape.countColoredSquares(n);
			StdOut.println("The count is ");
			for (int i = 0; i < n; i++) {
				StdOut.print(count[i] + "; ");
			}
			StdOut.println();
		};
		XGraph xG = random(n, shape, ITER);
		return xG;
	}
	
	/**
	 * Creates a random XGraph corresponding to tiling of  a rectangle 
	 * with width M and length N. The tiling is biased with the amount of 
	 * bias determined by the parameter bias. Bias = 0 should be equivalent to
	 * no bias. 
	 * 
	 * 
	 * @param n length of ribbon
	 * @param M width (``height'')
	 * @param N length 
	 * @param ITER number of iterations in the algorithm
	 * @param bias a parameter between -1 and 1. 
	 * @return random XGraph corresponding to this rectangular shape.
	 */		
	public static XGraph randomBiasedRectangle(int n, int M, int N, int ITER, double bias) {
		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();
        for (int i = 0; i < M; i++) {
        	shapeI.add(0);
        	shapeF.add(N - 1);
        }
		TreeSet<Square> squares = XUtility.shape2bag(shapeI, shapeF);
		XShape shape = new XShape(squares);		
		
		if (shape.squares.size() % n != 0) {
			StdOut.println("An error in XGraph constructor.");
			StdOut.println("The number of squares in the shape is " + shape.squares.size()  
			                + ". It must be divisible by " + n);
			StdOut.println("Quitting ...");
		}
		
		boolean test = shape.isTileable(n);
		if (!test) {
			StdOut.println("The region is not tileable. The numbers of squares of different colors are not the same.");
			int[] count = shape.countColoredSquares(n);
			StdOut.println("The count is ");
			for (int i = 0; i < n; i++) {
				StdOut.print(count[i] + "; ");
			}
			StdOut.println();
		};
		XGraph xG = randomBiased(n, shape, ITER, bias);
		return xG;
	}
	
	/** 
	 * 
	 * removes redundant forced  edges. 
	 * These are forced edges which are already determined 
	 * by other forced edges, so if we switch the orientation of 
	 * this edges we will get a cycle. 
	 * 
	 */
	
	private void reduceForced() {
		ArrayList<Edge> forcedEdgesCopy = new ArrayList<Edge>(forcedEdges);
		for (Edge e: forcedEdges) {
			DG.removeEdge(e.u, e.v);
			DG.addEdge(e.v, e.u);
			if (DG.hasCycle()) { //the edge is redundant, remove it.
				DG.removeEdge(e.v, e.u);
				forcedEdgesCopy.remove(e);
			} else {// the edge is not redundant; Put it back
				DG.removeEdge(e.v, e.u);
				DG.addEdge(e.u, e.v);
			}
		}
		forcedEdges = new ArrayList<Edge>(forcedEdgesCopy);
	}
	
	
	/**
	 * This might be useful if we want to fix an orientation on e
	 * However, it would be useful only if we already have e in 
	 * the orientation or at least the edge is flippable and can 
	 * be set to the desired orientation. 
	 * 
	 * @param e
	 */
	
	public void addForcedEdge(Edge e) {
		if (DG.isEdge(e.v, e.u)) {
			StdOut.println("addForcedEdge warning: Cannot add the edge " + e 
					+ " to forced because its opposite is in the graph");
			return;
		}
		if (!DG.isEdge(e.u, e.v)) {
		   DG.addEdge(e.u, e.v);
		   if (DG.hasCycle()) {
			   StdOut.println("addForcedEdge warning: adding edge " + e
					+ " would result in a cycle" );
			   DG.removeEdge(e.u, e.v);		
			   return;
		   }
		}
		if (!forcedEdges.contains(e)) {
		     forcedEdges.add(e);
		}
		edges.remove(e);
	}
	
	
	/**
	 * removes redundant free edges. 
	 * These are free edges, so if we switch the orientation of 
	 * this edges we will get a cycle. 
	 * So, this will remove free but not flippable edges. 
	 * After this operation performed the array edges will contain
	 * flippable edges
	 * 
	 */
	private void reduceFree() {
		ArrayList<Edge> edgesCopy = new ArrayList<Edge>(edges);
		for (Edge e: edges) {
			DG.removeEdge(e.u, e.v);
			DG.addEdge(e.v, e.u);
			if (DG.hasCycle()) { //the edge is redundant, remove it from edges
				              //but I will keep it in DG
				DG.removeEdge(e.v, e.u);
				DG.addEdge(e.u, e.v);
				edgesCopy.remove(e);
			} else {// the edge is not redundant; 
				DG.removeEdge(e.v, e.u);
				DG.addEdge(e.u, e.v);
			}
		}
		edges = new ArrayList<Edge>(edgesCopy);
	}
	
	/**
	 * update the associated tiling based on the current status
	 * of DG.
	 */
    public void updateTiling( ) {
    	tiling.buildTilingFromXG();
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
     * Returns tiling sequence that correspond to the tiling
     * 
     */
    
    public ArrayList<Integer> tilingSequence(){
    	ArrayList<Integer> seq = new ArrayList<Integer>();
		ArrayList<Integer> X = new ArrayList<Integer>();
		for (int i = 0; i < V; i++) {
			X.add(i);
		}
		while (X.size() > 0) {
		   int s = DG.smallestSink(X);
		   seq.add(s);
		   X.remove((Integer) s);
		}
    	return seq;
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
	
	/**
	 * Checks if edge e is flippable
	 */
	public boolean isFlip(Edge e) {
		boolean flippable;
		int u = e.u;
		int v = e.v;
        DG.removeEdge(u,v);
		DG.addEdge(v,u); 
		if (DG.hasCycle()) {
			flippable = false;
		} else {
			flippable = true;
		}
		DG.removeEdge(v,u);
		DG.addEdge(u,v);
		// also check if it is not by any chance in the list of forced
		//edges
		if (forcedEdges.contains(e)) {
			flippable = false;
		}
		return flippable;
	}
	
	/**
	 * returns the directed path that prevents reversing edge e
	 * It is assumed that e is free but not flippable.
	 * 
	 * Some deficiency of this method is that there are several cycles that 
	 * prevent flipping an edge and the method give no control over which one is chosen
	 * We should improve over this. 
	 * 
	 * @param e
	 * @return
	 */
	public ArrayList<Integer> obstacle(Edge e){
		ArrayList<Integer> obstacle = new ArrayList<Integer>();
		int u = e.u;
		int v = e.v;
		if (!DG.isEdge(u, v)) {
			StdOut.println("obstacle Warning: the edge " + e + " is not in the graph");
		    return obstacle;
		}
		if (isFlip(e)) {
			StdOut.println("obstacle Warning: the edge " + e + " is flippable");
			return obstacle;
		}
        DG.removeEdge(u,v);
		DG.addEdge(v,u); 
		obstacle = DG.cycle();
		DG.removeEdge(v,u);
		DG.addEdge(u,v);
		return obstacle;
	}
	
	/**
	 * returns the directed path that prevents reversing edge e
	 * It is assumed that e is free but not flippable.
	 * 
	 * The search will be in the new directed graph formed by
	 * free flippable edges and forced edges. While the 
	 * cycle is also not unique, the non-uniqueness is smaller. It is 
	 * now 
	 * (a) in the forced edges. 
	 * (b) there can be cycle that goes from above from u to v and 
	 * from below to u to v. 
	 * 
	 * @param e
	 * @return
	 */
	
	public ArrayList<Integer> obstacleClever(Edge e){
		ArrayList<Integer> obstacle = new ArrayList<Integer>();
		
		//TODO implement this method.
		MyDigraph tempDG = new MyDigraph(V);
		for (Edge f : forcedEdges) {
			tempDG.addEdge(f.u, f.v);
		}
		for (Edge f : edges) {
			tempDG.addEdge(f.u, f.v);
		}
		int u = e.u;
		int v = e.v;
		if (!DG.isEdge(u, v)) {
			StdOut.println("obstacle Warning: the edge " + e + " is not in the graph");
		    return obstacle;
		}
		if (isFlip(e)) {
			StdOut.println("obstacle Warning: the edge " + e + " is flippable");
			return obstacle;
		}
        tempDG.removeEdge(u,v);
		tempDG.addEdge(v,u); 
		obstacle = tempDG.cycle();
		//We also want to rotate the obstacle so that it would start with 
		// u and ended with v
		ArrayList<Integer> obst2 = new ArrayList<Integer>(obstacle);
		 //StdOut.println(obstacle);
		for (int i = 0; i < obstacle.size(); i++) {
			if (obstacle.get(i) == u) {
				break;
			} else {
				obst2.remove(0);
				obst2.add(obst2.get(0));
				continue;
			}
		}
		return obst2;
	}
	
	/**
	 * Biased random flip of edge e. 
	 * 
	 * If bias is between 0 and 1 then the corresponding percentage 
	 * of flips of left to right edges is rejected. If bias is between -1 and 0
	 * then the corresponding of flips of right to left edges is rejected.
	 * 
	 * The direction is based on the tiling so it should be always updated. 
	 * 
	 * NOTE: The meaning of bias here is not quite clear. This function
	 *  should be rewritten. A more clear meaning would be if the probability of 
	 *  the flip depended on the shape of the tiles. For example, a flip that would increase
	 *  the number of horizontal segments in the union of tiles could be made more probable.
	 * 
	 * 
	 * @param e
	 * @param bias a parameter between -1 and 1.
	 */
	
	
	public void biasedFlip(Edge e, double bias) {
		int u = e.u;
		int v = e.v;
		XRibTile utile = tiling.tiling.get(u);
		XRibTile vtile = tiling.tiling.get(v);
		double r = StdRandom.uniform();
		if (utile.compareWeak(vtile) > 0) {
			if (r > bias) {
				flip(e, true);
			}
		} else {
			if (r > - bias);
			flip(e, true);
		}
	}
	
	
	/**
	 * Flips the edge e in the graph. (It is assumed that it does not
	 * create a cycle.)
	 * 
	 * @param e
	 * @param updateTiling if true then associated tiling is updated. 
	 */
	public void flip(Edge e, boolean updateTiling) {
		int u = e.u;
		int v = e.v;
		Edge f = new Edge(v, u);
		edges.remove(e);
		edges.add(f);
		DG.removeEdge(u,v);
		DG.addEdge(v,u);
		//now we need to check all edges incident with u and v
		  // and update them
		  //the trouble that we also need to check the edges that
		  //point to u and v, not only the edges out of u and v.
		MyDigraph DG1 = new MyDigraph(DG);
		Digraph DG2 = DG.reverse();
		   //going over out-neighbors of u and v and checking if they are
		   //free and flippable
		for (int z : DG1.adj(u)) {		
			if (!isFreeAndComparable(u,z)) {
				continue;
			} else {
				Edge s = new Edge(u, z);
				if (!isFlip(s)) {//not-flippable
					edges.remove(s);
				} else { //flippable
					if (!edges.contains(s)) {
						edges.add(s);
					}
				}
			}
		}
		//same for v
		for (int z : DG1.adj(v)) {
			if (!isFreeAndComparable(v,z)) {
				continue;
			} else {
				Edge s = new Edge(v, z);
				if (!isFlip(s)) { //not-flippable
					edges.remove(s);
				} else { //flippable
					if (!edges.contains(s)) {
						edges.add(s);
					}
				}
			}
		}
		//now doing the same with the reversed graph (so, checking in-neighbors
		//of u and v
		for (int z : DG2.adj(u)) {
			if (!isFreeAndComparable(u,z)) {
				continue;
			} else {
				Edge s = new Edge(z, u);
				if (!isFlip(s)) { //not-flippable
					edges.remove(s);
				} else { //flippable
					if (!edges.contains(s)) {
						edges.add(s);
					}
				}
			}
		}
		//now treating in-neighbors of v
		for (int z : DG2.adj(v)) {
			if (!isFreeAndComparable(v,z)) {
				continue;
			} else {
				Edge s = new Edge(z, v);
				if (!isFlip(s)) { //not-flippable
					edges.remove(s);
				} else { //flippable
					if (!edges.contains(s)) {
						edges.add(s);
					}
				}
			}
		}
		if (updateTiling) {
			updateTiling();
		}
	}
	
	// Essentially builds a default graph;
	
	private void buildDefaultGraph() {
		for (int v = 0; v < V; v++) {
			for (int u = 0; u < V; u++) {
				if (isFreeAndComparable(u, v)) {
					if (!DG.isEdge(u,v) && !DG.isEdge(v,u)) {//these vertices are not yet connected
						DG.addEdge(u,v);
						if (DG.hasCycle()) {
							DG.removeEdge(u,v);
							DG.addEdge(v,u);
						}
					}
				}
			}
		}
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
		edges = new ArrayList<Edge>(); //free edges.
		ArrayList<Edge> possibleEdges = new ArrayList<Edge>();
		int E; //number of free edges;

		for (int u = 0; u < V - 1; u++) {
			int lu = ig.tile2level.get(u);
			for (int v = u + 1; v < V; v++) {
				int lv = ig.tile2level.get(v);
				if ((lu - lv < n) && (lv - lu < n) && (lu != lv)) {
					possibleEdges.add(new Edge(u, v));
				}
			}
		}
		E = possibleEdges.size();
		StdOut.println("Number of edges without orientation at the start = " + E);
		
		//make sure that at the beginning the digraph has no oriented free edges
		for (Edge e : possibleEdges) {
			DG.removeEdge(e.u, e.v);
			DG.removeEdge(e.v, e.u);
		}		
		
		ArrayList<Boolean> markedEdges = new ArrayList<Boolean>(E);
		for (int i = 0; i < E; i++) {
			markedEdges.add(false); 
		}
		int countEdges = 0;
		while (countEdges < E) { //all edges must be marked to stop
			int e = StdRandom.uniform(E);
			if (!markedEdges.get(e)) { //we did not try this edge yet
				Edge edge = possibleEdges.get(e);
				//Let us try to set an orientation on e;
				double coin = StdRandom.uniform();
				if (coin < 1/2) {
					DG.addEdge(edge.u, edge.v);
					if (DG.hasCycle()) {
						DG.removeEdge(edge.u, edge.v);
						//DG.addEdge(edge.v, edge.u); // we do not need to add this edge
						         //it is already clear that there is a path from v to u
					} else { //it is also possible that the edge is redundant;
						DG.removeEdge(edge.u, edge.v);
						DG.addEdge(edge.v, edge.u);
						if (DG.hasCycle()) { //redundant edge
							DG.removeEdge(edge.v, edge.u);
						} else { //useful new edge
							//StdOut.println("Coin is " + coin);
							//StdOut.println("Adding useful edge " + edge);
							//StdOut.println("The graph is " + DG);
							DG.removeEdge(edge.v, edge.u);
							DG.addEdge(edge.u, edge.v);
							edges.add(new Edge(edge.u,edge.v));
						}
					}
				} else {
					DG.addEdge(edge.v, edge.u);
					if (DG.hasCycle()) {// contradictory
						DG.removeEdge(edge.v, edge.u);
					} else {
						DG.removeEdge(edge.v, edge.u);
						DG.addEdge(edge.u, edge.v);
						if (DG.hasCycle()) {//redundant
							DG.removeEdge(edge.u, edge.v);
						} else { //useful
							//StdOut.println("Coin is " + coin);
							//StdOut.println("Adding useful edge " + edge);
							//StdOut.println("The graph is " + DG);
							DG.removeEdge(edge.v, edge.u);
							DG.removeEdge(edge.u, edge.v);
							DG.addEdge(edge.v, edge.u);
							edges.add(new Edge(edge.v,edge.u));
						}
					}
				}
				markedEdges.set(e, true);
				countEdges++;
			}
		}
		//although we have not added redundant edges during the construction, 
		//they could still arise post-factum. So we reduce them. 
		reduceFree();
	}
	
    /**
     * Returns a string representation of this XGraph
     *
     * @return a string representation of this XGraph
     */
    @Override
    public String toString() {
    	String s = DG.toString();
        return s;
    }

    public void drawWithoutLabels() {
		tiling.draw();
		draw();
    }
    
    /*
    public void drawWithLabels(String s, int size) {
		tiling.drawWithLabels(s, size);
        draw(s, size);
    }
    */
    
    /**
     * basic draw method
     */
    public void draw() {
    	draw ("", 500, false);
    }
       
    public void draw(String s, int size, boolean withLabels) {
	double r = 0.001;
	double offs = 0.2;
	//if (tiling.shape.myDr == null) {
		tiling.draw(s, size, withLabels);
	//}
	dr = tiling.shape.myDr;
	/*
	for (Edge e : forcedEdges) {
		r = 0.008;	
		dr.setPenColor(Draw.WHITE);
		dr.setPenRadius(r);
		XRibTile a = tiling.tiling.get(e.u);
		XRibTile b = tiling.tiling.get(e.v);
		dr.line(a.xmin + offs,a.ymin + offs, b.xmin + offs,b.ymin + offs);
		dr.filledCircle(a.xmin + offs, a.ymin + offs, 10 * r);
		dr.filledCircle(b.xmin + offs, b.ymin + offs, 10 * r);
	}
	*/
	for (Edge e : edges) {
		dr.setPenColor(Draw.PINK);
		r = 0.006;
		dr.setPenRadius(r);
		XRibTile a = tiling.tiling.get(e.u);
		XRibTile b = tiling.tiling.get(e.v);
		dr.line(a.xmin + offs,a.ymin + offs, b.xmin + offs,b.ymin + offs);
		dr.filledCircle(a.xmin + offs, a.ymin + offs, 10 * r);
		dr.filledCircle(b.xmin + offs, b.ymin + offs, 10 * r);
	}
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
        
		int k = 2;
		int n = 3;
		int M = n * k;
		int N = n * k;
		
        for (int i = 0; i < M; i++) {
        	shapeI.add(0);
        	shapeF.add(N - 1);
        }
		TreeSet<Square> squares = XUtility.shape2bag(shapeI, shapeF);
		XShape shape = new XShape(squares);
		//IntervalGraph ig = new IntervalGraph(n, shape);
		XGraph xG = new XGraph(n, shape);
		//xG.addForcedEdge(xG.new Edge(1,0));
		
		StdOut.println("DG graph is " + xG.DG);
		StdOut.println("edges = " + xG.edges);
		StdOut.println("Forced Edges = " + xG.forcedEdges);
		
		
		

		
		
		int ITER = 4000;
		double bias = 0.9;
		//xG = XGraph.random(n, shape, ITER);
		xG = XGraph.randomBiasedRectangle(n, M, N, ITER, bias);
		xG.draw("Old", 500, true);
		StdOut.println("DG graph is " + xG.DG);
		StdOut.println("edges = " + xG.edges);
		
		
		ArrayList<Integer> X = new ArrayList<Integer>();
		for (int i = 0; i < xG.V; i++) {
			X.add(i);
		}
		StdOut.println("Smallest sink is " +  xG.DG.smallestSink(X));
		StdOut.println("Tiling sequence is " + xG.tilingSequence());
		
		/**
		 * The piece below is about flipping a pair of tiles by reversing a 
		 * chain of edges
		 */
		
		/*
		XGraph xG2 = new XGraph(xG);
		xG2.flip(xG2.edges.get(0), true);
		xG2.draw("New", 500, false);
		
		StdOut.println("Distance between graphs 1 and 2 is " + XUtility.distance(xG, xG2));
		
		ArrayList<Integer> vudir = XUtility.chooseVudir(xG);
		
		StdOut.println("Parameters: " + vudir);
		int v = vudir.get(0);
		int u = vudir.get(1);
		int dir = vudir.get(2);
		
		Edge e = xG.new Edge(v, u);
		if (! xG.DG.isEdge(v,u)) {
            e = xG.new Edge(u, v);
		} 
		
		boolean flag = xG.isFlip(e);
		StdOut.println("Flippable ? " + flag);
		StdOut.println("Obstacle is " + xG.obstacle(e));
		StdOut.println("Clever Obstacle is " + xG.obstacleClever(e));
		
		*/
		
		
		//XRibTiling region = XRibTiling.aztecDiamond(N);
		//XRibTiling region = XRibTiling.rectangle(n, M, N);
		//XRibTiling region = XRibTiling.stair(n, M, N);
		//XRibTiling region = XRibTiling.downStair(n, M, N);
		//XShape shape = region.shape;
		//IntervalGraph ig = new IntervalGraph(n, shape);
		/*
		ig.draw();
		StdOut.println("Structure of tiles, level2tiles: ");
		StdOut.println(ig.level2tiles);
		StdOut.println("tile2level structure is :");
		StdOut.println(ig.tile2level);
        */

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
		/*
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
		XUtility.plotIntArray(distances);
		xg2.tiling.draw();
		*/

	}
}

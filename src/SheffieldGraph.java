import edu.princeton.cs.algs4.Graph;
//import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.Draw;
//import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdOut;
//import edu.princeton.cs.algs4.StdIn;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

/*
 * This object keeps information about the reduced Sheffield graph, 
 * in particular the structure of this graph and a method that allows us
 * to check if a particular edge is forced. 
 * 
 */
public class SheffieldGraph{
	public int V; //V is the number of vertices in the graph.
	private int N, M; //N and M are the height and width of the tiling rectangle.
	
	
	private int n = 3;
	// n is the number of squares in the tile (currently, only n = 3 is supported)
	private Graph G;  // This non-oriented keeps information about the comparability of tiles and so the connectivity of the Sheffield graph
	                  // It is likely that we can avoid using this field by using weakCompare from the tiling object instead.
	private Digraph DG;
	//private Digraph reverseDG;
	private Digraph copyDG;
	private RibTiling tiling; //pointer to the associated tiling
	public HashMap<Integer, RibTile> labels; //This hashmap keeps information about 
	                                          //the tile associated with a particular vertex
	public HashMap<RibTile, Integer> labels2vertices; //This hashmap associates tiles 
	                                                  //with vertices
	public HashMap<Long, HashSet<Integer>> levels2vertices; //This hashmap shows what are vertices 
	//in a given level.
	
	public ArrayList<ArrayList<Integer>> l2v; //This is a vector of vectors x; an k-th element of l2v
	//is a collection of all vertices at the level k. The difference from levels2vertices
	//is that this collection is sorted by Sheffield's order.
	
	/*
	 * This constructor calculates the graph given a tiling 
	 */
	public SheffieldGraph(RibTiling T) {
		this.N = T.N;
		this.M = T.M;
		tiling = T; 
		// Now we need to calculate V;
		V = N * M / 3;
		G = new Graph(V);
		DG = new Digraph(V);
		// assign vertices to tiles
		labels = new HashMap<Integer,RibTile>();
		labels2vertices = new HashMap<RibTile, Integer>();
		levels2vertices = new HashMap<Long, HashSet<Integer>>();
		for (long i = 0; i < N + M - n; i++) {
			HashSet<Integer> set = new HashSet<Integer>();
			levels2vertices.put(i, set);
		}
		int k = 0;
		for (RibTile tile : T.tiling) {
			labels.put(k, tile);
			labels2vertices.put(tile, k);
			levels2vertices.get(tile.level).add(k);
			k++;
		}
		//create edges
		for (int i = 0; i < V; i++) {
			for (int j = 0; j < V; j++) {
				if (labels.get(i).compareWeak(labels.get(j)) == 1) {
					G.addEdge(i, j);
					DG.addEdge(i, j);
				} else if (labels.get(i).compareWeak(labels.get(j)) == -1) {
					G.addEdge(i, j);
					DG.addEdge(j, i);
				}
			}
		}
		//creating edges of the reverse digraph
		//reverseDG = DG.reverse();
		distributeVertices();
	}
	
	/**
	 * update the graph on the basis of the current arrangement of tiles in the tiling.
	 * IMPORTANT: it does not recalculate the arrangement of tiles on levels or reduce the graph
	 */
	public void update() {
		//remove all existing edges in the digraph 
		// We need a copy of digraph DG to modify it at will
		copyDG = new Digraph(DG);
		for (int v = 0; v < V; v++) {
			for (int w : DG.adj(v)) {
				copyDG.removeEdge(v, w);
			}
		}
		DG = new Digraph(copyDG);
		//add new edges
		for (int i = 0; i < V; i++) {
			for (int j = 0; j < V; j++) {
				if (labels.get(i).compareWeak(labels.get(j)) == 1) {
					DG.addEdge(i, j);
				} else if (labels.get(i).compareWeak(labels.get(j)) == -1) {
					DG.addEdge(j, i);
				}
			}
		}
	}

	/**
	 * returns level of the tile associated with vertex v.
	 * @param v
	 * @return
	 */
	public long level(int v) {
		return labels.get(v).level;
	}
	/*
	 * Removes edges that cannot be reversed because a reversal would create a cycle.
	 */
	public void reduce() { //removes edges that cannot be reversed
		// We need a copy of digraph DG to modify it at will
		copyDG = new Digraph(DG);
		for (int v = 0; v < V; v++) { //cycle over all vertices.
			for (int w: DG.adj.get(v)) {
				//Let us try to reverse this edge
				copyDG.removeEdge(v, w);
				copyDG.addEdge(w, v);
				DirectedCycle dc = new DirectedCycle(copyDG);
				if (dc.hasCycle()) {
					copyDG.removeEdge(w, v);
				} else { //this edge can be reversed so we keep it.
					copyDG.removeEdge(w, v);
					copyDG.addEdge(v, w);
				}
			}
		}
		DG = new Digraph(copyDG);
	}
	//helper function that adds all visible red edges to a vertex v
	/*
	private void addRedEdges(int v) {
		RibTile t = labels.get(v);
        RibTile tile = tiling.findTile(t.xmin - 0.5, t.ymin + 0.5); 
        if (tile != null && tile.level == t.level - n) {
        	int x = labels2vertices.get(tile);
        	DG.addEdge(v, x);
        	reverseDG.addEdge(x,v);   	
        }
        tile = tiling.findTile(t.xmin + 0.5, t.ymin - 0.5); 
        if (tile != null && tile.level == t.level - n) {
        	int x = labels2vertices.get(tile);
        	DG.addEdge(x, v);
        	reverseDG.addEdge(v, x);   	
        }
        
        tile = tiling.findTile(t.xmax - 0.5, t.ymax + 0.5); 
        if (tile != null && tile.level == t.level + n) {
        	int x = labels2vertices.get(tile);
        	DG.addEdge(v, x);
        	reverseDG.addEdge(x,v);   	
        }
        
        tile = tiling.findTile(t.xmax + 0.5, t.ymax - 0.5); 
        if (tile != null && tile.level == t.level + n) {
        	int x = labels2vertices.get(tile);
        	DG.addEdge(x, v);
        	reverseDG.addEdge(v, x);   	
        }    
	}
	*/
	
	//compares v and w
	private int compare(int v, int w) {
		RibTile t = labels.get(v);
		return t.compareTo(labels.get(w));
	}
	
	/**
	 * The function distributes all vertices into bins according to their level and then
	 * sort the vertices in each bin according to the Sheffield order. 
	 */
	public void distributeVertices() {
			tiling.distributeTiles();
		    l2v = new ArrayList<ArrayList<Integer>>(N + M - n);
			for (int i = 0; i < N + M - n; i++) {
				ArrayList<Integer> listVertices = new ArrayList<Integer>();
				ArrayList<RibTile> listTiles = tiling.levels2tiles.get(i);
				for (int j = 0; j < listTiles.size(); j++) {
					int v = labels2vertices.get(listTiles.get(j));
					listVertices.add(v);
				}
		       l2v.add(listVertices); 
		    }
	}
	/*
	 * finds a closest vertex at a level with given offset in a given direction
	 * @param v a vertex in the graph
	 * @param offset is the offset from the level of v, can be from -n to n,
	 * @param dir is the direction. 1 means forward, -1 means backward.
	 * @returns the target vertex if the search was completed successfully, -1 if the shift
	 * in the given direction at the given offset is impossible. 
	*/
	public int findTargetAtLevel(int v, int offset, int dir) {
		// let us find the vertex u which is 
		// 1) has the level equal to the level of v + offset. 
		// 2)smaller than v in the Sheffield order. (or larger if dir = -1)
		// 3)smallest one with these properties, that is, there is no u' with properties 1) and 2)
		// such that u < u' < v.
		if (offset > n  || offset < -n) {
			StdOut.println("Offset out of allowed range");
			return -1;
		}
		int l = (int) labels.get(v).level;
		int i = l + offset;
		if (i < 0 || i > N + M - n - 1) {
			return -1;
		}
			ArrayList<Integer> listLevel = l2v.get(i);
			if (dir == 1) {
			for (int j = 0; j < listLevel.size(); j++) {
				int w = listLevel.get(j);
				if (j == 0 && compare(v, w) < 0) { //shift is impossible
					return -1;
				} else if (j < listLevel.size() - 1 && compare(v,listLevel.get(j + 1)) < 0){ //found it
				   return w;
				} else if (j == listLevel.size() - 1) {
					return w;
				} else {
					continue;
				}
			}
			} else {
				for (int j = 0; j < listLevel.size(); j++) {
					int w = listLevel.get(j);
					if (j == 0 && compare(v, w) < 0) { 
						return w;
					} else if (j < listLevel.size() - 1 && compare(v,listLevel.get(j + 1)) < 0){ //found it
					   return listLevel.get(j + 1);
					} else if (j == listLevel.size() - 1) {
						return w;
					} else {
						continue;
					}
				}
			}
			StdOut.println("Something might be wrong in the findTargetAtLevel function");
			return -1;
	}
	/**
	 * the first step in shifting a given vertex up or down in Sheffield's partial ordering.
	 * finds a target vertex which will be exchanged with a given vertex
	 * @param v a vertex in the graph
	 *  should be different from zero
	 * @param dir is the direction. 1 means forward, -1 means backward.
	 * @returns the target vertex if the search was completed successfully, -1 if the shift
	 * in the given direction at is impossible. 
	 */
	public int findTarget(int v, int dir) {
		int target = -1;
		for (int off = -(n - 1); off < n; off++) {
			if (off == 0) continue; 
			int w = findTargetAtLevel(v, off, dir);
			if (w == -1) continue;
			if (w != -1 && target == -1) {
				target = w;
			} else if (dir == 1 && compare(target, w) == -1) {
				target = w;
			} else if (dir == -1 && compare(target, w) == 1) {
				target = w;
			}
		}
		return target;
	}
	
	/**
	 * the second step in shifting. Given a pair of comparable vertices v and w,
	 * the function finds a vertex u, which is (a) between v and w; (b) has the level
	 * equal modulo n to the level of v, and (c) is closest to w among all vertices with 
	 * properties (a) and (b) 
	 * @param v : a vertex
	 * @param w : a vertex comparable with v, obtained by findTarget function
	 * @return u : a vertex
	 */
	
	public int findArrow(int v, int w) {
		long k = labels.get(v).level;
		long m = labels.get(w).level;
		ArrayList<Integer> levelK = l2v.get((int) k); //all integers in the level equal to the level of v.
		ArrayList<Integer> levelAux; //all integers in the level which is equal either to k + n or k - n.

		
		//looking for the vertex u with the right properties with initialization u = v.
		int u = v;
		for (int i = 0; i < levelK.size(); i++) {
			int x = levelK.get(i);
			if (compare(u,x) * compare(w,x) < 0) { //x is between w and current candidate u
				u = x;
			}
		} 
		if (m > k) {
			 levelAux = l2v.get((int) k + n); //all vertices in the level equal to k + n.
		} else {
			levelAux = l2v.get((int) k - n); //all vertices in the level equal to k - n.
		}
		
		for (int i = 0; i < levelAux.size(); i++) {
			int x = levelAux.get(i);
			StdOut.println("checking tile " + labels.get(x));
			int check = compare(u,x) * compare(w,x);
			StdOut.println("check = " + check);
			if (check < 0) { //x is between w and current candidate u 
				u = x;
			}
		} 
		return u;
	}

	
	/**
	 * exchanges a pair of comparable vertices using  a recursive algorithm
	 * The vertices must be comparable, or a message produced.
	 * @param v1 a vertex in the graph
	 * @param v2 another vertex in the graph.
	 * @returns nF number of flips used in the exchange process.
	 */
	/* I do not want to use this method, since it does not lead to a uniform distribution
	
	public int exchange(int v1, int v2) {
		int nF = 0;
		RibTile t1 = labels.get(v1);
		RibTile t2 = labels.get(v2);
		if ((t1.level >= t2.level + n) 
				|| (t1.level <= t2.level - n)
				|| (t1.level == t2.level)) {
			StdOut.println("Cannont exchange tiles. They are either \n"
                              +  "not comparable or connected by a sequence of forced edges.");
			StdOut.println(t1);
			StdOut.println(t2);
			return 0;
		}
		
		ArrayList<RibTile> btiles = tiling.findBetween(t1, t2);
		if (btiles.isEmpty()) { //no tiles between t1 and t2. They must be flippable.
			flip(v1,v2);
			nF = 1;
		} else {
		//We should take a tile between t1 and t2 and use recursion. 
			       // Let us take the first available
			       // although possibly one can optimize here
			
				int n1, n2;
				RibTile t = btiles.get(0);
				//StdOut.println("middle tile is " + t);
				int v = labels2vertices.get(t);

				long off1 = t1.level - t.level;
				if ((off1 != n) && (off1 !=0) && (off1 != -n)) { //we can exchange t1 and t.
					n1 = exchange(v1, v);
					n2 = exchange(v1, v2);
					nF = n1 + n2;
				} else {
					n1 = exchange(v, v2);
					n2 = exchange(v1, v2);
					nF = n1 + n2;
				}
		}
		//StdOut.println("Tiles " + t1 + " and " + t2 + " are exchanged using " + nF +"flips.");
		return nF;
	}
	*/
	/**
	 * This function will 
	 * (1) flip tiles t1 and t2 associated with vertices v1 and v2;
	 * (2) update association maps labels and labels2vertices
	 * This function does not recalculate any of the graph structure or any of the distribution of 
	 * vertices along levels. This can be done with update method.
	 * 
	 */
	
    public void flip(int v1, int v2){
    	RibTile t1 = labels.get(v1);
    	RibTile t2 = labels.get(v2);
    	RibTile nt1, nt2;
    	Pair<RibTile, RibTile> pair;
        //we flip t1 and t2 in the tiling
    	pair = tiling.flipGeneric(t1, t2);
    	nt1 = pair.a;
    	nt2 = pair.b;
    	labels.put(v1, nt1);
    	labels.put(v2, nt2);
    	//we also change associations from tiles to vertices
    	labels2vertices.remove(t1);
    	labels2vertices.remove(t2);
    	labels2vertices.put(nt1, v1);
    	labels2vertices.put(nt2, v2);
    	
    	//we recalculate the structures that contain the order of the vertices and tiles in the levels.
    	//(this can be optimized and done by an exchange of the elements of the array, however, for simplicity
    	//we will do it by a simple recalculation
    	//distributeVertices();
    }
	
	
	/**
	 * This function will 
	 * (1) flip tiles t1 and t2 associated with vertices v1 and v2;
	 * (2) modify the cover graph to adjust it for the flip of the tiles .
	 * The assumptions are:
	 * (1) the tiles are flippable,
	 * (2) the cover graph has been already computed (by the reduce() method).
	 * (3) the directed edge from v1 to v2 exists in the cover graph.
	 */
	/* I don't want to use this sophisticated version
	 * 
    public void flip(int v1, int v2){
    	RibTile t1 = labels.get(v1);
    	RibTile t2 = labels.get(v2);
    	RibTile nt1, nt2;
    	Pair<RibTile, RibTile> pair;

        //to preserve the cover graph properties.
        //First we check if we have an exceptional situation. In an exceptional situation none of the 
        //outgoing edges of v2 is comparable with v1 and none of the incoming edges of v1 is comparable with v2.
        boolean flag = true;
        for (int x: DG.adj(v2)) {
        	if (labels.get(x).compareWeak(t1) == -1) {
        		flag = false;
        		break;
        	}
        }
        for (int x: reverseDG.adj(v1)) {
        	if (labels.get(x).compareWeak(t1) == 1) {
        		flag = false;
        		break;
        	}
        }
        if (flag) {
        	StdOut.println("Exceptional situation occured. Updating of the cover graph might be incorrect.");
        }
        //we flip t1 and t2 in the tiling
    	pair = tiling.flipGeneric(t1, t2);
    	//we change associations of vertices v1 and v2;
    	nt1 = pair.a;
    	nt2 = pair.b;
    	labels.put(v1, nt1);
    	labels.put(v2, nt2);
    	//we also change associations from tiles to vertices
    	labels2vertices.remove(t1);
    	labels2vertices.remove(t2);
    	labels2vertices.put(nt1, v1);
    	labels2vertices.put(nt2, v2);
    	
    	//we recalculate the structures that contain the order of the vertices and tiles in the levels.
    	//(this can be optimized and done by an exchange of the elements of the array, however, for simplicity
    	//we will do it by a simple recalculation
    	distributeVertices();
    	
    	//Now we are going to update the cover graph around nt1 and nt2
    	//First, remove all edges incident with v1 and v2
        ArrayList<Integer> removeList = new ArrayList<Integer>(); 
        for (int x: DG.adj(v1)) {
        	removeList.add(x);
        }
        for (int x: removeList) {
        	DG.removeEdge(v1, x);
        	reverseDG.removeEdge(x, v1);
        }
        removeList = new ArrayList<Integer>();
        for (int x: reverseDG.adj(v1)) {
        	removeList.add(x);
        }
        for (int x: removeList) {
        	DG.removeEdge(x, v1);
        	reverseDG.removeEdge(v1, x);
        }
        
        removeList = new ArrayList<Integer>(); 
        for (int x: DG.adj(v2)) {
            removeList.add(x);
        }
        for (int x: removeList) {
        	DG.removeEdge(v2, x);
        	reverseDG.removeEdge(x, v2);
        }
        removeList = new ArrayList<Integer>();
        for (int x: reverseDG.adj(v2)) {
        	removeList.add(x);
        }
        for (int x: removeList) {
        	DG.removeEdge(x, v2);
        	reverseDG.removeEdge(v2, x);
        }


        //Next, we are going to go over all tiles that can form 
        //a flippable pair with nt1 or nt2
        ArrayList<RibTile> flips = tiling.findFlips(nt1);
        for (RibTile t : flips) {
        	int x = labels2vertices.get(t);
        	if (t.compareWeak(nt1) == -1) { // t is on the left of nt1
        		DG.addEdge(v1, x);
        		reverseDG.addEdge(x, v1);
        	} else if (t.compareWeak(nt1) == 1) {
        		DG.addEdge(x, v1);
        		reverseDG.addEdge(v1, x);
        	}
        }
        
        flips = tiling.findFlips(nt2);
        for (RibTile t : flips) {
        	int x = labels2vertices.get(t);
        	if (t.compareWeak(nt2) == -1) {
        		DG.addEdge(v2, x);
        		reverseDG.addEdge(x, v2);
        	} else if (t.compareWeak(nt2) == 1) {
        		DG.addEdge(x, v2);
        		reverseDG.addEdge(v2, x);
        	}
        }
      
        
        //Finally, we are going to add the red edges from and to v1 and v2
       addRedEdges(v1);
        addRedEdges(v2);
    }
    */
    /**
    *Draw the cover graph
    */
	public void draw (Draw dw) {
		double x0, y0, x1, y1;
		dw.setPenRadius(0.01);
		dw.setPenColor(Draw.BOOK_BLUE);
		for (int v = 0; v < V; v++) {
			x0 = labels.get(v).xmin + 0.5; 
			y0 = labels.get(v).ymin + 0.5;
			for (int w : DG.adj(v)){
				x1 = labels.get(w).xmin + 0.5; 
				y1 = labels.get(w).ymin + 0.5;
				if ((labels.get(v).level - labels.get(w).level) % n == 0) {
					dw.setPenColor(Draw.PINK);	
					continue; //This turns off red arrows
				} else if ((((labels.get(v).level - labels.get(w).level) % n) 
						+ n) % n == 2) {
					dw.setPenColor(Draw.CYAN);
					//continue; //this turns off cyan arrows (if levels are different by 2 modulo n)
				} else {
					dw.setPenColor(Draw.BOOK_BLUE);	
					
				}
				dw.line(x0, y0, x1, y1);
				//draw arrow
				double[] X = new double[3];
				double[] Y = new double[3];
				X[0] = x1;
				Y[0] = y1;
				double c1 = 0.2;
				double c2 = 0.075;
				double r = 0.15;

				X[1] = x1 - c1 * (x1 - x0) - c2 * (y1 - y0);
				Y[1] = y1 - c1 * (y1 - y0) + c2 * (x1 - x0);
				X[2] = x1 - c1 * (x1 - x0) + c2 * (y1 - y0);
				Y[2] = y1 - c1 * (y1 - y0) - c2 * (x1 - x0);		
				dw.filledPolygon(X,Y);
				//draw tail
				dw.filledCircle(x0, y0, r);
			}
		}
		dw.show(40);
		//StdOut.println(DG);
		//StdOut.println(labels);
	}
}

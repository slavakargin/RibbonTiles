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
	private Digraph reverseDG;
	private Digraph copyDG;
	private RibTiling tiling; //pointer to the associated tiling
	public HashMap<Integer, RibTile> labels; //This hashmap keeps information about 
	                                          //the tile associated with a particular vertex
	public HashMap<RibTile, Integer> labels2vertices; //This hashmap associates tiles 
	                                                  //with vertices
	public HashMap<Long, HashSet<Integer>> levels2vertices; //This hashmap shows what are vertices in a given level.
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
			//StdOut.println(i);
			HashSet<Integer> set = new HashSet<Integer>();
			//set.add(-1);
			//StdOut.println(set);
			levels2vertices.put(i, set);
			//StdOut.println(levels2vertices.get(i));
		}
		int k = 0;
		for (RibTile tile : T.tiling) {
			labels.put(k, tile);
			labels2vertices.put(tile, k);
			//StdOut.println(tile.level);
			//StdOut.println(levels2vertices.get(tile.level));
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
		reverseDG = DG.reverse();
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
	
	
	/**
	 * exchanges a pair of comparable vertices using  a recursive algorithm
	 * The vertices must be comparable, or a message produced.
	 * @param v1 a vertex in the graph
	 * @param v2 another vertex in the graph.
	 * @returns nF number of flips used in the exchange process.
	 */
	
	public int exchange(int v1, int v2) {
		int nF = 0;
		RibTile t1 = labels.get(v1);
		RibTile t2 = labels.get(v2);
		if ((t1.level >= t2.level + n) 
				|| (t1.level <= t2.level - n)
				|| (t1.level == t2.level)) {
			StdOut.println("Cannont exchange tiles. They are either \n"
                              +  "not comparable or connected by a sequence of forced edges.");
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
	
	/**
	 * This function will 
	 * (1) flip tiles t1 and t2 associated with vertices v1 and v2;
	 * (2) modify the cover graph to adjust it for the flip of the tiles .
	 * The assumptions are:
	 * (1) the tiles are flippable,
	 * (2) the cover graph has been already computed (by the reduce() method).
	 * (3) the directed edge from v1 to v2 exists in the cover graph.
	 */
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
	}
}

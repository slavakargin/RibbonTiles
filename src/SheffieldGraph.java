import edu.princeton.cs.algs4.Graph;
import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdOut;
//import edu.princeton.cs.algs4.StdIn;
import java.util.HashMap;
import java.util.Scanner;

/*
 * This object keeps information about the reduced Sheffield graph, 
 * in particular the structure of this graph and a method that allows us
 * to check if a particular edge is forced. 
 * 
 */
public class SheffieldGraph{
	private int N, M, V; //N and M are the height and width of the tiling rectangle.
	//V is the number of vertices in the graph.
	private int n = 3;
	// n is the number of squares in the tile (currently, only n = 3 is supported)
	private Graph G; 
	private Digraph DG;
	private int root; //root of the directed graph (a source, which is unique in the case of the rectangle region).
	private Digraph copyDG;
	public HashMap<Integer, RibTile> labels; //This hashmap keeps information about 
	//the tile associated with a particular vertex
	public HashMap<RibTile, Integer> labels2vertices; //This hashmap associates tiles 
	//with vertices
	private RibTiling tiling; //pointer to the originating tiling
	/*
	 * This constructor calculates the graph given a tiling 
	 */
	public SheffieldGraph(RibTiling T) {
		this.N = T.N;
		this.M = T.M;
		this.tiling = T;
		// Now we need to calculate V;
		V = N * M / 3;
		G = new Graph(V);
		DG = new Digraph(V);
		// assign vertices to tiles
		labels = new HashMap<Integer,RibTile>();
		labels2vertices = new HashMap<RibTile, Integer>();
		int k = 0;
		for (RibTile tile : T.tiling) {
			labels.put(k, tile);
			labels2vertices.put(tile, k);
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
		// compute the root. (This is needed only if we want to compute the 
		// spanning branching, which we usually don't do. So this can be removed if desired.)
		RibTile rootTile = tiling.findTile(M - 0.5, 0.5);
		root = labels2vertices.get(rootTile);
	}

	/*
	 * This function checks if there is a directed path from u to v such that each of the 
	 * vertices in the path is comparable with both of its endpoints.
	 * If v < u in weak order and such a path exists, the function returns true.  
	 * If v < u but the path with this property does not exists,
	 * then the function returns false. 
	 * If it is not true that v < u in weak order, then the function 
	 *  returns true. (The Existence of Standard Path property is not violated.)
	 *  [This property is very often violated. So this function is obsolete and 
	 *  is going to be removed.]
	 */
	/*
	public boolean isStandardPath(int u, int v) {
		RibTile start = labels.get(u);
		RibTile finish = labels.get(v);
		if (start.compareWeak(finish) != 1) return true;
		copyDG = new Digraph(DG);
		DepthFirstDirectedPaths dfs = new DepthFirstDirectedPaths(copyDG, u);
		boolean flag = dfs.hasPathTo(v);
		while (flag) {
			int break1 = 0;
			int break2 = 0;
			for (int x: dfs.pathTo(v)) {
				if (x == u) {
					break1 = x;
					continue;
				} else if (x == v) {
					return true; //this path has all vertices comparable with both u and v;
				} else {
					if (start.compareWeak(labels.get(x)) != 1 || labels.get(x).compareWeak(finish) != 1) {
						break2 = x;
						break; //this path has a vertex outside of the allowed range.
					} else {
						break1 = x; //this vertex was OK so we update break1.
					}
				}
			}
			copyDG.removeEdge(break1, break2);
			dfs = new DepthFirstDirectedPaths(copyDG, u);
			flag = dfs.hasPathTo(v);
		}
		return false;
	}
	*/
	/*
	 * This function checks the existence of the path
	 * with the all vertices comparable to endpoints for
	 * all pairs of vertices in the graph.
	 * It returns true if this property satisfied for all pairs of points.
	 * Otherwise, it prints out the pair of the corresponding tiles and 
	 * returns false. 
	 * [This function is not needed any more and is going to be removed.] 
	 */
	/*
	public boolean isStandardPathProperty() {
		for (int u = 0; u < V; u++) {
			//StdOut.println(u);
			for (int v = 0; v < V; v++) {
				if (!isStandardPath(u, v)) {
					StdOut.println("There is no good path between tiles "
							+ labels.get(u) + " and " + labels.get(v));
					return false;
				}
			}
		}
		return true;
	}
    */


	/*
	 * removes edges so that the result is a spanning branching (spanning arborescence, in other terminology),
	 * that is, a subgraph which is a rooted tree with each edge directed from the root, and such 
	 * that every vertex is still reachable from the root. (This is possible for a rectangle.)
	 * [Currently, I do not use this function. The reason is that the spanning branching does not have an
	 * important property valid for the cover graph. Namely, even if t < t' in the partial order (that is,
	 * t is reachable from t'), it might occur that t is not reachable from t' in the spanning branching.
	 * One can ensure that t is reachable from t' for a particular spanning branching, but it is not true 
	 * for all spanning branchings.]
	 */
	/*
	public void strongReduce() {
		reduce();
		copyDG = new Digraph(DG);
		DirectedDFS dfs = new DirectedDFS(copyDG, root);
		//let us check if every vertex in the graph is reachable
		//StdOut.println(labels.get(root));
		//StdOut.println(V);
		//StdOut.println(dfs.count());

		//Variant I: we simply try removing random edges and check if every vertex is still 
		//reachable from the root.
		for (int v = 0; v < V; v++) {
			for (int w : DG.adj(v)) {
				copyDG.removeEdge(v, w);
				DirectedDFS modDFS = new DirectedDFS(copyDG, root);
				if (modDFS.count() != V){ //undo: this edge can't be removed.
					copyDG.addEdge(v, w);
				}
			}
		}
		DG = new Digraph(copyDG);
	}
    */
	/*
	 * removes just one edge that cannot be reversed because 
	 * a reversal would create a cycle. (It is used for debugging.)
	 * [It is going to be removed].
	 */
	/*
	public void reduceStep(Scanner sc) {
		// We need a copy of digraph DG to modify it at will
		copyDG = new Digraph(DG);
		int size = N; 
		if (M > N) size = M; 
		StdDraw.setXscale(- 0.5, size + 0.5);
		StdDraw.setYscale(-0.5, size + 0.5);
		StdDraw.clear(Draw.LIGHT_GRAY);
		StdDraw.setPenRadius(0.005);
		for (int v = 0; v < V; v++) { //cycle over all vertices.
			for (int w: DG.adj.get(v)) {
				//Let us try to reverse this edge
				copyDG.removeEdge(v, w);
				copyDG.addEdge(w, v);
				DirectedCycle dc = new DirectedCycle(copyDG);
				if (dc.hasCycle()) {
					StdOut.println("Reversing edge (" + v + ", " + w + "). Found a cycle"); //this edge cannot be reversed, so we remove it.
					copyDG.removeEdge(w, v);
					drawStdDraw(dc.cycle());
					StdOut.println("Edge from (" + labels.get(v) + ") to (" + labels.get(w) + ") was removed." );
					StdOut.println("To continue, press Enter.");
					String input = sc.nextLine();		  
				} else { //this edge can be reversed so we keep it.
					copyDG.removeEdge(w, v);
					copyDG.addEdge(v, w);
					//   StdOut.println("Reversing edge (" + v + ", " + w + "). There are no cycles");
				}
			}
		}
		DG = new Digraph(copyDG);
	}
	*/

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
					// StdOut.println("Reversing edge (" + v + ", " + w + "). Found a cycle"); //this edge cannot be reversed, so we remove it.
					copyDG.removeEdge(w, v);
				} else { //this edge can be reversed so we keep it.
					copyDG.removeEdge(w, v);
					copyDG.addEdge(v, w);
					//   StdOut.println("Reversing edge (" + v + ", " + w + "). There are no cycles");
				}
			}
		}
		DG = new Digraph(copyDG);
	}

	public void draw (Draw dw) {
		double x0, y0, x1, y1;
		dw.setPenRadius(0.01);
		dw.setPenColor(Draw.BOOK_BLUE);
		for (int v = 0; v < V; v++) {
			//StdOut.println(v);
			x0 = labels.get(v).xmin + 0.5; 
			y0 = labels.get(v).ymin + 0.5;
			for (int w : DG.adj(v)){
				//StdOut.print(w);
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
			//StdOut.println();
		}
		dw.show(40);
	}
	//This is a helper for debugging. It will show the graph copyDG in the StdDraw window.
	// [It is going to be removed.]
	/*
	private void drawStdDraw (Iterable<Integer> cycle) {
		double x0, y0, x1, y1;
		StdDraw.setPenRadius(0.01);
		StdDraw.setPenColor(Draw.BOOK_BLUE);
		for (int v = 0; v < V; v++) {
			x0 = labels.get(v).xmin + 0.5; 
			y0 = labels.get(v).ymin + 0.5;
			for (int w : copyDG.adj(v)){
				x1 = labels.get(w).xmin + 0.5; 
				y1 = labels.get(w).ymin + 0.5;
				if ((labels.get(v).level - labels.get(w).level) % n == 0) {
					StdDraw.setPenColor(Draw.PINK);	
				} else {
					StdDraw.setPenColor(Draw.BOOK_BLUE);	
				}
				StdDraw.line(x0, y0, x1, y1);
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
				StdDraw.filledPolygon(X,Y);
				//draw tail
				StdDraw.filledCircle(x0, y0, r);
			}
		}
		//Draw cycle
		StdDraw.setPenColor(Draw.YELLOW);
		double prevX = 0.0;
		double prevY = 0.0;
		for (Integer u : cycle ) {
			StdOut.println(u + ": " + labels.get(u));
			x0 = labels.get(u).xmin + 0.5; 
			y0 = labels.get(u).ymin + 0.5;
			if (prevX == 0.0) {
				prevX = x0;
				prevY = y0;
			} else {
				StdDraw.line(prevX, prevY, x0, y0);
			}
		}
		StdDraw.show(40);
	}
	*/
}

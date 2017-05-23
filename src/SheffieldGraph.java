import edu.princeton.cs.algs4.Graph;
import edu.princeton.cs.algs4.Draw;
import edu.princeton.cs.algs4.StdOut;
import java.util.HashMap;
import java.util.Iterator;

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
	public HashMap<Integer, RibTile> labels; //This hashmap keeps information about 
	                                         //the label associated with a particular vertex

	public SheffieldGraph(RibTiling T) {
		// TODO Auto-generated constructor stub
		this.N = T.N;
		this.M = T.M; 
		// Now we need to calculate V;
		V = N * M / 3;
		G = new Graph(V);
		DG = new Digraph(V);
		labels = new HashMap<Integer,RibTile>();
		int k = 0;
		for (RibTile tile : T.tiling) {
			labels.put(k, tile);
			k++;
		}
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
	}
	public void reduce() { //removes edges that cannot be reversed
		// We need a copy of digraph DG to modify it at will
		Digraph copyDG = new Digraph(DG);
		for (int v = 0; v < V; v++) {
			for (int w: DG.adj.get(v)) {
				//Let us try to reverse this edge
				copyDG.removeEdge(v, w);
			    copyDG.addEdge(w, v);
			    DirectedCycle dc = new DirectedCycle(copyDG);
			    if (dc.hasCycle()) {
				   StdOut.println("Reversing edge (" + v + ", " + w + "). Found a cycle"); //this edge cannot be reversed, so we remove it.
				   copyDG.removeEdge(w, v);
			    } else { //this edge can be reversed so we keep it.
			    	copyDG.removeEdge(w, v);
			    	copyDG.addEdge(v, w);
				    StdOut.println("Reversing edge (" + v + ", " + w + "). There are no cycles");
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
			    double[] X = new double[3];
			    double[] Y = new double[3];
			    X[0] = x1;
			    Y[0] = y1;
			    X[1] = x1 - (x1 - x0)/7 - (y1 - y0)/14;
			    Y[1] = y1 - (y1 - y0)/7 + (x1 - x0)/14;
			    X[2] = x1 - (x1 - x0)/7 + (y1 - y0)/14;
			    Y[2] = y1 - (y1 - y0)/7 - (x1 - x0)/14;		    
			    dw.filledPolygon(X,Y);
			}
			//StdOut.println();
		}
		dw.show(40);
	}
}

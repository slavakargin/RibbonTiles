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
 *  (2) the intersection of every line  y = c with R has only one or zero components, and 
 *  (3) the intersection of every line x + y = l with R has only one component except 
 *       when the intersection consists of separated points. 
 * 
 *  <p>
 *
 *  @author Vladislav Kargin
 */

public class XGraph {

    MyDigraph DG; 
    
    
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

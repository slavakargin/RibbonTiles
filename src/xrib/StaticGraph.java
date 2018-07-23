package xrib;

import java.util.ArrayList;
import java.util.TreeSet;
import edu.princeton.cs.algs4.StdOut;

//import edu.princeton.cs.algs4.Digraph;

/**
 * This class contains a graph of forced edges associated with a tiling, denoted sG. It is static in the sense that it 
 * depends only on the shape of the tiled region.
 * 
 * In addition, this class contains the information on how many vertices are in each level (i.e., for
 * which corresponding tiles have this level), in graphStructure; which are starting vertices 
 * in each level (startLevel), and contains a map from vertices to their levels (vertex2level). 
 * 
 * An important restriction of the current implementation is that it assumes convexity of the shape in 
 * the sense that all lines x + y = l intersect the shape by only one interval (the intervals of length 0 
 * are not counted here). 
 * If this assumption is violated than the graph is not calculated correctly and the 
 * constructor is likely to fail. 
 * TODO add a verification of this assumption. 
 * 
 * 
 * 
 * @author vladislavkargin
 *
 */

public class StaticGraph {
	int L; //number of levels
	int Lmin; // minimal level
	int V; //number of vertices
	int n; //size of the ribbon tile
	ArrayList<Integer> graphStructure; // this list shows how many tiles/vertices is in each level. So, the k-th element of this 
	// list shows how many vertices in level k.
	// this list depends only on the shape of the region and so should be calculated
	//only once, during construction.
	private ArrayList<ArrayList<Integer>> heightAtCrosses;
	private ArrayList<Integer> heightDiffs;
	ArrayList<Integer> startLevel; // The k-th element in this list shows the index of the first vertex in level k
	ArrayList<Integer> vertex2level;
	MyDigraph sG;

	public StaticGraph(XRibTiling xrt) {
		L = xrt.shape.Lmax - xrt.shape.Lmin - xrt.n + 2;	
		V = xrt.shape.squares.size()/xrt.n;
		Lmin = xrt.shape.Lmin;
		n = xrt.n;
		calcHeightAtCrosses(xrt);
        calcHeightDiffs(xrt);
        calcGraphStructure(xrt);
        calcStartLevel();
        calcVertex2Level();
        createGraph(xrt);
	}
	/**
	 * Copy constructor
	 * @param other
	 */
	public StaticGraph(StaticGraph other) {
		this.L = other.L;
		this.graphStructure = new ArrayList<Integer>(other.graphStructure);
		this.startLevel = new ArrayList<Integer>(other.startLevel);
		this.vertex2level = new ArrayList<Integer>(other.vertex2level);
		this.sG = new MyDigraph(other.sG);
	}
	
 private void calcHeightAtCrosses(XRibTiling xrt){
		// we start by calculating heightAtCrosses. It is assumed that the height at the border was
		//	already calculated.
		// first we calculate heights at the crosses of the line x + y = l with the shape, and the
		//differences of the heights at these crosses.
		heightAtCrosses = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < xrt.shape.Lmax + 1; i++) { //Initialization;
			heightAtCrosses.add(new ArrayList<Integer>());
		}
		for (int l = 0; l < xrt.shape.Lmax + 1; l++) { //cycle over levels
			ArrayList<Integer> heights = heightAtCrosses.get(l); //we are going to fill in the entries in this array
			int comp = (l + xrt.n - 1) % xrt.n; //this is the component of the height that we are interested in 
			//(a fancy way to compute (l - 1) mod n.
			for (int x : xrt.shape.crosses.get(l)) { //cycle within the level. The variable "crosses" contains x coordinates of points
				//at which the line x + y = l crosses the region
				int y = l - x;
				ArrayList<Integer> vectorVysoty = xrt.H.height.get(new Square(x, y));
				heights.add(vectorVysoty.get(comp));
			}
			heightAtCrosses.set(l, heights);
		}
 }
 
 private void calcHeightDiffs(XRibTiling xrt) {
		heightDiffs = new ArrayList<Integer>();
		for (int l = 0; l < xrt.shape.Lmax + 1; l++) { //calculate the difference at the points of intersection of 
			//line x + y = l with the region.
			int s = heightAtCrosses.get(l).size();
			if (s == 0) {
				heightDiffs.add(0);
			} else {
				heightDiffs.add((heightAtCrosses.get(l).get(s - 1) - heightAtCrosses.get(l).get(0))/2);
			}
		}
 }
 
 private void calcGraphStructure(XRibTiling xrt){
		/* Next we calculate graph structure from this data about heights.
		 * The structure of the graph is the array of integers that 
		 * shows how many vertices are in each level
		 * This is a rather complicated formula but it is essentially an accounting which 
		 * relates the number of tiles crossed by the line x + y = l and the difference of 
		 * heights on the endpoints of this path.
		 * that 
		 */
		graphStructure = new ArrayList<Integer>();
		for (int l = xrt.shape.Lmin; l < xrt.shape.Lmin + xrt.n - 1; l++) {
			graphStructure.add(heightDiffs.get(l + 1));
		}
		for (int l = xrt.shape.Lmin + xrt.n - 1; l < xrt.shape.Lmax - xrt.n + 2; l++) {
			graphStructure.add(heightDiffs.get(l + 1) + graphStructure.get(l - xrt.n + 1 - xrt.shape.Lmin));
		}
 }
 private void calcStartLevel() {
		//now we calculate the index of the first vertex in each level.
		startLevel = new ArrayList<Integer>();
		int start = 0;
		for (int i = 0; i < L; i ++) {
			if (graphStructure.get(i) > 0) {
				startLevel.add(start);
			} else {
				startLevel.add(null);
			}
			start = start + graphStructure.get(i);
		}
 }
  
 private void calcVertex2Level() {
		//and here we calculate the map of vertices to levels.	
		vertex2level = new ArrayList<Integer>();
		for (int v = 0; v < V; v++) { //initialization
			vertex2level.add(0);
		}
		for (int l = 0; l < graphStructure.size(); l++) {
			if (graphStructure.get(l) > 0) {
				for (int i = 0; i < graphStructure.get(l); i++) {
					vertex2level.set(startLevel.get(l) + i, l + Lmin);
				}
			}
		}
 }
 private void createGraph(XRibTiling xrt) {
		//now we need to create the graph. 	
	    int ladj, a, a1, k, u, v;
		sG = new MyDigraph(V);

		/*
		 * This calculates the edges which are forced. 
		 */

		// First, we add edges between vertices at the same level. 		
		for (int i = 0; i < L; i ++) {
			if (graphStructure.get(i) > 1) { // there are at least two tiles in this level.
				for (int j = 0; j < graphStructure.get(i) - 1; j++) {
					sG.addEdge(startLevel.get(i) + j + 1, startLevel.get(i) + j);
				}
			}
		}
		   
			//Now we add edges between levels. We are going level by level.
			//for level l we need to find out which tile of levels l and l + n is 
			// the most far to the left. For this we look at the intersections
			// of level lines l + n and l + n + 1 with the region. Call the
			// intervals [a, b] and [a1, b1]. If a < a1, then the tile from l is before the
			//tile from l + n
		for (int l = 0; l < graphStructure.size() - n; l++) { // go over all non-empty levels except the top n levels and relate
			                                                  //tiles in level l to tiles in level l + n.
			ladj = l + Lmin; //needs this adjustment because the graph structure starts the level
			                    //which is non-empty 
			                    //while xrt.crosses has all levels from 0 to Lmax, even if they are empty
			a = xrt.shape.crosses.get(ladj + n).get(0);
			a1 = xrt.shape.crosses.get(ladj + n + 1).get(0);
			if (a >= a1) { //if a = a1, then there is one tile in the level l + n, which is 
				            //to the left of all tiles in level l.
				            k = a - a1 + 1; //number of tiles in the level l + n to the left 
				                            //of all tiles in l
				for (int j = 0; j < graphStructure.get(l); j++) { //we go over all tiles in level l;
					v = startLevel.get(l) + j; //tile in l
					u = startLevel.get(l + n) + k - 1 + j; //corresponding tile in level l+n, this tile is 
					                                      //to the left of v, however, the next tile in level l+n
					                                      //will be already to the right of v.
					if (vertex2level.get(u) == ladj + n) { //this vertex u is indeed in the level l+n
						sG.addEdge(v, u);	
						if ( u + 1 < V && vertex2level.get(u + 1) == ladj + n) { //if the next vertex is still in the level l+n,
							                                        //then it is to the right of v;
							sG.addEdge(u + 1, v);
						}
					}					
				}		
			} else  {
				 k = a1 - a; //under our assumptions it must be that there are k tiles at the level l which precedes the first tile
				//at level l + n;
				for (int j = 0; j < graphStructure.get(l + n); j++) { //we go over all tiles in level l + n;
					v = startLevel.get(l + n) + j;
					u = startLevel.get(l) + k - 1 + j;
					if (vertex2level.get(u) == ladj) {
						sG.addEdge(v, u);	
						if (vertex2level.get(u + 1) == ladj) {
							sG.addEdge(u + 1, v);
						}
					}				
				}		
			}		
		}

 }
	
	//@override
	public String toString() {
		return sG.toString();
	}
	/**
	 * For testing methods.
	 */
	public static void main(String[] args) {

		ArrayList<Integer> shapeI = new ArrayList<Integer>();
		ArrayList<Integer> shapeF = new ArrayList<Integer>();
		XRibTile tile;
		
		
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
        tile = new XRibTile(0, 0, "010");
		xrt.addTile(tile);
		tile = new XRibTile(2, 0, "110");
		xrt.addTile(tile);
		tile = new XRibTile(3, 1 + 3, "010");
		xrt.addTile(tile);
		tile = new XRibTile(0, 1, "001");
		xrt.addTile(tile);
		tile = new XRibTile(0, 3, "001");
		xrt.addTile(tile);
		tile = new XRibTile(1, 3, "000");
		xrt.addTile(tile);
		tile = new XRibTile(5, 0 + 3, "100");
		xrt.addTile(tile);
		tile = new XRibTile(5, 1 + 3, "010");
		xrt.addTile(tile);
		tile = new XRibTile(0, 5, "000");
		xrt.addTile(tile);
		xrt.draw();
		StaticGraph sG = new StaticGraph(xrt);
		StdOut.println("the static graph is " + sG);
	}
}

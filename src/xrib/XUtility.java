package xrib;
import java.util.ArrayList;
import java.util.TreeSet;

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.Draw;


class XUtility {

	private XUtility() {}


	/**
	 * This convert a simple shape to the bag of squares. the shape consists of several raws. 
	 * ShapeI is the coordinate of the initial square in each row. 
	 * ShapeF is the coordinate of the final square in each row. 
	 * 
	 * @param shapeI
	 * @param shapeF
	 * @return
	 */
	static TreeSet<Square> shape2bag(ArrayList<Integer> shapeI, ArrayList<Integer> shapeF) {
		TreeSet<Square> bag = new TreeSet<Square>();
		for (int i = 0; i < shapeI.size(); i++) {
			for (int j = 0; j < shapeF.get(i) - shapeI.get(i) + 1; j ++) {
				//S++;	
				bag.add(new Square(shapeI.get(i) + j, i)); 
			}
		}	
		return bag;
	}

	/**
	This function finds a sequence of sinks in the acyclic Digraph DG. The sinks are
	increasing in the order of increasing level from left to right
	 */

	static ArrayList<Integer> findSinkSequence(MyDigraph DG){
		int V; //Number of vertices
		Boolean[] marked; //marks the vertices that were found as sinks.
		ArrayList<Integer> sinkSeq; //sequence of the vertices as they appear as sinks 
		//as the previous sinks are removed from the graph.
		//This is just a generic placeholder for the recursive algorithm below
		V = DG.V();
		sinkSeq = new ArrayList<Integer>(V);
		marked = new Boolean[V];
		for (int i = 0; i < V; i++) {
			marked[i] = false;
			sinkSeq.add(0);
		}
		findSink(DG, sinkSeq, marked, V);
		return(sinkSeq);
	}

	/**
	This function finds a sink in the acyclic Digraph DG. More specifically, if there are several sinks,  
	then it produces the smallest of them in the order of increasing level from left to right
	 */

	private static void findSink(MyDigraph DG, ArrayList<Integer> sinkSeq, Boolean[] marked, int V) {
		//we will do the search recursively
		//First, we check how many vertices are yet unmarked.
		int s = 0; //number of unmarked vertices.
		for (int i = 0; i < marked.length; i++) {
			if (!marked[i]) s++;
		}
		if (s == 0) return; //all vertices are marked

		boolean sink = false;
		for (int i = 0; i < V; i++) { //looking for a sink 
			sink = true; //our initial guess is optimistic: it is a sink.
			if (marked[i]) continue; //this vertex was already marked as a sink 
			for (int v : DG.adj(i)) {
				//StdOut.println("The entry in the Marked array is: " + marked[v]);
				if (!marked[v]) { //we found a vertex, which follows i, and which was not 
					//marked as a sink before. Hence, vertex i not a sink.
					sink = false;
					break; //look at the next vertex i.
				}
			}
			if (sink) { //vertex i is a new sink, it is a sink number V + 1 - s
				sinkSeq.set(V - s, i);
				marked[i] = true;
				findSink(DG, sinkSeq, marked, V);
			}
		}
	}

	/**
	 * s0 is a square on the left border of the shape. The program calculates the tile with the
	 * root at s0 and which lies on the left border of the shape.
	 * 
	 * @param s0
	 * @return
	 */

	static XRibTile getBorderTile(Square s0, int n, TreeSet<Square> squares) {
		XRibTile tile;
		TreeSet<Square> tileSquares = new TreeSet<Square>();
		tileSquares.add(s0);
		int l0 = s0.x + s0.y;

		//find the other squares from the tile
		for (int level = l0 + 1; level < l0 + n; level++) {
			for (Square s : squares) {
				if (s.x == s0.x && s.y == s0.y + 1) {
					tileSquares.add(s);
					s0 = new Square(s.x, s.y);
					break;
				} else if (s.x == s0.x + 1 && s.y == s0.y) {
					tileSquares.add(s);
					s0 = new Square(s.x, s.y);
					break;
				}
			}
		}
		tile = new XRibTile(tileSquares);
		return tile;
	}


	/**
	 * 
	 * converts an integer n to a string of length numOfBits 
	 * with the binary representation of n.
	 * 
	 * @param n
	 * @param numOfBits
	 * @return
	 */		
	static String intToBinary (int n, int numOfBits) {
		String binary = "";
		for(int i = 0; i < numOfBits; ++i, n/=2) {
			switch (n % 2) {
			case 0:
				binary = "0" + binary;
				break;
			case 1:
				binary = "1" + binary;
				break;
			}
		}
		return binary;
	}
	/**
	 * This method realizes the Glauber dynamics on the tiling. A tile is chosen at random.
	 * Then a flip is chosen at random (if possible) and performed. This procedure is repeated
	 * for ITER iterations. Note that the original tiling is modified.
	 * 
	 * @param xrt the original tiling.
	 * @param ITER number of steps in the Glauber dynamics
	 */
	static void Glauber(XRibTiling xrt, int ITER){
		XRibTile tile, otherTile;
		ArrayList<XRibTile> flips;
		int randNum;
		//StdRandom.setSeed(17);
		for (int i = 0; i < ITER; i++) {
			if (i % 1000 == 0 & i > 0) {
				StdOut.println("Iteration " + i);
			}
			randNum = StdRandom.uniform(xrt.tiles().size());
			tile = xrt.tiles().get(randNum);
			flips = xrt.findFlips(tile);
			if (flips != null && flips.size() > 0) {
				randNum = StdRandom.uniform(flips.size());
				otherTile = flips.get(randNum);
				//StdOut.println("tile = " + tile);
				//StdOut.println("otherTile = " + otherTile);
				//StdOut.println("xrt.tiling.xG = " + xrt.xG);
				boolean flag = xrt.flip(tile, otherTile);
				if (!flag) { //flag == false means that there was a problem with this flip.
					StdOut.println("there was a problem with a flip; quitting Glauber algorithm.");
					return;
				}
			} else {
				continue;
			}
		}
	}
	

	
	

	/**
	 * A version of Glauber dynamics with randomness controlled by seed.
	 * Useful for reproducibility.
	 * 
	 * @param xrt
	 * @param ITER
	 * @param seed
	 */
	
	static void Glauber(XRibTiling xrt, int ITER, int seed){
		StdRandom.setSeed(seed);
		Glauber(xrt, ITER);
	}
	
	
	
	
	/**
	 * calculate the distance between two xgraphs. Returns twice the number of edges with different orientation
	 * in xg1 and xg2
	 * 
	 * @return
	 */
	static int distance(XGraph xg1, XGraph xg2) {
		int d = 0;
		int V = xg1.V;
		for (int u = 0; u < V; u++) {
			for (int v : xg1.DG.adj(u)) {
				if (!xg2.DG.isEdge(u, v)) { //the edge (u, v) has a different orientation in xg2
					d++;
				}
			}
			for (int v : xg2.DG.adj(u)) {
				if (!xg1.DG.isEdge(u, v)) { //the edge (u, v) has a different orientation in xg2
					d++;
				}
			}
		}	
		return d;
	}
	
	
	
	/**
	 * Draws a plot of the height of a random tiling of the N by N square
	 * 
	 * The square should be tileable
	 * 
	 * @param n size of ribbon tiles
	 * @param N size of the square region
	 */
	
	static void HeightPlot(int n, int N) {
		//create a random tiling
		XRibTiling region = XRibTiling.rectangle(n, N, N);
		XShape shape = region.shape;
		XGraph xg = new XGraph(n, shape);
		xg.randomOrientation();
		int ITER = 30000;
		XUtility.Glauber(xg.tiling, ITER);
		xg.tiling.draw();
		//calculate tiling
		XHeight H = new XHeight(xg.tiling);  
		H.calcHeightInside();
		//Now we want to select the heights on the line 
		//approximately in the middle of the square
		ArrayList<ArrayList<Integer>> plotData = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < N + 1; i++) {
			ArrayList<Integer> h = new ArrayList<Integer>();
			plotData.add(h);
		}
		int M = N/2;
		for (Square s: H.height.keySet()) {
			if (s.y == M) {
				//StdOut.println("s.x = " + s.x);
				plotData.set(s.x, H.height.get(s));
			}
		}
		StdOut.println("Midline Height = " + plotData);

		xg.tiling.shape.myDr.setPenColor(Draw.RED);
		xg.tiling.shape.myDr.setPenRadius(0.01);
		xg.tiling.shape.myDr.line(0, M, N, M);
		
		Draw dr = new Draw();
		dr.setXscale(-0.5, N + 0.5);
		dr.setYscale(-6, +6);
		for(int i = 0; i < N; i++) {
			dr.line(i, plotData.get(i).get(0), i + 1, plotData.get(i+1).get(0));
		}
		//TODO Plot the norm of the height vector, also may be plot the height along the level line.
	}
	
	/**
	 * plots an array of integers in a new window.
	 * 
	 * @param data, an array of integers
	 */
	
	static void plot(ArrayList<Integer> data) {
		Draw dr = new Draw();
		int N = data.size();
		int max = data.get(0);
		int min = data.get(0);
		for (int i = 1; i < N; i++) {
			if (max < data.get(i)) {
				max = data.get(i);
			}
			if (min > data.get(i)) {
				min = data.get(i);
			}
		}
		dr.setXscale(-N * 0.05, N* 1.05);
		dr.setYscale(min - (max - min) * 0.05, max + (max - min) * 0.05);
		dr.setPenColor(Draw.BOOK_RED);
		dr.setPenRadius(0.01);
		for(int i = 0; i < N - 1; i++) {
			dr.line(i, data.get(i), i + 1, data.get(i+1));
		}
		dr.setPenColor(Draw.BLACK);
		dr.setPenRadius(0.01);
		dr.line(0, max, N, max);
		dr.text(N * 0.05, max + (max - min) * 0.02, "max = " + max);
		dr.line(0, min, N, min);
		dr.text(N * 0.05, min + (max - min) * 0.02, "min = " + min);
	}
	
	
	
	/**
	 * walks on the graph of XGraph objects, avoiding returning to the same object twice. 
	 * Records the objects that it found and returns the list of these objects. 
	 * 
	 * @param xg initial XGraph
	 * @param ITER maximum number of steps allowed
	 */
	static ArrayList<XGraph> selfAvoidWalk(XGraph xg, int ITER) {
		ArrayList<XGraph> spisok = new ArrayList<XGraph>();
		//TODO create a realization of this method. 
		
		return spisok;
	}
	

}


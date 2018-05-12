package rib3;
import java.util.ArrayList;
import java.util.HashMap;

//import java.util.HashSet;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Draw;
/**
 * creates a path of edge reversals, which connects one tiling with another
 * (tiling 1 is transformed to tiling 2)
 */

public class TilingConnector {
	private RibTiling tiling1, tiling2; //tilings to connect. We transform tiling 1 to tiling 2.
	private RibTileVisualizer vz; //the reference to the visualizer that called the class
	protected int V; //number of tiles in the tilings.
	
	//We will keep the sequence of oriented edges processed by the algorithm as
	//two sequences of vertices U and W, so that U(i) and W(i)
	//give us the edge the orientation of which was reversed at stage i,
	//and U(i) is the new vertex (sink) being processed. (so the edge was w -> u before the step i and u -> w after this step.
	protected ArrayList<Integer> U, W;
	
	//We also need a hashmap that would allow us to recover the 
	//time at which an edge was processed. (Essentially, it is an inverse of the (U, W) structure).
	//We will realize it as a hashmap from a pair of two vertices (u, w) to the integer index. 
	protected HashMap<Pair<Integer,Integer>, Integer> edge2index;
	
	
	protected ArrayList<Integer> sinkSeq1; //This is the sink sequence for the first tiling. (That is,  it is 
	                                       // a sequence of the vertices
	                                       // as they appear as sinks 
                                           // when the previous sinks are removed from the graph.)
	protected ArrayList<Integer> sinkSeq2; // This is the sink sequence for the second tiling.
	
	//Height is the position of a vertex in the sink sequence. For example, if vertex 3 appears as the first sink, then
	//its height is 0. If vertex 7 appears next, its height is 1, and so on.
	//Both sink sequence and height are calculated in class Utility, that collects some static functions. 
	protected ArrayList<Integer> height1;
	protected ArrayList<Integer> originalHeight1; //height of the first tiling before the transition.
	protected ArrayList<Integer> height2;
	


	public TilingConnector(RibTiling tiling1, RibTiling tiling2, RibTileVisualizer vz) {
		this.tiling1 = tiling1;
		this.tiling2 = tiling2;
		this.vz = vz;
		V = tiling1.V;
		U = new ArrayList<Integer>();
		W = new ArrayList<Integer>();
		height1 = new ArrayList<Integer>(V);
		height2 = new ArrayList<Integer>(V);
		//initialization of heights
		for (int i = 0; i < V; i++) {
			height1.add(0);
			height2.add(0);
		}	
		//calculate sink sequences
		sinkSeq1 = Utility.findSinkSequence(tiling1.G.DG);
		sinkSeq2 = Utility.findSinkSequence(tiling2.G.DG);
		//calculate heights
		height1 = Utility.calculateHeight(tiling1.G.DG);
		height2 = Utility.calculateHeight(tiling2.G.DG);
		// preserve the original version of the height for 
		//the first tiling. 
		originalHeight1 = new ArrayList<Integer>(height1);
	}

	
	public void connectByHeight(int stage) {
		if (stage > V) {
			stage = V;
		}
		for (int i = 0; i < stage ; i++) {
			int v = sinkSeq2.get(i);
			changeHeight(v);
		}
		StdOut.println("New height1 is ");
		StdOut.println(height1);
		tiling1.draw(vz.window3, "vertex");
	}
	
	/**
	 * Changes the height of the first tiling making it to approach the second tiling. 
	 * The program is recursive. If it encounters an obstacle it tries to change the height of the obstacle.
	 * 
	 * @param v
	 */
	public void changeHeight(int v) {
		ArrayList<Integer> comps;
		int h, w;
		comps = tiling1.G.comparables(v);
		if (height1.get(v) < height2.get(v)) { //need to move up
			while (height1.get(v) < height2.get(v)) {
				h = height1.get(v) + 1;
				w = height1.indexOf(h);
				if (!comps.contains(w)) {
					height1.set(v, h);
					height1.set(w, h - 1);
				} else { 
					if (tiling1.G.isFlip(v, w)) {
						tiling1.G.flip(v, w);
						height1.set(v, h);
						height1.set(w, h - 1);
					} else {
							changeHeight(w); //remove obstacle
							changeHeight(v);
						}
				}
			}					
		} else if (height1.get(v) > height2.get(v)) { // need to move down
			while (height1.get(v) > height2.get(v)) {
				h = height1.get(v) - 1;
				w = height1.indexOf(h);
				if (!comps.contains(w)) {
					height1.set(v, h);
					height1.set(w, h + 1);
				} else { 
					if (tiling1.G.isFlip(v, w)) {
						tiling1.G.flip(v, w);
						height1.set(v, h);
						height1.set(w, h + 1);
					} else {
							changeHeight(w); //remove obstacle
							changeHeight(v);
						}
				}
			}					
		}
	}
	
	/**
	 * this will transform tiling1 to tiling2
	 * In this version we will stop the transition process when a certain number of vertices were reversed. 
	 * stage is the number of vertices that were processed. (If it exceeds the number of all vertices then all vertices are processed.)
	 */
	public void connect(int stage) {
		int counter = 0;
		if (stage > V) {
			stage = V;
		}
		for (int i = 2; i < stage + 1; i++){ 
			int v = sinkSeq1.get(V - i); //vertex v is the i-th from the end in the sequence of sinks.
			                             //We are going to check the incoming edges and reverse them if they 
			                            //disagree in the two given tilings.
			StdOut.println("Processing vertex " + v);
			tiling1.G.update();
			tiling1.G.reduce();
			boolean flag = true;
			while (flag) {
				flag = false; //we will run over all edges incoming to v. If we encounter reversible edge and it has different orientation
				                 // in the tilings, then we (1) reverse this edge, 
				               //(2) update the cover graph (that is, find all reversible edges in the updated graph), 
				                //and repeat the procedure
				               //until we have the situation that all edges which are incoming to v in current 
				               //orientation agree with orientation in tiling2. 
				for (int w: tiling1.G.reverseDG.adj(v)) {
					if (tiling1.G.compare(v, w) != tiling2.G.compare(v, w)) { //orientations disagree on this edge.
						StdOut.println("Need to flip edge to " + w);
						if (!tiling1.isFlip(tiling1.G.labels.get(v), tiling1.G.labels.get(w))) { //these tiles cannot be flipped
							StdOut.println("Can't flip the tiles."); //I never observed this. And this is natural, since
							                                         // I always work with reduced graphs which
							                                         //consist of reversible and red edges. However, the orientation on the 
							                                         //red lines is the same in both graphs. And the reversible edges
							                                         //can be reversed. 
							                                         //what is interesting is another thing. We never run into the situation
							                                        //when there is  another  incoming edge with orientation
							                                        // different from tiling2 orientation
							                                        //an reversion of one incoming edge would make another edge reversible 
							                                        // or will fundamentally change the order of sinks
							                                        // We never update the order of sinks although it can change in the process.
							                                        
							continue;
						} else { 
							flag = true; //need to repeat the procedure
							counter++;
							tiling1.G.flip(v, w);
							U.add(v);
							W.add(w);
							tiling1.G.update();
							tiling1.G.reduce();
						}
					}
				}
			}
		}
		tiling1.draw(vz.window3, "vertex");
		StdOut.println(counter + " flips performed.");
	}
	
	/**
	 * draws the arrows between tiles that were flipped.
	 * @param dr
	 */
	public void drawTransition(Draw dr) {
		    int l = U.size();
		    int v, w;
			double x0, y0, x1, y1;
			dr.setPenRadius(0.005);
			dr.setPenColor(Draw.BOOK_BLUE);
			for (int i = 0; i < l; i++){
				v = U.get(i);
				w = W.get(i);
				x0 = tiling1.G.labels.get(v).xmin + 0.5; 
				y0 = tiling1.G.labels.get(v).ymin + 0.5;
				x1 = tiling1.G.labels.get(w).xmin + 0.5; 
				y1 = tiling1.G.labels.get(w).ymin + 0.5;
				dr.line(x0, y0, x1, y1);
					//draw arrow
					double[] X = new double[3];
					double[] Y = new double[3];
					X[0] = x1;
					Y[0] = y1;
					double c1 = 0.1;
					double c2 = 0.075;
					double r = 0.15;

					X[1] = x1 - c1 * (x1 - x0) - c2 * (y1 - y0);
					Y[1] = y1 - c1 * (y1 - y0) + c2 * (x1 - x0);
					X[2] = x1 - c1 * (x1 - x0) + c2 * (y1 - y0);
					Y[2] = y1 - c1 * (y1 - y0) - c2 * (x1 - x0);		
					dr.filledPolygon(X,Y);
					//draw tail
					dr.filledCircle(x0, y0, r);
				}
			dr.show(40);
	}
	
	/**
	 * computes the code for the transformation edge which was used in the canonical transformation path
	 * from tiling1 to tiling2.
	 * @param stage -- The position of the last processed vertex in the sink sequence is (V - stage).  
	 */
	
	public ArrayList<Integer> computeCode(int stage) {
		ArrayList<Integer> codeHeight = new ArrayList<Integer>(V);
		for (int i = 0; i < V; i++) { //initialization.
			codeHeight.add(i, 0);
		}
		if (stage > V) {
			stage = V;
		}
		for (int j = 0; j < stage; j++) {
		int v = sinkSeq2.get(j);
		codeHeight.set(v, originalHeight1.get(v));
		}
		for (int j = stage; j < V; j++) {
			int v = sinkSeq2.get(j);
			codeHeight.set(v, height2.get(v));
		}
		return codeHeight;
	}
	
	public Digraph computeCodeGraph(int stage) {
		Digraph codeGraph = new Digraph(V); // graph that represent the transition path. 
		ArrayList<Integer> codeHeight = computeCode(stage);
		for (int v = 0; v < V; v++) {
			for (int w : tiling2.G.comparables(v)) {
				if (codeHeight.get(w) < codeHeight.get(v)) {
					codeGraph.addEdge(v, w);
				}
				if (codeHeight.get(w) == codeHeight.get(v)) {
					StdOut.println("Vertices " + v + " and " + w + " have the same height.");
				}
			}
		}		
		return codeGraph;
	}
}

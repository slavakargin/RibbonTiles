package rib3;
import edu.princeton.cs.algs4.Draw;
//import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class will create a tiling from a given digraph.
 * The vertices of the digraph should be numbered according to a certain convention,
 * and it is assumed that all required constraints on the graph are satisfied.
 * 
 * @author vladislavkargin
 *
 */


public class TilingReconstructor {
	private int V; //total number of vertices.
	private int L; //number of levels
	private int n = 3;
	protected RibTiling rt; // the reconstructed tiling.
	protected HashMap<Integer, RibTile> labels; //This hashmap keeps information about 
	                                            //the tile associated with a particular vertex
	protected HashMap<RibTile, Integer> labels2vertices; //This hashmap associates tiles 
	                                                     //with vertices
	protected ArrayList<Integer> vertex2level;
	//private ArrayList<Integer> sinkSeq;
	private ArrayList<Integer> frontier; //keeps information about the largest xmin of the tiles in every level.
	Boolean[] marked;



	public TilingReconstructor(int N, int M, Digraph dgTransition, Digraph dgCode) {
		this(N, M, dgTransition, dgCode, 400, 400, 400, 400);
	}
	
	public TilingReconstructor(int N, int M, Digraph dgTransition, Digraph dgCode, int canvasSizeW, int canvasSizeH, int offsetX, int offsetY) {	
		V = M * N / n;
		L = N + M - n;
		rt = new RibTiling(N, M, "Reconstruction", canvasSizeW, canvasSizeH, offsetX, offsetY);
		labels2vertices = new HashMap<RibTile, Integer>();
		labels = new HashMap<Integer, RibTile>();
		//initialize frontier;
		frontier = new ArrayList<Integer>(L + n - 1);
		for (int i = 0; i < L + n - 1; i++) {
			if (i < N) {
				frontier.add(i,-1);				
			} else {
				frontier.add(i, i - N);
			}
		}
		//building a convenience hasmap to calculate a level of a vertex in a given tiling. 
		// we assume that the area is N-by-M rectangle (N is height) and the numeration of 
		//vertices is standard: along the levels 
		vertex2level = Utility.buildVertex2Level(N, M, n);
		
		//constructing the sequence of sinks that we need in reconstruction.
		marked = new Boolean[V];
		for (int i = 0; i < V; i++) {
			marked[i] = false;
		}	
		//Now calculating the reconstructed tiling.  
		reconstruct(dgCode);
	}
	

	
	
	/**
	 * This function is supposed to show that we can build a tiling rt with the Sheffield Graph equal to the given Code Graph
	 * This will allow us to conclude that the number of code graphs of a transition is bounded by the number of tilings.
	 * This will allows to apply the Jerrum-Sinclair method.
	 * 
	 * @param codeGraph
	 */
	
	protected void reconstruct(Digraph codeGraph) {
		int typeCode, v, level, xmin, x0, ymin, x1;
		String str, stringCode;
		StringBuilder sb;
		RibTile tile;
		ArrayList<Integer> sinkSeq;
		sinkSeq = Utility.findSinkSequence(codeGraph);
		//We will build the tiling by using the sinkOrder and frontier, tile by tile
		//Namely, we take a vertex from the sinkSeq and look at the frontier around 
		//the level of the vertex. We modify the frontier appropriately. At the same time
		//we calculate the typecode of the added tile, generate the tile and add it to the 
		//reconstructed tiling.
		for (int i = 0; i < V; i++) {
			typeCode = 0;
			v = sinkSeq.get(i);
			level = vertex2level.get(v);
			xmin = frontier.get(level) + 1;
			x0 = xmin;
			ymin = level - xmin;
			frontier.set(level, xmin); //updating frontier at this level;
			for (int l = level + 1; l < level + n; l++) { //looking at the levels of all squares in the tile.
				x1 = frontier.get(l);
				if (x1 < x0) { // in this case we can make a vertical step.
					typeCode = typeCode | (1 << (l - level - 1)); 
					frontier.set(l, x0);
				} else { // make horizontal step
					x0++;
					frontier.set(l, x0);
				}
			}
            
			//In order to generate a corresponding tile, we need to convert typeCode to typeString
			str = Integer.toBinaryString(typeCode);
			//We also need to make sure that the string has the length n - 1;
			sb = new StringBuilder();
			for (int toPrepend = n - 1 - str.length(); toPrepend > 0; toPrepend--) {
				sb.append('0');
			}
			sb.append(str);
			
			stringCode = sb.toString();		
			tile = new RibTile(xmin, ymin, stringCode, rt);
			rt.tiling.add(tile);
			labels.put(v, tile);
			labels2vertices.put(tile, v);
		}
	}
	
	
	/*
	 * Something wrong with the code below since it does not use the graph dg at all. So this program can never compute
	 * anything related to codeGraph. It might be calculating something related to the transition Graph but it is not clear 
	 * to me. On the other hand it appears that it recreates the initial drawing reliably. May be I do not understand something here
	 * 
	 */
	/*
	protected void reconstruct(Digraph dg) {
	    		//sinkSeq = Utility.findSinkSequence(dgTransition);
		String stringCode;
		int v, level, xmin = 0, ymin = 0, x0, x1, typeCode;
		RibTile tile;
		//We will build the tiling by using the sinkOrder and frontier, tile by tile
		for (int i = 0; i < V; i++) {
			typeCode = 0;
			v = sinkSeq.get(i);
			level = vertex2level.get(v);
			xmin = frontier.get(level) + 1;
			x0 = xmin;
			ymin = level - xmin;
			frontier.set(level, xmin); //updating frontier;
			for (int l = level + 1; l < level + n; l++) { //looking at the levels of all squares in the tile.
				x1 = frontier.get(l);
				if (x1 < x0) { // in this case we can make a vertical step.
					typeCode = typeCode | (1 << (l - level - 1)); 
					frontier.set(l, x0);
				} else { // make horizontal step
					x0++;
					frontier.set(l, x0);
				}
			}

			String str = Integer.toBinaryString(typeCode);
			StringBuilder sb = new StringBuilder();
			for (int toPrepend = n - 1 - str.length(); toPrepend > 0; toPrepend--) {
				sb.append('0');
			}
			sb.append(str);
			stringCode = sb.toString();		
			tile = new RibTile(xmin, ymin, stringCode, rt);
			rt.tiling.add(tile);
			labels.put(v, tile);
			labels2vertices.put(tile, v);
		}
	}
    */

	/**
	 * Draws the reconstructed tiling
	 * 
	 * @param dr
	 */		
	protected void draw(Draw dr) {
		for (RibTile t : rt.tiling) {
			t.drawFilled(dr, labels2vertices.get(t));
		}
	}


	protected void draw(Draw dr, int size, int offsetX, int offsetY) {		
		rt.setWindow(dr, size, offsetX, offsetY);
		for (RibTile t : rt.tiling) {
			t.drawFilled(dr, labels2vertices.get(t));
		}
	}

	/**
	 * For testing methods.
	 */
	public static void main(String[] args) {
		int M, N, ITER;
		RibTiling original;
		M = 15;
		N = 7;
		ITER = 100;
		original = new RibTiling(N, M, 1, "Original"); 
		original.mix(ITER);
		original.draw(original.myDr, "vertex");
		TilingReconstructor tr = new TilingReconstructor(N, M, original.G.DG, original.G.DG, 500, 500, 600, 0); 	
		tr.draw(tr.rt.myDr);	
	}
}
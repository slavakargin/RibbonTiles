package xrib;
import java.util.ArrayList;
import java.util.TreeSet;

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;


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
	This function finds a sink in the acyclic Digraph DG. More specifically, if there are several sinks,  
	then it produces the smallest of them in the order of increasing level from left to right
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
	 * for ITER iterations. 
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
    	   if (i % 1000 == 0) {
    		   StdOut.println("Iteration " + i);
    	   }
       randNum = StdRandom.uniform(xrt.tiles().size());
       tile = xrt.tiles().get(randNum);
       flips = xrt.findFlips(tile);
       if (flips != null && flips.size() > 0) {
         randNum = StdRandom.uniform(flips.size());
         otherTile = flips.get(randNum);
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

}
